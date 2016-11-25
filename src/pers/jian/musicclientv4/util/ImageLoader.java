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

				// 获取需要设置图片不同tag的位置
				ImageView imageview = (ImageView) listView
						.findViewWithTag(task.path);
				if (imageview != null) {
					if (bitmap != null) {
						imageview.setImageBitmap(bitmap);
					} else { // 数据不存在图片, 设置默认图片
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
		// 启动工作线程, 轮循任务集合
		// 注： 这个工作线程, 只在通过构造方法创建对象时执行一次
		workThread = new Thread() {
			@Override
			public void run() {
				while (isLoop) {
					if (!tasks.isEmpty()) { // 不是空集
						// 移除集合中第一个，并返回移除的对象ImageLoadTask
						ImageLoadTask task = tasks.remove(0);
						String url = task.path;
						// 发送 http 请求, 下载图片
						Bitmap bitmap = loadBitmap(url);
						task.bitmap = bitmap;

						// 界面更新 发送handler消息
						Message msg = new Message();
						msg.what = GlobalConsts.HANDLER_IMAGE_LOAD_SUCCESS;
						msg.obj = task;
						handler.sendMessage(msg);

					} else { // 空集
						try {
							// 阻塞当前线程, 等待唤醒
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
	 * 通过Url地址, 发送http请求,获取图片
	 * 
	 * @param url
	 *            pic_small路径
	 * @return bitmap
	 */
	protected Bitmap loadBitmap(String url) {
		try {
			// 获取get请求返回的字节流
			InputStream is = HttpUtils.getInputStream(url);

			// 解析bitmap, 执行压缩算法
			int width = 45;
			int height = 45;
			Bitmap bitmap = BitmapUtils.loadBitmap(is, width, height);

			// 内存缓存 存储
			cache.put(url, new SoftReference<Bitmap>(bitmap));  // 软引用, 详见java4种引用
			Log.v("jian", "" + cache.toString());

			// 文件存储 存储
			String filename = url.substring(url.lastIndexOf("/") + 1);
			File file = new File(context.getCacheDir(), "images/" + filename);
			BitmapUtils.save(bitmap, file);

			return bitmap; // 返回图片对象
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; // 当下载失败 , 返回空
	}

	public void displayImage(String url, ImageView imageView) {  // 封装的getView()里的读取bitmap
		// 给ivPic设置图片
		// new Thread(){}.start(); 不能写， 写了会卡， 不停的创建线程
		// 卡： 掉帧

		// 先从内存缓存中读取
		SoftReference<Bitmap> ref = cache.get(url);
		if (ref != null) { // 以前存过
			Bitmap bitmap = ref.get();
			if (bitmap != null) { // 以前存的还没有被GC回收
				imageView.setImageBitmap(bitmap);
				return;
			}
		}

		// 从文件存储中读取
		String filename = url.substring(url.lastIndexOf("/") + 1);
		File file = new File(context.getCacheDir(), "images/" + filename);
		Bitmap bitmap = BitmapUtils.loadBitmap(file, 0);
		if (bitmap != null) { // 在文件中存在bitmap图片
			// 从文件中读取出来, 先存入内存缓存中
			cache.put(url, new SoftReference<Bitmap>(bitmap));
			imageView.setImageBitmap(bitmap);
			return;
		}

		// 向任务集合中添加一个图片下载任务
		imageView.setTag(url);
		ImageLoadTask task = new ImageLoadTask();
		task.path = url; // task.path 设置路径与给ivPic设置的tag路径一致
		tasks.add(task);

		// 唤醒线程
		synchronized (workThread) {
			workThread.notify();
		}
	}

	class ImageLoadTask {
		String path; // 保存图片下载路径
		Bitmap bitmap; // 下载成功后的图片
	}

	public void stopThread() {
		isLoop = false;
		// 线程执行到wait()阻塞状态, 需要唤醒, 并将isLoop值改成false,让线程中的run()正常执行完毕
		synchronized (workThread) {
			workThread.notify();
		}
	}
}
