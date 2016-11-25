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
	private int size = 20; // 一次列表歌曲数目

	
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_list, null);

		// 初始化控件
		setViews(view);
		
		// 监听
		setListeners();

		// 调用业务层代码, 加载热歌榜榜单
		int offset = 0;

		MusicModel model = new MusicModel();
		model.loadHotMusicList(offset, size, new MusicCallback() {

			@Override
			public void onMusicListLoaded(List<Music> musics) {
				// 更新适配器
				setAdapter(musics);
			}
		});
		return view;
	}
	
	/**
	 * 监听
	 */
	private void setListeners() {
		// 列表项滚动事件
		lvHotMusicList.setOnScrollListener(new OnScrollListener() {
			// 标记变量 是否底部
			private boolean isBottom;

			// 当滚动状态改变时, 执行
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					Log.v("jian", "onScrollStateChanged SCROLL_STATE_FLING");
					// 手离开屏幕, 一甩, 飞快滑动
					break;
				case SCROLL_STATE_IDLE:
					Log.v("jian", "onScrollStateChanged SCROLL_STATE_IDLE");
					// 空闲状态, 手指离开屏幕且不在列表滑动
					if (isBottom) { // 是否滑到底部
						if (hotMusicListLoading) { // 是否正在加载列表, 不需要重复加载
							return;
						}

						hotMusicListLoading = true; // 正在加载列表
						model.loadNewMusicList(musics.size(), size,
								new MusicCallback() {

									@Override
									public void onMusicListLoaded(
											List<Music> hotMusics) {
										if (hotMusics == null
												|| hotMusics.isEmpty()) { // 加载不到歌曲或已加载完毕,提示用户
											Toast.makeText(getActivity(),
													"歌曲加载完毕",
													Toast.LENGTH_SHORT).show();
											Log.e("jian", "歌曲加载完毕");
											return;
										}
										// 加载歌曲
										musics.addAll(hotMusics);
										// 更新adapter
										adapter.notifyDataSetChanged();
										hotMusicListLoading = false; // 加载列表完毕
									}
								});
					}
					break;

				case SCROLL_STATE_TOUCH_SCROLL:
					Log.v("jian",
							"onScrollStateChanged SCROLL_STATE_TOUCH_SCROLL");
					// 手触摸在屏幕上滑动
					break;

				default:
					break;
				}
			}

			// 每当滚动都会执行
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				Log.v("jian", "onScroll");
				if (firstVisibleItem + visibleItemCount == totalItemCount) {
					isBottom = true; // 滑到底部
				} else {
					isBottom = false;
				}
			}
		});

		// 列表项点击事件
		lvHotMusicList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				final Music music = musics.get(position);
				String songId = music.getSong_id();
				// 调用业务层, 异步加载这首歌的基本信息
				model.loadMusicInfoBySongId(songId, new MusicInfoCallback() {

					@Override
					public void onMusicInfoLoaded(List<SongUrl> urls,
							SongInfo info) {
						music.setUrls(urls);
						music.setInfo(info);

						// 把当前播放列表, 还有position存入application
						MusicApplication application = MusicApplication
								.getApplication();
						application.setMusicList(musics);
						application.setPosition(position);

						// 准备通过该URL地址执行播放业务
						String fileLink = urls.get(0).getFile_link();
						if (fileLink == null) {
							fileLink = urls.get(0).getShow_link();
						}
						// 播放歌曲
						musicBinder.playMusic(fileLink);
					}
				});
			}

		});
	}

	@Override
	public void onDestroy() {
		// 把adapter中的线程销毁
		adapter.stopThread();
		
		super.onDestroy();
	}

	private void setViews(View view) {
		lvHotMusicList = (ListView) view.findViewById(R.id.lv_music_list);
	}
	
	/**
	 * 更新adapter
	 * 
	 * @param musics
	 */
	private void setAdapter(List<Music> musics) {
		// 把musics保存在成员变量
		this.musics = musics;
		adapter = new MusicAdapter(getActivity(), musics, lvHotMusicList);
		lvHotMusicList.setAdapter(adapter);
	}
	
	/**
	 * 接收binder对象
	 * 
	 * @param binder
	 */
	public void setMusicBinder(MusicBinder binder) {
		this.musicBinder = binder;
	}

}
