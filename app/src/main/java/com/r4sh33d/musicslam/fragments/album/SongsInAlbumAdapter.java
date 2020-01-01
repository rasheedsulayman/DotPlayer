package com.r4sh33d.musicslam.fragments.album;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dataloaders.ArtistLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.dialogs.SongDetailsDialog;
import com.r4sh33d.musicslam.models.Album;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.SlamUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class SongsInAlbumAdapter extends RecyclerView.Adapter<SongsInAlbumAdapter.MyHolder> {
    private List<Song> songsInAlbumArrayList;
    private Context context;
    private Album album;

    public SongsInAlbumAdapter(List<Song> songArrayList, Context context, Album album) {
        this.songsInAlbumArrayList = songArrayList;
        this.context = context;
        this.album = album;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_two_lines_duration_two, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Song tempSong = songsInAlbumArrayList.get(position);
        holder.songTitleTextView.setText(tempSong.title);
        holder.songArtistTextView.setText(tempSong.artistName);
        holder.songDuration.setText(MusicUtils.makeShortTimeString(context, tempSong.duration / 1000));
        holder.trackNoTextView.setText(tempSong.getTrackNumberString());
    }


    public List<Song> getData() {
        return songsInAlbumArrayList;
    }


    @Override
    public int getItemCount() {
        return songsInAlbumArrayList.size();
    }

    public void updateData(List<Song> data) {
        songsInAlbumArrayList = data;
        notifyDataSetChanged();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView songTitleTextView;
        @BindView(R.id.line_two_text)
        TextView songArtistTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.track_no_textview)
        TextView trackNoTextView;
        @BindView(R.id.duration)
        TextView songDuration;

        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
        }

        @Override
        public void onClick(View v) {
            MusicPlayer.playAll(songsInAlbumArrayList, getAdapterPosition(), false);
        }


        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                android.widget.PopupMenu popUpmenu = new android.widget.PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    long[] currentSongListWrapper = {songsInAlbumArrayList.get(position).id};
                    Song currentSong = songsInAlbumArrayList.get(position);
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(songsInAlbumArrayList, position, false);
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
                popUpmenu.getMenu().findItem(R.id.menu_song_go_to_album)
                        .setVisible(false).setEnabled(false);
                popUpmenu.show();
            });
        }
    }
}
