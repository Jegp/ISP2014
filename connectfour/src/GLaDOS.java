import java.awt.List;
import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
	private int x = 0, y = 0, lastMoveColumn = -1;
	private int playerID;
	private int opponentID;
	private LongBoard gameBoard;
	private int statescheack = 0, cutoffs = 0;
	private boolean hasReachedMaxDepth;
	//for search in knowledge base
	private int startDepth = 2;
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
		return H.h(state, winner);
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
		while (i < 9 && hasReachedMaxDepth) {
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
			H = new Threats();
			initKnowledge();
		} else {
			H = new Threats();
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

		gameBoard.move(column);
	}

	public int decideNextMove() {

		return iterativeSearch();
	}

	public interface Heuristic {
		public float h(LongBoard state, Integer lastMove);
	}

	private class baseLookUp implements Heuristic {
		public float h(LongBoard state, Integer ignored_var) {
			return 0f;
		}
	}

	private class Threats implements Heuristic {

		@Override
		public float h(LongBoard state, Integer lastMove) {
			int player = -1;
			Set<Integer> AEVEN = new TreeSet<>();
			Set<Integer> AODD = new TreeSet<>();
			Set<Integer> BEVEN = new TreeSet<>();
			Set<Integer> BODD = new TreeSet<>();
			ArrayList<Set<Integer>> lists = new ArrayList<Set<Integer>>();
			lists.add(0, AEVEN);
			lists.add(1, BEVEN);
			lists.add(0 + 2, AODD);
			lists.add(1 + 2, BODD);
			for (int h=0; h <= state.HEIGHT; h++) {
				for (int w=h; w < state.SIZE1; w+=state.H1) {
					player = -1;
					long mask = 1l<<w;
					//A owns postion
					if((state.boards[0] & mask) !=0) {
						player = 0;
					}
					//B owns postion
					if((state.boards[1] & mask) !=0) {
						player = 1;
					} 

					if(player != -1) {
						//VERT
						if(h + 3 < state.HEIGHT) {
							int emptyPos = explore(w, 3, 1, player, state, -1, 0);
							if(emptyPos != -1) {
								//System.err.println(w);
								//System.err.println("Found threat VERT " + player + " at pos " + emptyPos);
								lists.get(player + (emptyPos%2)*2).add(emptyPos);
							}
						}
						//HORI
						if((w / state.H1)+ 3 < state.WIDTH) {
							int emptyPos = explore(w, 3, state.H1, player, state, -1, 0);
							if(emptyPos != -1) {
								//System.err.println(w);
								//System.err.println("Found threat HORI " + player + " at pos " + emptyPos);
								lists.get(player+ (emptyPos%2)*2).add(emptyPos);
							}
						}
						// '/'
						if((h + 3 < state.HEIGHT) && ((w / state.H1)+ 3 < state.WIDTH)) {
							int emptyPos = explore(w, 3, state.H2, player, state, -1, 0);
							if(emptyPos != -1) {
								//System.err.println(w);
								//System.err.println("Found threat / " + player + " at pos " + emptyPos);
								lists.get(player+ (emptyPos%2)*2).add(emptyPos);
							}
						}
						if(h + 3 < state.HEIGHT && ((w / state.H1) - 3 >= 0)) {
							int emptyPos = explore(w, 3, -state.HEIGHT, player, state, -1, 0);
							if(emptyPos != -1) {
								//System.err.println(w);
								//System.err.println("Found threat \\ " + player + " at pos " + emptyPos);
								lists.get(player+ (emptyPos%2)*2).add(emptyPos);
							}
						}
					}
					//No one owns postion
					else {
						//VERT
						if(h + 3 < state.HEIGHT) {
							int play = zeroExplore(w,3,1,-1,state);
							if(play != -1) {
								//System.err.println(w);
								//System.err.println("Found Threat VERT " + play + " at pos " + w);
								lists.get(play+ (w%2)*2).add(w);
							}
						}
						//HORI
						if((w / state.H1)+ 3 < state.WIDTH) {
							int play = zeroExplore(w,3,state.H1,-1,state);
							if(play != -1) {
							//	System.err.println(w);
								//System.err.println("Found Threat HORI " + play + " at pos " + w);
								lists.get(play+ (w%2)*2).add(w);
							}
						}
						// '/'
						if((h + 3 < state.HEIGHT) && ((w / state.H1)+ 3 < state.WIDTH)) {
							int play = zeroExplore(w,3,state.H2,-1,state);
							if(play != -1) {
					//			System.err.println(w);
						//		System.err.println("Found Threat / " + play + " at pos " + w);
								lists.get(play+ (w%2)*2).add(w);
							}
						}
						if(h + 3 < state.HEIGHT && ((w / state.H1) - 3 >= 0)) {
							int play = zeroExplore(w,3,-state.HEIGHT,-1,state);
							if(play != -1) {
			//					System.err.println(w);
				//				System.err.println("Found Threat \\ " + play + " at pos " + w);
								lists.get(play+ (w%2)*2).add(w);
							}
						}
					}
				}
			}
		/* for (Integer set : lists.get(0)) {
			System.err.println("A");
			System.err.println(set);
		}
		 for (Integer set : lists.get(1)) {

				System.err.println("B");
				System.err.println(set);
			}
		 for (Integer set : lists.get(2)) {
			System.err.println("AODD");
			System.err.println(set);
		}
		 for (Integer set : lists.get(3)) {

				System.err.println("BODD");
				System.err.println(set);
			}
		 */
		 int neg = 0;
		 if(playerID == 1) {
			 neg = 1;
		 } else {
			 neg = -1;
		 }
		 
		 for (Integer pos : AODD) {
			int col = pos/state.HEIGHT;
			if(!ThreatsBelow(col, pos, BEVEN) && !ThreatsInOtherColums(col,BODD)) {
				//WIN FOR A
				return 0.9F*neg;
			}
		 }
		 int count = 0;
		 for (Integer pos : AODD) {
				int col = pos/state.HEIGHT;
				if(!ThreatsBelow(col, pos, BEVEN)) {
					count++;
				}
			 }
		 if(count > BODD.size() && BEVEN.size() == 0) {
			 return 0.9F * neg;
		 }
		 
		 if(BEVEN.size() > 0) {
			 return -0.9F *neg;
		 }
				
			return 0;
		}
		
	
	public boolean ThreatsBelow(int col,int pos,Set<Integer> haystack) {
		for (Integer pos2 : haystack) {
			if(pos2/gameBoard.HEIGHT == col) {
				if(pos2 < pos) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean ThreatsInOtherColums(int col,Set<Integer> haystack) {
		for (Integer pos2 : haystack) {
			if(pos2/gameBoard.HEIGHT != col) {
				return true;
			}
		}
		return false;
	}

		//Returns placement of the threat
		private int explore(int startPostion, int depth, int dicrection, int lastFoundPlayer, LongBoard state,int threatPlacement, int emptyPostions) {
			if(depth == 0) {
				return threatPlacement;
			}
			long mask = 1l <<(startPostion + dicrection);
			if((state.boards[lastFoundPlayer] & mask) != 0) {
				return explore(startPostion + dicrection, depth-1, dicrection, lastFoundPlayer, state,-1,emptyPostions);
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

	private class MovesToWin implements Heuristic {

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

		public float h(LongBoard state, Integer lastMove){
			for (int i=0; i<y; i++){
				System.out.println();
				for(int j=0; j<x; j++){
					//System.out.print("" + state[j][i] + ", ");
				}
			}
			System.out.println();
			System.out.println(hTrace(state, lastMove, row(state, lastMoveColumn)));
			//System.console().readLine();
			return 2f;
		}
	}

	/**
	 * A board that is based on a representation of a single long per player.
	 */
	public class LongBoard {
		long boards[];
		byte height[];
		int player = -1; // Set player to -1 to avoid the hasWon method to check for the wrong player
		// (because player is incremented whenever a move has been made)
		int HEIGHT, WIDTH, H1, H2, SIZE, SIZE1, COLUMN;
		long ALL, BOTTOM, TOP;

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
