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
	// ����������ؼ�
	private ViewPager vpMusicList;
	private RadioGroup rgButtons;
	private RadioButton rbNewMusicList;
	private RadioButton rbHotMusicList;
	private ImageView ivCurrentMusicPicture;
	private TextView tvCurrentMusicTitle;
	private Button btnToSearchMusic;

	// �������Ž���ؼ�
	private RelativeLayout rlPlayMusic;
	private TextView tvPlayMusicTitle;
	private TextView tvPlayMusicSinger;
	private TextView tvPlayMusicLrc;
	private TextView tvPlayMusicCurrentTime;
	private TextView tvPlayMusicTotalTime;
	private ImageView ivPlayMusicAlbum;
	private ImageView ivPlayMusicBackground;
	private SeekBar sbPlayMusicProgress;

	// ������������ؼ�
	private Button btnSearchMusic;
	private Button btnCancel;
	private EditText etKeyWord;
	private ListView lvSearchMusicResult;
	private RelativeLayout rlSearchMusic;

	// Fragment�ؼ�����
	private ArrayList<Fragment> fragments;
	private MyViewPagerAdapter viewpagerAdapter;

	// ��Service
	private ServiceConnection conn;
	private MusicBinder musicBinder;
	
	// ����service ��intent����
	private Intent service;

	// �㲥
	private MusicInfoReceiver receiver;

	// ����
	private static final int FRAGMENT_NEW_MUSIC_LIST = 0;
	private static final int FRAGMENT_HOT_MUSIC_LIST = 1;

	// �������ҵ���
	private MusicModel model = new MusicModel();

	// ��ѯ���ֽ������
	private List<Music> searchMusicList;
	// ��ѯ���ֽ��adapter
	private MusicResultAdapter musicResultAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// �ؼ���ʼ��
		setViews();

		// ����viewPager������
		setViewPagerAdapter();

		// ���ü�����
		setListeners();

		// ��Service
		bindService();

		// ע��㲥������
		registerReceiver();
	}

	/**
	 * ���ü�����
	 */
	private void setListeners() {
		// ������������õ������
		lvSearchMusicResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				// ��ȡ��ǰ��������ֶ���
				final Music music = searchMusicList.get(position);
				String songId = music.getSong_id();
				
				// �Ȱ������б�,position�浽application��
				MusicApplication application = MusicApplication
						.getApplication();
				application.setMusicList(searchMusicList);
				application.setPosition(position);
				
				// ����ҵ���, ����songId��ȡ������Ϣ����
				model.loadMusicInfoBySongId(songId, new MusicInfoCallback() {

					@Override
					public void onMusicInfoLoaded(List<SongUrl> urls,
							SongInfo info) {

						music.setUrls(urls);
						music.setInfo(info);
						
						// ���СͼƬ pic_small
						String picSmall = info.getPic_small();
						if ("".equals(picSmall)) {
							picSmall = info.getAlbum_500_500();
						}
						music.setPic_small(picSmall);

						String url = urls.get(0).getFile_link();
						if ("".equals(url)) { // File_link Ϊ""
							url = urls.get(0).getShow_link();
						}

						// ��������
						musicBinder.playMusic(url);

						// ��ʾ���Ž���ؼ�
						rlPlayMusic.setVisibility(View.VISIBLE);
						rlSearchMusic.setVisibility(View.INVISIBLE);
						
						// �������Ŷ���
						TranslateAnimation anim = new TranslateAnimation(0, 0 , -rlSearchMusic.getHeight(), 0);
						anim.setDuration(500);
						rlPlayMusic.startAnimation(anim);
					}

				});

			}
		});

		// ���������� ������ť btnSearchMusic ���õ������, �õ���ʾ��listview
		btnSearchMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchMusic(); // ͨ����������������ʾ���б���
			}

		});

		// ��������������ť btnToSearchMusic ���õ������ ��������ؿؼ���ʾ
		btnToSearchMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				rlSearchMusic.setVisibility(View.VISIBLE); // ���������ؼ��ɼ�
				// ���ö���, ��������ƽ��
				TranslateAnimation anim = new TranslateAnimation(0, 0,
						-rlSearchMusic.getHeight(), 0);
				anim.setDuration(500);
				rlSearchMusic.startAnimation(anim);
			}
		});

		// ��ȡ����ť btnCancel ���õ������, �ص���������ؿؼ�
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				rlSearchMusic.setVisibility(View.INVISIBLE); // ���������ؼ����ɼ�
				// ���ö���, ��������ƽ��
				TranslateAnimation anim = new TranslateAnimation(0, 0, 0,
						-rlSearchMusic.getHeight());
				anim.setDuration(500);
				rlSearchMusic.startAnimation(anim);
			}
		});

		// ���������� rlSearchMusic ���ô�������
		rlSearchMusic.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// �����Ž��� rlPlayMusic���ô�������
		rlPlayMusic.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// ��������sbPlayMusicProgress ���ÿ������
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
						if (fromUser) { // ���û���ק
							musicBinder.seekTo(progress);
						}
					}
				});

		// ����ViewPager����RadioButton
		vpMusicList.setOnPageChangeListener(new OnPageChangeListener() {

			// ��ѡ��ĳһҳʱ,ִ�и÷���
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

		// ����RadioButton����ViewPager
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
	 * ������ ����ͼƬ���������ʾ�������ֵ���ؿؼ�
	 */
	public void doClick(View view) {
		MusicApplication application = MusicApplication.getApplication();
		switch (view.getId()) {
		case R.id.iv_current_music_picture:
			// ��ʾ���Ž���ؼ�
			rlPlayMusic.setVisibility(View.VISIBLE);

			// ִ�д������ϵ�ƽ�ƶ���
			TranslateAnimation anim = new TranslateAnimation(0,
					rlPlayMusic.getHeight(), 0, 0);
			anim.setDuration(500);
			rlPlayMusic.setAnimation(anim);
			break;

		case R.id.iv_playmusic_playorpause: // ����/��ͣ��ť
			musicBinder.startOrPause();
			break;

		case R.id.iv_playmusic_previous: // ��һ��
			application.setPosition(application.getPosition() == 0 ? 0
					: application.getPosition() - 1); // ��˳��ѭ��
			final Music music1 = application.getCurrentMusic();
			if (music1.getUrls() != null) { // SongInfo�Ѿ����ع���
				String url = music1.getUrls().get(0).getFile_link();
				musicBinder.playMusic(url);
			} else { // ͨ��song_id��ȡ������Ϣ
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

		case R.id.iv_playmusic_next: // ��һ��
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
	 * ���ؼ���
	 * @param view
	 */
	public void download (View view) {
		// ����AlertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		MusicApplication application = MusicApplication.getApplication();
		Music music = null;
		try {
			music = application.getCurrentMusic();
		} catch (NullPointerException e) {
			Toast.makeText(this, "�ݲ�֧������", Toast.LENGTH_SHORT).show();
			return;
		}
		final List<SongUrl> urls = music.getUrls();
		final String title = music.getTitle();
		if (urls == null) {
			Toast.makeText(this, "�ݲ�֧������", Toast.LENGTH_SHORT).show();
			return;
		}
		String[] items = new String[urls.size()];
		for (int i=0; i<urls.size(); i++) {
			SongUrl url = urls.get(i);
			// �ļ���С size ��λΪ�ֽ�, �������
			int size = Integer.parseInt(url.getFile_size());
			// ��size ת�� ��  ���� 1.23M 
			String s = Math.floor(100.0 * size/ 1024 /1024) / 100 + "M"; 
			items[i] = s;
		}
		
		builder.setTitle("ѡ�����ذ汾")
			.setItems(items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SongUrl url = urls.get(which);
					
					// ��ȡ����·��
					String musicPath = url.getFile_link();
					
					// ��ʼ����, ����Intent�����ݲ���
					service = new Intent(MainActivity.this, DownloadService.class);
					service.putExtra("url", musicPath);
					service.putExtra("bitrate", url.getFile_bitrate());
					service.putExtra("title", title);
				 	service.putExtra("size", url.getFile_size());
				 	service.putExtra("extension", url.getFile_extension());
					// ��������
					startService(service);
				}
			});
		AlertDialog dialog = builder.create();
		dialog.show();
		
		
	}

	/**
	 * BroadcastReceiver���� ����������Ϣ�Ĺ㲥������
	 */
	class MusicInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(GlobalConsts.ACTION_UDTADE_MUSIC_PROGRESS)) { // �������ֲ��Ž���
				int currentTime = intent.getIntExtra(
						GlobalConsts.EXTRA_CURRENT_TIME, 0);
				int totalTime = intent.getIntExtra(
						GlobalConsts.EXTRA_TOTAL_TIME, 0);

				// ���� sbPlayMusicProgress tvPlayMusicCurrentTime
				// tvPlayMusicTotalTime
				sbPlayMusicProgress.setProgress(currentTime);
				sbPlayMusicProgress.setMax(totalTime);
				tvPlayMusicCurrentTime.setText(GlobalConsts.FORMAT
						.format(new Date(currentTime)));
				tvPlayMusicTotalTime.setText(GlobalConsts.FORMAT
						.format(new Date(totalTime)));

				// ���¸��
				MusicApplication application = MusicApplication
						.getApplication();
				Music music = application.getCurrentMusic();
				HashMap<String, String> lrc = music.getLrc();
				Log.w("jian",
						"currentTime: "
								+ GlobalConsts.FORMAT.format(new Date(
										currentTime)));
				if (lrc != null) { // �����������
					String lrcContent = lrc.get(GlobalConsts.FORMAT
							.format(new Date(currentTime)));
					if (lrcContent != null) {
						tvPlayMusicLrc.setText(lrcContent);
					}
				} else { // ���δ��������
					// tvPlayMusicLrc.setText("δ��ȡ�����");
				}
			} else if (action.equals(GlobalConsts.ACTION_MUSIC_STARTED)) { // ���ֿ�ʼ����
				// ��ʼ��������, ��ȡ��ǰ���ֶ������UI
				MusicApplication application = MusicApplication
						.getApplication();
				final Music music = application.getCurrentMusic();

				// ��ȡͼƬ·��, ����ͼƬ�ؼ�
				String picPath = music.getPic_small();
				BitmapUtils.loadBitmap(picPath, new BitmapCallback() {

					@Override
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap != null) { // ͼƬ������, ������ʾͼƬ
							ivCurrentMusicPicture.setImageBitmap(bitmap);
							// ִ����ת����
							RotateAnimation anim = new RotateAnimation(0, 360,
									ivCurrentMusicPicture.getWidth() / 2,
									ivCurrentMusicPicture.getHeight() / 2);
							anim.setDuration(10000);
							// ������ת
							anim.setInterpolator(new LinearInterpolator());
							// ������ת
							anim.setRepeatCount(Animation.INFINITE);
							ivCurrentMusicPicture.startAnimation(anim);
						} else { // ͼƬ����ʧ��, ��ʾĬ��ͼƬ
							ivCurrentMusicPicture
									.setImageResource(R.drawable.ic_contact_picture_2);
						}
					}
				});

				// ��ȡ����������Ϣ, �����ı��ؼ�
				tvCurrentMusicTitle.setText(music.getTitle());

				// ���� tvPlayMusicTitle tvPlayMusicSinger
				tvPlayMusicTitle.setText(music.getInfo().getTitle());
				tvPlayMusicSinger.setText(music.getInfo().getAuthor());

				// ��ʼ������ͼƬ
				ivPlayMusicBackground
						.setImageResource(R.drawable.default_music_background);
				// ���� ivPlayMusicBackground
				String backgroundPath = music.getInfo().getArtist_480_800();
				if ("".equals(backgroundPath)) { // û�б���ͼƬ
					backgroundPath = music.getInfo().getArtist_640_1136();
				}
				if ("".equals(backgroundPath)) { // û�б���ͼƬ
					backgroundPath = music.getInfo().getArtist_500_500();
				}
				if ("".equals(backgroundPath)) { // û�б���ͼƬ
					backgroundPath = music.getInfo().getArtist_1000_1000();
				}
				if ("".equals(backgroundPath)) { // û�б���ͼƬ
					backgroundPath = music.getInfo().getAlbum_500_500();
				}
				if ("".equals(backgroundPath)) { // û�б���ͼƬ
					backgroundPath = music.getInfo().getAlbum_1000_1000();
				}
				if ("".equals(backgroundPath)) { // û�б���ͼƬ
					backgroundPath = music.getInfo().getPic_small();
				}

				// ѹ���ȼ�
				int scale = 10;
				// ���ż���ͼƬ
				BitmapUtils.loadBitmap(backgroundPath, scale,
						new BitmapCallback() {

							@Override
							public void onBitmapLoaded(Bitmap bitmap) {
								if (bitmap != null) { // ͼƬ��Ϊnull
									// ģ��������
									BitmapUtils.loadBlurBitmap(bitmap,
											new BitmapCallback() {

												@Override
												public void onBitmapLoaded(
														Bitmap bitmap) {
													// ���ؼ����ñ���ͼƬ
													ivPlayMusicBackground
															.setImageBitmap(bitmap);
												}
											});
								} else { // ͼƬ����ʧ��, ����Ĭ��ͼƬ
									ivPlayMusicBackground
											.setImageResource(R.drawable.default_music_background);
								}
							}
						});

				// ��ʼ�� ivPlayMusicAlbum
				ivPlayMusicAlbum.setImageResource(R.drawable.default_music_pic);
				// ���� ivPlayMusicAlbum
				String albumPath = music.getInfo().getAlbum_500_500();
				if ("".equals(albumPath)) { // û��ר��ͼƬ
					albumPath = music.getInfo().getAlbum_1000_1000();
				}
				if ("".equals(albumPath)) { // û��ר��ͼƬ
					albumPath = music.getPic_big();
				}

				BitmapUtils.loadBitmap(albumPath, new BitmapCallback() {

					@Override
					public void onBitmapLoaded(Bitmap bitmap) {
						if (bitmap != null) { // ͼƬ��Ϊnull
							ivPlayMusicAlbum.setImageBitmap(bitmap);
						} else { // ͼƬ����ʧ��, ����Ĭ��ͼƬ
							ivPlayMusicAlbum
									.setImageResource(R.drawable.default_music_pic);
						}
					}
				});
				// ���ظ��
				model.downloadLrc(music.getInfo().getLrclink(),
						new LrcCallback() {

							@Override
							public void onLrcDownloaded(
									HashMap<String, String> lrc) {
								// ͨ���ص�, �����غõĸ�ʴ���music����
								music.setLrc(lrc);
								// �����Ӧ�ĸ�ʳ��ֵ�������
								// ÿ���1�� ��Ҫ���¸������
								// �ڸ������ֽ��ȵĹ㲥�������и��¼���
							}
						});
			}
		}

	}

	/**
	 * ��������
	 */
	private void searchMusic() {
		//1.  ��ȡ�ؼ���
		String keyWord = etKeyWord.getText().toString();
		if ("".equals(keyWord)) {
			return;
		}
		// 2. ����ҵ������,���ݹؼ���  ��ѯ��ؽ��
		model.searchMusic(keyWord, new MusicCallback() {

			@Override
			public void onMusicListLoaded(List<Music> musics) {
				// 3.  List<Music>
				searchMusicList = musics;
				// 4.�Ѳ�ѯ�����ʾ��ListView��,����adapter
				musicResultAdapter = new MusicResultAdapter(MainActivity.this,
						musics);
				lvSearchMusicResult.setAdapter(musicResultAdapter);

			}
		});
	}

	/**
	 * ��������ذ�ť ����ڲ��Ž���, �˻ص������б������� ����������б�, ����Activity�ص�����
	 */
	@Override
	public void onBackPressed() {
		if (rlPlayMusic.getVisibility() == View.VISIBLE) { // ���Ž�����ʾʱ
			rlPlayMusic.setVisibility(View.INVISIBLE);

			// ִ��ƽ�ƶ���, ����ƽ��
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
	 * ע��㲥������
	 */
	private void registerReceiver() {
		receiver = new MusicInfoReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_MUSIC_STARTED);
		filter.addAction(GlobalConsts.ACTION_UDTADE_MUSIC_PROGRESS);
		registerReceiver(receiver, filter);
	}

	/**
	 * ��PlayMusicService
	 */
	private void bindService() {
		Intent service = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {

			// ����ʱ
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				musicBinder = (MusicBinder) service;
				// ��binder���󽻸�����fragment
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
	 * ����viewPager������
	 */
	private void setViewPagerAdapter() {
		// ׼��Fragment������Ϊadapter������Դ
		fragments = new ArrayList<Fragment>();
		// �򼯺����������Fragment �¸����ȸ��
		fragments.add(new NewMusicListFragment());
		fragments.add(new HotMusicListFragment());

		// ����viewPager������
		// ע��getSupportFragmentManager() ��Activity��̳�FragmentActivity
		viewpagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
		vpMusicList.setAdapter(viewpagerAdapter);

	}

	/**
	 * �ҵ�viewPager��������
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
	 * �ؼ���ʼ��
	 */
	private void setViews() {
		// ��ʼ��������ؼ�
		vpMusicList = (ViewPager) findViewById(R.id.vp_music_list);
		rgButtons = (RadioGroup) findViewById(R.id.rg_buttons);
		rbNewMusicList = (RadioButton) findViewById(R.id.rb_new_music_list);
		rbHotMusicList = (RadioButton) findViewById(R.id.rb_hot_music_list);
		ivCurrentMusicPicture = (ImageView) findViewById(R.id.iv_current_music_picture);
		tvCurrentMusicTitle = (TextView) findViewById(R.id.tv_current_music_title);
		btnToSearchMusic = (Button) findViewById(R.id.btn_to_search_music);

		// ��ʼ�����Ž���ؼ�
		rlPlayMusic = (RelativeLayout) findViewById(R.id.rl_playmusic);
		tvPlayMusicTitle = (TextView) findViewById(R.id.tv_playmusic_title);
		tvPlayMusicSinger = (TextView) findViewById(R.id.tv_playmusic_singer);
		tvPlayMusicLrc = (TextView) findViewById(R.id.tv_playmusic_lrc);
		tvPlayMusicCurrentTime = (TextView) findViewById(R.id.tv_playmusic_current_time);
		tvPlayMusicTotalTime = (TextView) findViewById(R.id.tv_playmusic_total_time);
		ivPlayMusicAlbum = (ImageView) findViewById(R.id.iv_playmusic_album);
		ivPlayMusicBackground = (ImageView) findViewById(R.id.iv_playmusic_background);
		sbPlayMusicProgress = (SeekBar) findViewById(R.id.sb_playmusic_progress);

		// ��ʼ����������ؼ�
		btnSearchMusic = (Button) findViewById(R.id.btn_search_music);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		etKeyWord = (EditText) findViewById(R.id.et_key_word);
		lvSearchMusicResult = (ListView) findViewById(R.id.lv_search_music_result);
		rlSearchMusic = (RelativeLayout) findViewById(R.id.rl_search_music);
	}

	@Override
	protected void onDestroy() {
		// ���Service
		unbindService(conn);
		
		// ֹͣService
		stopService(service);
		
		// ע���㲥
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
