
package ai_fiveinarow;

import java.awt.Point;
import java.util.Scanner;

/**
 * Implemetation of the abstract class Player, such that a human can control it.
 */
public class PlayerHuman extends Player{
    
    Scanner scanner = new Scanner(System.in);
    
    public PlayerHuman(Game game){
        super(game);
    }
    
    /**
     * Function for using the console to ask the player for a move.
     * Some error-cathing included, but not for entering things other than ints.
     * @param turn turn we are currently suppling a move for.
     * @return a point with the move the human desires.
     */
    public Point requestMove(int turn){
        System.out.println(
                    "Please make a move. Insert the x- and y- coordinates\n" +
                    "use numbers and a comma (,) between them"
                    );
        
        String input = scanner.nextLine();
        String[] splitInput = input.split(",");
        
        int x = Integer.parseInt(splitInput[0]);
        int y = Integer.parseInt(splitInput[1]);
        
        return new Point(x,y);
    }
    
}
