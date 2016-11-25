package pers.jian.musicclientv4.util;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;

/**
 * 存放常量接口
 */
public interface GlobalConsts {

	/**
	 * Action: 音乐开始播放了
	 */
	public static final String ACTION_MUSIC_STARTED = "pers.jian.musicclientv4.ACTION_MUSIC_STARTED";
	
	/**
	 * Action: 音乐播放进度更新
	 */
	public static final String ACTION_UDTADE_MUSIC_PROGRESS = "pers.jian.musicclientv4.ACTION_UDTADE_MUSIC_PROGRESS";
	
	/**
	 * Extra： 音乐播放到的时间
	 */
	public static final String EXTRA_CURRENT_TIME = "pers.jian.musicclientv4.EXTRA_CURRENT_TIME";
	/**
	 * Extra: 播放音乐的总时长
	 */
	public static final String EXTRA_TOTAL_TIME = "pers.jian.musicclientv4.EXTRA_TOTAL_TIME";
	
	/**
	 * Handler ： 图片加载成功
	 */
	public static final int HANDLER_IMAGE_LOAD_SUCCESS = 1;
	
	/**
	 * SimpleDateFormat： 时间格式化工具, 格式化成mm:ss
	 */
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("mm:ss");
	
	/**
	 * 通知Id
	 */
	public static final int NOTIFICATION_ID = 911;

}
