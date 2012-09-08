package mwsu.edu.stacked.FastFood.game;

/**
 * Allows different items to have different behaviours
 * @author Zach
 *
 */
public interface Behaviour {
	public void action(Racer racer, long gameTime);
}

/**
 * Behaviour for a banana peel
 * 
 * @author Zach
 *
 */
class BananaBehaviour implements Behaviour {

	@Override
	public void action(Racer racer, long gameTime) {
		racer.Boost(-3.0f, 500, gameTime);
	}

}

/**
 * Behaviour for a bomb
 * 
 * @author Zach
 *
 */
class BombBehaviour implements Behaviour {

	@Override
	public void action(Racer racer, long gameTime) {
		racer.Boost(-4.0f, 1000, gameTime);
	}

}

/**
 * Behaviour for a star, which gives a racer a speed boost
 * 
 * @author Zach
 *
 */
class StarBehaviour implements Behaviour {

	@Override
	public void action(Racer racer, long gameTime) {
		float boost = racer.getBoost();
		if(boost > 0) {
			boost += 5.0f;
		}
		else {
			boost = 5.0f;
		}
		
		racer.Boost(boost, 1000, gameTime);
	}

}

/**
 * The finish line, which indicates the end of a race
 * 
 * @author Zach
 *
 */
class FinishLine implements Behaviour {

	@Override
	public void action(Racer racer, long gameTime) {
		racer.flagWinner();
	}
	
}