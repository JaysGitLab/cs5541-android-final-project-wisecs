package wisecs.wheresmycar;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Carter W on 12/1/2016.
 */

public class DetailsActivity extends SingleFragmentActivity {

   public static Intent newIntent(Context packageContext) {
      Intent intent = new Intent(packageContext, DetailsActivity.class);
      //intent.putSerializable?  .putExtra?
      return intent;
   }

   public Fragment createFragment() {
      //parameter parameter getIntent.getParcelableExtraExtra
      return DetailsFragment.newInstance();
   }
}
