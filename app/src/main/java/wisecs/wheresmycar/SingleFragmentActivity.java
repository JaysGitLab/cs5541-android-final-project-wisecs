package wisecs.wheresmycar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Carter W on 9/20/2016.
 *
 * For use with an activity that will only contain one Fragment.
 * Takes care of the Fragment Manager for this Activity
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

   protected abstract Fragment createFragment();

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_fragment);

      FragmentManager fm = getSupportFragmentManager();
      Fragment fragment = fm.findFragmentById(R.id.fragment_container);

      if(fragment == null) {
         fragment = createFragment();
         fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
      }
   }
}
