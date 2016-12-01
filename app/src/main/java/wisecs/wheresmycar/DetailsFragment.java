package wisecs.wheresmycar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewGroupCompat;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 12/1/2016.
 */

public class DetailsFragment extends Fragment {
   private static final String ARG_OPTIONS_ID = "options_id";

   private TextView mTitle;
   private EditText mDetailsText;
   private Button mButton;
   private MarkerOptions marker;


   public static DetailsFragment newInstance() {
      Bundle args = new Bundle();

      DetailsFragment fragment = new DetailsFragment();
      fragment.setArguments(args);

      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      marker = (MarkerOptions) getArguments().getParcelable(ARG_OPTIONS_ID);
      //set things maybe?
   }

   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.fragment_details, container, false);

      mTitle = (TextView) v.findViewById(R.id.details_title);

      mDetailsText = (EditText) v.findViewById(R.id.details_input);

      mButton = (Button) v.findViewById(R.id.details_save);
      mButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(final View view) {

            if(mDetailsText.getText() != null) {
               marker.snippet(mDetailsText.getText().toString());
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra(ARG_OPTIONS_ID, marker);
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
         }
      });

      return v;
   }
}
