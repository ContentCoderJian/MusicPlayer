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
	 * ͨ���������� ʹ��XMLPull ����xml�ĵ�
	 * 
	 * @param is
	 *            ������
	 * @return List<Music>
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static List<Music> parseMusicList(InputStream is)
			throws XmlPullParserException, IOException {
		// 1. ����Xmlpull������
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "UTF-8");

		// 2. ��ȡ�¼�����, ���whileѭ��, ���������¼�
		int eventType = parser.getEventType();
		
		// ׼������
		List<Music> musics = new ArrayList<Music>();
		Music music = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG: // �ӿ�ʼ��ǩλ��
				String name = parser.getName(); // ��ȡ��ǩ��
				if (name.equals("song")) { // ��ʼ����Music����
					music = new Music();
					musics.add(music); // ����ӣ���ֵ
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
			// ��������¼�
			eventType = parser.next();
		}
		return musics;
	}

}
