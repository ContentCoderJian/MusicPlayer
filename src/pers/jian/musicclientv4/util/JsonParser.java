package pers.jian.musicclientv4.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pers.jian.musicclientv4.entity.Music;
import pers.jian.musicclientv4.entity.SongInfo;
import pers.jian.musicclientv4.entity.SongUrl;

/**
 * 解析Json字符串的工具类
 */
public class JsonParser {

	/**
	 * 解析SongUrl
	 * 
	 * @param obj
	 *            { url : [{},{},{},{},{}] }
	 * @return List<SongUrl>
	 * @throws JSONException
	 */
	public static List<SongUrl> parserSongUrls(JSONObject obj)
			throws JSONException {
		JSONArray array = obj.getJSONArray("url");
		List<SongUrl> urls = new ArrayList<SongUrl>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject urlObj = array.getJSONObject(i);
			SongUrl url = new SongUrl();
			url.setFile_bitrate(urlObj.getString("file_bitrate"));
			url.setFile_duration(urlObj.getString("file_duration"));
			url.setFile_extension(urlObj.getString("file_extension"));
			url.setFile_link(urlObj.getString("file_link"));
			url.setFile_size(urlObj.getString("file_size"));
			url.setShow_link(urlObj.getString("show_link"));
			url.setSong_file_id(urlObj.getString("song_file_id"));
			urls.add(url);
		}
		return urls;
	}
	
	/**
	 * 解析SongInfo
	 * @param obj
	 * @return SongInfo
	 * @throws JSONException
	 */
	public static SongInfo parserSongInfo(JSONObject obj) throws JSONException {
			SongInfo info = new SongInfo(
					obj.getString("pic_huge"),
					obj.getString("album_1000_1000"),
					obj.getString("album_500_500"),
					obj.getString("compose"),
					obj.getString("toneid"),
					obj.getString("bitrate"),
					obj.getString("artist_500_500"),
					obj.getString("file_duration"),
					obj.getString("album_title"),
					obj.getString("sound_effect"),
					obj.getString("title"),
					obj.getString("high_rate"),
					obj.getString("pic_radio"),
					obj.getString("lrclink"),
					obj.getString("distribution"),
					obj.getString("pic_big"),
					obj.getString("pic_premium"),
					obj.getString("artist_480_800"),
					obj.getString("artist_id"),
					obj.getString("album_id"),
					obj.getString("versions"),
					obj.getString("ting_uid"),
					obj.getString("artist_1000_1000"),
					obj.getString("all_artist_id"),
					obj.getString("artist_640_1136"),
					obj.getString("publishtime"),
					obj.getString("songwriting"),
					obj.getString("share_url"),
					obj.getString("author"),
					obj.getString("has_mv_mobile"),
					obj.getString("all_rate"),
					obj.getString("pic_small"),
					obj.getString("song_id"));
			return info;
		
		
	}

	/**
	 * json解析搜索结果转换成List
	 * @param array  song_list:[{} {} {} {}]
	 * @return List<Music>
	 * @throws JSONException 
	 */
	public static List<Music> parserSearchResult(JSONArray array) throws JSONException {
		// 准备集合
		List<Music> musics = new ArrayList<Music>();
		for (int i=0; i<array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			Music music = new Music();
			music.setTitle(obj.getString("title"));
			music.setSong_id(obj.getString("song_id"));
			music.setAuthor(obj.getString("author"));
			music.setAlbum_title(obj.getString("album_title"));
			musics.add(music);
		}
		return musics;
	}
}
