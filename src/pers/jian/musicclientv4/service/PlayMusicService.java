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
	 * ��Service����ʵ��ʱִֻ��һ��
	 */
	@Override
	public void onCreate() {
		// ��player���ü���
		player.setOnPreparedListener(new OnPreparedListener() {

			// �����Ѿ�׼����
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				// ��Activity���͹㲥 ,֪ͨActivity player�Ѿ���ʼ����, ���Ը���UI��
				Intent intent = new Intent();
				intent.setAction(GlobalConsts.ACTION_MUSIC_STARTED);
				sendBroadcast(intent);
			}
		});
		// ���������߳�, ÿ1�뷢һ�θ������ֽ��ȵĹ㲥
		new UpdateProgressThread().start();
	}

	/**
	 * ���½��ȵ��߳� ÿ1�����һ���߳� ���͵Ĺ㲥��Я�����������Ϣ
	 */
	class UpdateProgressThread extends Thread {
		@Override
		public void run() {
			while (isLoop) {
				// �����Զ���㲥
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
		// ���幩�ͻ��˵��õķ���  (���Service���Կ��ɷ����)
		
		public void playMusic(String url) {
			try {
				player.reset();
				player.setDataSource(url);
				player.prepareAsync(); // �첽׼���� ���Mediaplayerע��
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
		 * ���ݵ�ǰ����״̬,�����ǲ��Ż�����ͣ
		 */
		public void startOrPause() {
			if (player.isPlaying()) { // �������ڲ���ʱ
				player.pause();
			} else {  // ������ͣ��ֹͣʱ
				player.start();
			}
		}
		
		/**
		 * ��ת����Ӧλ��, �������Ż���ͣ
		 * @param position
		 */
		public void seekTo(int position) {
			player.seekTo(position);
		}
	}
	
	/**
	 * �� Activity ��� Service ʱ, ���߳���ֹѭ��
	 */
	@Override
	public void onDestroy() {
		isLoop = false;  // ֹͣ�߳�
		player.release(); // ֹͣ����
		player = null;
	}
}
