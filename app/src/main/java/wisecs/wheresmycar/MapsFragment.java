package wisecs.wheresmycar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 11/26/2016.
 */

public class MapsFragment extends SupportMapFragment /*implements GoogleApiClient.ConnectionCallbacks*/{
   private static final String TAG = "MapsFragment";

   private GoogleApiClient mClient;
   private GoogleMap mMap;
   private Location mCurrentLocation;

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
            updateUI();
         }
      });
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

      LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

      MarkerOptions myMarker = new MarkerOptions()
            .position(myPoint);

      mMap.clear();
      mMap.addMarker(myMarker);

      /*LatLngBounds bounds = new LatLngBounds.Builder()
            .include(myPoint)
            .build();

      int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);*/
      CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myPoint, 17.0f);
      mMap.animateCamera(update);
   }
}