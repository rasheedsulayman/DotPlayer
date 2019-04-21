package com.r4sh33d.musicslam.fragments.playlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dataloaders.SongIdsLoader;
import com.r4sh33d.musicslam.dataloaders.SongLoader;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeletePlaylistDialog;
import com.r4sh33d.musicslam.dialogs.RenamePlaylistDialog;
import com.r4sh33d.musicslam.interfaces.FastScrollerAdapter;
import com.r4sh33d.musicslam.models.Playlist;
import com.r4sh33d.musicslam.models.Song;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.utils.NavigationUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyHolder> implements FastScrollerAdapter {

    private Context context;
    private List<Playlist> playListArrayList;

    public PlaylistAdapter(Context context, List<Playlist> playListArrayList) {
        this.context = context;
        this.playListArrayList = playListArrayList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_two_line_no_image_big_margin,
                parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Playlist playList = playListArrayList.get(position);
        holder.playListNameTextView.setText(playList.name);
        if (playList.id > 0) {
            holder.songCountTextView.setVisibility(View.VISIBLE);
            holder.songCountTextView.setText(context.getResources().getQuantityString(R.plurals.n_songs, playList.songCount, playList.songCount));
        } else {
            holder.songCountTextView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return playListArrayList.size();
    }

    public void updateData(List<Playlist> data) {
        this.playListArrayList = data;
        notifyDataSetChanged();
    }

    @Override
    public String getFastScrollerThumbCharacter(int position) {
        Playlist playlist = playListArrayList.get(position);
        return TextUtils.isEmpty(playlist.name) || playlist.id < 0 ? "" :
                playlist.name.substring(0, 1).toUpperCase();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.line_one_text)
        TextView playListNameTextView;
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

        private List<Song> getSongsForPlaylist() {
            return SongLoader.getSongsInPlaylist(playListArrayList.get(getAdapterPosition()).id, context);
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                PopupMenu popUpmenu = new PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    Playlist playlist = playListArrayList.get(getAdapterPosition());
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playAll(getSongsForPlaylist(), 0, false);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(getSongsForPlaylist(), context);
                            break;
                        case R.id.menu_song_delete:
                            DeletePlaylistDialog.newInstance(playlist)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            DeletePlaylistDialog.DELETE_PLAYLIST_FRAG_TAG);
                            break;

                        case R.id.menu_song_add_to_playlist:
                            AddToPlaylistDialog.newInstance(SongIdsLoader.getSongIdsListForPlaylist(context, playlist))
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                            break;
                        case R.id.menu_song_add_to_queue:
                            MusicPlayer.addToQueue(context, getSongsForPlaylist());
                            break;

                        case R.id.menu_rename:
                            RenamePlaylistDialog.newInstance(playlist)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            RenamePlaylistDialog.RENAME_PLAYLIST_FRAG_TAG);
                            break;
                    }
                    return true;
                });
                popUpmenu.inflate(R.menu.menu_popup_pager_fragments_items);
                Menu menu = popUpmenu.getMenu();
                //enable rename,
                menu.findItem(R.id.menu_rename)
                        .setVisible(true).setEnabled(true);
                if (playListArrayList.get(getAdapterPosition()).id < 0) {
                    //It's a smart playlist, we can not delete
                    menu.findItem(R.id.menu_song_delete)
                            .setVisible(false).setEnabled(false);
                    //Also we can't rename, so disable rename menu
                    menu.findItem(R.id.menu_rename)
                            .setVisible(false).setEnabled(false);

                }
                popUpmenu.show();
            });
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.moveToPlaylist(playListArrayList.get(getAdapterPosition()), context);
        }
    }
}
