package wisecs.wheresmycar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 12/1/2016.
 *
 * Handles the editing of a Marker's description
 */

public class DetailsFragment extends Fragment {
   private static final String ARG_MARKER = "marker_options";

   private TextView mTitle;
   private EditText mDetailsText;
   private Button mButton;
   private MarkerOptions mMarker;

   /**
    * Returns a new instance of DetailsFragment, ensuring that an MarkerOptions
    *    object is being passed between the calling Activity and this one.
    * @param marker The MarkerOptions instance to edit
    * @return Returns a new instance of DetailsFragment
    */
   public static DetailsFragment newInstance(MarkerOptions marker) {
      Bundle args = new Bundle();
      args.putParcelable(ARG_MARKER, marker);

      DetailsFragment fragment = new DetailsFragment();
      fragment.setArguments(args);

      return fragment;
   }

   /**
    * Inflates the view and handles the editing of the provided MarkerOptions' description
    * When the "Save Details" button is pressed, the edited MarkerOptions will be passed
    *    back to the calling Activity and this Activity will close.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mMarker = (MarkerOptions) getArguments().getParcelable(ARG_MARKER);
   }

   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.fragment_details, container, false);

      mTitle = (TextView) v.findViewById(R.id.details_title);
      //if(mMarker.getTitle() != null)
      //    mTitle.setText(mMarker.getTitle());

      mDetailsText = (EditText) v.findViewById(R.id.details_input);
      if(mMarker.getSnippet() != null)
         mDetailsText.setText(mMarker.getSnippet());

      mButton = (Button) v.findViewById(R.id.details_save);
      mButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(final View view) {
            if(mDetailsText.getText() != null) {
               mMarker.snippet(mDetailsText.getText().toString());
            }

            Intent resultIntent = MapsActivity.newIntent(getActivity(), mMarker);
            getActivity().setResult(Activity.RESULT_OK, resultIntent);

            getActivity().finish();
         }
      });

      return v;
   }
}
