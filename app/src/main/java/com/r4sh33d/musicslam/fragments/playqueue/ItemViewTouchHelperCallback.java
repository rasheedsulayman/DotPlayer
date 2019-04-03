package com.r4sh33d.musicslam.fragments.playqueue;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;


public class ItemViewTouchHelperCallback extends ItemTouchHelper.Callback {

    private int dragFrom = -1;
    private int dragTo = -1;

    private OnItemMovedListener listener;

    public ItemViewTouchHelperCallback(OnItemMovedListener listener) {
        this.listener = listener;
    }

    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();

        if (dragFrom == -1) {
            dragFrom = fromPosition;
        }
        dragTo = toPosition;
        listener.onItemMoved(fromPosition, dragTo);
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
            listener.onItemDropped(dragFrom, dragTo);
        }
        dragFrom = dragTo = -1;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    public interface OnItemMovedListener {
        void onItemDropped(int from, int to);

        void onItemMoved(int from, int to);
    }
}
