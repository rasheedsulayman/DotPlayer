package com.r4sh33d.musicslam.fragments.artist;

import android.annotation.SuppressLint;
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
import com.r4sh33d.musicslam.customglide.artist.ArtistImage;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.interfaces.FastScrollerAdapter;
import com.r4sh33d.musicslam.models.Artist;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.NavigationUtil;
import com.r4sh33d.musicslam.utils.PrefsUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistGridAdapter extends RecyclerView.Adapter<ArtistGridAdapter.MyViewHolder>
        implements FastScrollerAdapter {

    private Context context;
    private List<Artist> artistArrayList;
    private boolean isAlbumArtTheme;

    public ArtistGridAdapter(Context context, List<Artist> artistArrayList) {
        this.context = context;
        this.artistArrayList = artistArrayList;
        isAlbumArtTheme = PrefsUtils.getInstance(context).isAlbumArtTheme();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).
                inflate(isAlbumArtTheme ? R.layout.item_album_grid_one : R.layout.item_album_grid_one_card,
                        parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Artist artist = artistArrayList.get(position);
        holder.artistNameTextView.setText(artist.name);
        holder.albumCountTextView.setText(context.getResources().getQuantityString(R.plurals.n_albums, artist.albumCount, artist.albumCount));
        holder.songCounttextView.setText(String.format(context.getString(R.string.number_in_parentheses_format), artist.songCount));
        GlideApp.with(context)
                .load(new ArtistImage(artist.name))
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .placeholder(context.getDrawable(R.drawable.default_artwork_large))
                .into(holder.albumArtImageView);
    }

    @Override
    public int getItemCount() {
        return artistArrayList.size();
    }


    public void updateData(List<Artist> data) {
        this.artistArrayList = data;
        notifyDataSetChanged();
    }

    @Override
    public String getFastScrollerThumbCharacter(int position) {
        String sectionName = artistArrayList.get(position).name;
        return TextUtils.isEmpty(sectionName) ? "" : sectionName.substring(0, 1).toUpperCase();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView artistNameTextView;
        @BindView(R.id.line_two_text_one)
        TextView albumCountTextView;
        @BindView(R.id.line_two_text_two)
        TextView songCounttextView;

        @BindView(R.id.overflow_menu)
        ImageView popupMenuImageView;
        @BindView(R.id.album_art)
        ImageView albumArtImageView;

        @SuppressLint("NewApi")
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
                    Artist artist = artistArrayList.get(getAdapterPosition());
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

        private long[] getSongIdsForArtist() {
            return SongIdsLoader.getSongIdsListForArtist(context, artistArrayList.get(getAdapterPosition()).id);
        }

        private List<Song> getSongsForArtist() {
            return SongLoader.getSongsForArtist(artistArrayList.get(getAdapterPosition()).id, context);
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.moveToArtist(context, artistArrayList.get(getAdapterPosition()));
        }
    }
}
