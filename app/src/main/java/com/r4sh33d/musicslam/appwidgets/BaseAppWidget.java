package com.r4sh33d.musicslam.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.r4sh33d.musicslam.playback.Constants;
import com.r4sh33d.musicslam.playback.MusicPlayer;
import com.r4sh33d.musicslam.playback.MusicService;

import timber.log.Timber;

import static com.r4sh33d.musicslam.playback.Constants.ACTION_UPDATE_APP_WIDGETS;
import static com.r4sh33d.musicslam.playback.Constants.EXTRA_WIDGET_TYPE;
import static com.r4sh33d.musicslam.playback.Constants.PLAY_STATE_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.REPEAT_MODE_CHANGED;
import static com.r4sh33d.musicslam.playback.Constants.SHUFFLE_MODE_CHANGED;

public abstract class BaseAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        //if our widget is attached, or ever need to update.
        initializeAppWidgetsToDefaultState(context, appWidgetIds);
        final Intent updateIntent = new Intent(ACTION_UPDATE_APP_WIDGETS);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.putExtra(EXTRA_WIDGET_TYPE, getType());
        updateIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
        if (MusicPlayer.mService == null && hasInstances(context)) {
            //We probably just finished the boot process, so start service to update
            context.startService(new Intent(context, MusicService.class));
        }
    }

    public void pushUpdate(final Context context, final int[] appWidgetIds, final RemoteViews views) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), views);
        }
    }

    public void initializeAppWidgetsToDefaultState(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetViews = new RemoteViews(context.getPackageName(), getWidgetLayoutRes());
        linkButtons(context, appWidgetViews);
        pushUpdate(context, appWidgetIds, appWidgetViews);
    }

    private boolean hasInstances(final Context context) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] mAppWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                getClass()));
        return mAppWidgetIds.length > 0;
    }

    public void notifyChange(final MusicService service, final String what) {
        if (hasInstances(service)) {
            if (Constants.META_CHANGED.equals(what)
                    || PLAY_STATE_CHANGED.equals(what)
                    || REPEAT_MODE_CHANGED.equals(what)
                    || SHUFFLE_MODE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    protected PendingIntent buildPendingIntent(Context context, final String action,
                                               final ComponentName serviceName) {
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(context, 0, intent, 0);
    }


    public abstract void linkButtons(final Context context, final RemoteViews views);

    public abstract int getWidgetLayoutRes();

    public abstract void performUpdate(final MusicService service, final int[] appWidgetIds);

    public abstract String getType();
}
