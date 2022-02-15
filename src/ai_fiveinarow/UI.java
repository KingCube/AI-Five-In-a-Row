
package ai_fiveinarow;

import java.awt.Point;
import java.util.Scanner;

/**
 * Class with main responsibility for communicating the status of a Game-class.
 */
public class UI {
    Game game;

    /**
     * Main constructor sets the game, and prints an inital greeting.
     * @param game the game to represent graphically.
     */
    public UI(Game game){
        this.game = game;
        System.out.println("********** Welcome to 5 in a Row **********");
    }
        
    /**
     * Function for printing the gamestate, such that a human can see it
     * @param turn int with the turnnumber to print from.
     */
    public void printGameState(int turn){
        int x = game.gameState[turn].length;
        int y = game.gameState[turn][0].length;
        
        int helper1;
        int helper2;
        
        System.out.println("Game is now at turn " + turn + ", and board is as follows: \n");
        System.out.print("   ");
        
        String strBlanks = "";
        helper1 = x;

        while(true){
            helper1/=10;
            if(helper1 == 0)
                break;
            else
                strBlanks += " ";
        }
        System.out.print(strBlanks);

        
        for(int i = 0; i < x; i++){
            helper1 = x;
            helper2 = i;
            String strZeros = "";
            
            while(true){
                helper1/=10;
                helper2/=10;
                
                if(helper1 == 0 && helper2 == 0)
                    break;
                else if(helper2 == 0)
                    strZeros += "0";
            }
            
            
            System.out.print("|" + strZeros + i);
            
        }
        
        
        System.out.print("|\n");
            
        for(int j = 0; j < y; j++){
            
            helper1 = y;
            helper2 = j;
            while(true){
                helper1/=10;
                helper2/=10;
                
                if(helper1 == 0 && helper2 == 0)
                    break;
                else if(helper2 == 0)
                    System.out.print("0");
            }
                
            
            System.out.print(j + ": |");
            
            
            for(int i = 0; i < x; i++){
               if(strBlanks.length()> 0)
                    System.out.print(strBlanks.substring(0, strBlanks.length()/2) + game.gameState[turn][i][j] + strBlanks.substring(strBlanks.length()/2, strBlanks.length()) + "|");
               else
                   System.out.print(game.gameState[turn][i][j] + "|");
            }
                
            System.out.print("\n");
        }
    }
    
}
