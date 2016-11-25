package pers.jian.musicclientv4.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pers.jian.musicclientv4.R;
import pers.jian.musicclientv4.util.GlobalConsts;
import pers.jian.musicclientv4.util.HttpUtils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Notification.Builder;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class DownloadService extends IntentService {  // ���̳�Service  �����Ѿ���д�� onStartCommand()��

	private NotificationManager manager;

	/**
	 * �޲ι��췽��
	 */
	public DownloadService() {
		super("download");
	}

	/**
	 * ����û���޲ι��췽��
	 * 
	 * @param name
	 */
	public DownloadService(String name) {
		super(name);
	}

	/**
	 * ִֻ��һ��
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	/**
	 * �����ڹ����߳������� �����еĴ��벻������ִ�� 
	 * ������ִ�е�ʱ��ִ�� (�ֵ�����Ϣʱ) ����handler��Ϣ����
	 * ���ǿ��������������ֱ��ִ�к�ʱ����
	 * 
	 * @param intent
	 *            �������ڴ��ݲ�������
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// ����http����, ִ�����ز���
		String url = intent.getStringExtra("url");
		String bitrate = intent.getStringExtra("bitrate");
		String title = intent.getStringExtra("title");
		String size = intent.getStringExtra("size");
		String extension = intent.getStringExtra("extension");
		int total = Integer.parseInt(size);

		// ��ȡĿ��File���� ��������
		// ��Ҫ bitrate, title , extension
		File targetFile = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
				"downloads/" + bitrate + "/" + title + "." + extension);
		if (targetFile.exists()) { // ���������Ѿ�����
			Log.d("jian", "�����Ѿ�����");
			return;
		}

		if (!targetFile.getParentFile().exists()) { // �ļ���Ŀ¼�����ڵ�ʱ�򴴽���Ŀ¼
			targetFile.getParentFile().mkdirs();
		}

		try {
			// �ļ������ , ���������ص�����
			FileOutputStream fos = new FileOutputStream(targetFile);
			InputStream is = HttpUtils.getInputStream(url);
			int current = 0; // ��ǰֵ
			int progress = 0; // ���½���
			// ����֪ͨ, ��ʾ��ʼ����
			sendNotification(title + "��ʼ����", title + "����", "���ؽ��ȣ� " + progress
					+ "%", progress);
			// �߶�, ����SD����д
			byte[] buffer = new byte[1024 * 200];
			int length = 0; // ��ȡ��һ�еĳ���
			while ((length = is.read(buffer)) != -1) { // ��ȡ����β ����-1
				fos.write(buffer, 0, length);
				fos.flush();
				current += length;
				// ��֪ͨ ����֪ͨ����ʾ�Ľ��� 12.34444
				progress = (int) Math.floor(100 * current / total);
				sendNotification(title + "��ʼ����", title + "����", "���ؽ��ȣ� "
						+ progress + "%", progress);

			}

			fos.close();
			// ����һ��֪ͨ �����, �ٷ���֪ͨ
			clearNotification();
			sendNotification(title + "�������", title + "�������", "���ؽ��ȣ� "
					+ progress + "%", progress);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����֪ͨ
	 * 
	 * @param ticker ����֪ͨ
	 * @param contentTitle ֪ͨ����
	 * @param contentText ֪ͨ����
	 * @param progress ֪ͨ����
	 */
	public void sendNotification(String ticker, String contentTitle,
			String contentText, int progress) {
		Builder builder = new Builder(this);
		builder.setTicker(ticker).setContentText(contentText)
				.setContentTitle(contentTitle)
				.setWhen(System.currentTimeMillis())
//				.setOngoing(true)  // ���ó�פ֪ͨ��
				.setProgress(100, progress, false)
				.setSmallIcon(R.drawable.ic_launcher);
		Notification notification = builder.build();
		manager.notify(GlobalConsts.NOTIFICATION_ID, notification);
	}

	/**
	 * ���֪ͨ
	 */
	public void clearNotification() {
		manager.cancel(GlobalConsts.NOTIFICATION_ID);
	}

}
