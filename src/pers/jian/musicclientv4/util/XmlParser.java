package pers.jian.musicclientv4.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import pers.jian.musicclientv4.entity.Music;
import android.util.Xml;

public class XmlParser {

	/**
	 * 通过输入流， 使用XMLPull 解析xml文档
	 * 
	 * @param is
	 *            输入流
	 * @return List<Music>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static List<Music> parseMusicList(InputStream is)
			throws XmlPullParserException, IOException {
		// 1. 创建Xmlpull解析器
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "UTF-8");

		// 2. 获取事件类型, 配合while循环, 不断驱动事件
		int eventType = parser.getEventType();
		
		// 准备集合
		List<Music> musics = new ArrayList<Music>();
		Music music = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG: // 从开始标签位置
				String name = parser.getName(); // 获取标签名
				if (name.equals("song")) { // 开始创建Music对象
					music = new Music();
					musics.add(music); // 先添加，后赋值
				} else if (name.equals("pic_big")) {
					music.setPic_big(parser.nextText());
				} else if (name.equals("pic_small")) {
					music.setPic_small(parser.nextText());
				} else if (name.equals("publishtime")) {
					music.setPublishtime(parser.nextText());
				} else if (name.equals("lrclink")) {
					music.setLrclink(parser.nextText());
				} else if (name.equals("song_id")) {
					music.setSong_id(parser.nextText());
				} else if (name.equals("title")) {
					music.setTitle(parser.nextText());
				} else if (name.equals("author")) {
					music.setAuthor(parser.nextText());
				} else if (name.equals("album_title")) {
					music.setAlbum_title(parser.nextText());
				} else if (name.equals("artist_name")) {
					music.setArtist_name(parser.nextText());
				}
				
				break;
			}
			// 向后驱动事件
			eventType = parser.next();
		}
		return musics;
	}

}
