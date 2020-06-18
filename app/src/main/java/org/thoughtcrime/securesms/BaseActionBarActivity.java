package org.thoughtcrime.securesms;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.configuration.Constants;

import org.thoughtcrime.securesms.color.MaterialColor;
import org.thoughtcrime.securesms.config.Config;
import org.thoughtcrime.securesms.logging.Log;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.dynamiclanguage.DynamicLanguageActivityHelper;
import org.thoughtcrime.securesms.util.dynamiclanguage.DynamicLanguageContextWrapper;

import java.lang.reflect.Field;


public abstract class BaseActionBarActivity extends AppCompatActivity {
  private static final String TAG = BaseActionBarActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (BaseActivity.isMenuWorkaroundRequired()) {
      forceOverflowMenu();
    }
    super.onCreate(savedInstanceState);
    if(!isFinishing()) {
      initializeAppsgeyser();
    }
  }

  public void initializeAppsgeyser(){
    if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("takeoff", true)){
      Log.d(TAG, "takeoff");
      AppsgeyserSDK.takeOff(this,
              getString(R.string.widgetID),
              getString(R.string.app_metrica_on_start_event),
              getString(R.string.template_version));
      PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("takeoff", false).commit();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    initializeScreenshotSecurity();
    DynamicLanguageActivityHelper.recreateIfNotInCorrectLanguage(this, TextSecurePreferences.getLanguage(this));
    AppsgeyserSDK.onResume(this);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return (keyCode == KeyEvent.KEYCODE_MENU && BaseActivity.isMenuWorkaroundRequired()) || super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_MENU && BaseActivity.isMenuWorkaroundRequired()) {
      openOptionsMenu();
      return true;
    }
    return super.onKeyUp(keyCode, event);
  }

  private void initializeScreenshotSecurity() {
    if (TextSecurePreferences.isScreenSecurityEnabled(this)) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    } else {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
  }

  /**
   * Modified from: http://stackoverflow.com/a/13098824
   */
  private void forceOverflowMenu() {
    try {
      ViewConfiguration config       = ViewConfiguration.get(this);
      Field             menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
      if(menuKeyField != null) {
        menuKeyField.setAccessible(true);
        menuKeyField.setBoolean(config, false);
      }
    } catch (IllegalAccessException e) {
      Log.w(TAG, "Failed to force overflow menu.");
    } catch (NoSuchFieldException e) {
      Log.w(TAG, "Failed to force overflow menu.");
    }
  }

  protected void startActivitySceneTransition(Intent intent, View sharedView, String transitionName) {
    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedView, transitionName)
                                         .toBundle();
    ActivityCompat.startActivity(this, intent, bundle);
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  protected void setStatusBarColor(int color) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(color);
    }
  }

//  private void setActionBarColor() {
//    ActionBar supportActionBar = getSupportActionBar();
//    if (supportActionBar == null) throw new AssertionError();
//    supportActionBar.setBackgroundDrawable(new ColorDrawable(color.toActionBarColor(this)));
//    setStatusBarColor(color.toStatusBarColor(this));
//  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(DynamicLanguageContextWrapper.updateContext(newBase, TextSecurePreferences.getLanguage(newBase)));
  }

  public void showFullscreen(String bannerTag){
    AppsgeyserSDK.getFastTrackAdsController()
            .showFullscreen(Constants.BannerLoadTags.ON_START, this, bannerTag, true);
  }

  @Override
  protected void onPause() {
    super.onPause();
    AppsgeyserSDK.onPause(this);
  }
}
