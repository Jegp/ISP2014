import java.io.Console;
import java.util.ArrayList;

/**
 * The cake is a lie.
 * Awesome quote from exercise description: 
 * 'Finally, it is not recommended to write all the code in a single class
e class.'
 */
public class GLaDOS implements IGameLogic {
    private int x = 0, y = 0, lastMoveColumn = -1;
    private int playerID;
    private int opponentID;
    private int[][] gameBoard;
    private int statescheack = 0, cutoffs = 0;
    private boolean hasReachedMaxDepth;

    private ArrayList<Integer> generateActions(int[][] state) {
    ArrayList<Integer> result = new ArrayList<Integer>();
    int middle = x/2;
  //TODO choose random when x is even
    if (state[middle][0] == 0) {
        result.add(middle);
    }
    for (int i=1; i <= x/2; i++){
    	if(middle + i < x) {
            if (state[middle + i][0] == 0) {
                result.add(middle + i);
            }
    	}
        if(middle - i > -1) {
            if (state[middle - i][0] == 0) {
                result.add(middle - i);
            }	
    	}
    }
    return result;
    }

    private float utility(Winner win){
        if (win == Winner.TIE) {
            return 0.0f;
        } else if (win.ordinal() == playerID -1) {
            return 1.0f;
        } else if (win == Winner.NOT_FINISHED) {
            throw new IllegalArgumentException("Faggot");
        } else {
           return -1.0f;
        }
    }

    private float max(int[][] state,float alpha, float beta, int action, int depth) {
    	Winner win = gameFinished(state,action);
    	statescheack++;
    	float y = Integer.MIN_VALUE;
    	if(depth == 0) { 
    		hasReachedMaxDepth = true;
    		MovesToWin h = new MovesToWin();
    		return h.h(state, action);
    	}
    	if(win != Winner.NOT_FINISHED) return utility(win);

    	for (int newaction : generateActions(state)) {
			y = Math.max(y, min(result(state,newaction,playerID),alpha,beta,newaction,depth-1));
			//tests for possible beta cut 
			if( y >= beta) {
				cutoffs++;
				return y;
			}
			alpha = Math.max(alpha, y);	
		
    	}
    	return y;
    }
    

    private float min(int[][] state, float alpha, float beta, int action, int depth) {
    	statescheack++;
    	float y = Integer.MAX_VALUE;
    	
    	Winner win = gameFinished(state,action);
    	
    	if(depth == 0) {
    		hasReachedMaxDepth = true;
    		MovesToWin h = new MovesToWin();
    		return h.h(state, action);
    	}
    	//If the state is a finished state
    	if(win != Winner.NOT_FINISHED) return utility(win);

    	
		for (int newaction : generateActions(state)) {
			y = Math.min(y,max(result(state,newaction, opponentID),alpha,beta,newaction,depth-1));
			//tests for possible alpha cut 
			if( y <= alpha) {
				cutoffs++;
				return y;
			}
			beta = Math.min(beta, y);	
    	}
    	return y;
    }

    //Iterative 
    public int iterativeSearch() {
    	int i = 0;
    	int move =-1;
    	hasReachedMaxDepth = true;
    	//TODO stop if we find a sure win util = 1;
    	//TODO make stop after x sec. maybe with an exception
    	while(i < 11 && hasReachedMaxDepth) {
    		System.out.println("depth: " + i);
    		move = minimax(gameBoard, ++i);
    	}
    	return move;
    }
    
    private int minimax(int[][] state, int depth) {
    	int bestAction = -1;
    	statescheack = 0;
    	cutoffs = 0;
    	float y = Integer.MIN_VALUE;
    	
    	hasReachedMaxDepth = false;
    	//Generate the valid actions from the start state
    	for (int action : generateActions(state)) {
    		float max = min(result(state,action,playerID),Integer.MIN_VALUE,Integer.MAX_VALUE,action,depth-1);
    		//If the current action is better than the previous ones, choose this
    		if(max > y) {
    			bestAction = action;
    			y = max;
    		}
			
		}
    	System.out.println("States: " + statescheack);
    	System.out.println("Cutoffs; "+ cutoffs);
    	System.out.println("H - value: " + y);
    	System.out.println("Move: " + bestAction);
    	System.out.println();
    	return bestAction;
    }
    
    private int[][] result(int[][] state, int action, int playerID) {
		int[][] newstate = new int[x][y];//= state.clone();
		for(int i=0; i<state.length; i++) {
			  for(int j=0; j<state[i].length; j++) {
				  newstate[i][j]=state[i][j]; 
				  }
			}
        int r = y-1;
        while(newstate[action][r]!=0) {
        	r--;
        }
        newstate[action][r]=playerID;
    	return newstate;
    }

    public void initializeGame(int x, int y, int playerID) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        if(playerID == 1) {
        	opponentID = 2;
        } else {
        	opponentID = 1;
        }
        gameBoard = new int[x][y];
    }

    public Winner gameFinished() {
        return gameFinished(gameBoard, lastMoveColumn);
    }

    /**
     * Decides how many subsequent coins there are in the direction specified by dx and dy. Does NOT
     * examine the initial (x, y) position. A call to the function starts in (x + dx, y + dy).
     */
    private static int subsequentCoins(int[][] board, int x, int y, int dx, int dy, int playerID) {
        int newX = x + dx;
        int newY = y + dy;
        if ((newX >= 0 && newX < board.length) && (newY >= 0 && newY < board[0].length) && board[newX][newY] == playerID) {
            return 1 + subsequentCoins(board, newX, newY, dx, dy, playerID);
        } else {
            return 0;
        }
    }

    /**
     * Returns the Winner enum from the given number. Bloody enums.
     */
    private static Winner getWinner(int playerID) {
        return playerID == 1 ? Winner.PLAYER1 : Winner.PLAYER2;
    }

    /**
     * Tests whether the given player has one on a board with the given last move (column and player).
     */
    private static Winner gameFinished(int[][] board, int lastMoveColumn) {
        // Test if the first move has been  made
        if (lastMoveColumn != -1) {
            // Number of coherent columns. If >= 4, someone wins!
            int coherentFields = 0;

            // Find the 'active' row number, where the last coin was placed
            int row = 0;
            while (board[lastMoveColumn][row] == 0) { row++; }

            // The player id to examine for coherent fields
            int playerID = board[lastMoveColumn][row];

            // Horizontal win
            //TODO Maybe end search if left is above 3, just to make it terminate faster
            //TODO Maybe start with Vertical win, since these should be more common?
            int left  = subsequentCoins(board, lastMoveColumn, row, -1, 0, playerID);
            int right = subsequentCoins(board, lastMoveColumn, row,  1, 0, playerID);
            if (left + right >= 3) { return getWinner(playerID); }

            // Vertical win
            int up   = subsequentCoins(board, lastMoveColumn, row, 0, -1, playerID);
            int down = subsequentCoins(board, lastMoveColumn, row, 0,  1, playerID);
            if (up + down >= 3) { return getWinner(playerID); }

            // Diagonal left to right win
            int upLeft    = subsequentCoins(board, lastMoveColumn, row, -1, -1, playerID);
            int downRight = subsequentCoins(board, lastMoveColumn, row,  1,  1, playerID);
            if (upLeft + downRight >= 3) { return getWinner(playerID); }

            // Diagonal right to left win
            int upRight   = subsequentCoins(board, lastMoveColumn, row,  1, -1, playerID);
            int downLeft  = subsequentCoins(board, lastMoveColumn, row, -1,  1, playerID);
            if (upRight + downLeft >= 3) { return getWinner(playerID); }

            // Test for not finished or draw
            for (int[] aBoard : board) {
                // Not finished: One of the upper coins are not assigned to a player (0)
                if (aBoard[0] == 0) return Winner.NOT_FINISHED;
            }

            // Tie: All the upper coins are assigned, but no winner found.
            return Winner.TIE;
        } else {
            return Winner.NOT_FINISHED;
        }
    }

    public void insertCoin(int column, int playerID) {
        int r = gameBoard[column].length-1;
        while(gameBoard[column][r]!=0) r--;
        gameBoard[column][r]=playerID;    
        lastMoveColumn = column; 
    }

    public int decideNextMove() {
    	return iterativeSearch();
    	//return minimax(gameBoard,2);
    }

    public interface Heuristic {
        public float h(int[][] state, Integer lastMove);
    }
    public class MovesToWin implements Heuristic {

        private int row(int[][] state, int lastMove){
            for(int i=0; i<y; i++){
                if(state[lastMove][i] != 0){
                    return i;
                }
            }
            throw new IllegalArgumentException("Illegal move made it to heuristic");
        }

        private float hTrace(int[][] state, int lastMoveX, int lastMoveY){
            int freeOrOwned = 0;
            int owned =0;
            boolean met = false;
                System.out.println(lastMoveX);
                System.out.println(lastMoveY);
            for(int i = 0; i < x; i++){
                met = i >= lastMoveX;
                System.out.println(met);
                if(state[i][lastMoveY] == 0){
                    System.out.println("free");
                    freeOrOwned++;
                } else if (state[i][lastMoveY] == playerID){
                    System.out.println("owned");
                    freeOrOwned++;
                    owned++;
                } else {
                    System.out.println("oponent");
                    if (met){
                        return freeOrOwned - owned;
                    }
                    freeOrOwned = 0;
                    owned = 0;
                }
            }
            return freeOrOwned - owned;
        }

        public float h(int[][] state, Integer lastMove){
            for (int i=0; i<y; i++){
                System.out.println();
                for(int j=0; j<x; j++){
                    System.out.print("" + state[j][i] + ", ");
                }
            }
            System.out.println();
            System.out.println(hTrace(state, lastMove, row(state, lastMoveColumn)));
            System.console().readLine();
            return 2f;
        }
    }
}
// vim: set ts=4 sw=4 expandtab:
