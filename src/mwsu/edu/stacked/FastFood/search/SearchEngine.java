package mwsu.edu.stacked.FastFood.search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import mwsu.edu.stacked.R;
import mwsu.edu.stacked.FastFood.gui.GUIController;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SearchEngine extends Activity {
	
	// Request codes
	private static final int CRITERIA_CODE = 13;
	private static final int GPS_REQUEST = 30;
	
	// Error codes that I made up
	private static final int YELP_UNAVAILABLE = 4;
	private static final int NO_CONNECTION = 303;
	private static final int LOCATION_NOT_FOUND = 302;
	private static final int GPS_DISABLED = 301;
	private static final int NO_RESULTS = 300;
	private static final int BAD_LOCATION = 200;
	
	private float gpsRange;
	private boolean useGps = true;
	private String cuisine;
	private String type;
	private Boolean wildcard;
	private String cuisineDisplay;
	private GetPlacesTask yelpSearch = new GetPlacesTask();
	private TextView criteriaDisplay;
	private ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
	private Yelp places;
	
	private LocationManager locationManager;
	private Location loc;
	
	// Location listener necessary for requesting location updates
	// could put messages in these functions for debugging purposes
	private LocationListener locationListener = new LocationListener() {
		
		public void onLocationChanged(Location location) {}
		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider,
				int status, Bundle extras) {}
	};
	
	/**
	 * A custom seekbar change listener for checking the value of the GPS range 
	 * display
	 */
	private OnSeekBarChangeListener gpsMilesDisplay = new OnSeekBarChangeListener() {	
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			// Progress bar goes from 0-100, this is used to cap it at 25 (25 miles)
			int gpsMaxRange = 100 / 25; 

			//Multiply by 10 so the number can be rounded to the nearest tenth
			gpsRange = ((float)progress / gpsMaxRange) * 10;	

			//Round the result so it's readable
			gpsRange = Math.round(gpsRange);
			gpsRange += 1;
			gpsRange /= 10;

			//Update the layout
			TextView tv = (TextView) findViewById(R.id.textView5);
			tv.setText("Search in range of\t"+gpsRange+"\t miles");
		}

		//Not interested in these
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

	};
	
	/**
	 * Simple checkbox listener that swaps the GPS display and the
	 * Enter city text field.
	 */
	OnCheckedChangeListener gps_display = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {

			LinearLayout layout;
			
			// If the box is checked, show the GPS bar
			if(isChecked){
				layout = (LinearLayout) findViewById(R.id.linearLayout5);
				layout.setVisibility(LinearLayout.VISIBLE);
				layout = (LinearLayout) findViewById(R.id.linearLayout3);
				layout.setVisibility(LinearLayout.GONE);

				useGps = true;
			}
			// Otherwise, show the "Select a city" text field
			else {
				layout = (LinearLayout) findViewById(R.id.linearLayout5);
				layout.setVisibility(LinearLayout.GONE);
				layout = (LinearLayout) findViewById(R.id.linearLayout3);
				layout.setVisibility(LinearLayout.VISIBLE);

				useGps = false;
			}
		}
		
	};
	
	/**
	 * Main entry point for the fast food application. 
	 */
	@Override
	public void onCreate (Bundle savedInstanceState) 
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// initialize the slider to 7.5 miles.
		SeekBar slider = (SeekBar) findViewById(R.id.seekBar1);

		slider.setOnSeekBarChangeListener(gpsMilesDisplay);
		slider.setProgress(30); 
		
		// Set listener for the checkbox
		CheckBox cb = (CheckBox) findViewById(R.id.checkBox1);
		cb.setOnCheckedChangeListener(gps_display);
		
		// Show the city field and hide the gps display
		LinearLayout layout;
		layout = (LinearLayout) findViewById(R.id.linearLayout5);
		layout.setVisibility(LinearLayout.GONE);
		
		useGps = false;
		
		// Hid the criteria display until the user enters criteria
		criteriaDisplay = (TextView) findViewById(R.id.criteriaDisplay);
		criteriaDisplay.setVisibility(TextView.INVISIBLE);
		
		readCities();   
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 30, 
				locationListener);
		
//		getDir()
	}
	
	@Override
	public void onResume() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 30, 
				locationListener);
		super.onResume();
	}
	
	public void onDestroy() {
		locationManager.removeUpdates(locationListener);
		super.onDestroy();
	}
	
	/**
	 * Handles clicking events for anything in the main layout/activity
	 *      * 
	 *      * @param view The view that was clicked
	 */
	public void clickHandler(View view) {
		//See which view was clicked
		switch(view.getId()){

		// Search button creates the yelp query
		case R.id.search :
			yelpSearch = new GetPlacesTask();
			yelpSearch.execute();
			break;
			
		// Add criteria opens up the criteria form
		case R.id.add_criteria :
			Intent i = new Intent(this, CriteriaForm.class);
			startActivityForResult(i, CRITERIA_CODE);
			break;
		
		// Clear any previous criteria
		case R.id.reset_criteria :
			cuisine = null;
			type = null;
			wildcard = false;
			criteriaDisplay.setVisibility(TextView.INVISIBLE);
			break;
		}
	}

	/**
	 * Show an appropriate dialog for the appropriate id
	 */
	@Override
	public Dialog onCreateDialog(int id) {
		
		Dialog dialog = null;
		
		switch(id) {
		
		// Show the yelp search progress dialog
		case 0 :
			ProgressDialog yelpProgress = new ProgressDialog(this);
			
			yelpProgress.setTitle("Searching Restaurants");
			yelpProgress.setMessage("Searching for restaurants, please wait a moment \n\n" +
					"Search Powered by www.yelp.com");
			yelpProgress.setButton("Cancel", new OnClickListener() {
	
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					yelpSearch.cancel(true);
				}
				
			});
			
			dialog = yelpProgress;
			break;
		case YELP_UNAVAILABLE :
			dialog = createAlertDialog("Yelp is unavailable at the moment, try again later");
			break;
		case BAD_LOCATION :
			dialog = createAlertDialog("Location was unspecified, make sure you entered a correct city name");
			break;
		case NO_RESULTS :
			dialog = createAlertDialog("Search yielded no results, try different criteria");
			break;
			
		// If the users gps is disabled, give them the option to turn it on
		case GPS_DISABLED :
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
		           .setCancelable(false)
		           
		           // Open the activity that shows gps options
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_REQUEST);		               }
		           })
		           
		           // Do nothing, close the dialog
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.cancel();
		               }
		           });
		    dialog = builder.create();
		    break;
		case LOCATION_NOT_FOUND :
			dialog = createAlertDialog("Unable to pinpoint your GPS location");
			break;
		case NO_CONNECTION :
			dialog = createAlertDialog("Could not establish an internet connection");
			yelpSearch.cancel(true);
			break;
			
		// If there is an error code we missed, show the error that yelp returns
		default :
			String error = places.getLastError();
			dialog = createAlertDialog("An error ocurred: " + error);
		}
		
		
		return dialog;
	}
	
	/**
	 * Creates a generic alert dialoge
	 * 
	 * @param message - message to be shown in the dailog
	 * @return - a dialog object to be used by the activity
	 */
	public Dialog createAlertDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setCancelable(false)
		       .setNeutralButton("Okay", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
		    	   
		       });
		AlertDialog alert = builder.create();
		
		return alert; 
	}
	
	/**
	 * Read in a raw text file containing cities that will appear in 
	 * the auto complete text form
	 */
	public void readCities(){
		try {
			// Open the raw text file
			BufferedReader br = new BufferedReader(
					new InputStreamReader(getResources().openRawResource(R.raw.cities2)));
			
			// Grab each city as a line of input and store in an arraylist
			String line;
			ArrayList<String> al = new ArrayList<String>();
			
			while((line=br.readLine())!=null){
				al.add(line);
			}
			
			// Convert list to standard array
			String[] array = new String[al.size()];
			al.toArray(array);
			
			// Send the new array adapter to the form for usage.
			AutoCompleteTextView myAutoComplete = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
		    myAutoComplete.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, array));
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Grab values from the criteria form when it finishes.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == CRITERIA_CODE) {
			
			// Grab the yelp id for the cuisine that is selected
			if (data.hasExtra("cuisine")) {
				cuisine = data.getExtras().getString("cuisine");
				if(cuisine.equals("any")) cuisine = null;
			}
			
			// If there is a type, grab it
			if(data.hasExtra("type")){
				type = data.getExtras().getString("type");
			}
			
			// If they selected wildcard, get it
			if(data.hasExtra("wildcard")){
				wildcard = data.getExtras().getBoolean("wildcard");
			}
			
			// This is the cuisine value that should be displayed to the user
			if(data.hasExtra("cuisineDisplay")){
				cuisineDisplay = data.getExtras().getString("cuisineDisplay");
			}
			
			// Show the criteria that they selected
			criteriaDisplay.setText("Looking for " + type + " restaurants that taste like " 
					+ cuisineDisplay);
			criteriaDisplay.setVisibility(TextView.VISIBLE);
		}
		if (resultCode == RESULT_OK && requestCode == GPS_REQUEST) {
			yelpSearch.cancel(true);
		}
		
	}
	

	/**
	 * Thread for executing a yelp search outside of the main UI thread
	 * @author Zach
	 *
	 */
	private class GetPlacesTask extends AsyncTask <Void, Void, Void>  {
		
		@Override
		protected void onPreExecute () 
		{
			// Let the user know we're searching
			showDialog(0);	
			
			// Don't want to reuse results from a past query
			if(restaurants != null && restaurants.size() > 0)
				restaurants.clear();
			
			// Want to check if the device has GPS enabled
			if(useGps) {
				
				// If not, stop everything and kindly ask the user to turn on their gps
				if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					removeDialog(0);
					showDialog(GPS_DISABLED);
					this.cancel(true);
				}
				
				// Otherwise, get their location
				else {
					
					loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					
					if(loc == null) {
						removeDialog(0);
						showDialog(LOCATION_NOT_FOUND);
						this.cancel(true);
					}
				}
			}
		}
		
		/**
		 * Actual work done by the thread, don't manipulate the UI or show any dialogs here,
		 * or bad things could happen.
		 */
		@Override
		protected Void doInBackground (Void... params) 
		{		
			
			// Get the city input
			AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
			String city = tv.getText().toString();
			
			// Clean it of trailing whitespace, replace white space with +'s and
			// remove any digits
			city = city.trim();
			city = city.replaceAll("\\s+", "+");
			city = city.replaceAll("\\d+", "");
			
			places = new Yelp();
			
			String term;
			
			// Add the type delimiter if they selected fast food
			// TODO: Note, yelp does not return restaurants that haven't
			// received reviews, and most people don't review mcdonalds, so
			// don't expect to see a lot of fast food chains
			if(type != null) {
				if(type.equals("Fast Food")) {
					term = "hotdogs";
				}
				else {
					term = "restaurants";
				}
			}
			else
				term = "restaurants";
						
			boolean success = false;
			
			// Run the actual query
			if(useGps){
				float radiusInMeters = gpsRange * 1609.344f;
				success = places.searchByLatLon(term, loc.getLatitude(), loc.getLongitude(), radiusInMeters, cuisine);
			}
			else {
				success = places.searchByCity(term, city, cuisine);
			}				
			
			// If a connection could not be made, kill the task
			if(!success) {
				publishProgress();
			}
			
			// Get the restaurants, if there is more than 6, randomly select 6
			int numPlaces = places.getNumLocations() >= 7 ? 6 : places.getNumLocations();
			
			if (numPlaces > 0) {
				restaurants = places.getNPlacesAtRandom(numPlaces);
			}

			return null;
		}
		
		// For not this just kills the thread and shows a connection error
		@Override
		protected void onProgressUpdate (Void... params) 
		{
			removeDialog(0);
			showDialog(NO_CONNECTION);
			cancel(true);
		}
		
		// Make sure that pesky dialog is gone when the task is cancelled
		@Override
		protected void onCancelled() {
			//locationManager.removeUpdates(locationListener);
			removeDialog(0);
		}
		
		@Override
		protected void onPostExecute (Void result) 
		{
			// Get rid of the progress dialog when we're done
			removeDialog(0);
			
			// Stop getting GPS updates to conserve battery power
			if(useGps)
				locationManager.removeUpdates(locationListener);
			
			// Show an error if one occurred
			if(places.getLastError() != null){
				showDialog(places.getMessageCode());
			}
			
			// If there are no restaurants found, indicate so
			else if(restaurants.size() == 0) {
				showDialog(NO_RESULTS);
			}
			
			// Otherwise, everything is fine, go onto the next stage
			else {
				Intent guiController = new Intent(SearchEngine.this, GUIController.class);
				guiController.putExtra("restaurants", restaurants);
				guiController.putExtra("wildcard", wildcard);
				startActivity(guiController);
			}
		}

	}
}
