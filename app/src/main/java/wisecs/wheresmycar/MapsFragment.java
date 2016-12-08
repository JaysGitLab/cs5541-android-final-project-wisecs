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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Carter W on 11/26/2016.
 *
 * The main Controller for the Where'sMyCar? app.
 *
 * Handles map, location, and marker data
 */

public class MapsFragment extends SupportMapFragment {
   private static final String TAG = "MapsFragment";
   private static final int EDIT_REQUEST = 1;

   private GoogleApiClient mClient;
   private GoogleMap mMap;
   private LatLng mCurrentLocation;
   private MarkerOptions mCurrentMarker;
   private MenuItem mPlaceItem;
   private MenuItem mEditItem;

   /**
    * @return Returns a new instance of MapsFragment
    */
   public static MapsFragment newInstance() {
      return new MapsFragment();
   }

   /**
    * Enables the option menu, connects the GoogleApiClient, and gets the GoogleMap
    */
   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);

      /**
       * Connects to the Google Api
       * Once the client is connected the current location will be found,
       *    any saved Marker attributes will be restored and a Marker will
       *    be placed onto the map and zoomed to.
       */
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

      /**
       * Gets a Google Map
       * Enables the device's location to be displayed.
       * Makes it so that tapping a Marker will display a DetailsFragment.
       * Updates the current Marker's position when it has been dragged.
       */
      getMapAsync(new OnMapReadyCallback() {
         @Override
         public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            if (ActivityCompat.checkSelfPermission(getContext(),
                  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               //supposed to request permissions if do not have them...
               return;
            }
            //should check permissions...
            mMap.setMyLocationEnabled(true);
            mMap.setInfoWindowAdapter(new DetailsAdapter(getLayoutInflater(savedInstanceState)));
            mMap.setOnMarkerDragListener( new GoogleMap.OnMarkerDragListener() {
               @Override
               public void onMarkerDragEnd(Marker marker) {
                  mCurrentMarker.position(marker.getPosition());
               }

               @Override
               public void onMarkerDragStart(Marker marker) {}
               @Override
               public void onMarkerDrag(Marker marker) {}
            });
         }
      });
   }

   /**
    * Will restore any saved Marker attributes.
    * Tells the GoogleApiClient to connect.
    */
   @Override
   public void onStart() {
      super.onStart();

      getActivity().invalidateOptionsMenu();
      restorePin();
      mClient.connect();
   }

   /**
    * Saves any current Marker attributes.
    * Disconnects the GoogleApiClient.
    */
   @Override
   public void onStop() {
      super.onStop();

      savePin();
      mClient.disconnect();
   }

   /**
    * Inflates this Activity's menu with two buttons.
    * The "Edit Pin" button will only be enabled if there is a Marker placed.
    */
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_maps, menu);

      mPlaceItem = menu.findItem(R.id.action_place);
      mPlaceItem.setEnabled(mClient.isConnected());
      mEditItem = menu.findItem(R.id.action_edit);
      mEditItem.setEnabled(mPlaceItem.isEnabled() && mCurrentMarker != null);
   }

   /**
    * Responds to presses of the "Place Pin" and "Edit Pin" buttons.
    * Place Pin places a new pin at the current location.
    * Edit Pin will start a new DetailsActivity
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.action_place:
            findLocation();
            putPin(mCurrentLocation);
            return true;
         case R.id.action_edit:
            editPin();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * Retrieves the Marker attributes from a DetailsActivity
    *    and places a Marker with those attributes
    * Will do nothing if DetailsActivity did not close with RESULT_OK
    *
    * @param requestCode Should be EDIT_REQUEST
    * @param resultCode Should be RESULT_OK
    * @param data Should include a Parcelable Extra of class MarkerOptions
    */
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

   /**
    * Finds the location of this device with high accuracy.
    * Will fail if ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION have not been granted.
    * Updates mCurrentLocation
    * Zooms to the found location to a depth of 17.0
    */
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
            zoomTo(mCurrentLocation, 17.0f);  //Probably shouldn't be here, need to sync somehow
         }
      });
   }

   /**
    * Places a Marker at the location of the parameter.
    * The Marker will have the title "My Car"
    * Removes any previous marker that was on the map and
    *    updates the current Marker to the new Marker.
    * Saves the new Marker's attributes.
    * Enables the "Edit Marker" option button.
    *
    * @param location The location to place the new Marker
    */
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

   /**
    * Places a Marker at the location of the parameter.
    * Removes any previous marker that was on the map and
    *    updates the current Marker to the new Marker.
    * Saves the new Marker's attributes.
    *
    * @param marker The attributes of the new Marker to place
    */
   private void putPin(MarkerOptions marker) {
      if(mMap == null) {
         Log.i(TAG, "Failed to put pin: Map is null");
         return;
      }
      if(marker == null) {
         Log.i(TAG, "Failed to put pin: Location is null");
         return;
      }
      clearPin();

      mCurrentMarker = marker;
      mMap.addMarker(mCurrentMarker);
      savePin();
   }

   /**
    * Removes any marker from the map, sets the current Marker to null,
    *    and clears saved Marker attributes.
    */
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

   /**
    * Starts a DetailsActivity, providing it with the current Marker's location, title, and details
    * The activity is started expecting it to provide the information about a Marker when the
    *    activity ends.
    */
   private void editPin() {
      Intent intent = DetailsActivity.newIntent(getActivity(), mCurrentMarker);
      startActivityForResult(intent, EDIT_REQUEST);
   }

   /**
    * Saves the location, title, and description of the current Marker, if there is one.
    */
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

   /**
    * Pulls the location, title, and description of a saved Marker if there is one.
    * Updates currentMarker to a Marker with the attributes of the saved Marker.
    */
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
      String snippet = sharedPref.getString("snippet", null);

      mCurrentMarker = new MarkerOptions()
            .position(location)
            .draggable(true)
            .title(title)
            .snippet(snippet)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));;
   }

   /**
    * Zooms the map in or out.
    * If there is a current Marker it will zoom to show the given location and the marker
    * If there is no current Marker it will zoom to the given location at the given depth
    *
    * @param location The location to be able to view after the zoom
    * @param depth The depth to zoom to if there is no Marker
    */
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
      CameraUpdate update;
      LatLngBounds bounds;
      if(mCurrentMarker == null) {
         update = CameraUpdateFactory.newLatLngZoom(location, depth);
      } else {
         bounds = new LatLngBounds.Builder()
               .include(location)
               .include(mCurrentMarker.getPosition())
               .build();

         int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
         update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
      }
      mMap.animateCamera(update);
   }

   /**
    * Was originally used to clear a pin, put a pin, and zoom to the location
    * Upon completing DetailsActivity it no longer worked
    * An encapsulating method like this could have some place in this Activity if done properly
    */
   /*private void updateUI() {
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

   }*/
}
