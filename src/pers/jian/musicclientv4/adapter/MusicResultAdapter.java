package pers.jian.musicclientv4.adapter;

import java.util.List;

import pers.jian.musicclientv4.R;
import pers.jian.musicclientv4.entity.Music;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicResultAdapter extends BaseAdapter {
	@SuppressWarnings("unused")
	private Context context;
	private List<Music> musics;
	private LayoutInflater inflater;

	public MusicResultAdapter(Context context, List<Music> musics) {
		super();
		this.context = context;
		this.musics = musics;
		this.inflater = LayoutInflater.from(context);
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

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.music_result_item, null);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			holder.tvArtist = (TextView) convertView
					.findViewById(R.id.tv_artist);
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		Music music = (Music) getItem(position);
		holder.tvTitle.setText(music.getTitle());
		holder.tvArtist.setText(music.getAuthor());
		return convertView;
	}

	class ViewHolder {
		TextView tvTitle;
		TextView tvArtist;
	}

}
