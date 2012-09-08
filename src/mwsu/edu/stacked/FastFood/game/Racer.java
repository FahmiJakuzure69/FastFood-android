package mwsu.edu.stacked.FastFood.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Racer extends AnimatedSprite {

	private Bitmap ratingImg;
	private float forwardSpeed; 
	private float backwardSpeed;
	private float boost;
	private long boostTimer;
	private boolean isBoosting;
	private boolean isWinner;
	private Rect bounds;
	
	// Pass the speed here so you don't have to overload the initialize function 
	public Racer() {
		super();
		this.forwardSpeed = 0;
		backwardSpeed = 0;
		boost = 0;
		boostTimer = 0;
		isBoosting = false;
	}
	
	public void Initialize(Bitmap bitmap, Bitmap rating, float scale, int fps, int frameCount, float speed, Rect bounds) {
		super.Initialize(bitmap, scale, fps, frameCount);
		this.forwardSpeed = speed;
		this.bounds = bounds;
		this.ratingImg = rating;
	}
	
	@Override
	public void Update(long gametime) {
		super.Update(gametime);
		
		float newPos = getYPos() - (forwardSpeed - backwardSpeed) - boost;
		
		// Move the racer forward if that doesn't mean going off the screen
		if(bounds.contains((int) getXPos(), (int) newPos)) {
			setYPos((getYPos() - (forwardSpeed - backwardSpeed) - boost));
		}
		
		// Check the boost timer against the current game timer
		if(isBoosting && gametime > boostTimer) {
			isBoosting = false;	// Remove the boost
			boost = 0;
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
		// have to make sure the rating img takes up about 10% of the racers rectangle
		Rect b = getRect();
		
		int x = (int) (getXPos() + getWidth() * .1f), y = (int) getYPos() + getHeight();
		Rect rateBox = new Rect(x, y,  x + (int) (b.width() *.9f), y + (int) (b.height() * .2f));
		
		if(ratingImg != null) {
			canvas.drawBitmap(ratingImg, null, rateBox, null);
		}
	}
	
	/**
	 * Boost the racer for a limited duration of time.  
	 * Boosts are allowed to have a negative speed (to slow the racer down)
	 * 
	 * @param boost - amount of speed gain
	 * @param Duration - duration of the boost
	 * @param gameTime - current game timer
	 */
	public void Boost(float boost, int Duration, long gameTime) {
		this.boost = boost;
		boostTimer = gameTime + Duration;
		
		isBoosting = true;
	}
	
	public float getFowardSpeed() {
		return forwardSpeed;
	}
	
	public float getBackwardSpeed() {
		return backwardSpeed;
	}
	
	public float getBoost() {
		return boost;
	}
	
	public void setForwardSpeed(float speed) {
		forwardSpeed = speed;
	}
	
	public void setBackwardSpeed(float speed) {
		backwardSpeed = speed;
	}
	
	public void flagWinner() {
		isWinner = true;
	}
	
	public boolean isWinner() {
		return isWinner;
	}
	
	public boolean isBoosting() {
		return isBoosting;
	}
}
