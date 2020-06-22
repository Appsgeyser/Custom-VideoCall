package org.thoughtcrime.securesms.util;


import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.thoughtcrime.securesms.ApplicationContext;

import java.io.File;

public class FileProviderUtil {

  public static final String     getAUTHORITY(Context context){
    return  context.getPackageName() + ".securesms.fileprovider";
  }

  public static Uri getUriFor(@NonNull Context context, @NonNull File file) {
    if (Build.VERSION.SDK_INT >= 24) return FileProvider.getUriForFile(context, getAUTHORITY(context), file);
    else                             return Uri.fromFile(file);
  }

  public static boolean isAuthority(@NonNull Uri uri) {
    return getAUTHORITY(ApplicationContext.getInstance().getApplicationContext()).equals(uri.getAuthority());
  }

  public static boolean delete(@NonNull Context context, @NonNull Uri uri) {
    if (getAUTHORITY(context).equals(uri.getAuthority())) {
      return context.getContentResolver().delete(uri, null, null) > 0;
    }
    return new File(uri.getPath()).delete();
  }
}
