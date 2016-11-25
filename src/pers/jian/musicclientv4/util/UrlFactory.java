package pers.jian.musicclientv4.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Url工厂 用于获取需要的Url地址字符串
 */
public class UrlFactory {

	/**
	 * 新歌榜列表
	 * 
	 * @param offset
	 *            起始位置
	 * @param size
	 *            歌曲数目
	 * @return url
	 */
	public static String getNewMusicListUrl(int offset, int size) {
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.billboard.billList&format=xml&type=1&offset="
				+ offset + "&size=" + size;
		return url;
	}

	/**
	 * 热歌榜列表
	 * 
	 * @param offset
	 *            起始位置
	 * @param size
	 *            歌曲数目
	 * @return url
	 */
	public static String getHotMusicListUrl(int offset, int size) {
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.billboard.billList&format=xml&type=2&offset="
				+ offset + "&size=" + size;
		return url;
	}

	/**
	 * 获取songId的歌曲信息
	 * 
	 * @param songId
	 * @return url
	 */
	public static String getSongInfoUrl(String songId) {
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.song.getInfos&format=json&songid="
				+ songId
				+ "&ts=1408284347323&e=JoN56kTXnnbEpd9MVczkYJCSx%2FE1mkLx%2BPMIkTcOEu4%3D&nw=2&ucf=1&res=1";
		return url;
	}

	/**
	 * 通过关键字搜索音乐路径 1页30条信息
	 * 	并针对中文进行解码
	 * @param keyWord 关键字
	 * @return url 
	 */
	public static String getSearchMusicUrl(String keyWord) {
		try {
			// 将 keyWord 通过UTF-8解码
			keyWord = URLEncoder.encode(keyWord, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.search.common&format=json&query="
				+ keyWord + "&page_no=1&page_size=30 ";
		return url;
	}
}
