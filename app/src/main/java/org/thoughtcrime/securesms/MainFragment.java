package org.thoughtcrime.securesms;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.configuration.Constants;

public class MainFragment extends Fragment {

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    if (!(requireActivity() instanceof MainActivity)) {
      throw new IllegalStateException("Can only be used inside of MainActivity!");
    }
  }

  protected @NonNull MainNavigator getNavigator() {
    return MainNavigator.get(requireActivity());
  }

  public void showFullscreen(String bannerTag){
    AppsgeyserSDK.getFastTrackAdsController()
            .showFullscreen(Constants.BannerLoadTags.ON_START, getContext(), bannerTag, true);
  }
}
