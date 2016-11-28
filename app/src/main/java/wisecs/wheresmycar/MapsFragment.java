package wisecs.wheresmycar;

import android.Manifest;
import android.content.Context;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 11/26/2016.
 */

public class MapsFragment extends SupportMapFragment /*implements GoogleApiClient.ConnectionCallbacks*/{
   private static final String TAG = "MapsFragment";

   private GoogleApiClient mClient;
   private GoogleMap mMap;
   private Location mCurrentLocation;
   private Location mCurrentMarker;
   //private MenuItem searchItem;

   public static MapsFragment newInstance() {
      return new MapsFragment();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
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

            //should check permissions...
            mMap.setMyLocationEnabled(true);
            //updateUI();
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

      MenuItem searchItem = menu.findItem(R.id.action_locate);
      searchItem.setEnabled(mClient.isConnected());
      MenuItem editItem = menu.findItem(R.id.action_edit);
      editItem.setEnabled(searchItem.isEnabled());
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch(item.getItemId()) {
         case R.id.action_locate:
            findLocation();
            updateUI();
            return true;
         case R.id.action_edit:
            clearPin();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /*@Override
   public void onConnected(Bundle bundle) {
      Log.i(TAG,"Google Client is Connected");

      updateUI();
      findLocation();
   }

   @Override
   public void onConnectionSuspended(int cause) {
      Log.i(TAG,"Google Client is Suspended");
   }*/

   private void findLocation() {
      if(!mClient.isConnected()) {
         Log.i(TAG, "Couldn't find location: Client not connected");
         return;
      }

      LocationRequest request = LocationRequest.create();
      request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      request.setNumUpdates(1);
      request.setInterval(0);
      if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         //supposed to request permissions if do not have them
         return;
      }
      LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
         @Override
         public void onLocationChanged(Location location) {
            Log.i(TAG, "Got a fix: " + location);
            mCurrentLocation = location;
            zoomTo(mCurrentLocation, 17.0f);  //NEEDS TO NOT BE HERE, need to sync somehow
         }
      });
   }

   /**
    *  Need to somehow NOT be integrated into findLocation
    */
   private void updateUI() {
      if(mMap == null) {
         Log.i(TAG, "Failed UI update: Map is null");
         return;
      }
      if(mCurrentLocation == null) {
         Log.i(TAG, "Failed UI update: Location is null");
         return;
      }

      clearPin();
      putPin(mCurrentLocation);
      zoomTo(mCurrentLocation, 17.0f);

   }

   private void putPin(Location location) {
      if(mMap == null) {
         Log.i(TAG, "Failed to put pin: Map is null");
         return;
      }
      if(location == null) {
         Log.i(TAG, "Failed to put pin: Location is null");
         return;
      }
      mCurrentMarker = location;
      savePin();
      LatLng myPoint = new LatLng(location.getLatitude(), location.getLongitude());
      MarkerOptions myMarker = new MarkerOptions()
            .position(myPoint);
      //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
      mMap.addMarker(myMarker);
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

   private void savePin() {
      if(mCurrentMarker ==  null) {
         Log.i(TAG, "Failed to save pin: Current pin is null");
      }

      SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
      editor.putString("latitude", "" + mCurrentMarker.getLatitude());
      editor.putString("longitude", "" + mCurrentMarker.getLongitude());
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

      //Test Code
      mCurrentLocation = new Location("");
      mCurrentLocation.setLatitude(latitude);
      mCurrentLocation.setLongitude(longitude);

      putPin(mCurrentLocation);
   }

   private void zoomTo(Location location, float depth) {
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

      LatLng myPoint = new LatLng(location.getLatitude(), location.getLongitude());
      /*LatLngBounds bounds = new LatLngBounds.Builder()
            .include(myPoint)
            .build();

      int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);*/
      CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myPoint, depth);
      mMap.animateCamera(update);
   }
}
