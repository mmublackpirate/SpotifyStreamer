package com.yemyatthu.spotifystreamer.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * Created by yemyatthu on 6/8/15.
 */
public class GeneralUtils {
  public static String getCountry() {
    String country = Locale.getDefault().getCountry();
    if (country.equals("")) {
      country = "US";
    }
    return country;
  }

  public static String convertBitmapToBase64(Bitmap bitmap) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
    byte[] byteArray = byteArrayOutputStream.toByteArray();
    return Base64.encodeToString(byteArray, Base64.DEFAULT);
  }

  public static Bitmap convertBase64ToBitmap(String base64) {
    byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
  }
}
