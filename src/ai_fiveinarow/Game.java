
package ai_fiveinarow;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Main class for running the game. Handles turn order, and knows how to evaluate 
 * a board state.
 */
public class Game {
    public int currentTurn;
    public char[] markers = new char[]{'x','o'};
    
    public int[][] foundInARow = new int[2][5];
    public int[][] foundInARowPartial = new int[2][5];
    
    public Player[] players = new Player[2];

    char[][][] gameState;
    int maxTurns;
    int Nx;
    int Ny;
    UI ui;
    PlayerAI ai;
    
    public int winScore[]  = new int[]{(int)Math.pow(10,7), (int)-Math.pow(10,7)};
    public int winScoreOneOff[] = new int[]{winScore[0]/10-10,winScore[1]/10+10};
    public int winScoreTwoOff[] = new int[]{winScore[0]/10-20,winScore[1]/10+20};
    int[] pointValues = new int[4];

    Map<Character, Integer> markerToPlayer = new HashMap<>();
    
    /**
     * Constructor for the class sets up dimensions of board, and initalizes
     * some of the help variables.
     * @param Nx
     * @param Ny 
     */
    public Game(int Nx,int Ny){
        this.Nx = Nx;
        this.Ny = Ny;
        maxTurns = Nx*Ny;
        gameState = new char[maxTurns+1][Nx][Ny];
        currentTurn = 0;
        
        markerToPlayer.put('x', 0);
        markerToPlayer.put('o', 1);
        
        for(int i = 0; i < pointValues.length; i++)
            pointValues[i] = (int)Math.pow(10, i);

        InitializeGameState();
    }
    
    /**
     * Sets an UI through which the game communicates.
     * @param ui the UI tommunicate through
     */
    public void setUI(UI ui){
        this.ui = ui;
    }
    
    /**
     * Adds a instance of a Player-object to the game.
     * Note that the function does nothing if attempting to add third player.
     * @param p is a Player to add to the game.
     */
    public void addPlayer(Player p){
        for(int i = 0; i < players.length; i++){
            if(players[i] == null){
                System.out.println("entered");
                players[i] = p;
                break;
            }
        }
    }

    /**
     * Starts the game and alternates between the players asking for moves.
     * Also responsinble for checking if someone won.
     */
    public void requestMoves(){    
        Point cP;
        
        while(true){
            ui.printGameState(currentTurn);
            int cScore = scoring(currentTurn);
            System.out.println("DEBUG: Score is: " + cScore);
            
            //check if someone won.
            if(cScore == winScore[0] || cScore == winScore[1]){
                if(cScore == winScore[0])
                    System.out.println("Player 1 wins!");
                else 
                    System.out.println("Player 2 wins!");
                
                break;
            }
            
            //check if board is full
            if(currentTurn == maxTurns){
                System.out.println("Nobody wins! Fools!");
                break;
            }
            
            //otherwise, alternate beween the players.
            cP = players[currentTurn%2].requestMove(currentTurn);
            makeMove(currentTurn, cP, true);
        }
        
    }
    
    /**
     * Help function to fill game with blanks.
     */
    void InitializeGameState(){
        for(int i = 0; i < Nx; i++)
            for(int j = 0; j<Ny; j++)
                gameState[0][i][j] = ' ';
    }
    
    /**
     * Function that carries out a supplied move, if possible. 
     * @param originTurn int with turnnumber from which the move originates
     * @param target Point with coordinates desired square to fill in
     * @param realMove boolean if move is part of simulation or not.
     * @return boolean if move was a possible move to make
     */
    public boolean makeMove(int originTurn, Point target, boolean realMove){
        if(gameState[originTurn][target.x][target.y] != ' ')
            return false;
        
        //copy gamestate
        copyGameState(originTurn);
        
        //place marker in newly copied state
        gameState[originTurn+1][target.x][target.y] = markers[originTurn%2];
        
        //increase turncounter if this was a real move.
        if(realMove){
            currentTurn = originTurn + 1;
        }
        
        return true;
    }
    
    /**
     * Helpfunction for copying a gamestate
     * @param originTurn turn from which to copy, to originTurn +1
     */
    void copyGameState(int originTurn){
        for(int i = 0; i < Nx; i++)
            for(int j = 0; j<Ny; j++)
                gameState[originTurn+1][i][j] = gameState[originTurn][i][j];
    }
    
    /**
     * Function for checking if a point is adjacent to a previous move.
     * @param turn int with current turnnumber
     * @param target Point to check around
     * @param distance Radius around point to check
     * @return boolean if any other move was found
     */
    public boolean Adjacency(int turn,Point target, int distance){
        for(int i = target.x - distance; i <= target.x + distance; i++)
            for(int j = target.y - distance; j <= target.y + distance; j++)
                if(i >=0 && i < Nx && j>= 0 && j < Ny && gameState[turn][i][j] != ' ')
                    return true;
        
        return false;
    }
    
    /**
     * Main function for scoring, also fills in global variables with info 
     * on the current gamestate.
     * @param turn int with turnnumber to evaluate.
     * @return int with the current numerical value
     */
    public int scoring(int turn){
        //reset global variables carrying info
       for(int i = 0; i< foundInARow[0].length; i++){
           foundInARow[0][i] = 0;
           foundInARowPartial[0][i] = 0;
           foundInARow[1][i] = 0;
           foundInARowPartial[1][i] = 0;
       }
        
       int score = 0;
       
       //loop through in all direction that one can get points.
       scoreLoopThroughCardinal(turn, true);
       scoreLoopThroughCardinal(turn, false);
       scoreLoopThroughDiagonal(turn, true);
       scoreLoopThroughDiagonal(turn, false);
       
       //if some 5 in a row was found, just return that as winner.
       if(foundInARow[0][4] > 0 || foundInARowPartial[0][4] > 0)
           return winScore[0];
       if(foundInARow[1][4] > 0 || foundInARowPartial[1][4] > 0)
           return winScore[1];

       //score according to global-variable info.
       for(int i = 0; i< foundInARow[0].length-1; i++){
           score += pointValues[i]*foundInARow[0][i];//*(1+0.5*((turn+1) %2));
           score += pointValues[i]*foundInARowPartial[0][i]/20;//*(1+0.5*((turn+1) %2));
           score -= pointValues[i]*foundInARow[1][i];//*(1+0.5*(turn %2));
           score -= pointValues[i]*foundInARowPartial[1][i]/20;
       }
       
        return score;
    }
    
    /**
     * Function for evaluating a boardstate, which can search in CardinalDirections
     * returns void as it fills in the global variables.
     * @param turn int with turnnumber to evaluate
     * @param rowWise boolean if we are looking horixontally or vertically
     */
    void scoreLoopThroughCardinal(int turn, boolean rowWise){
       int inARow = 0;
       int clearSpacesBefore = 0;
       int clearSpacesAfter = 0;
       
       int totalLength = 0;
       List<Integer> foundLengths = new ArrayList<>();
       List<Integer> foundLengthsPartial = new ArrayList();

       //help variables for looping through
       int z1 = rowWise ? Ny : Nx;
       int z2 = rowWise ? Nx : Ny;
       
       for(int j = 0; j < z1; j++){
           clearSpacesBefore = 0;
           clearSpacesAfter = 0;
           
           for(int i = 0; i < z2; i++){
               
               char c = rowWise ? gameState[turn][i][j] : gameState[turn][j][i];
               
               //skip all blanks
               //this will only be relevant on first iteration;
               if(c == ' '){
                   clearSpacesBefore++;
                   continue;
               }
               
               //if we found one of the player markers
               if(c == markers[0] || c == markers[1]){
                   inARow++;
                   
                   //keep going as long as it is the same marker
                   while(i < z2-1){
                       if(c != (rowWise ? gameState[turn][i+1][j] : gameState[turn][j][i+1]))
                           break;
                       inARow++;
                       i++;
                   }
                   
                   //if we are here we have run out of similar charactes;
                   while(i < z2-1){
                       //check how many blanks we have after
                       if((rowWise ? gameState[turn][i+1][j] : gameState[turn][j][i+1]) != ' ')
                           break;
                       clearSpacesAfter++;
                       i++;
                   }
                   
                   //save info on how long current region with blanks/marker is
                   totalLength += clearSpacesBefore + inARow + clearSpacesAfter;
                   //save info on found subsets in a row.
                   if(clearSpacesBefore > 0 && clearSpacesAfter > 0)
                       foundLengths.add(inARow);
                   else
                       foundLengthsPartial.add(inARow);
                   
                   //add info to final global variables if:
                   //We are on the last space of the board OR
                   //Next character is of enemy-marker
                   if(i == z2-1 || (rowWise ? gameState[turn][i+1][j] : gameState[turn][j][i+1]) != c){
                       if(totalLength >= 5){
                           foundLengths.forEach(x -> foundInARow[markerToPlayer.get(c)][Math.min(x,5)-1] += 1);
                           foundLengthsPartial.forEach(x -> foundInARowPartial[markerToPlayer.get(c)][Math.min(x,5)-1] += 1);
                           foundLengths.clear();
                           foundLengthsPartial.clear();
                           totalLength = 0;
                       }
                   }

                   //reset variables for next iteration
                   clearSpacesBefore = clearSpacesAfter;
                   clearSpacesAfter = 0;
                   inARow = 0;
               }
           }
       }
    }
    
    /**
     * Function for evaluating a boardstate, which can search in DiagonalDirections
     * returns void as it fills in the global variables.
     * @param turn int with turnnumber to evaluate
     * @param noFlip90 boolean if we are looking left-to-right or vice verca.
     */
    void scoreLoopThroughDiagonal(int turn, boolean noFlip90){
        int inARow = 0;
        int clearSpacesBefore = 0;
        int clearSpacesAfter = 0;
        
        int totalLength = 0;
        List<Integer> foundLengths = new ArrayList<>();
        List<Integer> foundLengthsPartial = new ArrayList();

        int[] dm = noFlip90 ? new int[]{1,1} : new int[]{-1,1};
       
        for(int j = 0; j < Nx+Ny-1; j++){

            int[] cPos = noFlip90 ? new int[]{-Ny+j+1,0} : new int[]{Nx-1,-Nx+1+j};

            while(cPos[0] < 0 || cPos[1] < 0){
                cPos[0] += dm[0];
                cPos[1] += dm[1];
            }
           
            clearSpacesBefore = 0;
            clearSpacesAfter = 0;
            
            while(!(cPos[1] >= Ny || (noFlip90 && cPos[0] >= Nx) || (!noFlip90 && cPos[0] < 0))){
               
                char c = gameState[turn][cPos[0]][cPos[1]];
               
               //skip all blanks
               //this will only be relevant on first iteration
                if(c == ' ')
                   clearSpacesBefore++;
                else{
                   inARow++;
                   
                   //if we found one of the player markers
                   while(cPos[1] < Ny -1 && ((noFlip90 && cPos[0] < Nx -1) || (!noFlip90 && cPos[0] > 0))){
                       //keep going as long as it is the same marker
                       if(c != gameState[turn][cPos[0] + dm[0]][cPos[1]+dm[1]])
                           break;
                       inARow++;
                       cPos[0] += dm[0];
                       cPos[1] += dm[1];
                   }
                   
                   //if we are here we have run out of similar charactes;
                   while(cPos[1] < Ny -1 && ((noFlip90 && cPos[0] < Nx -1) || (!noFlip90 && cPos[0] > 0))){
                       //check how many blanks we have after
                       if(' ' != gameState[turn][cPos[0]+dm[0]][cPos[1]+dm[1]])
                           break;
                       clearSpacesAfter++;
                       cPos[0] += dm[0];
                       cPos[1] += dm[1];
                   }
                   
                   //save info on how long current region with blanks/marker is
                   totalLength += clearSpacesBefore + inARow + clearSpacesAfter;
                   //save info on found subsets in a row.
                   if(clearSpacesBefore > 0 && clearSpacesAfter > 0)
                       foundLengths.add(inARow);
                   else
                       foundLengthsPartial.add(inARow);
                   
                   //add info to final global variables if:
                   //We are on the last space of the board OR
                   //Next character is of enemy-marker
                   if(
                           (cPos[1] >= Ny-1 || (noFlip90 && cPos[0] >= Nx-1) || (!noFlip90 && cPos[0] <= 0)) || 
                           gameState[turn][cPos[0]+dm[0]][cPos[1]+dm[1]] != c)
                   {
                       if(totalLength >= 5){
                           foundLengths.forEach(x -> foundInARow[markerToPlayer.get(c)][Math.min(x,5)-1] += 1);
                           foundLengthsPartial.forEach(x -> foundInARowPartial[markerToPlayer.get(c)][Math.min(x,5)-1] += 1);
                           foundLengths.clear();
                           foundLengthsPartial.clear();
                           totalLength = 0;
                       }
                   }
                   
                   //reset variables for next iteration
                   clearSpacesBefore = clearSpacesAfter;
                   clearSpacesAfter = 0;
                   inARow = 0;
               }
               
               cPos[0] += dm[0];
               cPos[1] += dm[1];
               
           }
       }
        
        
    }
    
    
    
}
