package mwsu.edu.stacked.FastFood.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Simple animated sprite that works using a horizontal strip of images
 * 
 * @author Zach
 */
public class AnimatedSprite {
	private Bitmap animation;
	private float xPos;
	private float yPos;
	private Rect sRectangle;
	private int fps;
	private int numFrames;
	private int currentFrame;
	private long frameTimer;
	private int spriteHeight;
	private int spriteWidth;
	private float scale;
	
	public AnimatedSprite() {
		sRectangle = new Rect(0, 0, 0, 0);
		frameTimer = 0;
		currentFrame = 0;
		xPos = 80;
		yPos = 200;
		scale = 1;
	}
	
	/**
	 * Initialize the sprite, determines width as image width / framecount
	 * so don't enter 0 as a frame count TODO: fix that
	 * 
	 * @param bitmap - horizontal strip image of the animation
	 * @param scale - amount to scale the sprite by
	 * @param fps - speed at which to animate in frames per second
	 * @param frameCount - Number of frames in the sprite
	 */
	public void Initialize(Bitmap bitmap, float scale, double fps, int frameCount) {
		this.animation = bitmap;
		this.spriteHeight = bitmap.getHeight();
		this.spriteWidth = bitmap.getWidth() / frameCount;
		this.sRectangle.top = 0;
		this.sRectangle.bottom = spriteHeight;
		this.sRectangle.left = 0;
		this.sRectangle.right = spriteWidth;
		this.fps = (int) (1000 / fps);
		this.numFrames = frameCount;
		this.scale = scale;
	}
	
	public float getXPos() {
		return xPos;
	}
	
	public float getYPos() {
		return yPos;
	}
	
	public int getHeight() {
		return (int) (spriteHeight * scale);
	}
	
	public int getWidth() {
		return (int) (spriteWidth * scale);
	}
	
	public Rect getRect() {
		return new Rect((int)getXPos(), (int)getYPos(), (int)(getXPos() + spriteWidth * scale),
				(int)(getYPos() + spriteHeight * scale));
	}
	
	public void setXPos(float value) {
		xPos = value;
	}
	
	public void setYPos(float value) {
		yPos = value;
	}
	
	/**
	 * Update the animated image
	 * @param gameTime - game timer
	 */
	public void Update(long gameTime) {
		
		// Check if it's time to move to the next frame
		if( gameTime > frameTimer + fps) {
			frameTimer = gameTime;
			currentFrame += 1;
			
			// Go back to the starting frame when over the frame count
			if( currentFrame >= numFrames ) {
				currentFrame = 0;
			}
			
			// Shift the source rectangle to the next frame of the image
			sRectangle.left = currentFrame * spriteWidth;
			sRectangle.right = sRectangle.left + spriteWidth;
		}
	}
	
	/**
	 * Draw it
	 * @param canvas - canvas of the panel being drawn to
	 */
	public void draw(Canvas canvas) {
		
		// Create the rectangle representing the scaled size and location of the sprite
		Rect dest = new Rect((int)getXPos(), (int)getYPos(), (int)(getXPos() + spriteWidth * scale),
										(int)(getYPos() + spriteHeight * scale));
		canvas.drawBitmap(animation, sRectangle, dest, null);
	}
}
