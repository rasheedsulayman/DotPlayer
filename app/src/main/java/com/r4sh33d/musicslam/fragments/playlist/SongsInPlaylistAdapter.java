package com.r4sh33d.musicslam.fragments.playlist;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
public class SongsInPlaylistAdapter extends RecyclerView.Adapter<SongsInPlaylistAdapter.MyHolder> {
    private List<Song> songsInPlayListArrayList;
    private Context context;

    public SongsInPlaylistAdapter(List<Song> songsInPlayListArrayList, Context context) {
        this.songsInPlayListArrayList = songsInPlayListArrayList;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_two_lines_and_image, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Song tempSong = songsInPlayListArrayList.get(position);
        holder.songTitleTextView.setText(tempSong.title);
        holder.songArtistTextView.setText(tempSong.artistName);
        GlideApp.with(context)
                .load(new AudioCoverImage(tempSong.data))
                .placeholder(context.getDrawable(R.drawable.default_artwork_small))
                .transition(DrawableTransitionOptions.withCrossFade())
                .signature(SlamUtils.getMediaStoreSignature(tempSong))
                .into(holder.albumArtImageView);
    }

    @Override
    public int getItemCount() {
        return songsInPlayListArrayList.size();
    }

    public void updateData(List<Song> data) {
        this.songsInPlayListArrayList = data;
        notifyDataSetChanged();
    }

    public List<Song> getData() {
        return songsInPlayListArrayList;
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
                PopupMenu popUpmenu = new PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    long[] currentSongListWrapper = {songsInPlayListArrayList.get(position).id};
                    Song currentSong = songsInPlayListArrayList.get(position);
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(songsInPlayListArrayList, position, false);
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
            MusicPlayer.playAll(songsInPlayListArrayList, getAdapterPosition(), false);
        }

    }
}
