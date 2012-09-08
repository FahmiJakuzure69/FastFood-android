package mwsu.edu.stacked.FastFood.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import mwsu.edu.stacked.R;
import mwsu.edu.stacked.FastFood.game.RaceTrack.TrackState;
import mwsu.edu.stacked.FastFood.search.Restaurant;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

/**
 * Holder activity for the race
 * 
 * @author Zach
 */
public class FastFood extends Activity {
	
	private int numRacers;
	
	private Racer[] racers;
	private RaceTrack raceTrack = new RaceTrack();
	private ArrayList<Restaurant> restaurants;
	private boolean wildcard;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	 
    	restaurants = getIntent().getExtras().getParcelableArrayList("restaurants");
    	wildcard = getIntent().getExtras().getBoolean("wildcard");
    	
    	numRacers = restaurants.size();
    	racers = new Racer[numRacers];
    	
    	// Initalize racers at random speeds TODO: interface this with Search Engine
    	for(int i = 0; i < numRacers; i++) {
    		racers[i] = new Racer();
    	}
    	
    	// Turn off the title bar
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
        super.onCreate(savedInstanceState);       
        setContentView(new FastFoodSurface(this));
        
    }
    
    @Override
    public void finish() {
    	Intent data = new Intent();
    
    	if(raceTrack.getState() == TrackState.FINISH) {
    		int pos = numRacers + 1;
    		for(int i = 0; i < numRacers; i++) {
    			if(racers[i].isWinner())
    				pos = i;
    		}
    		if(pos < numRacers + 1) {
    			data.putExtra("winner", restaurants.get(pos));	
    			setResult(RESULT_OK, data);    			
    		}
    	} 
    	super.finish();
    }
    
    /**
     * Tell the app to finish when it pauses so it doesn't crash when the
     * user gets a phone call or presses the home button during the race
     */
    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
    
    /**
     * A racing mini-game.  Operates using a framework commonly used
     * in game development.  
     * 
     * @author Zach
     *
     */
    class FastFoodSurface extends DrawablePanel {

    	private int width;
    	private int height;
    			
		private HashMap<String, Bitmap> resources;
		
		private AnimatedSprite trafficLight;
		    	
		public FastFoodSurface(Context context) {
			super(context);
		}

		/**
		 * Initializes any objects that are used for the race
		 */
		@Override
		public void onInitalize() {
			
			resources = new HashMap<String, Bitmap>();
			
			// Grab images for the race 
			allocateResources();
			
			// Traffic Light
			trafficLight = new AnimatedSprite();
			trafficLight.Initialize(resources.get("traffic_light"), 2, .6, 3);
			trafficLight.setXPos(30);
			trafficLight.setYPos(60);
			
			// Get the metrics of the screen
			Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			
			width = display.getWidth();
			height = display.getHeight(); 
			
			initializeRacers();
			
			ArrayList<RaceItem> items = initializeItems();
			
			// Initialize the race track
			raceTrack.Initialize(resources.get("asphalt_bg"), racers, items, width, height);
		}
		
		private void initializeRacers() {
			
			int xPos = 0;
			Rect bounds = new Rect(0, 0, width, height);
			
			float shift = (width - 10) - (((resources.get("racer1").getWidth() / 2)) * 1.4f * numRacers);
			shift /= 2;
			
			Bitmap rating;
			float speed;
			
			// Initialize the racers to be in one horizontal line beside each other
			for(int i = 0; i < numRacers; i++) {
	    		
	    		if(wildcard) {
	    			speed = 3f + (float) Math.random();
		    		racers[i].Initialize(resources.get("racer" + (i + 1)), null, 1.4f, 15, 2, speed, bounds);
	    		}
	    		else {
	    			rating = resources.get("rating" + (int) Math.ceil(restaurants.get(i).avg_rating));
	    			racers[i].Initialize(resources.get("racer" + (i + 1)), rating, 1.4f, 15, 2, restaurants.get(i).avg_rating, bounds);
	    		}
	    		racers[i].setXPos(xPos + shift);
	    		racers[i].setYPos(height / 2);
	    		xPos += racers[i].getWidth();
	    	}
		}

		private ArrayList<RaceItem> initializeItems() {
			
			int xPos = 0;
			
			float shift = (width - 10) - (racers[0].getWidth() * numRacers);
			shift /= 2;
			
			Random rand = new Random();
			int rand_index;
			
			ArrayList<RaceItem> items = new ArrayList<RaceItem>();
			
			Behaviour[] behaviours = new Behaviour[] {
					new BananaBehaviour(),
					new BombBehaviour(),
					new StarBehaviour(),
			};
			
			String[] itemImgs = new String[]{"banana_peel", "bomb", "star"};
			
			// Randomly generate a list of items used in the race
			for(int i = 0; i < 5 * numRacers; i++) {
				xPos = (int) (shift + rand.nextInt(numRacers) * racers[0].getWidth());
				xPos += racers[0].getWidth() / 4;
				RaceItem temp = new RaceItem();
				rand_index = rand.nextInt(3);
				temp.setBehaviour(behaviours[rand_index]);
				temp.Initialize(resources.get(itemImgs[rand_index]), 1, 1, 1);
				temp.setXPos(xPos);
				temp.setYPos(-rand.nextInt(900));
				items.add(temp);
			}
			
			RaceItem finishLine = new RaceItem();
			finishLine.Initialize(resources.get("finish"), 1, 1, 1);
			finishLine.setYPos(-1000);
			finishLine.setXPos(-100);
			finishLine.setBehaviour(new FinishLine());
			
			items.add(finishLine);
			
			return items;
		}
		/**
		 * Places all the system resources in a hashmap for easy
		 * maintainability.
		 */
		private void allocateResources() {
			
			// Racer images
			resources.put("racer1", BitmapFactory.decodeResource(getResources(), R.drawable.racer1));
			resources.put("racer2", BitmapFactory.decodeResource(getResources(), R.drawable.racer2));
			resources.put("racer3", BitmapFactory.decodeResource(getResources(), R.drawable.racer3));
			resources.put("racer4", BitmapFactory.decodeResource(getResources(), R.drawable.racer4));
			resources.put("racer5", BitmapFactory.decodeResource(getResources(), R.drawable.racer5));
			resources.put("racer6", BitmapFactory.decodeResource(getResources(), R.drawable.racer6));
			
			// Different rating imgs
			resources.put("rating1", BitmapFactory.decodeResource(getResources(), R.drawable.rating1));
			resources.put("rating2", BitmapFactory.decodeResource(getResources(), R.drawable.rating2));
			resources.put("rating3", BitmapFactory.decodeResource(getResources(), R.drawable.rating3));
			resources.put("rating4", BitmapFactory.decodeResource(getResources(), R.drawable.rating4));
			resources.put("rating5", BitmapFactory.decodeResource(getResources(), R.drawable.rating5));
			
			// On your mark, ready, go images
			resources.put("on_your_mark", BitmapFactory.decodeResource(getResources(), R.drawable.mark));
			resources.put("ready", BitmapFactory.decodeResource(getResources(), R.drawable.ready));
			resources.put("go", BitmapFactory.decodeResource(getResources(), R.drawable.go));
			
			// Images used for the items
			resources.put("banana_peel", BitmapFactory.decodeResource(getResources(), R.drawable.banana));
			resources.put("bomb", BitmapFactory.decodeResource(getResources(), R.drawable.bomb));
			resources.put("star", BitmapFactory.decodeResource(getResources(), R.drawable.star));
			
			// Background image and finish line
			resources.put("asphalt_bg", BitmapFactory.decodeResource(getResources(), R.drawable.track));
			resources.put("finish", BitmapFactory.decodeResource(getResources(), R.drawable.finish));
			
			// Traffic light
			resources.put("traffic_light", BitmapFactory.decodeResource(getResources(), R.drawable.traffic_light));
		}
		
		/**
		 * Game logic gets updated, computations and the like go here.
		 */
		@Override
		public void onUpdate(long gameTime) {
			
			// Update the race track
			raceTrack.Update(gameTime);
			trafficLight.Update(gameTime);
			
			if(raceTrack.getState() == TrackState.FINISH) {
				finish();
			}
			
		}
		
		/**
		 * Draws everything to the screen
		 */
		@Override
		public void onDraw(Canvas canvas) {
			
			// Clear the last frame
			canvas.drawColor(Color.GRAY);
			
			// Race track should be drawn first, then everything else on top of it
			raceTrack.Draw(canvas);
			
			if(raceTrack.getState() == TrackState.PREPARATION) {
				trafficLight.draw(canvas);
			}
		}
    	
    }
}