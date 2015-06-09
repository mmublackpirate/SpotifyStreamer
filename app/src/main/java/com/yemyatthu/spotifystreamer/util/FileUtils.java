package com.yemyatthu.spotifystreamer.util;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by yemyatthu on 6/9/15.
 */
public class FileUtils {
  public static void saveJsonString(Context context,String fileName,String string){
    OutputStream outputStream = null;
    try {
      outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
      outputStream.write(string.getBytes());
      outputStream.close();
    } catch (FileNotFoundException exception) {
      Log.d("FileNotFound", "File not found Error");
    } catch (IOException ioException) {
      Log.d("IOError", "IO Error");
    }
  }

  public static String  convertListToJsonString(List<Track> tracks){
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    return gson.toJson(tracks);
  }

  public static List<Track> convertJsonStringToList(String builderString) {
    if(builderString!=null) {
      GsonBuilder gsonBuilder = new GsonBuilder();
      Gson gson = gsonBuilder.create();
      Type type = new TypeToken<List<Track>>() {
      }.getType();
      return gson.fromJson(builderString, type);
    }else{
      return null;
    }
  }

  public static String loadJsonString(Context context,String fileName){
    StringBuilder builder = null;
    InputStream inputStream = null;
    try {
      inputStream = context.openFileInput(fileName);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      builder = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
      reader.close();
    } catch (FileNotFoundException fileNotFoundExcepiton) {
      Log.d("FileNotFound", "No File Found");
    } catch (IOException ioException) {
      Log.d("IO Exception", "IO Exception");
    }
   return builder!=null?builder.toString():null;
  }
}
