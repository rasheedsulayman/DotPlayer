package com.r4sh33d.musicslam.fragments.songlist;

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

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dataloaders.AlbumLoader;
import com.r4sh33d.musicslam.dataloaders.ArtistLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.dialogs.SongDetailsDialog;
import com.r4sh33d.musicslam.interfaces.FastScrollerAdapter;
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
public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.Holder>
        implements FastScrollerAdapter {
    private Context context;
    private List<Song> songArrayList;

    public SongsListAdapter(Context context, List<Song> songArrayList) {
        this.context = context;
        this.songArrayList = songArrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_two_lines_and_image,
                parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Song tempSong = songArrayList.get(position);
        holder.songTitleTextView.setText(tempSong.title);
        holder.songArtistTextView.setText(tempSong.artistName);
        GlideApp.with(context)
                .load(new AudioCoverImage(tempSong.data))
                .placeholder(context.getDrawable(R.drawable.default_artwork_small))
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .signature(SlamUtils.getMediaStoreSignature(tempSong))
                .into(holder.albumArtImageView);
    }

    public void updateData(List<Song> data) {
        songArrayList = data;
        notifyDataSetChanged();
    }

    @Override
    public String getFastScrollerThumbCharacter(int position) {
        String sectionName = songArrayList.get(position).title;
        return TextUtils.isEmpty(sectionName) ? "" : sectionName.substring(0, 1).toUpperCase();
    }

    public List<Song> getData() {
        return songArrayList;
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView songTitleTextView;
        @BindView(R.id.line_two_text)
        TextView songArtistTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView albumArtImageView;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            albumArtImageView.setClipToOutline(true);
            setUpPopUpMenu();
        }

        @Override
        public void onClick(View v) {
            MusicPlayer.playAll(songArrayList, getAdapterPosition(), false);
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                PopupMenu popUpmenu = new PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = Holder.this.getAdapterPosition();
                    long[] currentSongListWrapper = {songArrayList.get(position).id};
                    Song currentSong = songArrayList.get(position);
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(songArrayList, position, false);
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
    }
}
