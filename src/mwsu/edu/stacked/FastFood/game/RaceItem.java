package mwsu.edu.stacked.FastFood.game;

/**
 * An item that affects the racer it touches
 * 
 * @author Zach
 */
public class RaceItem extends AnimatedSprite {
	
	// Stores how the item acts when activated
	private Behaviour behaviour;

	private float scroll_speed = 6;
	
	public RaceItem() {
		super();
	}

	// Simply scroll the item downward
	@Override
	public void Update(long gameTime) {
		super.Update(gameTime);
		setYPos(getYPos() + scroll_speed);
	}
	
	public void setBehaviour(Behaviour action) {
		behaviour = action;
	}
	
	// Activate the item
	public void execute(Racer racer, long gameTime) {
		behaviour.action(racer, gameTime);
	}
}
