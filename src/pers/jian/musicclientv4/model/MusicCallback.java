package pers.jian.musicclientv4.model;

import java.util.List;

import pers.jian.musicclientv4.entity.Music;

/**
 * 音乐数据相关回调接口
 */
public interface MusicCallback {
	
	/**
	 * 当音乐列表加载完毕后将返回此方法
	 * @param musics
	 */
	void onMusicListLoaded(List<Music> musics);
}
