import java.io.Console;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.plaf.OptionPaneUI;

/**
 * The cake is a lie. Awesome quote from exercise description: 'Finally, it is
 * not recommended to write all the code in a single class e class.'
 */
public class GLaDOS implements IGameLogic {
    private HashMap<String, Float> knowledgeBase;
    private int x = 0, y = 0;
    private int playerID;
    private int opponentID;
    private LongBoard gameBoard;
    private int statescheack = 0, cutoffs = 0;
    private boolean hasReachedMaxDepth;
    //for search in knowledge base
    private int startDepth = 13;
    private Heuristic H;

    private ArrayList<Integer> generateActions(LongBoard state) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int middle = x/2;
        //TODO choose random when x is even
        if (state.isPlayable(middle)) {
            result.add(middle);
        }
        for (int i=1; i <= x/2; i++){
            if(middle + i < x) {
                if (state.isPlayable(middle + i)) {
                    result.add(middle + i);
                }
            }
            if(middle - i > -1) {
                if (state.isPlayable(middle - i)) {
                    result.add(middle - i);
                }
            }
        }
        return result;
    }

    private float h(LongBoard state, Integer winner){
        return H.h(state, null);
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

    private float max(LongBoard state,float alpha, float beta, int action, int depth) {
        Winner win = gameFinished(state);
        statescheack++;
        float y = Integer.MIN_VALUE;
        if(depth == 0) {
            hasReachedMaxDepth = true;
            return h(state, action);
        }
        if(win != Winner.NOT_FINISHED) {
        	return utility(win);
        }

        for (int newaction : generateActions(state)) {
            y = Math.max(y,min(result(state, newaction, playerID), alpha, beta,newaction, depth - 1));
            // tests for possible beta cut
            if (y >= beta) {
                cutoffs++;
                return y;
            }
            alpha = Math.max(alpha, y);

        }
        return y;
    }

    private float min(LongBoard state,float alpha,float beta,int action,
            int depth) {
        statescheack++;
        float y = Integer.MAX_VALUE;

        Winner win = gameFinished(state);

        if (depth == 0) {
            hasReachedMaxDepth = true;
            return h(state, action);
        }
        // If the state is a finished state
        if (win != Winner.NOT_FINISHED)
            return utility(win);

        for (int newaction : generateActions(state)) {
            y = Math.min(
                    y,
                    max(result(state, newaction, opponentID), alpha, beta,
                            newaction, depth - 1));
            // tests for possible alpha cut
            if (y <= alpha) {
                cutoffs++;
                return y;
            }
            beta = Math.min(beta, y);
        }
        return y;
    }

    // knowledge!
    public int knowledgeSearch() {
        hasReachedMaxDepth = true;
        return minimax(gameBoard, startDepth--);
    }

    // Iterative
    public int iterativeSearch() {
        int i = 0;
        int move = -1;
        hasReachedMaxDepth = true;
        // TODO stop if we find a sure win util = 1;
        // TODO make stop after x sec. maybe with an exception
        while (i < 11 && hasReachedMaxDepth) {
            System.out.println("depth: " + i);
            move = minimax(gameBoard, ++i);
        }
        return move;
    }

    private int minimax(LongBoard state, int depth) {
        int bestAction = -1;
        statescheack = 0;
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
    
    private LongBoard result(LongBoard state, int action, int playerID) {
        LongBoard newBoard = new LongBoard(state);
        newBoard.move(action);
        return newBoard;
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
        gameBoard = new LongBoard(x, y);
        if (x == 7 && y == 6){
            H = new baseLookUp();
            initKnowledge();
        } else {
            H = new baseLookUp();
        }
    }

    public Winner gameFinished() {
        return gameFinished(gameBoard);
    }

    public static Winner gameFinished(LongBoard board) {
        return board.hasWon() ?
                ((board.player & 1) == 0 ? Winner.PLAYER1 : Winner.PLAYER2) :
                (board.player == board.SIZE ? Winner.TIE : Winner.NOT_FINISHED);
    }
    
    public void insertCoin(int column, int playerID) {
    	Threats t = new Threats();
        gameBoard.move(column);
    	t.h(gameBoard, null);
    }

    public int decideNextMove() {
        return knowledgeSearch();
    }

    /**
     * A data interface for the heuristics to contain states.
     */
    public interface HeuristicData {}

    /**
     * A heuristic that describes a numeric value between -1 to 1 of a game-state, where 1 is a win and -1 a loss.
     * @param <T>  The type of the heuristic data (if any).
     */
    public interface Heuristic<T extends HeuristicData> {
        public float h(LongBoard state, T data);
    }

    private class baseLookUp implements Heuristic {
        public float h(LongBoard state, HeuristicData data) {
            return 0f;
        }
    }
    
    private class Threats implements Heuristic {

		@Override
		public float h(LongBoard state, HeuristicData ignoreThisVariable) {
			int empty = -1;
			int empty2 = -1;
			for (int h=0; h <= state.HEIGHT; h++) {
			      for (int w=h; w < state.SIZE1; w+=state.H1) {
				long mask = 1l<<w;
				//AI owns postion
				if((state.boards[playerID-1] & mask) !=0) {
					if(h + 3 < state.HEIGHT) {
						empty=explore(w, 3, 1, playerID-1, state, -1, 0);
					}
				}
				//Opponent owns postion
				else if((state.boards[opponentID-1] & mask) !=0) {
					if(h + 3 < state.HEIGHT) {
						empty2 = explore(w, 3, 1, opponentID-1, state, -1, 0);
					}
				} 
				//No one owns postion
				else {
					int player = zeroExplore(w,3,1,-1,state);
				}
			}
			}
			if(empty != -1 || empty2 != -1) {
				System.out.println(empty + " "  +empty2);
			}
			
			return 0;
		}
    	
		//Returns placement of the threat
		private int explore(int startPostion, int depth, int dicrection, int lastFoundPlayer, LongBoard state,int threatPlacement, int emptyPostions) {
			if(depth == 0) {
				return threatPlacement;
			}
			long mask = 1l <<(startPostion + dicrection);
			if((state.boards[lastFoundPlayer] & mask) != 0) {
				return explore(startPostion + dicrection, depth-1, dicrection, lastFoundPlayer, state,-1,0);
			} else if ((state.boards[Math.abs(lastFoundPlayer-1)] & mask) != 0) {
				return -1;
			} else {
				if(emptyPostions == 0) {
					return explore(startPostion + dicrection, depth-1, dicrection, lastFoundPlayer, state,startPostion+dicrection,1);
				} else {
					return -1;
				}
			}
		}
		
		//Returns the index of the player who owns the threat
		private int zeroExplore(int startPostion, int depth, int dicrection, int lastFoundPlayer, LongBoard state) {
			if(depth == 0) {
				return lastFoundPlayer;
			}
			long mask = 1l <<(startPostion + dicrection);
			
			if((state.boards[0] & mask) != 0) {
				if(lastFoundPlayer == 0 || lastFoundPlayer == -1) {
					return zeroExplore(startPostion + dicrection, depth-1, dicrection, 0, state);
				}
				return -1;
			} else if ((state.boards[1] & mask) != 0) {
				if(lastFoundPlayer == 1 || lastFoundPlayer == -1) {
					return zeroExplore(startPostion + dicrection, depth-1, dicrection, 1, state);
				}
				return -1;
			} else {
				return -1;
			}
		}
    }

    /**
     * A heuristic that considers moves to win.
     */
    private class MovesToWin implements Heuristic<MovesToWin.MTWData> {

        /**
         * An int tuple class. Just because I have an inherent disrespect for my RAM. Thanks Java.
         */
        class Tuple {
            int x, y;
            Tuple(int x, int y) { this.x = x; this.y = y; }
        }

        /**
         * Data for the MTW heuristic containing the board (with the moves) and a list of the possible wins.
         */
        class MTWData implements HeuristicData {
            int board[][] = new int[x][y];
            List<List<Tuple>> mtw = Collections.emptyList();
            MTWData(int board[][], List<List<Tuple>> mtw) {
                this.board = board;
                this.mtw   = mtw;
            }
        }

        private int row(LongBoard state, int lastMove) {
            return state.height[lastMove];
        }

        private float hTrace(LongBoard state, int lastMoveX, int lastMoveY) {
            int freeOrOwned = 0;
            int owned = 0;
            boolean met = false;
             //   System.out.println(lastMoveX);
             //   System.out.println(lastMoveY);
            /*
            for(int i = 0; i < x; i++) {
                met = i >= lastMoveX;
                System.out.println(met);
                if(state[i][lastMoveY] == 0) {
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
            }*/
            // TODO: Fix
            return freeOrOwned - owned;
        }

        public float h(LongBoard state, MTWData data){
            for (int i=0; i<y; i++){
                System.out.println();
                for(int j=0; j<x; j++){
                    //System.out.print("" + state[j][i] + ", ");
                }
            }
            System.out.println();
            //System.console().readLine();
            return 2f;
        }
    }

    /**
     * A board that is based on a representation of a single long per player.
     */
    public class LongBoard {
        // -- The following comments are made for Sigurt, who cannot see the errors in his ways -- //
        long boards[]; // Two board for player one (0) and player two (1).
        byte height[]; // The largest index of the columns where a coin has been inserted.
        int player = -1; // Set player to -1 to avoid the hasWon method to check for the wrong player
                         // (because player is incremented whenever a move has been made)
        int HEIGHT,  // The height of the board
            WIDTH,   // The width of the board
            H1,      // The height of the board PLUS 1 (hence the 1)
            H2,      // The height of the board PLUS 2 (hence the 2)
            SIZE,    // The size of the board (height * width)
            SIZE1,   // The size of the board PLUS 1 (height * width) + 1 (hence the 1)
            COLUMN;  // All bytes filled in one single column, found by shifting 1 H1 bytes to the left (to the left).

        long ALL,    // All bytes in the board ignited.
             BOTTOM, // A long with all the bytes in the bottom row ignited.
             TOP;    // A long with all the bytes in the top row ignited.

        public LongBoard(int width, int height) {
            init(width, height);
        }

        public LongBoard(LongBoard old) {
            init(old.WIDTH, old.HEIGHT);
            System.arraycopy(old.boards, 0, this.boards, 0, 2);
            System.arraycopy(old.height, 0, this.height, 0, WIDTH);
            player = old.player;
        }

        private void init(int width, int height) {
            assert (height * width < 63);
            HEIGHT = height;
            WIDTH = width;
            boards = new long[2];
            H1 = HEIGHT + 1;
            H2 = HEIGHT + 2;
            SIZE = HEIGHT * WIDTH;
            SIZE1 = H1 * WIDTH;
            COLUMN = (1 << H1)- 1;
            ALL = (1L << SIZE1) - 1L;
            BOTTOM = ALL / COLUMN;
            TOP = BOTTOM << HEIGHT;

            this.height = new byte[width];
            for (int i = 0; i < WIDTH; i++) {
                this.height[i] = (byte) (H1 * i);
            }
        }

        boolean hasWon() {
            long board = boards[player & 1]; // Awesome page at https://codebrew.io/
            long diagonalLeft  = board & (board >> HEIGHT);
            long horizontal    = board & (board >> H1);
            long diagonalRight = board & (board >> H2);
            long vertical      = board & (board >> 1);
            return ((diagonalLeft & (diagonalLeft >> 2 * HEIGHT)) |
                    (horizontal & (horizontal >> 2* H1)) |
                    (diagonalRight & (diagonalRight >> 2 * H2)) |
                    (vertical & (vertical >> 2))) != 0;
        }

        boolean isPlayable(int column) {
            return ((boards[player & 1] | 1L << height[column]) & TOP) == 0;
        }

        void move(int column) {
            player++;
            boards[player & 1] ^= 1L << height[column]++;
        }

    }

    //initialize knowledge base from file
    private void initKnowledge(){
        knowledgeBase = new HashMap<String, Float>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("connect-4.data"));
            String line;
            while ((line = br.readLine()) != null){
                int commaIdx = line.lastIndexOf(",");
                knowledgeBase.put(line.substring(0, commaIdx), Float.parseFloat(line.substring(commaIdx+1)));
            }
            br.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Why you no have connect-4.data in folder?");
        } catch (IOException e) {
            throw new RuntimeException("Some IO went wrong me thinks");
        }
        System.out.println("lars");
    }
}
// vim: set ts=4 sw=4 expandtab:
