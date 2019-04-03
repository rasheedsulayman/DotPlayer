package com.r4sh33d.musicslam.fragments.genres;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.interfaces.FastScrollerAdapter;
import com.r4sh33d.musicslam.models.Genres;
import com.r4sh33d.musicslam.models.Song;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by r4sh33d on 4/30/17.
 */

public class GeneresListAdapter extends RecyclerView.Adapter<GeneresListAdapter.MyHolder> implements
        FastScrollerAdapter {
    private Context context;
    private List<Genres> genresArrayList;

    public GeneresListAdapter(Context context, List<Genres> genresArrayList) {
        this.context = context;
        this.genresArrayList = genresArrayList;
    }

    public void updateData(List<Genres> genresArrayList) {
        this.genresArrayList = genresArrayList;
        notifyDataSetChanged();
    }


    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_two_line_no_image_big_margin, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Genres genres = genresArrayList.get(position);
        holder.genresNameTextView.setText(genres.name);
        holder.songCountTextView.setText(String.valueOf(genres.songCount) + " Songs");
    }

    @Override
    public int getItemCount() {
        return genresArrayList.size();
    }

    @Override
    public String getFastScrollerThumbCharacter(int position) {
        String sectionName = genresArrayList.get(position).name;
        return TextUtils.isEmpty(sectionName) ? "" : sectionName.substring(0, 1).toUpperCase();
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView genresNameTextView;
        @BindView(R.id.line_two_text)
        TextView songCountTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;

        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
        }


        private long[] getSongIdsForGenres() {
            return SongIdsLoader.getSongListForGenres(context, genresArrayList.get(getAdapterPosition()).id);
        }


        private List<Song> getSongsForGenres() {
            return SongLoader.getSongsForGenres(genresArrayList.get(getAdapterPosition()).id, context);
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                PopupMenu popUpmenu = new PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    Genres genres = genresArrayList.get(getAdapterPosition());
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(getSongsForGenres(), 0,  false);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(getSongsForGenres(), context);
                            break;
                        case R.id.menu_song_delete:
                            DeleteSongsDialog.newInstance(getSongIdsForGenres(), genres.name)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(), DeleteSongsDialog.DELETE_FRAG_TAG);
                            break;

                        case R.id.menu_song_add_to_playlist:
                            AddToPlaylistDialog.newInstance(getSongIdsForGenres())
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                            break;
                        case R.id.menu_song_add_to_queue:
                            MusicPlayer.addToQueue(context, getSongsForGenres());
                            break;

                        case R.id.menu_rename:
                            //TODO comeback and rename
                            break;
                    }
                    return true;
                });
                popUpmenu.inflate(R.menu.menu_popup_pager_fragments_items);
                popUpmenu.show();
            });
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.moveToGenres(genresArrayList.get(getAdapterPosition()), context);

        }
    }
}
