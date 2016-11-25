package pers.jian.musicclientv4.service;

import java.io.IOException;

import pers.jian.musicclientv4.util.GlobalConsts;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;

public class PlayMusicService extends Service {
	private MediaPlayer player = new MediaPlayer();
	private boolean isLoop = true;

	/**
	 * 当Service创建实例时只执行一次
	 */
	@Override
	public void onCreate() {
		// 给player设置监听
		player.setOnPreparedListener(new OnPreparedListener() {

			// 音乐已经准备好
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				// 给Activity发送广播 ,通知Activity player已经开始播放, 可以更新UI了
				Intent intent = new Intent();
				intent.setAction(GlobalConsts.ACTION_MUSIC_STARTED);
				sendBroadcast(intent);
			}
		});
		// 启动工作线程, 每1秒发一次更新音乐进度的广播
		new UpdateProgressThread().start();
	}

	/**
	 * 更新进度的线程 每1秒更新一次线程 发送的广播中携带音乐相关信息
	 */
	class UpdateProgressThread extends Thread {
		@Override
		public void run() {
			while (isLoop) {
				// 发送自定义广播
				if (player.isPlaying()) {
					int currentTime = player.getCurrentPosition();
					int TotalTime = player.getDuration();
					Intent intent = new Intent();
					intent.setAction(GlobalConsts.ACTION_UDTADE_MUSIC_PROGRESS);
					intent.putExtra(GlobalConsts.EXTRA_CURRENT_TIME, currentTime);
					intent.putExtra(GlobalConsts.EXTRA_TOTAL_TIME, TotalTime);
					sendBroadcast(intent);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new MusicBinder();
	}

	public class MusicBinder extends Binder {
		// 定义供客户端调用的方法  (这个Service可以看成服务端)
		
		public void playMusic(String url) {
			try {
				player.reset();
				player.setDataSource(url);
				player.prepareAsync(); // 异步准备好 详见Mediaplayer注释
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 根据当前播放状态,决定是播放还是暂停
		 */
		public void startOrPause() {
			if (player.isPlaying()) { // 音乐正在播放时
				player.pause();
			} else {  // 音乐暂停或停止时
				player.start();
			}
		}
		
		/**
		 * 跳转到相应位置, 继续播放或暂停
		 * @param position
		 */
		public void seekTo(int position) {
			player.seekTo(position);
		}
	}
	
	/**
	 * 当 Activity 解绑 Service 时, 把线程终止循环
	 */
	@Override
	public void onDestroy() {
		isLoop = false;  // 停止线程
		player.release(); // 停止播放
		player = null;
	}
}
