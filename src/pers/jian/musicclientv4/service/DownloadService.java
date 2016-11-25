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

public class DownloadService extends IntentService {  // 它继承Service  并且已经重写过 onStartCommand()了

	private NotificationManager manager;

	/**
	 * 无参构造方法
	 */
	public DownloadService() {
		super("download");
	}

	/**
	 * 父类没有无参构造方法
	 * 
	 * @param name
	 */
	public DownloadService(String name) {
		super(name);
	}

	/**
	 * 只执行一次
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	/**
	 * 将会在工作线程中运行 方法中的代码不会立即执行 
	 * 当该它执行的时候执行 (轮到该消息时) 类似handler消息队列
	 * 我们可以在这个方法中直接执行耗时操作
	 * 
	 * @param intent
	 *            可以用于传递参数数据
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// 发送http请求, 执行下载操作
		String url = intent.getStringExtra("url");
		String bitrate = intent.getStringExtra("bitrate");
		String title = intent.getStringExtra("title");
		String size = intent.getStringExtra("size");
		String extension = intent.getStringExtra("extension");
		int total = Integer.parseInt(size);

		// 获取目标File对象 歌曲对象
		// 需要 bitrate, title , extension
		File targetFile = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
				"downloads/" + bitrate + "/" + title + "." + extension);
		if (targetFile.exists()) { // 歌曲对象已经存在
			Log.d("jian", "歌曲已经存在");
			return;
		}

		if (!targetFile.getParentFile().exists()) { // 文件父目录不存在的时候创建父目录
			targetFile.getParentFile().mkdirs();
		}

		try {
			// 文件输出流 , 从网络下载到本地
			FileOutputStream fos = new FileOutputStream(targetFile);
			InputStream is = HttpUtils.getInputStream(url);
			int current = 0; // 当前值
			int progress = 0; // 更新进度
			// 发送通知, 提示开始下载
			sendNotification(title + "开始下载", title + "下载", "下载进度： " + progress
					+ "%", progress);
			// 边读, 边往SD卡中写
			byte[] buffer = new byte[1024 * 200];
			int length = 0; // 读取到一行的长度
			while ((length = is.read(buffer)) != -1) { // 读取到行尾 返回-1
				fos.write(buffer, 0, length);
				fos.flush();
				current += length;
				// 发通知 更新通知中显示的进度 12.34444
				progress = (int) Math.floor(100 * current / total);
				sendNotification(title + "开始下载", title + "下载", "下载进度： "
						+ progress + "%", progress);

			}

			fos.close();
			// 更新一次通知 先清楚, 再发送通知
			clearNotification();
			sendNotification(title + "下载完成", title + "下载完成", "下载进度： "
					+ progress + "%", progress);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送通知
	 * 
	 * @param ticker 滚动通知
	 * @param contentTitle 通知标题
	 * @param contentText 通知内容
	 * @param progress 通知进度
	 */
	public void sendNotification(String ticker, String contentTitle,
			String contentText, int progress) {
		Builder builder = new Builder(this);
		builder.setTicker(ticker).setContentText(contentText)
				.setContentTitle(contentTitle)
				.setWhen(System.currentTimeMillis())
//				.setOngoing(true)  // 设置常驻通知栏
				.setProgress(100, progress, false)
				.setSmallIcon(R.drawable.ic_launcher);
		Notification notification = builder.build();
		manager.notify(GlobalConsts.NOTIFICATION_ID, notification);
	}

	/**
	 * 清除通知
	 */
	public void clearNotification() {
		manager.cancel(GlobalConsts.NOTIFICATION_ID);
	}

}
