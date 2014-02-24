import java.io.Console;
import java.util.ArrayList;

/**
 * The cake is a lie.
 */
public class GLaDOS implements IGameLogic {
    private int x = 0, y = 0, lastMoveColumn = -1;
    private int playerID;
    private int opponentID;
    private int[][] gameBoard;
    private int statescheack = 0;

    public GLaDOS() {
        //TODO Write your implementation for this method
    }
    
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

    private int utility(Winner win){
        if (win == Winner.TIE) {
            return 0;
        } else if (win.ordinal() == playerID -1) {
            return 1;
        } else if (win == Winner.NOT_FINISHED) {
            throw new IllegalArgumentException("Faggot");
        } else {
           return -1;
        }
    }

    private int max(int[][] state,int alpha, int beta, int action) {
    	Winner win = gameFinished(state,action);
    	statescheack++;
    	int y = Integer.MIN_VALUE;
    	if(win != Winner.NOT_FINISHED) {
    		return utility(win);
    	} else {
    		for (int newaction : generateActions(state)) {
    			y = Math.max(y, min(result(state,newaction,playerID),alpha,beta,newaction));
    			if( y >= beta) return y;
    			alpha = Math.max(alpha, y);	
    		}
    	}
    	return y;
    }
    

    private int min(int[][] state, int alpha, int beta, int action) {
    	statescheack++;
    	Winner win = gameFinished(state,action);
    	int y = Integer.MAX_VALUE;
    	if(win != Winner.NOT_FINISHED) {
    		return utility(win);
    	} else {
    		for (int newaction : generateActions(state)) {
    			int max = max(result(state,newaction, opponentID),alpha,beta,newaction);
    			y = Math.min(y,max);
    			if( y <= alpha) return y;
    			beta = Math.min(beta, y);	
    		}
    	}
    	return y;
    }

    private int minimax(int[][] state) {
    	int bestAction = -1;
    	statescheack = 0;
    	int y = Integer.MIN_VALUE;
    	for (int action : generateActions(state)) {
    		int min = min(result(state,action,playerID),Integer.MIN_VALUE,Integer.MAX_VALUE,action);
    		if(min > y) {
    			bestAction = action;
    			y = min;
    		}
			
		}
    	System.out.println(statescheack);
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
        //TODO Write your implementation for this method
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
            int upRight   = subsequentCoins(board, lastMoveColumn, row, -1, -1, playerID);
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
        //TODO Write your implementation for this method    
    }

    public int decideNextMove() {
    	return minimax(gameBoard);
    }

    public interface Heuristic {
        public float h(int[][] state, Integer lastMove);
    }
}
// vim: set ts=4 sw=4 expandtab:
