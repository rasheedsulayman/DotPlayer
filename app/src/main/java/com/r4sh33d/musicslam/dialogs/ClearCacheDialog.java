package com.r4sh33d.musicslam.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.r4sh33d.musicslam.GlideApp;
import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.network.LastFmRetrofitClient;

import java.lang.ref.WeakReference;
/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public class ClearCacheDialog extends DialogFragment {
    public static final String CLEAR_CACHE_FRAG_TAG = "delete_playlist_dialog_tag";

    public static ClearCacheDialog newInstance() {
        Bundle args = new Bundle();
        ClearCacheDialog fragment = new ClearCacheDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new MaterialDialog.Builder(getContext())
                .title(R.string.clear_cache_question)
                .content(R.string.delete_cache_new_content_will_be_created)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    GlideApp.get(getContext()).clearMemory();
                    new ClearCacheTask(getContext()).execute();
                })
                .onNegative((dialog, which) -> {
                    //Nothing
                })
                .build();
    }

    public static class ClearCacheTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> contextWeakReference;

        public ClearCacheTask(Context context) {
            contextWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = contextWeakReference.get();
            if (context != null) {
                LastFmRetrofitClient.clearCache(context);
                Glide.get(context.getApplicationContext()).clearDiskCache();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Context context = contextWeakReference.get();
            if (context != null) {
                Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
