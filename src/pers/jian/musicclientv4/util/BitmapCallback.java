package pers.jian.musicclientv4.util;

import android.graphics.Bitmap;

public interface BitmapCallback {

	/**
	 * 当图片下载成功,调用此方法
	 * @param bitmap
	 */
	void onBitmapLoaded(Bitmap bitmap);
}
