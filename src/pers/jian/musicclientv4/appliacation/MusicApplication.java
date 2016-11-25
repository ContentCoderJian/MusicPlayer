package pers.jian.musicclientv4.appliacation;

import java.util.List;

import pers.jian.musicclientv4.entity.Music;
import android.app.Application;

public class MusicApplication extends Application {
	private List<Music> musicList;
	private int position;
	private static MusicApplication musicApplication;

	public MusicApplication() {
		super();
	}

	public MusicApplication(List<Music> musicList, int position) {
		super();
		this.musicList = musicList;
		this.position = position;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		musicApplication = MusicApplication.this;
	}

	public static MusicApplication getApplication() {
		return musicApplication;
	}

	public List<Music> getMusicList() {
		return musicList;
	}

	public void setMusicList(List<Music> musicList) {
		this.musicList = musicList;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * 获取当前正在播放的歌曲
	 * @return music
	 */
	public Music getCurrentMusic() {
		return musicList.get(position);
	}

}
