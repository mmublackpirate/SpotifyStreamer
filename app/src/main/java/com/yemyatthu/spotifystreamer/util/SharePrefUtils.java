package com.yemyatthu.spotifystreamer.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yemyatthu on 6/23/15.
 */
public class SharePrefUtils {

  private static final String CURRENT_TRACK_IMAGE = "current_track_image";
  private static SharePrefUtils pref;
  protected SharedPreferences mSharePreferences;
  protected SharedPreferences.Editor mEditor;

  public SharePrefUtils(Context context) {
    mSharePreferences = context.getSharedPreferences("CPref", 0);
    mEditor = mSharePreferences.edit();
  }

  public static SharePrefUtils getInstance(Context context) {
    if (pref == null) {
      pref = new SharePrefUtils(context);
    }
    return pref;
  }

  public String getCurrentTrackImage() {
    return mSharePreferences.getString(CURRENT_TRACK_IMAGE, null);
  }

  public void saveCurrentTrackImage(String base64) {
    mEditor.putString(CURRENT_TRACK_IMAGE, base64).apply();
  }
}
