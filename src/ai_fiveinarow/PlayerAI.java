
package ai_fiveinarow;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implemetation of the abstract class Player, such that a AI can control it.
 */
public class PlayerAI extends Player {

    int Nx;
    int Ny;
    Point[] points;
    int maxDepth = 2; //minimum of 2 to get any of the algorithms to work;
    int adjRadius = 1;
    int maxTurns;
    boolean useABpruning = false;
    
    List<Integer> breakScores = new ArrayList<Integer>();
    
    /**
     * Constructor of the class. Mainly collects some dimensional info from the
     * supplied game. 
     * @param game game to which the AI should relate
     */
    public PlayerAI(Game game) {
        super(game);
        Nx = game.gameState[0].length;
        Ny = game.gameState[0][0].length;
        maxTurns = Nx*Ny;
        
        points = new Point[Nx * Ny];

        for (int j = 0; j < Ny; j++) {
            for (int i = 0; i < Nx; i++) {
                points[j * Nx + i] = new Point(i, j);
            }
        }
        
        breakScores.add(game.winScore[0]);
        breakScores.add(game.winScore[1]);
    }

    /**
     * Function for enabling/disabling ABpruning
     * @param bool 
     */
    public void setABpruning(boolean bool){
        useABpruning = bool;
    }
    
    /**
     * Function for externally changing the maxDepth
     * @param depth desired maxDepth
     */
    public void setMaxDepth(int depth){
        if(depth < 2)
            System.out.println("Warning, depth of " + depth + "will result in a very dumb AI");
        else if(depth < 0){
            System.out.println("Illegal depth set");
            return;
        }
            
        maxDepth = depth;
    }
    
    /**
     * This function provides the main game with a desired move. 
     * The function will use trivial moves for turns <2, but otherwise call a 
     * recursive funtion.
     * @param turn is the turn we are currently working with
     * @return a Point with the coordinate for the move that the Game should carry out
     */
    public Point requestMove(int turn) {
        System.out.println("AI starts, at turn " + turn);
        if (turn == 0) {
            return new Point(Nx / 2, Ny / 2);
        } else if (turn == 1) {
            Point pFirst = null;
            for (Point p : points) {
                //System.out.println("DEBUG: Scanning point " + p.x + "," + p.y);
                if (game.gameState[turn][p.x][p.y] == game.markers[0]) {
                    pFirst = p;
                    break;
                }
            }

            List<Point> neighbors = new ArrayList<Point>();

            for (int i = 0; i < 9; i++) {
                if (i == 4) {
                    continue;
                }

                int newX = pFirst.x - 1 + i % 3;
                int newY = pFirst.y - 1 + i / 3;

                if (newX >= 0 && newX < Nx && newY >= 0 && newY < Ny) {
                    neighbors.add(new Point(newX, newY));
                }
            }

            int rndIndex = (int) (Math.random() * neighbors.size());

            return neighbors.get(rndIndex);
        } else {
            //int moveIndex = evaluateMoves(turn, 0);
            int moveIndex = useABpruning ? evaluateMovesABpruning(turn,0) : evaluateMoves(turn,0);
            return points[moveIndex];
        }

    }
    
    /**
     * This function searches for best scenario using min-max and some additional logic for specialcases
     * @param turn current turn we are working with
     * @param depth current simulation depth
     * @return returns best possible scores if depth >0, otherwise the index for the best move
     */
    public int evaluateMoves(int turn, int depth) {

        //Refresh scores, likely really only needed in depth = 0
        int temp = game.scoring(turn);

        if(game.foundInARow[(turn+1)%2][3] > 0 && game.foundInARow[(turn)%2][3] == 0){
            //Check if game is already lost upon entering this scenario
            if(depth == 0){
                for (int i = 0; i < points.length; i++)
                    if(game.makeMove(turn, points[i], false))
                        return i;
            }   
            else
                return game.winScore[(turn+1)%2];
        }
        
        int bestIndex = -1;
        boolean startingPlayer = turn% 2 == 0;
        int extremeScore = 0;
        
        //boolean noFours = game.foundInARow[(turn)%2][3] == 0;
        
        for (int i = 0; i < points.length; i++) {
            boolean possible =  game.Adjacency(turn,points[i], adjRadius) && 
                                game.makeMove(turn, points[i], false); //note that this makes the move
            
            if (possible) {
                int initialScore = game.scoring(turn+1);
       
                //if scenario = win, just assume that is going to be final pick
                if(breakScores.contains(initialScore))
                    return depth == 0 ? i : initialScore;
                //otherwise, a bit more steps
                else{
                    int score;
                    //if we are not at the bottom, we need to dig deeper
                    if(depth != maxDepth)
                        score = evaluateMoves(turn+1,depth+1);
                    //If we are at the bottom, most likely we want the initialScore
                    //however, added extra check to see if we can infer a win.
                    else{
                        if(game.foundInARow[(turn)%2][3] > 0)
                            score = game.winScore[(turn)%2] +100*(-1 + 2*(turn%2));
                        else
                            score = initialScore;
                    }
                                           
                    //DEBUG: if(depth == 0) System.out.println(points[i].x +"," + points[i].y + ":" + score);
                    
                    //See if this observation is more extreme than others observed
                    if(bestIndex == -1 || (startingPlayer && score > extremeScore) || (!startingPlayer && score < extremeScore)){
                        bestIndex = i;
                        extremeScore = score;
                    }
                }
            }
            
        }
        
        //Return most extreme index/observation
        if (depth == 0) 
            return bestIndex;
        else 
            return extremeScore;

    }

    /**
     * This function searches for best scenario using min-max and AB-pruning.
     * @param turn current turn we are working with
     * @param depth current simulation depth
     * @return returns best possible scores if depth >0, otherwise the index for the best move
     */
    public int evaluateMovesABpruning(int turn, int depth) {
        //set up lists we are going to need
        List<int[]> shallowScores = new ArrayList<>();
        List<int[]> deepScores = new ArrayList<>();

        //get scores for all possible situations just one move away
        for (int i = 0; i < points.length; i++) {
            boolean possible = game.Adjacency(turn, points[i], adjRadius) && game.makeMove(turn, points[i], false);
            if (possible) {
                shallowScores.add(new int[]{i, game.scoring(turn + 1)});
            }
        }
        
        //sort the possible scores 
        if(turn%2 == 0)
            shallowScores.sort(Comparator.comparing(e -> -e[1]));
        else
            shallowScores.sort(Comparator.comparing(e -> e[1]));
        
        //if we are at deepest level, OR, already found a win, return best move
        if(depth >= maxDepth || turn == maxTurns -1 || Math.abs(shallowScores.get(0)[1]) == game.winScore[0])
            return depth == 0 ? shallowScores.get(0)[0] : shallowScores.get(0)[1];
         
        //Otherwise, dig deeper by calling own function recursively
        for (int i = 0; i < shallowScores.size()/2; i++) {
            game.makeMove(turn, points[shallowScores.get(i)[0]], false);
            deepScores.add(new int[]{shallowScores.get(i)[0],evaluateMovesABpruning(turn+1,depth+1)});
        }
        
        //sort the scores we are able to obtain by digging deeper
        if(turn%2 == 0)
            deepScores.sort(Comparator.comparing(e -> -e[1]));
        else
            deepScores.sort(Comparator.comparing(e -> e[1]));
        
        //return either best index or best score for this level
        return depth == 0 ? deepScores.get(0)[0] : deepScores.get(0)[1];

    }
    
}
