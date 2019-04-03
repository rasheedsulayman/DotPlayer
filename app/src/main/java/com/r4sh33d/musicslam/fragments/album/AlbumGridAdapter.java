package com.r4sh33d.musicslam.fragments.album;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
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
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.PrefsUtils;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.interfaces.FastScrollerAdapter;
import com.r4sh33d.musicslam.models.Album;
import com.r4sh33d.musicslam.models.Song;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumGridAdapter extends RecyclerView.Adapter<AlbumGridAdapter.MyViewHolder>
        implements FastScrollerAdapter {

    Context context;
    private List<Album> albumArrayList;
    private boolean isAlbumArtTheme;

    public AlbumGridAdapter(Context context, List<Album> albumArrayList) {
        this.context = context;
        this.albumArrayList = albumArrayList;
        isAlbumArtTheme = PrefsUtils.getInstance(context).isAlbumArtTheme();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(isAlbumArtTheme ? R.layout.item_album_grid_one :
                R.layout.item_album_grid_one_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Album album = albumArrayList.get(position);
        holder.albumNameTextView.setText(album.title);
        holder.artistNameTextView.setText(album.artistName);
        holder.songCountTextView.setText(String.format("(%d)", album.songCount));

        GlideApp.with(context)
                .load(new AudioCoverImage(album.firstSongPath))
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .placeholder(R.drawable.album_holdertest)
                .into(holder.albumArtImageView);
    }

    @Override
    public int getItemCount() {
        return albumArrayList.size();
    }

    public void updateData(List<Album> data) {
        albumArrayList = data;
        notifyDataSetChanged();
    }

    @Override
    public String getFastScrollerThumbCharacter(int position) {
        String sectionName = albumArrayList.get(position).title;
        return TextUtils.isEmpty(sectionName) ? "" : sectionName.substring(0, 1).toUpperCase();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView albumNameTextView;
        @BindView(R.id.line_two_text_one)
        TextView artistNameTextView;
        @BindView(R.id.line_two_text_two)
        TextView songCountTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView albumArtImageView;

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            albumArtImageView.setClipToOutline(true);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                PopupMenu popUpmenu = new PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    Album album = albumArrayList.get(getAdapterPosition());
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(getSongsForAlbum(), 0, false);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(getSongsForAlbum(), context);
                            break;
                        case R.id.menu_song_delete:
                            DeleteSongsDialog.newInstance(getSongIdsForAlbum(), album.title)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            DeleteSongsDialog.DELETE_FRAG_TAG);
                            break;

                        case R.id.menu_song_add_to_playlist:
                            AddToPlaylistDialog.newInstance(getSongIdsForAlbum())
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                            break;
                        case R.id.menu_song_add_to_queue:
                            MusicPlayer.addToQueue(context, getSongsForAlbum());
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

        private long[] getSongIdsForAlbum() {
            return SongIdsLoader.getSongIdsListForAlbum(context, albumArrayList.get(getAdapterPosition()).id);
        }

        private List<Song> getSongsForAlbum() {
            return SongLoader.getSongsInAlbum(albumArrayList.get(getAdapterPosition()).id, context);
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.moveToAlbum(context, albumArrayList.get(getAdapterPosition()));
        }
    }
}
