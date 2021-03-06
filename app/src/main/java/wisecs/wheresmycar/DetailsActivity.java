package wisecs.wheresmycar;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 12/1/2016.
 *
 */

public class DetailsActivity extends SingleFragmentActivity {
   private static final String EXTRA_MARKER = "marker_options";

   public static Intent newIntent(Context packageContext, MarkerOptions marker)
   {
      Intent intent = new Intent(packageContext, DetailsActivity.class);
      intent.putExtra(EXTRA_MARKER, marker);
      return intent;
   }

   @Override
   public Fragment createFragment() {
      MarkerOptions marker = (MarkerOptions) getIntent()
            .getParcelableExtra(EXTRA_MARKER);

      return DetailsFragment.newInstance(marker);
   }

}
