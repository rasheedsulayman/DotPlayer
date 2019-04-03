package com.r4sh33d.musicslam.fragments.playqueue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.dialogs.SongDetailsDialog;
import com.r4sh33d.musicslam.utils.MusicUtils;
import com.r4sh33d.musicslam.utils.SlamUtils;
import com.r4sh33d.musicslam.customviews.BlackAndWhiteImageView;
import com.r4sh33d.musicslam.customglide.audiocover.AudioCoverImage;
import com.r4sh33d.musicslam.dialogs.AddToPlaylistDialog;
import com.r4sh33d.musicslam.dialogs.DeleteSongsDialog;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.models.Song;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongsInPlayQueueAdapter extends RecyclerView.Adapter<SongsInPlayQueueAdapter.MyHolder>
        implements  ItemViewTouchHelperCallback.OnItemMovedListener {
    private List<Song> songsInQueueArrayList;
    private Context context;
    private OnStartDragListener onStartDragListener;

    public SongsInPlayQueueAdapter(List<Song> songsInQueueArrayList, Context context,
                                   OnStartDragListener onStartDragListener) {
        this.songsInQueueArrayList = songsInQueueArrayList;
        this.context = context;
        this.onStartDragListener = onStartDragListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_draggable, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Song localSong = songsInQueueArrayList.get(position);
        holder.songArtistTextView.setText(localSong.artistName);
        holder.songTitleTextView.setText(localSong.title);
        GlideApp.with(context)
                .load(new AudioCoverImage(localSong.data))
                .placeholder(R.drawable.album_holdertest)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.albumArtImageView);
    }


    public void updateData(List<Song> data) {
        this.songsInQueueArrayList = data;
        notifyDataSetChanged();
    }

    public List<Song> getData() {
        return songsInQueueArrayList;
    }

    @Override
    public int getItemCount() {
        return songsInQueueArrayList.size();
    }

    @Override
    public void onItemDropped(int from, int to) {
         MusicPlayer.moveQueueItem(from, to);
    }

    @Override
    public void onItemMoved(int from, int to) {
        notifyItemMoved(from, to);
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
        @BindView(R.id.drag_handle)
        BlackAndWhiteImageView dragHandleImageView;

        @SuppressLint("ClickableViewAccessibility")
        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            setUpPopUpMenu();
            dragHandleImageView.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    onStartDragListener.onStartDrag(this);
                }
                return false;
            });
        }

        private void setUpPopUpMenu() {
            popupMenuImageView.setOnClickListener(v -> {
                android.widget.PopupMenu popUpmenu = new android.widget.PopupMenu(context, v);
                popUpmenu.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    long[] currentSongListWrapper = {songsInQueueArrayList.get(position).id};
                    Song currentSong = songsInQueueArrayList.get(position);
                    switch (item.getItemId()) {
                        case R.id.menu_song_play:
                            MusicPlayer.playSongAt(position);
                            break;
                        case R.id.menu_song_play_next:
                            MusicPlayer.playNext(currentSong, context);
                            break;
                        case R.id.menu_song_delete:
                            DeleteSongsDialog.newInstance(currentSongListWrapper, currentSong.title)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(), DeleteSongsDialog.DELETE_FRAG_TAG);
                            break;
                        case R.id.menu_song_dequeue:
                            MusicPlayer.removeTrackAtPosition(currentSong.id, position);
                            break;
                        case R.id.menu_song_share:
                            SlamUtils.shareSong(currentSong, context);
                            break;
                        case R.id.menu_song_add_to_playlist:
                            AddToPlaylistDialog.newInstance(currentSongListWrapper)
                                    .show(((AppCompatActivity) context).getSupportFragmentManager(),
                                            AddToPlaylistDialog.ADD_TO_PLAYLIST_ARG);
                            break;
                        case R.id.menu_song_ringtone:
                            MusicUtils.setRingtone(context, currentSong.id);
                            break;
                        case R.id.menu_song_details:
                            SongDetailsDialog.newInstance(currentSong)
                                    .show(((AppCompatActivity)context).getSupportFragmentManager(),
                                            SongDetailsDialog.SONG_DETAILS_DIALOG);
                            break;
                    }
                    return true;
                });
                popUpmenu.inflate(R.menu.menu_play_queue_songs_popup);
                popUpmenu.show();
            });
        }

        @Override
        public void onClick(View v) {
            MusicPlayer.playSongAt(getAdapterPosition());
        }
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
