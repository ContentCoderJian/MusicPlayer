package pers.jian.musicclientv4.model;

import java.util.List;

import pers.jian.musicclientv4.entity.SongInfo;
import pers.jian.musicclientv4.entity.SongUrl;

/**
 * ������Ϣ�ص��ӿ�
 */
public interface MusicInfoCallback {
	
	/**
	 * ��������Ϣ������Ϻ�, ���ø÷���
	 * @param urls 
	 * @param info
	 */
	void onMusicInfoLoaded(List<SongUrl> urls, SongInfo info);

}
