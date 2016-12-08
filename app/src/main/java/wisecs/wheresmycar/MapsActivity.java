package wisecs.wheresmycar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 11/26/2016.
 *
 */

public class MapsActivity extends SingleFragmentActivity {

   private static final int REQUEST_ERROR = 0;

   public static final String EXTRA_MARKER = "marker_options";

   public static Intent newIntent(Context packageContext, MarkerOptions marker)
   {
      Intent intent = new Intent(packageContext, DetailsActivity.class);
      intent.putExtra(EXTRA_MARKER, marker);
      return intent;
   }

   @Override
   protected Fragment createFragment() {
      return MapsFragment.newInstance();
   }

   @Override
   protected void onResume() {
      super.onResume();

      int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

      if(errorCode != ConnectionResult.SUCCESS) {
         Dialog errorDialog = GooglePlayServicesUtil
               .getErrorDialog(errorCode, this, REQUEST_ERROR,
                     new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                           finish();
                        }
                     });

         errorDialog.show();
      }
   }
}
