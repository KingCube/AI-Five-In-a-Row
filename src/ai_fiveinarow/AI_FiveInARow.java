
package ai_fiveinarow;

import java.util.HashSet;
import java.util.Set;

/**
 * Main class of program. Initalizes components and players.
 * Sets Human or AI players, and any combination thereof.
 */
public class AI_FiveInARow {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Game game = new Game(15,15);
        UI ui = new UI(game);
        
        //Setup for two AI players ready
        PlayerAI p1 = new PlayerAI(game);
        PlayerAI p2 = new PlayerAI(game);
        //p2.setABpruning(true);
        //p2.setMaxDepth(3);
        
        game.addPlayer(new PlayerHuman(game));
        game.addPlayer(p2);
        
        game.setUI(ui);
        
        game.requestMoves();
        
    }
    
}
