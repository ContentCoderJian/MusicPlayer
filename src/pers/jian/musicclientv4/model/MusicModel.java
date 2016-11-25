package pers.jian.musicclientv4.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import pers.jian.musicclientv4.appliacation.MusicApplication;
import pers.jian.musicclientv4.entity.Music;
import pers.jian.musicclientv4.entity.SongInfo;
import pers.jian.musicclientv4.entity.SongUrl;
import pers.jian.musicclientv4.util.HttpUtils;
import pers.jian.musicclientv4.util.JsonParser;
import pers.jian.musicclientv4.util.UrlFactory;
import pers.jian.musicclientv4.util.XmlParser;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 音乐相关业务层
 */
public class MusicModel {

	/**
	 * 加载新歌榜列表
	 * 
	 * @param offset
	 *            起始位置 从0开始
	 * @param size
	 *            歌曲的数目
	 * @param musicCallback
	 *            歌曲回调
	 */
	public void loadNewMusicList(final int offset, final int size,
			final MusicCallback callback) {
		// 异步加载
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>() {

			// 发送http请求,获取响应数据,并且解析
			@Override
			protected List<Music> doInBackground(String... params) {
				// 通过Url工厂获取新歌榜列表
				String url = UrlFactory.getNewMusicListUrl(offset, size);

				try {
					// get请求方式获取响应字节流
					InputStream is = HttpUtils.getInputStream(url);

					// 通过xml pull解析方式解析xml文档后封装数据到List集合中
					List<Music> musics = XmlParser.parseMusicList(is);
					Log.v("jian", "新歌榜歌曲列表名： " + musics.toString());
					return musics;

					// 将字节流转换为字符串输出
					// String xml = HttpUtils.isToString(is);
					// Log.i("jian", "xml： " + xml);

				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}
				return null;
			}

			/**
			 * 在主线程中执行 我们应该在此更新UI
			 */
			@Override
			protected void onPostExecute(List<Music> musics) {
				// 在主线程中调用回调方法
				callback.onMusicListLoaded(musics);
			}

		};
		// 执行异步任务
		task.execute();
	}

	/**
	 * 加载热歌榜榜单
	 * 
	 * @param offset
	 *            起始位置 从0开始
	 * @param size
	 *            歌曲的数目
	 * @param musicCallback
	 *            歌曲回调
	 */
	public void loadHotMusicList(final int offset, final int size,
			final MusicCallback callback) {
		// 异步加载
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>() {

			// 发送http 请求,获取响应数据, 并且解析
			@Override
			protected List<Music> doInBackground(String... params) {
				// 通过Url工厂获取热歌榜榜单
				String url = UrlFactory.getHotMusicListUrl(offset, size);

				try {
					// get请求方式获取响应字节流
					InputStream is = HttpUtils.getInputStream(url);

					// 通过xml pull方式解析xml文档， 并封装到集合中
					List<Music> musics = XmlParser.parseMusicList(is);
					Log.d("jian", "热歌榜歌曲列表名： " + musics.toString());
					return musics;

				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}

				return null;
			}

			/**
			 * 在主线程中执行, 我们应该在此更新UI
			 */
			@Override
			protected void onPostExecute(List<Music> musics) {
				// 在主线程中调用回调方法
				callback.onMusicListLoaded(musics);
			}

		};
		// 执行异步任务
		task.execute();
	}

	/**
	 * 根据songid, 异步加载这首歌的信息
	 * 
	 * @param songId
	 * @param musicInfoCallback
	 */
	public void loadMusicInfoBySongId(final String songId,
			final MusicInfoCallback musicInfoCallback) {

		AsyncTask<String, String, Music> task = new AsyncTask<String, String, Music>() {

			@Override
			protected Music doInBackground(String... params) {
				String url = UrlFactory.getSongInfoUrl(songId);
				try {
					InputStream is = HttpUtils.getInputStream(url);
					String respJson = HttpUtils.isToString(is);
					// Log.i("jian", "Json：" + respJson);

					// 解析Json List<SongUrl>
					JSONObject obj = new JSONObject(respJson);
					JSONObject urlObj = obj.getJSONObject("songurl");
					List<SongUrl> urls = JsonParser.parserSongUrls(urlObj);

					// SongInfo
					JSONObject infoObj = obj.getJSONObject("songinfo");
					SongInfo info = JsonParser.parserSongInfo(infoObj);

					Music music = new Music();
					music.setUrls(urls);
					music.setInfo(info);
					
					return music;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Music music) {
				if (music == null) {
					musicInfoCallback.onMusicInfoLoaded(null, null);
				} else {
					musicInfoCallback.onMusicInfoLoaded(music.getUrls(),
							music.getInfo());
				}
			}
		};
		task.execute();
	}

	/**
	 * 异步下载歌词
	 * 
	 * @param lrcLink
	 *            歌词资源链接
	 * @param callback
	 */
	public void downloadLrc(final String lrcLink, final LrcCallback callback) {
		AsyncTask<String, String, HashMap<String, String>> task = new AsyncTask<String, String, HashMap<String, String>>() {

			@Override
			protected HashMap<String, String> doInBackground(String... params) {
				try {
					// 获取指向文件缓存目录中的缓存歌词文件的File对象
					String filename = lrcLink.substring(lrcLink
							.lastIndexOf("/") + 1);
					File file = new File(MusicApplication.getApplication()
							.getCacheDir(), "lrc/" + filename);
					if (!file.getParentFile().exists()) { // 如果缓存歌词文件的父目录不存在
						// 创建父目录
						file.getParentFile().mkdirs();
					}

					// 从文件缓存中加载歌词
					if (file.exists()) { // 文件缓存中存在歌词文件
						// 获取文件输入流
						FileInputStream fis = new FileInputStream(file);
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(fis, "UTF-8"));
						String line = "";
						HashMap<String, String> lrc = new HashMap<String, String>();
						while ((line = reader.readLine()) != null) { // 读取了一行歌词
							if ((line.indexOf("[ti:") != -1)) {
								String key = "ti: ";
								String value = line.substring(
										line.indexOf(":") + 1,
										line.lastIndexOf("]"));
								lrc.put(key, value);
							} else if ((line.indexOf("[ar:") != -1)) {
								String key = "ar: ";
								String value = line.substring(
										line.indexOf(":") + 1,
										line.lastIndexOf("]"));
								lrc.put(key, value);
							} else if ((line.indexOf("[al:") != -1)) {
								String key = "al: ";
								String value = line.substring(
										line.indexOf(":") + 1,
										line.lastIndexOf("]"));
								lrc.put(key, value);
							} else { // line: [00:01.50]为什么我好想告诉他我是谁
								if (line.length() < 10) { // 一行中的数据不够解析
									continue;
								}
								String key = line.substring(
										line.indexOf("[") + 1,
										line.indexOf("[") + 6);
								String value = null;
								if (line.length() == 10) {
									value = "";
								} else {
									value = line
											.substring(line.indexOf("]") + 1);
								}
								lrc.put(key, value);
							}
						}
						// 关闭输入流, 释放资源
						fis.close();
						reader.close();
						fis = null;
						reader = null;
						Log.d("jian", "lrc from file!");
						return lrc;
					}

					// 如果文件缓存中没有, 执行网络下载歌词并添加本地歌词缓存
					InputStream is = HttpUtils.getInputStream(lrcLink);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, "UTF-8"));
					String line = "";
					HashMap<String, String> lrc = new HashMap<String, String>();
					// 声明指向缓存目录中的文件输出流
					PrintWriter out = new PrintWriter(file);
					while ((line = reader.readLine()) != null) { // 读取了一行歌词
						// 把line一行写入文件缓存中,完成歌词本地缓存操作
						out.println(line);
						out.flush();
						if ((line.indexOf("[ti:") != -1)) {
							String key = "ti: ";
							String value = line.substring(
									line.indexOf(":") + 1,
									line.lastIndexOf("]"));
							lrc.put(key, value);
						} else if ((line.indexOf("[ar:") != -1)) {
							String key = "ar: ";
							String value = line.substring(
									line.indexOf(":") + 1,
									line.lastIndexOf("]"));
							lrc.put(key, value);
						} else if ((line.indexOf("[al:") != -1)) {
							String key = "al: ";
							String value = line.substring(
									line.indexOf(":") + 1,
									line.lastIndexOf("]"));
							lrc.put(key, value);
						} else { // line: [00:01.50]为什么我好想告诉他我是谁
							if (line.length() < 10) { // 一行中的数据不够解析
								continue;
							}
							String key = line.substring(line.indexOf("[") + 1,
									line.indexOf("[") + 6);
							String value = null;
							if (line.length() == 10) {
								value = "";
							} else {
								value = line.substring(line.indexOf("]") + 1);
							}
							lrc.put(key, value);
						}
					}
					Log.d("jian", "lrc from baidu: " + lrc.toString());
					// 关闭输出流, 释放资源
					out.close();
					out = null;
					return lrc;
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("jian", "未读取到歌词");
				}
				return null;
			}

			@Override
			protected void onPostExecute(HashMap<String, String> lrc) {
				callback.onLrcDownloaded(lrc);
			}

		};
		task.execute();
	}
	
	/**
	 * 异步搜索歌曲
	 * @param keyWord  关键字
	 * @param musicCallback
	 */
	public void searchMusic(final String keyWord, final MusicCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			@Override
			protected List<Music> doInBackground(String... params) {
				String url = UrlFactory.getSearchMusicUrl(keyWord);
				try {
					InputStream is = HttpUtils.getInputStream(url);
					String respJson = HttpUtils.isToString(is);
					JSONObject obj = new JSONObject(respJson);
					JSONArray array = obj.getJSONArray("song_list");
					
					// 把array 转换成List<Music> musics
					List<Music> musics = JsonParser.parserSearchResult(array);
					return musics;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(List<Music> result) {
				callback.onMusicListLoaded(result);
			}
		};
		task.execute();
	}

}
