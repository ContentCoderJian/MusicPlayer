package pers.jian.musicclientv4.model;

import java.util.List;

import pers.jian.musicclientv4.entity.SongInfo;
import pers.jian.musicclientv4.entity.SongUrl;

/**
 * 音乐信息回调接口
 */
public interface MusicInfoCallback {
	
	/**
	 * 当音乐信息加载完毕后, 调用该方法
	 * @param urls 
	 * @param info
	 */
	void onMusicInfoLoaded(List<SongUrl> urls, SongInfo info);

}
