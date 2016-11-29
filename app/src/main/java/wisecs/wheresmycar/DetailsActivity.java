package wisecs.wheresmycar;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

/**
 * Created by Carter W on 11/29/2016.
 */

public class DetailsActivity extends SingleFragmentActivity {

   protected Fragment createFragment() {
      return DetailsFragment.newInstance();
   }
}
