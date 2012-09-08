package mwsu.edu.stacked.FastFood.gui;

import mwsu.edu.stacked.R;
import mwsu.edu.stacked.FastFood.gps.LocationView;
import mwsu.edu.stacked.FastFood.search.Restaurant;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * Simply shows the winning contestant of the race
 * 
 * @author Zach
 *
 */
public class DisplayWinner extends Activity {
	private Restaurant winner;

	@Override
	public void onCreate (Bundle cheese) 
	{
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(cheese);
		setContentView(R.layout.foodrace);
		
		winner = getIntent().getExtras().getParcelable("winner");
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		showResult();
	}

	/**
	 * Handles the buttons
	 * 
	 * @param view - the button that was clicked
	 */
	public void clickHandler(View view) {
		switch(view.getId()) {
		
		// Show the winner on the map
		case R.id.view_map :
			Intent gps = new Intent(this, LocationView.class);
			gps.putExtra("winner", winner);
			startActivity(gps);
			break;
			
		// Go back to start a new race
		case R.id.race_again :
			finish();
			break;
		}
	}

	/**
	 * Show the winner of the race
	 */
	public void showResult() {

		// Make sure there is a winner
		if(winner != null) {
			
			// Name of the restaurant
			TextView tv = (TextView) findViewById(R.id.restaurant_selection);
			tv.setText(winner.name);

			// Location, city and address of the restaurant
			tv = (TextView) findViewById(R.id.vicinity);
			tv.setText(winner.vicinity);
		}
	}

}
