package mwsu.edu.stacked.FastFood.gui;

import java.util.ArrayList;

import mwsu.edu.stacked.R;
import mwsu.edu.stacked.FastFood.game.FastFood;
import mwsu.edu.stacked.FastFood.search.Restaurant;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class acts as a layer between the results from the search engine
 * and finding the winning restaurant.  This activity sends the user to
 * the race and once the race is finished, this activity will
 * redirect them to the winner.
 * 
 * @author Zach
 *
 */
public class GUIController extends Activity {

	public ArrayList<Restaurant> restaurants;
	public boolean wildcard;

	@Override
	public void onCreate (Bundle savedInstance) 
	{
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstance);
		setContentView(R.layout.fastfood_intro);
		
		// Force the screen orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// grab results from the search engine
		restaurants = getIntent().getExtras().getParcelableArrayList("restaurants");
		wildcard = getIntent().getExtras().getBoolean("wildcard");
		
		// if there is only one restaurant then indicate there is no need to race
		if(restaurants != null && restaurants.size() == 1) {
			Toast.makeText(this, "Only one restaurant found, press play to view", Toast.LENGTH_LONG).show();
		}
		
		// Show the contestants in the race, label them by number and color code
		// (to match the helmet colors of the racers)
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
		TextView tv;
		
		int[] colors = new int[]{
			Color.MAGENTA,
			Color.BLUE,
			Color.DKGRAY,
			Color.rgb(190, 90, 0),
			Color.YELLOW,
			Color.GREEN
		};
		
		int size = restaurants.size();
		
		// If we're in wildcard mode, don't display the restaurants
		if(wildcard) {
			String text = "? Wildcard Selected ?";
			tv = new TextView(this);
			tv.setText(text);
			tv.setTextColor(colors[3]);
			layout.addView(tv);
		}
		// Otherwise add each restaurant to the layout with their 
		// appropriate color
		else {
			for(int i = 0; i < size; i++) {
				tv = new TextView(this);
				String text = (i + 1) + ". " + restaurants.get(i).name;
				if(text.length() > 24) {
					text = text.substring(0, 18);
					text += "...";
				}
				tv.setText(text);
				tv.setTextSize(18);
				tv.setTextColor(colors[i]);
				layout.addView(tv);
			}
		}
		
	}

	// Handle the buttons
	public void clickHandler(View view) {
		switch(view.getId()) {

		case R.id.play_button :
			
			// Start the race
			if(restaurants.size() > 1) {
				Intent foodRace = new Intent(this, FastFood.class);
				foodRace.putExtra("restaurants", restaurants);
				foodRace.putExtra("wildcard", wildcard);
				startActivityForResult(foodRace, 10);
			}
			
			// One restaurant means no race, skip straight to the winner
			else {
				Intent result = new Intent(this, DisplayWinner.class);
				result.putExtra("winner", restaurants.get(0));
				startActivity(result);
			}
			break;
		case R.id.go_back :
			finish();
			break;
		}
	}
	
	// Get the winner of the race and display them in a new activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == 10) {
			if (data.hasExtra("winner")) {
				Intent result = new Intent(this, DisplayWinner.class);
				result.putExtra("winner", data.getExtras().getParcelable("winner"));
				startActivity(result);
			}
		}
	}
}
