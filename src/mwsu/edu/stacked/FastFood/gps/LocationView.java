package mwsu.edu.stacked.FastFood.gps;

import mwsu.edu.stacked.R;
import mwsu.edu.stacked.FastFood.search.Restaurant;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Window;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * A standard Map Activity that displays the location of
 * the winning restaurant and the user
 * 
 * @author Opal
 */
public class LocationView extends MapActivity {
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private Restaurant winner;
	
	// Empty location listener so we can request location updates
	private LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
	
	@Override
	protected boolean isRouteDisplayed () 
	{
		return false;
	}
	public void onCreate(Bundle bundle) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(bundle);
		setContentView(R.layout.gps); // bind the layout to the activity
		
		 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// create a map view
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(false);
		mapController = mapView.getController(); 
		mapController.setZoom(14); // Zoom 1 is world view 
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		winner = getIntent().getExtras().getParcelable("winner");

		// Get the users location
		Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		GeoPoint myLocation = null;
		
		// Make sure we actually have the users location to prevent the map from crashing
		if(loc != null) {
			int lat = (int) (loc.getLatitude() * 1E6);
			int lng = (int) (loc.getLongitude() * 1E6);
			
			myLocation = new GeoPoint(lat, lng);
		}

		// Create a marker for locations to be shown on the map. TODO markers should change based on location
		Drawable marker = getResources().getDrawable(android.R.drawable.star_big_on);
		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		// Create the overlay to show markers
		MapOverlay myItemizedOverlay = new MapOverlay(marker);
		mapView.getOverlays().add(myItemizedOverlay);
		
		GeoPoint theOtherLocation = new GeoPoint((int) (winner.location[0] * 1E6), (int) (winner.location[1] * 1E6));

		if(myLocation != null)
			myItemizedOverlay.addItem(myLocation, "My Location", "Doesn't matter");
		
		myItemizedOverlay.addItem(theOtherLocation, winner.name, winner.vicinity);
		
		mapController.animateTo(theOtherLocation);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

}
