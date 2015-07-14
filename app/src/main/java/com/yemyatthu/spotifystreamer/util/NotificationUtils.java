package com.yemyatthu.spotifystreamer.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import com.yemyatthu.spotifystreamer.Config;
import com.yemyatthu.spotifystreamer.R;
import com.yemyatthu.spotifystreamer.SearchActivity;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by yemyatthu on 6/25/15.
 */
public class NotificationUtils {
  public static Notification buildNotiForAudiService(RemoteViews remoteView, Track track,
      Context context) {
    remoteView.setTextViewText(R.id.noti_track_title, track.name);
    String artistNames = "";
    for (ArtistSimple artist : track.artists) {
      artistNames += artist.name + " ";
    }
    remoteView.setTextViewText(R.id.noti_aritst_name, artistNames);
    Bitmap notiCoverBmp = GeneralUtils.convertBase64ToBitmap(
        SharePrefUtils.getInstance(context).getCurrentTrackImage());
    remoteView.setImageViewBitmap(R.id.noti_cover_image, notiCoverBmp);
    Intent notIntent = new Intent(context, SearchActivity.class); //for now, just open the app
    notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent playPauseIntent = new Intent(Config.NOTI_PRESS_PLAY_FILTER);
    PendingIntent pendingPlayPauseIntent =
        PendingIntent.getBroadcast(context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent nextIntent = new Intent(Config.NEXT_FILTER);
    PendingIntent pendingNextIntent =
        PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent prevIntent = new Intent(Config.PREV_FILTER);
    PendingIntent pendingPrevIntent =
        PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent quitIntent = new Intent(Config.QUIT_FILTER);
    PendingIntent pendingQuitIntent =
        PendingIntent.getBroadcast(context, 0, quitIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    remoteView.setOnClickPendingIntent(R.id.play_pause_btn_noti, pendingPlayPauseIntent);
    remoteView.setOnClickPendingIntent(R.id.next_btn_noti, pendingNextIntent);
    remoteView.setOnClickPendingIntent(R.id.prev_btn_noti, pendingPrevIntent);
    remoteView.setOnClickPendingIntent(R.id.close_btn_noti, pendingQuitIntent);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
    builder.setContentIntent(pendingIntent)
        .setOngoing(true)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContent(remoteView);
    return builder.build();
  }
}
