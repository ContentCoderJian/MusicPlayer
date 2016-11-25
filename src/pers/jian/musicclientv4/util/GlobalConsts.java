package pers.jian.musicclientv4.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;

/**
 * ��ų����ӿ�
 */
public interface GlobalConsts {

	/**
	 * Action: ���ֿ�ʼ������
	 */
	public static final String ACTION_MUSIC_STARTED = "pers.jian.musicclientv4.ACTION_MUSIC_STARTED";
	
	/**
	 * Action: ���ֲ��Ž��ȸ���
	 */
	public static final String ACTION_UDTADE_MUSIC_PROGRESS = "pers.jian.musicclientv4.ACTION_UDTADE_MUSIC_PROGRESS";
	
	/**
	 * Extra�� ���ֲ��ŵ���ʱ��
	 */
	public static final String EXTRA_CURRENT_TIME = "pers.jian.musicclientv4.EXTRA_CURRENT_TIME";
	/**
	 * Extra: �������ֵ���ʱ��
	 */
	public static final String EXTRA_TOTAL_TIME = "pers.jian.musicclientv4.EXTRA_TOTAL_TIME";
	
	/**
	 * Handler �� ͼƬ���سɹ�
	 */
	public static final int HANDLER_IMAGE_LOAD_SUCCESS = 1;
	
	/**
	 * SimpleDateFormat�� ʱ���ʽ������, ��ʽ����mm:ss
	 */
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("mm:ss");
	
	/**
	 * ֪ͨId
	 */
	public static final int NOTIFICATION_ID = 911;

}
