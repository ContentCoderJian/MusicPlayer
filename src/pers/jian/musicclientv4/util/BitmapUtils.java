package pers.jian.musicclientv4.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pers.jian.musicclientv4.appliacation.MusicApplication;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;

public class BitmapUtils {
	private static final int BUFFER = 1024 * 8;

	/**
	 * 压缩图片算法, 获取相应尺寸的图片
	 * 
	 * @param is
	 *            输入流
	 * @param width
	 *            目标宽度
	 * @param height
	 *            目标高度
	 * @return bitmap
	 * @throws IOException
	 */
	public static Bitmap loadBitmap(InputStream is, int width, int height)
			throws IOException {
		// 把is解析, 把数据t读取到byte[]中
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// 把输入流中的数据读取到baos中
		byte[] buffer = new byte[BUFFER];
		int length = 0;
		while ((length = is.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
			baos.flush();
		}
		// 该byte[] 表述的是一个图片的完整信息
		byte[] bytes = baos.toByteArray();

		// 获取图片的原始尺寸
		Options opts = new Options();
		// 仅仅加载边界属性
		// inJustDecodeBounds: 如果该值设为true那么将不返回实际的bitmap，也不给其分配内存空间这样就避免内存溢出了。
		// 但是允许我们查询图片的信息这其中就包括图片大小信息
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

		// 根据原始尺寸与width和height计算压缩比例
		int w = opts.outWidth / width;
		int h = opts.outHeight / height;
		Log.i("jian", "图片原始尺寸: (" + opts.outWidth + ", " + opts.outHeight + ")");
		int scale = w > h ? w : h;
		opts.inSampleSize = scale;
		Log.i("jian", "图片压缩比例: " + scale);

		// 执行压缩
		opts.inJustDecodeBounds = false; // 这次图片需要读取出来
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
				opts);
		return bitmap;
	}

	/**
	 * 用jpeg格式存储Bitmap图像到文件
	 * 
	 * @param bitmap
	 *            目标图片
	 * @param file
	 *            文件存储目录
	 * @throws IOException
	 */
	public static void save(Bitmap bitmap, File file) throws IOException {
		if (!file.getParentFile().exists()) { // 父目录不存在
			// 创建父目录
			file.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, fos);
		// 释放资源
		fos.close();
		fos = null;
	}

	/**
	 * 通过一个网络途径, 先从本地尝试获取, 获取不到再从网络下载并保存到文件中
	 * 
	 * @param path
	 *            网络路径
	 * @param callback
	 *            图片加载后回调
	 * @return bitmap
	 */
	public static void loadBitmap(final String path,
			final BitmapCallback callback) {
		if(path == null || path.equals("")){
			callback.onBitmapLoaded(null);
			return;
		}

		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>() {
			
			@Override
			protected Bitmap doInBackground(String... params) {

				// 先从内存缓存中读取
				// 再从文件缓存中读取
				String filename = path.substring(path.lastIndexOf("/") + 1);
				final File file = new File(MusicApplication.getApplication()
						.getCacheDir(), "images/" + filename);
				Bitmap bitmap = loadBitmap(file, 0);

				if (bitmap != null) { // 如果图片能从缓存中找到,返回; 否则从网络中下载
					return bitmap;
				}
				// 从网络下载
				try {
					InputStream is = HttpUtils.getInputStream(path);
					bitmap = BitmapFactory.decodeStream(is);

					// 保存到文件中
					save(bitmap, file);
					return bitmap;
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;

			}

			// 主线程执行回调方法
			@Override
			protected void onPostExecute(Bitmap bitmap) {
				callback.onBitmapLoaded(bitmap);
			}
		};
		task.execute();
	}

	/**
	 * 异步加载图片, 通过资源路径
	 * 
	 * @param file
	 *            资源路径
	 * @param scale
	 *            压缩比例
	 * @param callback
	 */
	public static void loadBitmap(final String path, final int scale,
			final BitmapCallback callback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				// 从文件缓存中读取
				String filename = path.substring(path.lastIndexOf("/") + 1);
				File file = new File(MusicApplication.getApplication()
						.getCacheDir(), "images/" + filename);
				Bitmap bitmap = loadBitmap(file, scale);
				if (bitmap != null) { // 图片存在
					return bitmap;
				}
				// 从网络下载原图, 并保存到文件缓存中
				try {
					InputStream is = HttpUtils.getInputStream(path);
					// 下载图片不压缩
					bitmap = BitmapFactory.decodeStream(is);

					// 未压缩原图保存到文件中
					save(bitmap, file);

					// 从文件中读取bitmap, 并根据scale进行压缩
					bitmap = loadBitmap(file, scale);

					// 释放资源
					is.close();
					is = null;

					return bitmap;
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				callback.onBitmapLoaded(result);
			}
		};

		task.execute();
	}

	/**
	 * 从文件中加载一个Bitmap对象个
	 * 
	 * @param file
	 *            内部文件存储目录
	 * @param scale
	 *            压缩比例 scale == 0 不压缩
	 * @return bitmap
	 */
	public static Bitmap loadBitmap(File file, int scale) {
		if (!file.exists()) { // 文件不存在
			return null;
		}
		if (scale == 0) { // 无损压缩
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			return bitmap;
		} else { // 压缩比例不为0时
			Options opts = new Options();
			opts.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),
					opts);
			return bitmap;
		}

	}

	/**
	 * 因为createBlurBitmap比较耗时, 异步加载一张模糊图片
	 * 
	 * @param bitmap
	 *            源图片
	 * @param callback
	 * @return bitmap
	 */
	public static void loadBlurBitmap(final Bitmap sourceBitmap,
			final BitmapCallback callback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>() {

			@Override
			protected Bitmap doInBackground(String... params) {
				int radius = 5; // 模糊半径
				Bitmap bitmap = createBlurBitmap(sourceBitmap, radius);
				return bitmap;
			}

			// 在主线程中回调这个方法
			@Override
			protected void onPostExecute(Bitmap result) {
				callback.onBitmapLoaded(result);
			}

		};
		task.execute();
	}

	/**
	 * 传递bitmap 传递模糊半径 返回一个被模糊的bitmap 比较耗时 (radius越大越耗时)
	 * 
	 * @param sentBitmap
	 *            传递的bitmap
	 * @param radius
	 *            模糊半径
	 * @return bitmap
	 */
	public static Bitmap createBlurBitmap(Bitmap sentBitmap, int radius) {
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		if (radius < 1) {
			return (null);
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];
		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);

		}
		yw = yi = 0;
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}

			}
			stackpointer = radius;
			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);

				}
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;

			}
			yw += w;

		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}
				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;

				}
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}
}
