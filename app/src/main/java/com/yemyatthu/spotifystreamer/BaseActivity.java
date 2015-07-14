package com.yemyatthu.spotifystreamer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yemyatthu on 6/10/15.
 */
public class BaseActivity extends AppCompatActivity {
  private SharedPreferences sharedPreferences = null;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
}
