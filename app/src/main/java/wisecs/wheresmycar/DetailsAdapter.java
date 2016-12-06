package wisecs.wheresmycar;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Carter W on 12/1/2016.
 */



public class DetailsAdapter implements InfoWindowAdapter {
   private View mPopup;
   private LayoutInflater mInflater;

   DetailsAdapter(LayoutInflater inflater) {
      mInflater = inflater;
   }

   @Override
   public View getInfoWindow(Marker marker) {
      return null;
   }

   @Override
   public View getInfoContents(Marker marker) {
      if(mPopup==null) { mPopup=mInflater.inflate(R.layout.adapter_details, null); }

      TextView title=(TextView)mPopup.findViewById(R.id.details_title);
      title.setText(marker.getTitle());

      TextView textView = (TextView) mPopup.findViewById(R.id.details_view);
      textView.setText(marker.getSnippet());

      return mPopup;
   }
}
