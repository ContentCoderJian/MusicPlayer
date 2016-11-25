package pers.jian.musicclientv4.adapter;

import java.util.List;

import pers.jian.musicclientv4.R;
import pers.jian.musicclientv4.entity.Music;
import pers.jian.musicclientv4.util.ImageLoader;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter {
	private List<Music> musics;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;

	public MusicAdapter(Context context, List<Music> musics, ListView listView) {
		this.musics = musics;
		this.inflater = LayoutInflater.from(context);
		this.imageLoader = new ImageLoader(context, listView);
	}

	@Override
	public int getCount() {
		return musics.size();
	}

	@Override
	public Object getItem(int position) {
		return musics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ����positon��ȡָ��������
		Music music = (Music) getItem(position);

		ViewHolder holder = null;
		// �ж�convertView�Ƿ�Ϊnull
		if (convertView == null) {
			// ��Ϊnull, �����xml�ĵ����ز���, ����װ�ؼ�
			convertView = inflater.inflate(R.layout.music_item, null);
			holder = new ViewHolder();
			holder.ivPic = (ImageView) convertView.findViewById(R.id.iv_pic);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tvArtist = (TextView) convertView
					.findViewById(R.id.tv_artist);
			convertView.setTag(holder);
		}
		// ȡ����װ�Ŀؼ�
		holder = (ViewHolder) convertView.getTag();

		// ���ؼ�
		holder.tvName.setText(music.getTitle());
		holder.tvArtist.setText(music.getAuthor());
		imageLoader.displayImage(music.getPic_small(), holder.ivPic);
		return convertView;
	}

	class ViewHolder {
		ImageView ivPic;
		TextView tvName;
		TextView tvArtist;
	}

	public void stopThread() {
		imageLoader.stopThread();
	}
}
