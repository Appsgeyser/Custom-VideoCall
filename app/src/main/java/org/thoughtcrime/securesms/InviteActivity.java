package org.thoughtcrime.securesms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.AnimRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.thoughtcrime.securesms.components.ContactFilterToolbar;
import org.thoughtcrime.securesms.components.ContactFilterToolbar.OnFilterChangedListener;
import org.thoughtcrime.securesms.contacts.ContactsCursorLoader.DisplayMode;
import org.thoughtcrime.securesms.contacts.SelectedContact;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientId;
import org.thoughtcrime.securesms.sms.MessageSender;
import org.thoughtcrime.securesms.sms.OutgoingTextMessage;
import org.thoughtcrime.securesms.util.DynamicNoActionBarInviteTheme;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.ThemeUtil;
import org.thoughtcrime.securesms.util.ViewUtil;
import org.thoughtcrime.securesms.util.WindowUtil;
import org.thoughtcrime.securesms.util.concurrent.ListenableFuture.Listener;
import org.thoughtcrime.securesms.util.task.ProgressDialogAsyncTask;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class InviteActivity extends PassphraseRequiredActionBarActivity {

  private ContactSelectionListFragment contactsFragment;
  private EditText                     inviteText;
  private Animation                    slideInAnimation;
  private Animation                    slideOutAnimation;
  private DynamicTheme                 dynamicTheme = new DynamicNoActionBarInviteTheme();
  private Toolbar                      primaryToolbar;

  @Override
  protected void onPreCreate() {
    super.onPreCreate();
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    getIntent().putExtra(ContactSelectionListFragment.DISPLAY_MODE, DisplayMode.FLAG_SMS);
    getIntent().putExtra(ContactSelectionListFragment.MULTI_SELECT, true);
    getIntent().putExtra(ContactSelectionListFragment.REFRESHABLE, false);

    setContentView(R.layout.invite_activity);

    initializeAppBar();
    initializeResources();
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }

  private void initializeAppBar() {
    primaryToolbar = findViewById(R.id.toolbar);
    setSupportActionBar(primaryToolbar);

    assert getSupportActionBar() != null;

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.AndroidManifest__invite_friends);
  }

  private void initializeResources() {
    slideInAnimation  = loadAnimation(R.anim.slide_from_bottom);
    slideOutAnimation = loadAnimation(R.anim.slide_to_bottom);

    View                 shareButton     = ViewUtil.findById(this, R.id.share_button);

    inviteText        = ViewUtil.findById(this, R.id.invite_text);

    inviteText.setText(getString(R.string.InviteActivity_lets_switch_to_signal, "https://play.google.com/store/apps/details?id=" + getPackageName()));

    shareButton.setOnClickListener(new ShareClickListener());
  }

  private Animation loadAnimation(@AnimRes int animResId) {
    final Animation animation = AnimationUtils.loadAnimation(this, animResId);
    animation.setInterpolator(new FastOutSlowInInterpolator());
    return animation;
  }

  private void setPrimaryColorsToolbarNormal() {
    primaryToolbar.setBackgroundColor(0);
    primaryToolbar.getNavigationIcon().setColorFilter(null);
    primaryToolbar.setTitleTextColor(ThemeUtil.getThemedColor(this, R.attr.title_text_color_primary));

    if (Build.VERSION.SDK_INT >= 23) {
      getWindow().setStatusBarColor(ThemeUtil.getThemedColor(this, android.R.attr.statusBarColor));
      getWindow().setNavigationBarColor(ThemeUtil.getThemedColor(this, android.R.attr.navigationBarColor));
      WindowUtil.setLightStatusBarFromTheme(this);
    }

    WindowUtil.setLightNavigationBarFromTheme(this);
  }

  private void setPrimaryColorsToolbarForSms() {
    primaryToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.core_ultramarine));
    primaryToolbar.getNavigationIcon().setColorFilter(ThemeUtil.getThemedColor(this, R.attr.conversation_subtitle_color), PorterDuff.Mode.SRC_IN);
    primaryToolbar.setTitleTextColor(ThemeUtil.getThemedColor(this, R.attr.conversation_title_color));

    if (Build.VERSION.SDK_INT >= 23) {
      getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.core_ultramarine));
      WindowUtil.clearLightStatusBar(getWindow());
    }

    if (Build.VERSION.SDK_INT >= 27) {
      getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.core_ultramarine));
      WindowUtil.clearLightNavigationBar(getWindow());
    }
  }

  private class ShareClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      Intent sendIntent = new Intent();
      sendIntent.setAction(Intent.ACTION_SEND);
      sendIntent.putExtra(Intent.EXTRA_TEXT, inviteText.getText().toString());
      sendIntent.setType("text/plain");
      if (sendIntent.resolveActivity(getPackageManager()) != null) {
        startActivity(Intent.createChooser(sendIntent, getString(R.string.InviteActivity_invite_to_signal)));
      } else {
        Toast.makeText(InviteActivity.this, R.string.InviteActivity_no_app_to_share_to, Toast.LENGTH_LONG).show();
      }
    }
  }

}
