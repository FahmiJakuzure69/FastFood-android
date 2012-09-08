package mwsu.edu.stacked.FastFood.game;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Stores most of the logic for a top-down 2D race.  
 * 
 * @author Zach
 */
public class RaceTrack {
	
	private Bitmap background;
	private Rect firstBG, secondBG;
	private Racer[] racers = new Racer[6];
	private ArrayList<RaceItem> items = new ArrayList<RaceItem>();
	private ArrayList<RaceItem> removalList = new ArrayList<RaceItem>();
	private RaceItem finishLine;
	private float scroll_speed;
	
	// For timing state changes
	private long stateChangeTimer = 0;
	
	// Keeping track of the state 
	private TrackState trackState;
	
	final private int BG_SCROLL_SPEED = 10;
	private static float EDGE_THRESHOLD;
	
	// Screen metrics
	private int display_height;
	
	public RaceTrack() {
		scroll_speed = 0;
	}
	
	/**
	 * Initialize the race track
	 * 
	 * @param backDrop - Background image to be used
	 * @param racers - The racers in the race
	 * @param items - Items used in the race
	 * @param width - Width of the screen
	 * @param height - Height of the screen
	 */
	public void Initialize(Bitmap backDrop, Racer[] racers, ArrayList<RaceItem> items, int width, int height) {
		this.background = backDrop;
		firstBG = new Rect(0, 0, width, height);
		secondBG = new Rect(0, -height, width, height);
		
		this.racers = racers;
		this.items = items;
		
		finishLine = items.get(items.size() - 1);
		
		display_height = height;
				
		EDGE_THRESHOLD = height / 3;
		
		// Start on your mark so the racers don't just shoot off
		trackState = TrackState.PREPARATION;
	}
	
	/**
	 * Update the race track based on the current state of the race
	 * 
	 * @param gameTime - Game timer
	 */
	public void Update(long gameTime) {
		
		switch(trackState) {
		case PREPARATION :	// Just stay still and update the timer
			if(stateChangeTimer == 0) stateChangeTimer = gameTime + 2500;
			if(gameTime >= stateChangeTimer){
				trackState = TrackState.RACING;
			}
			break;
		case RACING :	// Now start racing
			UpdateRacers(gameTime);
			
			// Update each item
			for(RaceItem item : items) {
				item.Update(gameTime);
				
				// If the item is on screen, check for collisions
				if(item.getYPos() > 0)
					checkCollision(item, gameTime);
			}
			items.removeAll(removalList);
			removalList.clear();
			break;
		case FINISH :
			break;
		default :
			
		}
		
	}
	
	/**
	 * Check if this item is touching any of the racers,
	 * activate it if it is
	 * 
	 * @param item - item being checked against
	 */
	private void checkCollision(RaceItem item, long gameTime) {
		for(Racer racer : racers) {
			if(Rect.intersects(item.getRect(), racer.getRect())) {
				item.execute(racer, gameTime);
				removalList.add(item); 
			}
		}
	}
	
	/**
	 * Determines the winner of the race, boosts slow racers that are 
	 * about to fall of the screen and sets counteractive scroll speeds
	 * to keep racers from driving into the top of the screen
	 * 
	 * @param gameTime
	 */
	public void UpdateRacers(long gameTime) {
		
		float winningPos = Float.MAX_VALUE;
		int winningSpot = racers.length + 1;
		
		// Find out who is winning
		for(int i = 0; i < racers.length; i++) {
    		racers[i].Update(gameTime);
    		
    		if(racers[i].isWinner()) {
    			trackState = TrackState.FINISH;
    			return;
    		}
    		if(racers[i].getYPos() < winningPos) {
    			winningPos = racers[i].getYPos();
    			winningSpot = i;
    		}
    	}
		
		// Racers who are in danger of falling off the screen get a boost
		for(Racer racer : racers) {
			
			// Check if they pass a constant threshold value
			if(AtEdge(racer)) {
				
				// Racers who are close to the edge get a boost equal to 1 higher than the winning racer 
    			float boost = racers[winningSpot].getFowardSpeed() - racer.getFowardSpeed();
    			boost += 1;
    			
    			// Boost them for 2500 Frames and move them up so they don't fall off the screen
    			racer.Boost(boost, 2500, gameTime);
    			racer.setYPos(display_height - (EDGE_THRESHOLD + racer.getHeight()));
    		}
		}
		
		// If a racer is going off the top of the screen
		if(winningPos < EDGE_THRESHOLD) {
			
			// Start cascading the background in the opposite direction at the speed of the winner
			scroll_speed = racers[winningSpot].getFowardSpeed();
			
			// Slow everyone else down while the background is moving
			for(Racer racer : racers) {
				racer.setBackwardSpeed(scroll_speed);
				racer.setYPos(racer.getYPos() + (EDGE_THRESHOLD - winningPos) - 1);
			}
		}
		// If for some reason no one can keep up with the scroll speed
		else if(racers[winningSpot].getFowardSpeed() < scroll_speed) {
			
			// Stop the cascading
			scroll_speed = 0;
			
			// Bring everyone back to their original velocities
			for(Racer racer : racers) {
				racer.setBackwardSpeed(scroll_speed);
			}
		}
			
	}
	/**
	 * Draw the events as they are happening in the race
	 * 
	 * @param canvas - canvas of the panel being drawn on
	 */
	public void Draw(Canvas canvas) {
		
		// Draw the background, first image is the background touching the bottom
		// Second is the background touching the top
		canvas.drawBitmap(background, null, firstBG, null);
		canvas.drawBitmap(background, null, secondBG, null);
		
		switch(trackState) {
		
		// Move the background if the racers are racing
		case RACING :
			firstBG.top += BG_SCROLL_SPEED;
			firstBG.bottom += BG_SCROLL_SPEED;
			secondBG.top += BG_SCROLL_SPEED;
			secondBG.bottom += BG_SCROLL_SPEED;
			
			// If one rectangle has scrolled completely, move it to the top
			if(firstBG.top >= display_height) {
				firstBG.top = -display_height;
				firstBG.bottom = 0;
			}
			if(secondBG.top >= display_height) {
				secondBG.top = -display_height;
				secondBG.bottom = 0;
			}
			break;
		
		}
		
		finishLine.draw(canvas);
		
		// Draw the racers
		for(Racer racer : racers) {
    		racer.draw(canvas);
    	}
		
		// Draw the objects
		for(RaceItem item : items) {
			if(item.getYPos() > 0)
				item.draw(canvas);
		}
	}
	
	/**
	 * Return true if an object or racer is nearing the edge of the screen
	 * 
	 * @param object - object testing against
	 * @return
	 */
	private boolean AtEdge(AnimatedSprite object) {
		if (object.getYPos() > display_height - (EDGE_THRESHOLD + object.getHeight()))
			return true;
		else
			return false;
	}
	
	public TrackState getState() {
		return trackState;
	}
	
	public enum TrackState {
		PREPARATION, RACING, FINISH
	}
}
