package pers.jian.musicclientv4.fragment;

import java.util.List;

import pers.jian.musicclientv4.R;
import pers.jian.musicclientv4.adapter.MusicAdapter;
import pers.jian.musicclientv4.appliacation.MusicApplication;
import pers.jian.musicclientv4.entity.Music;
import pers.jian.musicclientv4.entity.SongInfo;
import pers.jian.musicclientv4.entity.SongUrl;
import pers.jian.musicclientv4.model.MusicCallback;
import pers.jian.musicclientv4.model.MusicInfoCallback;
import pers.jian.musicclientv4.model.MusicModel;
import pers.jian.musicclientv4.service.PlayMusicService.MusicBinder;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class HotMusicListFragment extends Fragment {
	private ListView lvHotMusicList;
	private MusicAdapter adapter;
	private List<Music> musics;
	private MusicModel model = new MusicModel();
	private MusicBinder musicBinder;
	private boolean hotMusicListLoading = false;
	private int size = 20; // һ���б������Ŀ

	
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_list, null);

		// ��ʼ���ؼ�
		setViews(view);
		
		// ����
		setListeners();

		// ����ҵ������, �����ȸ���
		int offset = 0;

		MusicModel model = new MusicModel();
		model.loadHotMusicList(offset, size, new MusicCallback() {

			@Override
			public void onMusicListLoaded(List<Music> musics) {
				// ����������
				setAdapter(musics);
			}
		});
		return view;
	}
	
	/**
	 * ����
	 */
	private void setListeners() {
		// �б�������¼�
		lvHotMusicList.setOnScrollListener(new OnScrollListener() {
			// ��Ǳ��� �Ƿ�ײ�
			private boolean isBottom;

			// ������״̬�ı�ʱ, ִ��
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					Log.v("jian", "onScrollStateChanged SCROLL_STATE_FLING");
					// ���뿪��Ļ, һ˦, �ɿ컬��
					break;
				case SCROLL_STATE_IDLE:
					Log.v("jian", "onScrollStateChanged SCROLL_STATE_IDLE");
					// ����״̬, ��ָ�뿪��Ļ�Ҳ����б���
					if (isBottom) { // �Ƿ񻬵��ײ�
						if (hotMusicListLoading) { // �Ƿ����ڼ����б�, ����Ҫ�ظ�����
							return;
						}

						hotMusicListLoading = true; // ���ڼ����б�
						model.loadNewMusicList(musics.size(), size,
								new MusicCallback() {

									@Override
									public void onMusicListLoaded(
											List<Music> hotMusics) {
										if (hotMusics == null
												|| hotMusics.isEmpty()) { // ���ز����������Ѽ������,��ʾ�û�
											Toast.makeText(getActivity(),
													"�����������",
													Toast.LENGTH_SHORT).show();
											Log.e("jian", "�����������");
											return;
										}
										// ���ظ���
										musics.addAll(hotMusics);
										// ����adapter
										adapter.notifyDataSetChanged();
										hotMusicListLoading = false; // �����б����
									}
								});
					}
					break;

				case SCROLL_STATE_TOUCH_SCROLL:
					Log.v("jian",
							"onScrollStateChanged SCROLL_STATE_TOUCH_SCROLL");
					// �ִ�������Ļ�ϻ���
					break;

				default:
					break;
				}
			}

			// ÿ����������ִ��
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.v("jian", "onScroll");
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					isBottom = true; // �����ײ�
				} else {
					isBottom = false;
				}
			}
		});

		// �б������¼�
		lvHotMusicList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final Music music = musics.get(position);
				String songId = music.getSong_id();
				// ����ҵ���, �첽�������׸�Ļ�����Ϣ
				model.loadMusicInfoBySongId(songId, new MusicInfoCallback() {

					@Override
					public void onMusicInfoLoaded(List<SongUrl> urls,
							SongInfo info) {
						music.setUrls(urls);
						music.setInfo(info);

						// �ѵ�ǰ�����б�, ����position����application
						MusicApplication application = MusicApplication
								.getApplication();
						application.setMusicList(musics);
						application.setPosition(position);

						// ׼��ͨ����URL��ִַ�в���ҵ��
						String fileLink = urls.get(0).getFile_link();
						if (fileLink == null) {
							fileLink = urls.get(0).getShow_link();
						}
						// ���Ÿ���
						musicBinder.playMusic(fileLink);
					}
				});
			}

		});
	}

	@Override
	public void onDestroy() {
		// ��adapter�е��߳�����
		adapter.stopThread();
		
		super.onDestroy();
	}

	private void setViews(View view) {
		lvHotMusicList = (ListView) view.findViewById(R.id.lv_music_list);
	}
	
	/**
	 * ����adapter
	 * 
	 * @param musics
	 */
	private void setAdapter(List<Music> musics) {
		// ��musics�����ڳ�Ա����
		this.musics = musics;
		adapter = new MusicAdapter(getActivity(), musics, lvHotMusicList);
		lvHotMusicList.setAdapter(adapter);
	}
	
	/**
	 * ����binder����
	 * 
	 * @param binder
	 */
	public void setMusicBinder(MusicBinder binder) {
		this.musicBinder = binder;
	}

}
