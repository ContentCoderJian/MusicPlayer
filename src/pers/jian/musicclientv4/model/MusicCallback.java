package pers.jian.musicclientv4.model;

import java.util.List;

import pers.jian.musicclientv4.entity.Music;

/**
 * ����������ػص��ӿ�
 */
public interface MusicCallback {
	
	/**
	 * �������б������Ϻ󽫷��ش˷���
	 * @param musics
	 */
	void onMusicListLoaded(List<Music> musics);
}
