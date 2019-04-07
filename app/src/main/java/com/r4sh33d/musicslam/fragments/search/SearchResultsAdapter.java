package com.r4sh33d.musicslam.fragments.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
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
import com.r4sh33d.musicslam.customglide.artist.ArtistImage;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dataloaders.AlbumLoader;
import com.r4sh33d.musicslam.dataloaders.ArtistLoader;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.models.Album;
import com.r4sh33d.musicslam.models.Artist;
import com.r4sh33d.musicslam.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LABEL = 1;
    private static final int TYPE_SONG = 2;
    private static final int TYPE_ARTIST = 3;
    private static final int TYPE_ALBUM = 4;
    private Context context;
    private List<Object> resultList;

    public SearchResultsAdapter(Context context, List<Object> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    public void updateResultsLists(List<Object> resultList) {
        this.resultList = resultList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v;
        switch (viewType) {
            case TYPE_ARTIST:
                v = inflater.inflate(R.layout.item_list_two_lines_and_image, parent, false);
                return new SearchResultsAdapter.ArtistViewHolder(v);
            case TYPE_ALBUM:
                v = inflater.inflate(R.layout.item_list_two_lines_and_image, parent, false);
                return new SearchResultsAdapter.AlbumViewHolder(v);
            case TYPE_SONG:
                v = inflater.inflate(R.layout.item_list_two_lines_and_image, parent, false);
                return new SearchResultsAdapter.SongViewHolder(v);
        }
        v = inflater.inflate(R.layout.item_search_label, parent, false);
        return new SearchResultsAdapter.LabelViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object localObject = resultList.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_LABEL:
                ((LabelViewHolder) holder).bindViews(localObject);
                break;
            case TYPE_ARTIST:
                ((ArtistViewHolder) holder).bindViews(localObject);
                break;
            case TYPE_ALBUM:
                ((AlbumViewHolder) holder).bindViews(localObject);
                break;
            case TYPE_SONG:
                ((SongViewHolder) holder).bindViews(localObject);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object tempObject = resultList.get(position);
        if (tempObject instanceof Song) {
            return TYPE_SONG;
        }
        if (tempObject instanceof Artist) {
            return TYPE_ARTIST;
        }
        if (tempObject instanceof Album) {
            return TYPE_ALBUM;
        }
        return TYPE_LABEL;
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    //---->Label Holder
    class LabelViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.label_text_view)
        TextView labelTextView;

        public LabelViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindViews(Object localObject) {
            String label = (String) localObject;
            labelTextView.setText(label);
        }

    }

    //---->Artist Holder
    class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView artistNameTextView;
        @BindView(R.id.line_two_text)
        TextView albumsAndSongsTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView artistArtImageView;

        @SuppressLint("NewApi")
        public ArtistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
        }

        public void bindViews(Object localObject) {
            Artist artist = (Artist) localObject;
            artistNameTextView.setText(artist.name);
            String albums = String.format("%d %s", artist.albumCount, artist.albumCount > 1 ? "Albums" : "Album");
            String songs = String.format("%d %s", artist.songCount, artist.songCount > 1 ? "Songs" : "Song");
            albumsAndSongsTextView.setText(String.format("%s • %s", albums, songs));
            GlideApp.with(context)
                    .load(new ArtistImage(artist.name))
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .placeholder(context.getDrawable(R.drawable.default_artwork))
                    .into(artistArtImageView);
        }

        private List<Song> getSongsForArtist() {
            return SongLoader.getSongsForArtist(((Artist) resultList.get(getAdapterPosition())).id, context);
        }

        private long[] getSongIdsForArtist() {
            return SongIdsLoader.getSongIdsListForArtist(context, ((Artist) resultList.get(getAdapterPosition())).id);
        }


        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                android.widget.PopupMenu popUpmenu = new android.widget.PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    Artist artist = (Artist) resultList.get(getAdapterPosition());
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(getSongsForArtist(), 0, false);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(getSongsForArtist(), context);
                            break;
                        case R.id.menu_song_delete:
                            DeleteSongsDialog.newInstance(getSongIdsForArtist(), artist.name)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(), DeleteSongsDialog.DELETE_FRAG_TAG);
                            break;

                        case R.id.menu_song_add_to_playlist:
                            AddToPlaylistDialog.newInstance(getSongIdsForArtist())
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                            break;
                        case R.id.menu_song_add_to_queue:
                            MusicPlayer.addToQueue(context, getSongsForArtist());
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
            NavigationUtil.moveToArtist(context, (Artist) resultList.get(getAdapterPosition()));
        }
    }

    //----> Song Holder
    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView songTitleTextView;
        @BindView(R.id.line_two_text)
        TextView songArtistTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView albumArtImageView;

        public SongViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
        }

        public void bindViews(Object localObject) {
            Song song = (Song) localObject;
            songTitleTextView.setText(song.title);
            songArtistTextView.setText(song.artistName);
            GlideApp.with(context)
                    .load(new AudioCoverImage(song.data))
                    .placeholder(context.getDrawable(R.drawable.default_artwork))
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(albumArtImageView);
        }

        @Override
        public void onClick(View v) {
            Song song = (Song) resultList.get(getAdapterPosition());
            MusicPlayer.playSong(song);
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                android.widget.PopupMenu popUpmenu = new android.widget.PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    Song song = (Song) resultList.get(position);
                    long[] currentSongListWrapper = {song.id};
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playSong(song);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(song, context);
                            break;
                        case R.id.menu_song_delete:
                            DeleteSongsDialog.newInstance(currentSongListWrapper, song.title)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(), DeleteSongsDialog.DELETE_FRAG_TAG);
                            break;
                        case R.id.menu_song_share:
                            SlamUtils.shareSong(song, context);
                            break;
                        case R.id.menu_song_add_to_playlist:
                            AddToPlaylistDialog.newInstance(currentSongListWrapper)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                            break;
                        case R.id.menu_song_add_to_queue:
                            MusicPlayer.addToQueue(context, song);
                            break;
                        case R.id.menu_song_ringtone:
                            MusicUtils.setRingtone(context, song.id);
                            break;
                        case R.id.menu_song_go_to_album:
                            NavigationUtil.moveToAlbum(context, AlbumLoader.getAlbum(song.albumId, context));
                            break;
                        case R.id.menu_song_go_to_artist:
                            NavigationUtil.moveToArtist(context, ArtistLoader.getArtist(song.artistId, context));
                            break;
                        case R.id.menu_song_details:
                            SongDetailsDialog.newInstance(song)
                                    .show(((AppCompatActivity)context).getSupportFragmentManager(),
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

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView albumNameTextView;
        @BindView(R.id.line_two_text)
        TextView artistNameTextView;
        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView albumArtImageView;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
        }

        public void bindViews(Object localObject) {
            Album album = (Album) localObject;
            albumNameTextView.setText(album.title);
            artistNameTextView.setText(album.artistName);
            GlideApp.with(context)
                    .load(new AudioCoverImage(album.firstSongPath))
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .placeholder(context.getDrawable(R.drawable.default_artwork))
                    .into(albumArtImageView);
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                android.widget.PopupMenu popUpmenu = new android.widget.PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    Album album = (Album) resultList.get(getAdapterPosition());
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(getSongsForAlbum(), 0, false);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(getSongsForAlbum(), context);
                            break;
                        case R.id.menu_song_delete:
                            DeleteSongsDialog.newInstance(getSongIdsForAlbum(), album.title)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(), DeleteSongsDialog.DELETE_FRAG_TAG);
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
            return SongIdsLoader.getSongIdsListForAlbum(context, ((Album) resultList.get(getAdapterPosition())).id);
        }

        private ArrayList<Song> getSongsForAlbum() {
            return SongLoader.getSongsInAlbum(((Album) resultList.get(getAdapterPosition())).id, context);
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.moveToAlbum(context, (Album) resultList.get(getAdapterPosition()));
        }
    }
}
