package wisecs.wheresmycar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 11/26/2016.
 *
 * ************************LOOK HERE LOOK HERE GO TO HERE TO FIGURE OUT WHAT YOU WANT*****************************************
 * http://stackoverflow.com/questions/13904651/android-google-maps-v2-how-to-add-marker-with-multiline-snippet
 */

public class MapsFragment extends SupportMapFragment implements GoogleMap.OnMarkerClickListener {
   private static final String TAG = "MapsFragment";
   private static final int EDIT_REQUEST = 1;

   private GoogleApiClient mClient;
   private GoogleMap mMap;
   private LatLng mCurrentLocation;
   private MarkerOptions mCurrentMarker;
   private MenuItem mPlaceItem;
   private MenuItem mEditItem;


   public static MapsFragment newInstance() {
      return new MapsFragment();
   }

   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);

      mClient = new GoogleApiClient.Builder(getActivity())
            .addApi(LocationServices.API)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
               @Override
               public void onConnected(Bundle bundle) {
                  Log.i(TAG,"Google Client is Connected");

                  getActivity().invalidateOptionsMenu();

                  findLocation();
                  restorePin();
                  putPin(mCurrentMarker);
                  zoomTo(mCurrentLocation, 17.0f);
               }

               @Override
               public void onConnectionSuspended(int i) {
                  Log.i(TAG,"Google Client is Suspended");
               }
            })
            .build();

      getMapAsync(new OnMapReadyCallback() {
         @Override
         public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            if (ActivityCompat.checkSelfPermission(getContext(),
                  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               //supposed to request permissions if do not have them
               return;
            }
            //should check permissions...
            mMap.setMyLocationEnabled(true);
            mMap.setInfoWindowAdapter(new DetailsAdapter(getLayoutInflater(savedInstanceState)));
            //mMap.setOnMarkerClickListener(MapsFragment.this);
         }
      });
   }

   @Override
   public void onStart() {
      super.onStart();

      getActivity().invalidateOptionsMenu();

      mClient.connect();
   }

   @Override
   public void onStop() {
      super.onStop();

      mClient.disconnect();
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_maps, menu);

      mPlaceItem = menu.findItem(R.id.action_place);
      mPlaceItem.setEnabled(mClient.isConnected());
      mEditItem = menu.findItem(R.id.action_edit);
      mEditItem.setEnabled(mPlaceItem.isEnabled() && mCurrentMarker != null);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.action_place:
            findLocation();
            putPin(mCurrentLocation);
            return true;
         case R.id.action_edit:
            //clearPin();
            editPin();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public boolean onMarkerClick(Marker marker) {
      return false;
   }

   private void findLocation() {
      if(!mClient.isConnected()) {
         Log.i(TAG, "Couldn't find location: Client not connected");
         return;
      }

      LocationRequest request = LocationRequest.create();
      request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      request.setNumUpdates(1);
      request.setInterval(0);
      if (ActivityCompat.checkSelfPermission(this.getContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         //supposed to request permissions if do not have them
         return;
      }
      LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
         @Override
         public void onLocationChanged(Location location) {
            Log.i(TAG, "Got a fix: " + location);
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            zoomTo(mCurrentLocation, 17.0f);  //NEEDS TO NOT BE HERE, need to sync somehow
         }
      });
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      switch(requestCode) {
         case EDIT_REQUEST:
            if(resultCode == Activity.RESULT_OK) {
               putPin((MarkerOptions) data.getParcelableExtra(MapsActivity.EXTRA_MARKER));
            }
      }
   }

   private void updateUI() {
      if(mMap == null) {
         Log.i(TAG, "Failed UI update: Map is null");
         return;
      }
      if(mCurrentLocation == null) {
         Log.i(TAG, "Failed UI update: Location is null");
         return;
      }


      putPin(mCurrentLocation);
      zoomTo(mCurrentLocation, 17.0f);

   }

   /* creates generic new pin */
   private void putPin(LatLng location) {
      if(mMap == null) {
         Log.i(TAG, "Failed to put pin: Map is null");
         return;
      }
      if(location == null) {
         Log.i(TAG, "Failed to put pin: Location is null");
         return;
      }
      clearPin();

      mCurrentMarker = new MarkerOptions()
            .position(location)
            .draggable(true)
            .title("My Car")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
      mMap.addMarker(mCurrentMarker);
      savePin();
      
      mEditItem.setEnabled(mPlaceItem.isEnabled());
   }

   /* creates pin with the attributes of marker */
   private void putPin(MarkerOptions marker) {
      if(mMap == null) {
         Log.i(TAG, "Failed to put pin: Map is null");
         return;
      }
      if(marker.getPosition() == null) {
         Log.i(TAG, "Failed to put pin: Location is null");
         return;
      }
      clearPin();

      mCurrentMarker = marker;
      mMap.addMarker(mCurrentMarker);
      savePin();

      //mEditItem.setEnabled(mPlaceItem.isEnabled());
   }

   private void clearPin() {
      if(mMap == null) {
         Log.i(TAG, "Failed to clear pin: Map is null");
         return;
      }

      mMap.clear();
      mCurrentMarker = null;
      SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
      editor.clear();
      editor.commit();
   }

   private void editPin() {
      Intent intent = DetailsActivity.newIntent(getActivity(), mCurrentMarker);
      startActivityForResult(intent, EDIT_REQUEST);
   }

   private void savePin() {
      if(mCurrentMarker ==  null) {
         Log.i(TAG, "Failed to save pin: Current pin is null");
      }

      SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
      editor.putString("latitude", "" + mCurrentMarker.getPosition().latitude);
      editor.putString("longitude", "" + mCurrentMarker.getPosition().longitude);
      editor.putString("title", "" + mCurrentMarker.getTitle());
      editor.putString("snippet", "" + mCurrentMarker.getSnippet());
      editor.commit();
   }

   private void restorePin() {
      SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
      String lat = sharedPref.getString("latitude", null);
      String lon = sharedPref.getString("longitude", null);
      if(lat == null || lon == null) {
         Log.i(TAG, "Failed to restore pin: No saved pin");
         return;
      }

      Double latitude = Double.valueOf(lat);
      Double longitude = Double.valueOf(lon);

      LatLng location = new LatLng(latitude, longitude);
      String title = sharedPref.getString("title", null);
      String snippet = sharedPref.getString("snippet", "");

      mCurrentMarker = new MarkerOptions()
            .position(location)
            .draggable(true)
            .title(title)
            .snippet(snippet)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));;
   }

   private void zoomTo(LatLng location, float depth) {
      if(mMap == null) {
         Log.i(TAG, "Failed to zoom: Map is null");
         return;
      }
      if(location == null) {
         Log.i(TAG, "Failed to zoom: Location is null");
         return;
      }
      if(depth < 2.0f || depth > 22.0f) {
         Log.i(TAG, "Failed to zoom: out of bounds depth");
         return;
      }

      /*LatLngBounds bounds = new LatLngBounds.Builder()
            .include(myPoint)
            .build();

      int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);*/
      CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, depth);
      mMap.animateCamera(update);
   }
}
