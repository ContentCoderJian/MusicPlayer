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
 * �������ҵ���
 */
public class MusicModel {

	/**
	 * �����¸���б�
	 * 
	 * @param offset
	 *            ��ʼλ�� ��0��ʼ
	 * @param size
	 *            ��������Ŀ
	 * @param musicCallback
	 *            �����ص�
	 */
	public void loadNewMusicList(final int offset, final int size,
			final MusicCallback callback) {
		// �첽����
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>() {

			// ����http����,��ȡ��Ӧ����,���ҽ���
			@Override
			protected List<Music> doInBackground(String... params) {
				// ͨ��Url������ȡ�¸���б�
				String url = UrlFactory.getNewMusicListUrl(offset, size);

				try {
					// get����ʽ��ȡ��Ӧ�ֽ���
					InputStream is = HttpUtils.getInputStream(url);

					// ͨ��xml pull������ʽ����xml�ĵ����װ���ݵ�List������
					List<Music> musics = XmlParser.parseMusicList(is);
					Log.v("jian", "�¸������б����� " + musics.toString());
					return musics;

					// ���ֽ���ת��Ϊ�ַ������
					// String xml = HttpUtils.isToString(is);
					// Log.i("jian", "xml�� " + xml);

				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}
				return null;
			}

			/**
			 * �����߳���ִ�� ����Ӧ���ڴ˸���UI
			 */
			@Override
			protected void onPostExecute(List<Music> musics) {
				// �����߳��е��ûص�����
				callback.onMusicListLoaded(musics);
			}

		};
		// ִ���첽����
		task.execute();
	}

	/**
	 * �����ȸ���
	 * 
	 * @param offset
	 *            ��ʼλ�� ��0��ʼ
	 * @param size
	 *            ��������Ŀ
	 * @param musicCallback
	 *            �����ص�
	 */
	public void loadHotMusicList(final int offset, final int size,
			final MusicCallback callback) {
		// �첽����
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>() {

			// ����http ����,��ȡ��Ӧ����, ���ҽ���
			@Override
			protected List<Music> doInBackground(String... params) {
				// ͨ��Url������ȡ�ȸ���
				String url = UrlFactory.getHotMusicListUrl(offset, size);

				try {
					// get����ʽ��ȡ��Ӧ�ֽ���
					InputStream is = HttpUtils.getInputStream(url);

					// ͨ��xml pull��ʽ����xml�ĵ��� ����װ��������
					List<Music> musics = XmlParser.parseMusicList(is);
					Log.d("jian", "�ȸ������б����� " + musics.toString());
					return musics;

				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}

				return null;
			}

			/**
			 * �����߳���ִ��, ����Ӧ���ڴ˸���UI
			 */
			@Override
			protected void onPostExecute(List<Music> musics) {
				// �����߳��е��ûص�����
				callback.onMusicListLoaded(musics);
			}

		};
		// ִ���첽����
		task.execute();
	}

	/**
	 * ����songid, �첽�������׸����Ϣ
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
					// Log.i("jian", "Json��" + respJson);

					// ����Json List<SongUrl>
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
	 * �첽���ظ��
	 * 
	 * @param lrcLink
	 *            �����Դ����
	 * @param callback
	 */
	public void downloadLrc(final String lrcLink, final LrcCallback callback) {
		AsyncTask<String, String, HashMap<String, String>> task = new AsyncTask<String, String, HashMap<String, String>>() {

			@Override
			protected HashMap<String, String> doInBackground(String... params) {
				try {
					// ��ȡָ���ļ�����Ŀ¼�еĻ������ļ���File����
					String filename = lrcLink.substring(lrcLink
							.lastIndexOf("/") + 1);
					File file = new File(MusicApplication.getApplication()
							.getCacheDir(), "lrc/" + filename);
					if (!file.getParentFile().exists()) { // ����������ļ��ĸ�Ŀ¼������
						// ������Ŀ¼
						file.getParentFile().mkdirs();
					}

					// ���ļ������м��ظ��
					if (file.exists()) { // �ļ������д��ڸ���ļ�
						// ��ȡ�ļ�������
						FileInputStream fis = new FileInputStream(file);
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(fis, "UTF-8"));
						String line = "";
						HashMap<String, String> lrc = new HashMap<String, String>();
						while ((line = reader.readLine()) != null) { // ��ȡ��һ�и��
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
							} else { // line: [00:01.50]Ϊʲô�Һ������������˭
								if (line.length() < 10) { // һ���е����ݲ�������
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
						// �ر�������, �ͷ���Դ
						fis.close();
						reader.close();
						fis = null;
						reader = null;
						Log.d("jian", "lrc from file!");
						return lrc;
					}

					// ����ļ�������û��, ִ���������ظ�ʲ���ӱ��ظ�ʻ���
					InputStream is = HttpUtils.getInputStream(lrcLink);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, "UTF-8"));
					String line = "";
					HashMap<String, String> lrc = new HashMap<String, String>();
					// ����ָ�򻺴�Ŀ¼�е��ļ������
					PrintWriter out = new PrintWriter(file);
					while ((line = reader.readLine()) != null) { // ��ȡ��һ�и��
						// ��lineһ��д���ļ�������,��ɸ�ʱ��ػ������
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
						} else { // line: [00:01.50]Ϊʲô�Һ������������˭
							if (line.length() < 10) { // һ���е����ݲ�������
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
					// �ر������, �ͷ���Դ
					out.close();
					out = null;
					return lrc;
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("jian", "δ��ȡ�����");
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
	 * �첽��������
	 * @param keyWord  �ؼ���
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
					
					// ��array ת����List<Music> musics
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
