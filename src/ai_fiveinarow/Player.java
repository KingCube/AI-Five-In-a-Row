
package ai_fiveinarow;

import java.awt.Point;

/**
 * Abstract class which contains outlines for what a Participant in the 
 * five-in-a-Row game needs to be able to do.
 */
public abstract class Player {
    
    public Game game;
    
    public Player(Game g){
        game = g;
    }
    
    public abstract Point requestMove(int turn);
    

    
}
