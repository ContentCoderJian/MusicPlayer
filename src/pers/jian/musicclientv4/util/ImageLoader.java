package pers.jian.musicclientv4.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pers.jian.musicclientv4.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

public class ImageLoader {
	private Context context;
	private List<ImageLoadTask> tasks = new ArrayList<ImageLoadTask>();
	private Thread workThread;
	private boolean isLoop = true;
	private ListView listView;
	private HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GlobalConsts.HANDLER_IMAGE_LOAD_SUCCESS:
				ImageLoadTask task = (ImageLoadTask) msg.obj;
				Bitmap bitmap = task.bitmap;

				// ��ȡ��Ҫ����ͼƬ��ͬtag��λ��
				ImageView imageview = (ImageView) listView
						.findViewWithTag(task.path);
				if (imageview != null) {
					if (bitmap != null) {
						imageview.setImageBitmap(bitmap);
					} else { // ���ݲ�����ͼƬ, ����Ĭ��ͼƬ
						imageview
								.setImageResource(R.drawable.ic_contact_picture_2);
					}
				}

				break;
			}
		}
	};
	

	public ImageLoader(Context context, ListView listView) {
		super();
		this.context = context;
		this.listView = listView;
		// ���������߳�, ��ѭ���񼯺�
		// ע�� ��������߳�, ֻ��ͨ�����췽����������ʱִ��һ��
		workThread = new Thread() {
			@Override
			public void run() {
				while (isLoop) {
					if (!tasks.isEmpty()) { // ���ǿռ�
						// �Ƴ������е�һ�����������Ƴ��Ķ���ImageLoadTask
						ImageLoadTask task = tasks.remove(0);
						String url = task.path;
						// ���� http ����, ����ͼƬ
						Bitmap bitmap = loadBitmap(url);
						task.bitmap = bitmap;

						// ������� ����handler��Ϣ
						Message msg = new Message();
						msg.what = GlobalConsts.HANDLER_IMAGE_LOAD_SUCCESS;
						msg.obj = task;
						handler.sendMessage(msg);

					} else { // �ռ�
						try {
							// ������ǰ�߳�, �ȴ�����
							synchronized (workThread) {
								workThread.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		workThread.start();
	}

	/**
	 * ͨ��Url��ַ, ����http����,��ȡͼƬ
	 * 
	 * @param url
	 *            pic_small·��
	 * @return bitmap
	 */
	protected Bitmap loadBitmap(String url) {
		try {
			// ��ȡget���󷵻ص��ֽ���
			InputStream is = HttpUtils.getInputStream(url);

			// ����bitmap, ִ��ѹ���㷨
			int width = 45;
			int height = 45;
			Bitmap bitmap = BitmapUtils.loadBitmap(is, width, height);

			// �ڴ滺�� �洢
			cache.put(url, new SoftReference<Bitmap>(bitmap));  // ������, ���java4������
			Log.v("jian", "" + cache.toString());

			// �ļ��洢 �洢
			String filename = url.substring(url.lastIndexOf("/") + 1);
			File file = new File(context.getCacheDir(), "images/" + filename);
			BitmapUtils.save(bitmap, file);

			return bitmap; // ����ͼƬ����
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; // ������ʧ�� , ���ؿ�
	}

	public void displayImage(String url, ImageView imageView) {  // ��װ��getView()��Ķ�ȡbitmap
		// ��ivPic����ͼƬ
		// new Thread(){}.start(); ����д�� д�˻Ῠ�� ��ͣ�Ĵ����߳�
		// ���� ��֡

		// �ȴ��ڴ滺���ж�ȡ
		SoftReference<Bitmap> ref = cache.get(url);
		if (ref != null) { // ��ǰ���
			Bitmap bitmap = ref.get();
			if (bitmap != null) { // ��ǰ��Ļ�û�б�GC����
				imageView.setImageBitmap(bitmap);
				return;
			}
		}

		// ���ļ��洢�ж�ȡ
		String filename = url.substring(url.lastIndexOf("/") + 1);
		File file = new File(context.getCacheDir(), "images/" + filename);
		Bitmap bitmap = BitmapUtils.loadBitmap(file, 0);
		if (bitmap != null) { // ���ļ��д���bitmapͼƬ
			// ���ļ��ж�ȡ����, �ȴ����ڴ滺����
			cache.put(url, new SoftReference<Bitmap>(bitmap));
			imageView.setImageBitmap(bitmap);
			return;
		}

		// �����񼯺������һ��ͼƬ��������
		imageView.setTag(url);
		ImageLoadTask task = new ImageLoadTask();
		task.path = url; // task.path ����·�����ivPic���õ�tag·��һ��
		tasks.add(task);

		// �����߳�
		synchronized (workThread) {
			workThread.notify();
		}
	}

	class ImageLoadTask {
		String path; // ����ͼƬ����·��
		Bitmap bitmap; // ���سɹ����ͼƬ
	}

	public void stopThread() {
		isLoop = false;
		// �߳�ִ�е�wait()����״̬, ��Ҫ����, ����isLoopֵ�ĳ�false,���߳��е�run()����ִ�����
		synchronized (workThread) {
			workThread.notify();
		}
	}
}
