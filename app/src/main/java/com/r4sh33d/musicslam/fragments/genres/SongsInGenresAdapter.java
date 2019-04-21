package com.r4sh33d.musicslam.fragments.genres;

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
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dataloaders.AlbumLoader;
import com.r4sh33d.musicslam.dataloaders.ArtistLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.dialogs.SongDetailsDialog;
import com.r4sh33d.musicslam.models.Genres;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongsInGenresAdapter extends RecyclerView.Adapter<SongsInGenresAdapter.MyHolder> {
    private List<Song> songsInGenresArrayList;
    private Context context;
    private Genres genres;

    public SongsInGenresAdapter(List<Song> songsInGenresArrayList, Context context) {
        this.songsInGenresArrayList = songsInGenresArrayList;
        this.context = context;
    }


    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_two_lines_and_image, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Song tempSong = songsInGenresArrayList.get(position);
        holder.songTitleTextView.setText(tempSong.title);
        holder.songArtistTextView.setText(tempSong.artistName);
        GlideApp.with(context)
                .load(new AudioCoverImage(tempSong.data))
                .placeholder(context.getDrawable(R.drawable.default_artwork_small))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.albumArtImageView);
    }

    public long[] getSongIds() {
        if (songsInGenresArrayList != null) {
            long[] ret = new long[getItemCount()];
            for (int i = 0; i < songsInGenresArrayList.size(); i++) {
                ret[i] = songsInGenresArrayList.get(i).id;
            }
            return ret;
        }
        return null;
    }

    public List<Song> getData() {
        return songsInGenresArrayList;
    }

    @Override
    public int getItemCount() {
        return songsInGenresArrayList.size();
    }

    public void updateData(List<Song> data) {
        this.songsInGenresArrayList = data;
        notifyDataSetChanged();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView songTitleTextView;
        @BindView(R.id.line_two_text)
        TextView songArtistTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView albumArtImageView;


        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            albumArtImageView.setClipToOutline(true);
            setUpPopUpMenu();

        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                android.widget.PopupMenu popUpmenu = new android.widget.PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    long[] currentSongListWrapper = {songsInGenresArrayList.get(position).id};
                    Song currentSong = songsInGenresArrayList.get(position);
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(songsInGenresArrayList, position, false);
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
                        case R.id.menu_song_go_to_artist:
                            NavigationUtil.moveToArtist(context, ArtistLoader.getArtist(currentSong.artistId, context));
                            break;
                        case R.id.menu_song_details:
                            SongDetailsDialog.newInstance(currentSong)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            SongDetailsDialog.SONG_DETAILS_DIALOG);
                            break;
                    }
                    return true;
                });
                popUpmenu.inflate(R.menu.menu_song_list_popup);
                popUpmenu.show();
            });
        }

        @Override
        public void onClick(View v) {
            MusicPlayer.playAll(songsInGenresArrayList, getAdapterPosition(), false);
        }
    }
}