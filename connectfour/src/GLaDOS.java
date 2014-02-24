import java.util.ArrayList;
import java.util.Random;
/**
 * The cake is a lie.
 */
public class GLaDOS implements IGameLogic {
    private int x = 0, y = 0, lastMoveColumn = -1, lastMovePlayer = -1;
    private int playerID;
    private int oponentID;
    private int[][] gameBoard;

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
    	Winner win = gameFinished(state,action);
    	int y = Integer.MAX_VALUE;
    	if(win != Winner.NOT_FINISHED) {
    		return utility(win);
    	} else {
    		for (int newaction : generateActions(state)) {
    			int max = max(result(state,newaction,oponentID),alpha,beta,newaction);
    			y = Math.min(y,max);
    			if( y <= alpha) return y;
    			beta = Math.min(beta, y);	
    		}
    	}
    	return y;
    }

    private int minimax(int[][] state) {
    	int bestAction = -1;
    	int y = Integer.MIN_VALUE;
    	for (int action : generateActions(state)) {
    		int min = min(result(state,action,playerID),Integer.MIN_VALUE,Integer.MAX_VALUE,action);
    		if(min > y) {
    			bestAction = action;
    			y = min;
    		}
			
		}
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
        	oponentID = 2;
        } else {
        	oponentID = 1;
        }
        gameBoard = new int[x][y];
        //TODO Write your implementation for this method
    }

    public Winner gameFinished() {
        return gameFinished(gameBoard, lastMoveColumn);
    }

    /**
     * Tests whether the given player has one on a board with the given last move (column and player).
     * TODO: This can be optimised if you look 3 either way
     */
    private static Winner gameFinished(int[][] board, int lastMoveColumn) {
        // TODO: Implement recursive function that returns number of successors in each (8) (not up) directions


		
        // Test if the first move has been  made
        if (lastMoveColumn != -1) {
            // The player id to examine for coherent fields
            int playerID = 1;
            // Number of coherent columns. If >= 4, someone wins!
            int coherentFields = 0;

            // Find the 'active' row number, where the last coin was placed
            int row = 0;
            while (board[lastMoveColumn][row] == 0) { row++; }

            // Horizontal win
            for (int n = 0; n < board.length; n++) {
                //System.out.println(n + " " + row +  " " + playerID + " " + board[n][row]);
                if (board[n][row] == playerID) {
                    coherentFields++;
                    //System.out.println(coherentFields);
                    if (coherentFields >= 4) {
                        if (playerID == 1) {
                            return Winner.PLAYER1;
                        } else {
                            return Winner.PLAYER2;
                        }
                    }
                } else {
                    if (board[n][row] > 0) {
                        playerID = board[n][row];
                        coherentFields = 1;
                    } else {
                        coherentFields = 0;
                    }
                }
            }

            coherentFields = 0;

            // Vertical win
            for (int r = 0; r < board[0].length; r++) {
                if (board[lastMoveColumn][r] == playerID) {
                    coherentFields++;
                    //System.out.println(coherentFields);
                    if (coherentFields >= 4) {
                        if (playerID == 1) {
                            return Winner.PLAYER1;
                        } else {
                            return Winner.PLAYER2;
                        }
                    }
                } else {
                    if (board[lastMoveColumn][r] > 0) {
                        playerID = board[lastMoveColumn][r];
                        coherentFields = 1;
                    } else {
                        coherentFields = 0;
                    }
                }
            }

        	Boolean done = true;
    		for(int i=0; i<board.length; i++) {
    			  for(int j=0; j<board[i].length; j++) {
    				  if(board[i][j] == 0) {
    					 done = false;
    					 continue;
    				  }
    			}
    			 
    		}
    		if(done) {
    			 return Winner.TIE;
    		}
            // Diagonal from left to right

            // Diagonal from right to left
            return Winner.NOT_FINISHED;
        } else {
            return Winner.NOT_FINISHED;
        }
    }


    public void insertCoin(int column, int playerID) {
        int r = gameBoard[column].length-1;
        while(gameBoard[column][r]!=0) r--;
        gameBoard[column][r]=playerID;    
        lastMoveColumn = column;
        lastMovePlayer = playerID;
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
