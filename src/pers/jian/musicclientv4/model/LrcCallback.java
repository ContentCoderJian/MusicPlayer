package pers.jian.musicclientv4.model;

import java.util.HashMap;

/**
 * 歌词信息回调接口
 */
public interface LrcCallback {
	
	/**
	 * 当歌词下载完成后回调此方法
	 * @param lrc 
	 */
	void onLrcDownloaded(HashMap<String, String> lrc);
}
