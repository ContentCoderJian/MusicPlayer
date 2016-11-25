package pers.jian.musicclientv4.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pers.jian.musicclientv4.R;
import pers.jian.musicclientv4.adapter.MusicResultAdapter;
import pers.jian.musicclientv4.appliacation.MusicApplication;
import pers.jian.musicclientv4.entity.Music;
import pers.jian.musicclientv4.entity.SongInfo;
import pers.jian.musicclientv4.entity.SongUrl;
import pers.jian.musicclientv4.fragment.HotMusicListFragment;
import pers.jian.musicclientv4.fragment.NewMusicListFragment;
import pers.jian.musicclientv4.model.LrcCallback;
import pers.jian.musicclientv4.model.MusicCallback;
import pers.jian.musicclientv4.model.MusicInfoCallback;
import pers.jian.musicclientv4.model.MusicModel;
import pers.jian.musicclientv4.service.DownloadService;
import pers.jian.musicclientv4.service.PlayMusicService;
import pers.jian.musicclientv4.service.PlayMusicService.MusicBinder;
import pers.jian.musicclientv4.util.BitmapCallback;
import pers.jian.musicclientv4.util.BitmapUtils;
import pers.jian.musicclientv4.util.GlobalConsts;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	// 声明主界面控件
	private ViewPager vpMusicList;
	private RadioGroup rgButtons;
	private RadioButton rbNewMusicList;
	private RadioButton rbHotMusicList;
	private ImageView ivCurrentMusicPicture;
	private TextView tvCurrentMusicTitle;
	private Button btnToSearchMusic;

	// 声明播放界面控件
	private RelativeLayout rlPlayMusic;
	private TextView tvPlayMusicTitle;
	private TextView tvPlayMusicSinger;
	private TextView tvPlayMusicLrc;
	private TextView tvPlayMusicCurrentTime;
	private TextView tvPlayMusicTotalTime;
	private ImageView ivPlayMusicAlbum;
	private ImageView ivPlayMusicBackground;
	private SeekBar sbPlayMusicProgress;

	// 声明搜索界面控件
	private Button btnSearchMusic;
	private Button btnCancel;
	private EditText etKeyWord;
	private ListView lvSearchMusicResult;
	private RelativeLayout rlSearchMusic;

	// Fragment控件集合
	private ArrayList<Fragment> fragments;
	private MyViewPagerAdapter viewpagerAdapter;

	// 绑定Service
	private ServiceConnection conn;
	private MusicBinder musicBinder;
	
	// 启动service 的intent对象
	private Intent service;

	// 广播
	private MusicInfoReceiver receiver;

	// 常量
	private static final int FRAGMENT_NEW_MUSIC_LIST = 0;
	private static final int FRAGMENT_HOT_MUSIC_LIST = 1;

	// 音乐相关业务层
	private MusicModel model = new MusicModel();

	// 查询音乐结果集合
	private List<Music> searchMusicList;
	// 查询音乐结果adapter
	private MusicResultAdapter musicResultAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 控件初始化
		setViews();

		// 设置viewPager适配器
		setViewPagerAdapter();

		// 设置监听器
		setListeners();

		// 绑定Service
		bindService();

		// 注册广播接收者
		registerReceiver();
	}

	/**
	 * 设置监听器
	 */
	private void setListeners() {
		// 给搜索结果设置点击监听
		lvSearchMusicResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				// 获取当前点击的音乐对象
				final Music music = searchMusicList.get(position);
				String songId = music.getSong_id();
				
				// 先把音乐列表,position存到application中
				MusicApplication application = MusicApplication
						.getApplication();
				application.setMusicList(searchMusicList);
				application.setPosition(position);
				
				// 调用业务层, 根据songId获取音乐信息集合
				model.loadMusicInfoBySongId(songId, new MusicInfoCallback() {

					@Override
					public void onMusicInfoLoaded(List<SongUrl> urls,
							SongInfo info) {

						music.setUrls(urls);
						music.setInfo(info);
						
						// 解决小图片 pic_small
						String picSmall = info.getPic_small();
						if ("".equals(picSmall)) {
							picSmall = info.getAlbum_500_500();
						}
						music.setPic_small(picSmall);

						String url = urls.get(0).getFile_link();
						if ("".equals(url)) { // File_link 为""
							url = urls.get(0).getShow_link();
						}

						// 播放音乐
						musicBinder.playMusic(url);

						// 显示播放界面控件
						rlPlayMusic.setVisibility(View.VISIBLE);
						rlSearchMusic.setVisibility(View.INVISIBLE);
						
						// 弹出缩放动画
						TranslateAnimation anim = new TranslateAnimation(0, 0 , -rlSearchMusic.getHeight(), 0);
						anim.setDuration(500);
						rlPlayMusic.startAnimation(anim);
					}

				});

			}
		});

		// 给搜索界面 搜索按钮 btnSearchMusic 设置点击监听, 得到显示的listview
		btnSearchMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchMusic(); // 通过方法搜索音乐显示在列表中
			}

		});

		// 给主界面搜索按钮 btnToSearchMusic 设置点击监听 让搜索相关控件显示
		btnToSearchMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				rlSearchMusic.setVisibility(View.VISIBLE); // 设置搜索控件可见
				// 设置动画, 从上向下平移
				TranslateAnimation anim = new TranslateAnimation(0, 0,
						-rlSearchMusic.getHeight(), 0);
				anim.setDuration(500);
				rlSearchMusic.startAnimation(anim);
			}
		});

		// 给取消按钮 btnCancel 设置点击监听, 回到主界面相关控件
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				rlSearchMusic.setVisibility(View.INVISIBLE); // 设置搜索控件不可见
				// 设置动画, 从下向上平移
				TranslateAnimation anim = new TranslateAnimation(0, 0, 0,
						-rlSearchMusic.getHeight());
				anim.setDuration(500);
				rlSearchMusic.startAnimation(anim);
			}
		});

		// 给搜索界面 rlSearchMusic 设置触摸监听
		rlSearchMusic.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// 给播放界面 rlPlayMusic设置触摸监听
		rlPlayMusic.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// 给进度条sbPlayMusicProgress 设置快进监听
		sbPlayMusicProgress
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) { // 是用户拖拽
							musicBinder.seekTo(progress);
						}
					}
				});

		// 设置ViewPager控制RadioButton
		vpMusicList.setOnPageChangeListener(new OnPageChangeListener() {

			// 当选择某一页时,执行该方法
			@Override
			public void onPageSelected(int position) {
				switch (position) {
				case FRAGMENT_NEW_MUSIC_LIST:
					rbNewMusicList.setChecked(true);
					break;
				case FRAGMENT_HOT_MUSIC_LIST:
					rbHotMusicList.setChecked(true);
					break;
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		// 设置RadioButton控制ViewPager
		rgButtons.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_new_music_list:
					vpMusicList.setCurrentItem(FRAGMENT_NEW_MUSIC_LIST);
					break;

				case R.id.rb_hot_music_list:
					vpMusicList.setCurrentItem(FRAGMENT_HOT_MUSIC_LIST);
					break;
				}
			}
		});
	}

	/**
	 * 监听： 播放图片点击监听显示播放音乐的相关控件
	 */
	public void doClick(View view) {
		MusicApplication application = MusicApplication.getApplication();
		switch (view.getId()) {
		case R.id.iv_current_music_picture:
			// 显示播放界面控件
			rlPlayMusic.setVisibility(View.VISIBLE);

			// 执行从下向上的平移动画
			TranslateAnimation anim = new TranslateAnimation(0,
					rlPlayMusic.getHeight(), 0, 0);
			anim.setDuration(500);
			rlPlayMusic.setAnimation(anim);
			break;

		case R.id.iv_playmusic_playorpause: // 播放/暂停按钮
			musicBinder.startOrPause();
			break;

		case R.id.iv_playmusic_previous: // 上一首
			application.setPosition(application.getPosition() == 0 ? 0
					: application.getPosition() - 1); // 单顺序循环
			final Music music1 = application.getCurrentMusic();
			if (music1.getUrls() != null) { // SongInfo已经加载过了
				String url = music1.getUrls().get(0).getFile_link();
				musicBinder.playMusic(url);
			} else { // 通过song_id获取歌曲信息
				model.loadMusicInfoBySongId(music1.getSong_id(),
						new MusicInfoCallback() {

							@Override
							public void onMusicInfoLoaded(List<SongUrl> urls,
									SongInfo info) {
								music1.setUrls(urls);
								music1.setInfo(info);
								String url = music1.getUrls().get(0)
										.getFile_link();
								musicBinder.playMusic(url);
							}
						});
			}
			break;

		case R.id.iv_playmusic_next: // 下一首
			application.setPosition(application.getPosition() == application
					.getMusicList().size() - 1 ? 0
					: application.getPosition() + 1);
			final Music music2 = application.getCurrentMusic();
			if (music2.getUrls() != null) {
				String url = music2.getUrls().get(0).getFile_link();
				musicBinder.playMusic(url);
			} else {
				model.loadMusicInfoBySongId(music2.getSong_id(),
						new MusicInfoCallback() {

							@Override
							public void onMusicInfoLoaded(List<SongUrl> urls,
									SongInfo info) {
								music2.setUrls(urls);
								music2.setInfo(info);
								String url = music2.getUrls().get(0)
										.getFile_link();
								musicBinder.playMusic(url);
							}
						});
			}
			break;
		}
	}
	
	/**
	 * 下载监听
	 * @param view
	 */
	public void download (View view) {
		// 弹出AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		MusicApplication application = MusicApplication.getApplication();
		Music music = null;
		try {
			music = application.getCurrentMusic();
		} catch (NullPointerException e) {
			Toast.makeText(this, "暂不支持下载", Toast.LENGTH_SHORT).show();
			return;
		}
		final List<SongUrl> urls = music.getUrls();
		final String title = music.getTitle();
		if (urls == null) {
			Toast.makeText(this, "暂不支持下载", Toast.LENGTH_SHORT).show();
			return;
		}
		String[] items = new String[urls.size()];
		for (int i=0; i<urls.size(); i++) {
			SongUrl url = urls.get(i);
			// 文件大小 size 单位为字节, 换算成兆
			int size = Integer.parseInt(url.getFile_size());
			// 将size 转成 兆  类似 1.23M 
			String s = Math.floor(100.0 * size/ 1024 /1024) / 100 + "M"; 
			items[i] = s;
		}
		
		builder.setTitle("选择下载版本")
			.setItems(items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SongUrl url = urls.get(which);
					
					// 获取下载路径
					String musicPath = url.getFile_link();
					
					// 开始下载, 设置Intent并传递参数
					service = new Intent(MainActivity.this, DownloadService.class);
					service.putExtra("url", musicPath);
					service.putExtra("bitrate", url.getFile_bitrate());
					service.putExtra("title", title);
				 	service.putExtra("size", url.getFile_size());
				 	service.putExtra("extension", url.getFile_extension());
					// 开启服务
					startService(service);
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
		
		
	}

	/**
	 * BroadcastReceiver子类 接收音乐信息的广播接收器
	 */
	class MusicInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(GlobalConsts.ACTION_UDTADE_MUSIC_PROGRESS)) { // 更新音乐播放进度
				int currentTime = intent.getIntExtra(
						GlobalConsts.EXTRA_CURRENT_TIME, 0);
				int totalTime = intent.getIntExtra(
						GlobalConsts.EXTRA_TOTAL_TIME, 0);

				// 更新 sbPlayMusicProgress tvPlayMusicCurrentTime
				// tvPlayMusicTotalTime
				sbPlayMusicProgress.setProgress(currentTime);
				sbPlayMusicProgress.setMax(totalTime);
				tvPlayMusicCurrentTime.setText(GlobalConsts.FORMAT
						.format(new Date(currentTime)));
				tvPlayMusicTotalTime.setText(GlobalConsts.FORMAT
						.format(new Date(totalTime)));

				// 更新歌词
				MusicApplication application = MusicApplication
						.getApplication();
				Music music = application.getCurrentMusic();
				HashMap<String, String> lrc = music.getLrc();
				Log.w("jian",
						"currentTime: "
								+ GlobalConsts.FORMAT.format(new Date(
										currentTime)));
				if (lrc != null) { // 歌词正常加载
					String lrcContent = lrc.get(GlobalConsts.FORMAT
							.format(new Date(currentTime)));
					if (lrcContent != null) {
						tvPlayMusicLrc.setText(lrcContent);
					}
				} else { // 歌词未正常加载
					// tvPlayMusicLrc.setText("未获取到歌词");
				}
			} else if (action.equals(GlobalConsts.ACTION_MUSIC_STARTED)) { // 音乐开始播放
				// 开始播放音乐, 获取当前音乐对象更新UI
				MusicApplication application = MusicApplication
						.getApplication();
				final Music music = application.getCurrentMusic();

				// 获取图片路径, 更新图片控件
				String picPath = music.getPic_small();
				BitmapUtils.loadBitmap(picPath, new BitmapCallback() {

					@Override
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap != null) { // 图片不存在, 更新显示图片
							ivCurrentMusicPicture.setImageBitmap(bitmap);
							// 执行旋转动画
							RotateAnimation anim = new RotateAnimation(0, 360,
									ivCurrentMusicPicture.getWidth() / 2,
									ivCurrentMusicPicture.getHeight() / 2);
							anim.setDuration(10000);
							// 匀速旋转
							anim.setInterpolator(new LinearInterpolator());
							// 无限旋转
							anim.setRepeatCount(Animation.INFINITE);
							ivCurrentMusicPicture.startAnimation(anim);
						} else { // 图片加载失败, 显示默认图片
							ivCurrentMusicPicture
									.setImageResource(R.drawable.ic_contact_picture_2);
						}
					}
				});

				// 获取歌曲名称信息, 更新文本控件
				tvCurrentMusicTitle.setText(music.getTitle());

				// 更新 tvPlayMusicTitle tvPlayMusicSinger
				tvPlayMusicTitle.setText(music.getInfo().getTitle());
				tvPlayMusicSinger.setText(music.getInfo().getAuthor());

				// 初始化背景图片
				ivPlayMusicBackground
						.setImageResource(R.drawable.default_music_background);
				// 更新 ivPlayMusicBackground
				String backgroundPath = music.getInfo().getArtist_480_800();
				if ("".equals(backgroundPath)) { // 没有背景图片
					backgroundPath = music.getInfo().getArtist_640_1136();
				}
				if ("".equals(backgroundPath)) { // 没有背景图片
					backgroundPath = music.getInfo().getArtist_500_500();
				}
				if ("".equals(backgroundPath)) { // 没有背景图片
					backgroundPath = music.getInfo().getArtist_1000_1000();
				}
				if ("".equals(backgroundPath)) { // 没有背景图片
					backgroundPath = music.getInfo().getAlbum_500_500();
				}
				if ("".equals(backgroundPath)) { // 没有背景图片
					backgroundPath = music.getInfo().getAlbum_1000_1000();
				}
				if ("".equals(backgroundPath)) { // 没有背景图片
					backgroundPath = music.getInfo().getPic_small();
				}

				// 压缩等级
				int scale = 10;
				// 缩放加载图片
				BitmapUtils.loadBitmap(backgroundPath, scale,
						new BitmapCallback() {

							@Override
							public void onBitmapLoaded(Bitmap bitmap) {
								if (bitmap != null) { // 图片不为null
									// 模糊化处理
									BitmapUtils.loadBlurBitmap(bitmap,
											new BitmapCallback() {

												@Override
												public void onBitmapLoaded(
														Bitmap bitmap) {
													// 给控件设置背景图片
													ivPlayMusicBackground
															.setImageBitmap(bitmap);
												}
											});
								} else { // 图片加载失败, 设置默认图片
									ivPlayMusicBackground
											.setImageResource(R.drawable.default_music_background);
								}
							}
						});

				// 初始化 ivPlayMusicAlbum
				ivPlayMusicAlbum.setImageResource(R.drawable.default_music_pic);
				// 更新 ivPlayMusicAlbum
				String albumPath = music.getInfo().getAlbum_500_500();
				if ("".equals(albumPath)) { // 没有专辑图片
					albumPath = music.getInfo().getAlbum_1000_1000();
				}
				if ("".equals(albumPath)) { // 没有专辑图片
					albumPath = music.getPic_big();
				}

				BitmapUtils.loadBitmap(albumPath, new BitmapCallback() {

					@Override
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap != null) { // 图片不为null
							ivPlayMusicAlbum.setImageBitmap(bitmap);
						} else { // 图片加载失败, 设置默认图片
							ivPlayMusicAlbum
									.setImageResource(R.drawable.default_music_pic);
						}
					}
				});
				// 下载歌词
				model.downloadLrc(music.getInfo().getLrclink(),
						new LrcCallback() {

							@Override
							public void onLrcDownloaded(
									HashMap<String, String> lrc) {
								// 通过回调, 把下载好的歌词存入music对象
								music.setLrc(lrc);
								// 把相对应的歌词呈现到界面中
								// 每间隔1秒 需要更新歌词内容
								// 在更新音乐进度的广播接收器中更新即可
							}
						});
			}
		}

	}

	/**
	 * 搜索音乐
	 */
	private void searchMusic() {
		//1.  获取关键字
		String keyWord = etKeyWord.getText().toString();
		if ("".equals(keyWord)) {
			return;
		}
		// 2. 调用业务层代码,根据关键字  查询相关结果
		model.searchMusic(keyWord, new MusicCallback() {

			@Override
			public void onMusicListLoaded(List<Music> musics) {
				// 3.  List<Music>
				searchMusicList = musics;
				// 4.把查询结果显示在ListView中,更新adapter
				musicResultAdapter = new MusicResultAdapter(MainActivity.this,
						musics);
				lvSearchMusicResult.setAdapter(musicResultAdapter);

			}
		});
	}

	/**
	 * 当点击返回按钮 如果在播放界面, 退回到音乐列表主界面 如果在音乐列表, 销毁Activity回到桌面
	 */
	@Override
	public void onBackPressed() {
		if (rlPlayMusic.getVisibility() == View.VISIBLE) { // 播放界面显示时
			rlPlayMusic.setVisibility(View.INVISIBLE);

			// 执行平移动画, 向下平移
			TranslateAnimation anim = new TranslateAnimation(0, 0, 0,
					rlPlayMusic.getHeight());
			anim.setDuration(500);
			rlPlayMusic.setAnimation(anim);
		} else if (rlSearchMusic.getVisibility() == View.VISIBLE) {
			rlSearchMusic.setVisibility(View.INVISIBLE);
			TranslateAnimation anim = new TranslateAnimation(0, 0, 0,
					-rlSearchMusic.getHeight());
			anim.setDuration(500);
			rlSearchMusic.startAnimation(anim);
		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
		}
	}

	/**
	 * 注册广播接收者
	 */
	private void registerReceiver() {
		receiver = new MusicInfoReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_MUSIC_STARTED);
		filter.addAction(GlobalConsts.ACTION_UDTADE_MUSIC_PROGRESS);
		registerReceiver(receiver, filter);
	}

	/**
	 * 绑定PlayMusicService
	 */
	private void bindService() {
		Intent service = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {

			// 当绑定时
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				musicBinder = (MusicBinder) service;
				// 把binder对象交给两个fragment
				NewMusicListFragment newFragment = (NewMusicListFragment) fragments
						.get(FRAGMENT_NEW_MUSIC_LIST);
				newFragment.setMusicBinder(musicBinder);
				HotMusicListFragment hotFragment = (HotMusicListFragment) fragments
						.get(FRAGMENT_HOT_MUSIC_LIST);
				hotFragment.setMusicBinder(musicBinder);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
		};
		bindService(service, conn, BIND_AUTO_CREATE);
	}

	/**
	 * 设置viewPager适配器
	 */
	private void setViewPagerAdapter() {
		// 准备Fragment集合作为adapter的数据源
		fragments = new ArrayList<Fragment>();
		// 向集合中添加两个Fragment 新歌榜和热歌榜
		fragments.add(new NewMusicListFragment());
		fragments.add(new HotMusicListFragment());

		// 设置viewPager适配器
		// 注：getSupportFragmentManager() 该Activity需继承FragmentActivity
		viewpagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
		vpMusicList.setAdapter(viewpagerAdapter);

	}

	/**
	 * 我的viewPager适配器类
	 */
	class MyViewPagerAdapter extends FragmentPagerAdapter {

		public MyViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

	}

	/**
	 * 控件初始化
	 */
	private void setViews() {
		// 初始化主界面控件
		vpMusicList = (ViewPager) findViewById(R.id.vp_music_list);
		rgButtons = (RadioGroup) findViewById(R.id.rg_buttons);
		rbNewMusicList = (RadioButton) findViewById(R.id.rb_new_music_list);
		rbHotMusicList = (RadioButton) findViewById(R.id.rb_hot_music_list);
		ivCurrentMusicPicture = (ImageView) findViewById(R.id.iv_current_music_picture);
		tvCurrentMusicTitle = (TextView) findViewById(R.id.tv_current_music_title);
		btnToSearchMusic = (Button) findViewById(R.id.btn_to_search_music);

		// 初始化播放界面控件
		rlPlayMusic = (RelativeLayout) findViewById(R.id.rl_playmusic);
		tvPlayMusicTitle = (TextView) findViewById(R.id.tv_playmusic_title);
		tvPlayMusicSinger = (TextView) findViewById(R.id.tv_playmusic_singer);
		tvPlayMusicLrc = (TextView) findViewById(R.id.tv_playmusic_lrc);
		tvPlayMusicCurrentTime = (TextView) findViewById(R.id.tv_playmusic_current_time);
		tvPlayMusicTotalTime = (TextView) findViewById(R.id.tv_playmusic_total_time);
		ivPlayMusicAlbum = (ImageView) findViewById(R.id.iv_playmusic_album);
		ivPlayMusicBackground = (ImageView) findViewById(R.id.iv_playmusic_background);
		sbPlayMusicProgress = (SeekBar) findViewById(R.id.sb_playmusic_progress);

		// 初始化搜索界面控件
		btnSearchMusic = (Button) findViewById(R.id.btn_search_music);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		etKeyWord = (EditText) findViewById(R.id.et_key_word);
		lvSearchMusicResult = (ListView) findViewById(R.id.lv_search_music_result);
		rlSearchMusic = (RelativeLayout) findViewById(R.id.rl_search_music);
	}

	@Override
	protected void onDestroy() {
		// 解绑Service
		unbindService(conn);
		
		// 停止Service
		stopService(service);
		
		// 注销广播
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
