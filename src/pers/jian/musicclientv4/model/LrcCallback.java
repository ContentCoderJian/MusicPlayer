package pers.jian.musicclientv4.model;

import java.util.HashMap;

/**
 * �����Ϣ�ص��ӿ�
 */
public interface LrcCallback {
	
	/**
	 * �����������ɺ�ص��˷���
	 * @param lrc 
	 */
	void onLrcDownloaded(HashMap<String, String> lrc);
}
