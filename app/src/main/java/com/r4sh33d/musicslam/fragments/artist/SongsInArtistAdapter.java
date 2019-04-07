package com.r4sh33d.musicslam.fragments.artist;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dialogs.SongDetailsDialog;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dataloaders.AlbumLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.models.Artist;
import com.r4sh33d.musicslam.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SongsInArtistAdapter extends RecyclerView.Adapter<SongsInArtistAdapter.Myholder> {

    private List<Song> songsInArtistArrayList;
    private Context context;
    private Artist artist;

    public SongsInArtistAdapter(ArrayList<Song> songsInArtistArrayList, Context context, Artist artist) {
        this.artist = artist;
        this.songsInArtistArrayList = songsInArtistArrayList;
        this.context = context;
    }

    @Override
    public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_two_lines_duration_one, parent,
                false);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(Myholder holder, int position) {
        Song tempSong = songsInArtistArrayList.get(position);
        holder.songTitleTextView.setText(tempSong.title);
        holder.songAlbumTextView.setText(tempSong.albumName);
        holder.songDuration.setText(MusicUtils.makeShortTimeString(context, tempSong.duration / 1000));
        GlideApp.with(context)
                .load(new AudioCoverImage(tempSong.data))
                .placeholder(context.getDrawable(R.drawable.default_artwork))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.albumArtImageView);
    }


    public long[] getSongIds() {
        if (songsInArtistArrayList != null) {
            long[] ret = new long[getItemCount()];
            for (int i = 0; i < songsInArtistArrayList.size(); i++) {
                ret[i] = songsInArtistArrayList.get(i).id;
            }
            return ret;
        }
        return null;
    }

    public List<Song> getData(){
        return songsInArtistArrayList;
    }


    @Override
    public int getItemCount() {
        return songsInArtistArrayList.size();
    }

    public void updateData(List<Song> data) {
        this.songsInArtistArrayList = data;
        notifyDataSetChanged();
    }

    class Myholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView songTitleTextView;
        @BindView(R.id.line_two_text)
        TextView songAlbumTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView albumArtImageView;
        @BindView(R.id.duration)
        TextView songDuration;


        public Myholder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                android.widget.PopupMenu popUpmenu = new android.widget.PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    long[] currentSongListWrapper = {songsInArtistArrayList.get(position).id};
                    Song currentSong = songsInArtistArrayList.get(position);
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll( songsInArtistArrayList, position, false);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(currentSong, context);
                            break;
                        case R.id.menu_song_delete:
                            DeleteSongsDialog.newInstance(currentSongListWrapper, currentSong.title)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(), DeleteSongsDialog.DELETE_FRAG_TAG);
                            break;
                        case R.id.menu_song_share:
                            SlamUtils.shareSong(currentSong, context);
                            break;
                        case R.id.menu_song_add_to_playlist:
                            AddToPlaylistDialog.newInstance(currentSongListWrapper)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                            break;
                        case R.id.menu_song_add_to_queue:
                            MusicPlayer.addToQueue(context, currentSong);
                            break;
                        case R.id.menu_song_ringtone:
                            MusicUtils.setRingtone(context, currentSong.id);
                            break;
                        case R.id.menu_song_go_to_album:
                            NavigationUtil.moveToAlbum(context, AlbumLoader.getAlbum(currentSong.albumId, context));
                            break;
                        case R.id.menu_song_details:
                            SongDetailsDialog.newInstance(currentSong)
                                    .show(((AppCompatActivity)context).getSupportFragmentManager(),
                                            SongDetailsDialog.SONG_DETAILS_DIALOG);
                            break;
                    }
                    return true;
                });
                popUpmenu.inflate(R.menu.menu_song_list_popup);
                popUpmenu.getMenu().findItem(R.id.menu_song_go_to_artist)
                        .setVisible(false).setEnabled(false);
                popUpmenu.show();
            });
        }

        @Override
        public void onClick(View v) {
            MusicPlayer.playAll( songsInArtistArrayList, getAdapterPosition(),  false);
        }
    }
}
