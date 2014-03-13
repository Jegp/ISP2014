import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The cake is a lie. Awesome quote from exercise description: 'Finally, it is
 * not recommended to write all the code in a single class e class.'
 */
public class GLaDiOS implements IGameLogic {
    private HashMap<String, Float> knowledgeBase;
    private int x = 0, y = 0;
    private int playerID;
    private int opponentID;
    private LongBoard gameBoard;
    private HashMap<String, Float> cache = new HashMap<>();
    private int statesChecked = 0, cutoffs = 0,cacheHits = 0;
    private boolean hasReachedMaxDepth;
    //for search in knowledge base
    private int startDepth = 8;
    private Heuristic H;
    // Private time variables if heuristic is running too long
    private long start, time;


    /**
     * An tuple class parametrized over a type T. Just because I have an inherent disrespect for my memory.
     * Thanks Java.
     */
    class Tuple<T, U> {
        T _1; U _2;
        Tuple(T _1, U _2) { this._1 = _1; this._2 = _2; }
        @Override public boolean equals(Object that) {
            if (that instanceof Tuple) {
                Tuple other = (Tuple) that;
                return other._1.equals(this._1) && other._2.equals(this._2);
            } else return false;
        }
        @Override public int hashCode() {
            if (_1 instanceof Integer && _2 instanceof Integer) {
                return ((Integer) _1) << 4 | (Integer) _2;
            }
            return super.hashCode();
        }
        @Override public String toString() {
            return "(" + _1.toString() + " -> " + _2.toString() + ")";
        }
    }

    private ArrayList<Integer> generateActions(LongBoard state) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int middle = x/2;
        //TODO choose random when x is even
        if (state.isPlayable(middle)) {
            result.add(middle);
        }
        for (int i=1; i <= x/2; i++) {
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

    /**
     * Calls the underlying heuristics by inserting a coin and calculating the win-value.
     */
    private Tuple<Float, HeuristicData> h(LongBoard board, HeuristicData data){
        return H.h(board, data);
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

    private Tuple<Float, HeuristicData> max(LongBoard state, HeuristicData data, float alpha,
                                            float beta, int action, int depth) {
        Winner win = gameFinished(state);
        statesChecked++;
        Tuple<Float, HeuristicData> y = new Tuple<>((float) Integer.MIN_VALUE, null);

        if(win != Winner.NOT_FINISHED) {
        	float value = utility(win);
        	cache.put(state.toString(), value);
        	return new Tuple<>(value, null);
        }

        if (cache.get(state.toString()) != null) {
        	cacheHits++;
			return new Tuple<>(cache.get(state.toString()),null);
		}
        if (depth == 0) {
            hasReachedMaxDepth = true;
            HeuristicData newData = H.moveHeuristic(data, action, opponentID);
            Tuple<Float, HeuristicData> value = h(state, newData);
            cache.put(state.toString(), value._1);
            return value;
        }

        for (int newaction : generateActions(state)) {
            // Stop if time's up
            if (isTimeUp()) break;
            HeuristicData newData = H.moveHeuristic(data, newaction, opponentID);
            Tuple<Float, HeuristicData> min = min(result(state, newaction), newData, alpha, beta,newaction, depth - 1);
            if (min._1 > y._1) {
                y = min;
            }
            // tests for possible beta cut
            if (y._1 >= beta) {
                cutoffs++;
                cache.put(state.toString(), y._1);
                return y;
            }

            alpha = Math.max(alpha, y._1);

        }
        cache.put(state.toString(), y._1);
        return y;
    }

    private Tuple<Float, HeuristicData> min(LongBoard state, HeuristicData data, float alpha,
                                            float beta, int action, int depth) {
        statesChecked++;
        Tuple<Float, HeuristicData> y = new Tuple<>((float) Integer.MAX_VALUE, data);

        Winner win = gameFinished(state);

        if(cache.get(state.toString()) != null) {
        	
        	cacheHits++;
        	return new Tuple<>(cache.get(state.toString()),null);
        }
        // If the state is a finished state
        if (win != Winner.NOT_FINISHED) {
        	float value = utility(win);
        	cache.put(state.toString(), value);
            return new Tuple<>(value, null);
        }
        
        if(cache.get(state.toString()) != null) {
        	cacheHits++;
			return new Tuple<>(cache.get(state.toString()),null);
		}
        
        if (depth == 0) {
            hasReachedMaxDepth = true;
            HeuristicData newData = H.moveHeuristic(data, action, playerID);
            Tuple<Float, HeuristicData> value = h(state, newData);
            cache.put(state.toString(), value._1);
            return value;
        }
        for (int newaction : generateActions(state)) {
            // Stop if time's up
            if (isTimeUp()) break;
            HeuristicData newData = H.moveHeuristic(data, action, playerID);
            Tuple<Float, HeuristicData> max = max(
                    result(state, newaction), newData, alpha, beta, newaction, depth - 1
            );

            if (max._1 < y._1) {
                y = max;
            }

            // tests for possible alpha cut
            if (y._1 <= alpha) {
                cutoffs++;
                cache.put(state.toString(), y._1);
                return y;
            }
            beta = Math.min(beta, y._1);
        }
        cache.put(state.toString(), y._1);
        return y;
    }

    // knowledge!
    public int knowledgeSearch() {
        hasReachedMaxDepth = true;
        if (startDepth > 0){
            return minimax(gameBoard, startDepth);
        }
        H = new MovesToWin();
        return iterativeSearch();
    }

    // Iterative
    public int iterativeSearch() {
        int i = 0;
        int move = -1;
        hasReachedMaxDepth = true;
        // TODO stop if we find a sure win util = 1;
        // TODO make stop after x sec. maybe with an exception

        while (i < x * y && hasReachedMaxDepth) {
            System.out.println("depth: " + i);
            int newMove = minimax(gameBoard, ++i);
            // BRÆJK! if time's up
            if (!isTimeUp()) {
                move = newMove;
            } else {
                break;
            }
        }
        return move;
    }

    private boolean isTimeUp() {
        time = System.currentTimeMillis();
        return (time - start > 10000);
    }

    private int minimax(LongBoard state, int depth) {
        int bestAction = -1;
        cutoffs = 0;
        statesChecked = 0;
        cacheHits = 0;
        cache = new HashMap<>();
        hasReachedMaxDepth = false;
        float y = Integer.MIN_VALUE;
 
        //Generate the valid actions from the start state
        for (int action : generateActions(state)) {
            // Stop if we're out of time
            if (isTimeUp()) break;
            Tuple<Float, HeuristicData> max = min(
                    result(state,action),
                    H.createHeuristic(), Integer.MIN_VALUE, Integer.MAX_VALUE, action, depth - 1
            );
            //If the current action is better than the previous ones, choose this
            if(max._1 > y) {
                bestAction = action;
                y = max._1;
            }

        }
        System.out.println("Turn: " + (state.player+2));
        System.out.println("States: " + statesChecked);
        System.out.println("Cutoffs; "+ cutoffs);
        System.out.println("CacheHits; "+ cacheHits);
        System.out.println("H - value: " + y);
        System.out.println("Move: " + bestAction);
        System.out.println();
        return bestAction;
    }
    
    private LongBoard result(LongBoard state, int action) {
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
            H = new MovesToWin();
        }
    }

    public Winner gameFinished() {
        return gameFinished(gameBoard);
    }

    public static Winner gameFinished(LongBoard board) {
        return board.hasWon() ?
                ((board.player & 1) == 0 ? Winner.PLAYER1 : Winner.PLAYER2) :
                (board.player == board.SIZE-1 ? Winner.TIE : Winner.NOT_FINISHED);
    }
    
    public void insertCoin(int column, int playerID) {
        startDepth--;
        gameBoard.move(column);
    }

    public int decideNextMove() {
        start = System.currentTimeMillis();
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
        public T createHeuristic();
        public T moveHeuristic(T data, int column, int player);
        public Tuple<Float, T> h(LongBoard board, T data);
    }

    private class baseLookUp implements Heuristic {
        public HeuristicData createHeuristic() { return null; }
        public HeuristicData moveHeuristic(HeuristicData blah, int blah1, int blah2) {return null;}
        public Tuple<Float, HeuristicData> h(LongBoard board, HeuristicData data) {
            Float ret = knowledgeBase.get(board.toString());
            if (ret == null){
                ret = 0f;
            }
            ret = playerID == 1 ? ret : -ret;

            return new Tuple<Float, HeuristicData>(ret, null);
        }
    }

    private class Threats implements Heuristic {

        public HeuristicData createHeuristic() { return null; }
        public HeuristicData moveHeuristic(HeuristicData data, int column, int p) { return null; }

		public Tuple<Float, HeuristicData> h(LongBoard state, HeuristicData data) {
			int player;
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
		/*for (Integer set : lists.get(0)) {
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
			}*/
		 
		 int neg;
		 if(playerID == 1) {
			 neg = 1;
		 } else {
			 neg = -1;
		 }
		 
		 for (Integer pos : AODD) {
			int col = pos/state.HEIGHT;
			if(!ThreatsBelow(col, pos, BEVEN) && !ThreatsInOtherColums(col,BODD)) {
				//WIN FOR A
				return new Tuple<>(0.9F*neg,null);
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
			 return new Tuple<>(0.9F * neg,null);
		 }
		 
		 if(BEVEN.size() > 0) {
			 return new Tuple<>(-0.9F *neg,null);
		 }
				
		 return new Tuple<>(0F,null);
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

    /**
     * A heuristic that considers moves to win.
     */
    private class MovesToWin implements Heuristic<MovesToWin.MTWData> {

        public MTWData createHeuristic() { return new MTWData(); }
        public MTWData moveHeuristic(MTWData oldData, int column, int player) {
            // First duplicate and update the board
            MTWData newData = new MTWData(oldData);
            newData.move(column, player);

            // Find the new MTWCs for both players and return
            updateMTWCs(newData);
            return newData;
        }

        /**
         * Traces the number of coins and free spaces (tupled) in the given direction.
         */
        private Tuple<Integer, Integer> traceDirection(Set<Tuple<Integer, Integer>> set,
                                                       int board[][], int player,
                                                       int startX, int startY, int dx, int dy) {
            Tuple<Integer, Integer> res = new Tuple<>(0, 0);
            // Return if we are outside the board
            if ((startX >= x || startX < 0) || (startY >= y || startY < 0)) return res;
            int value = board[startX][startY];

            // Add the coordinate
            if (value == player) {
                set.add(new Tuple<>(startX, startY));
                res._1++;
            } else if (value == 0) {
                res._2++;
            }

            // Traverse recursively if the other player is not in the way
            if (value == 0 || value == player) {
                Tuple<Integer, Integer> newRes =
                        traceDirection(set, board, player, startX + dx, startY + dy, dx, dy);
                res._1 += newRes._1;
                res._2 += newRes._2;
            }

            return res;
        }

        private void addMTWCs(MTWData data, int x, int y, int dx, int dy) {
            // Create set of tuples with coordinates - with annoying syntax
            Set<Tuple<Integer, Integer>> set = new HashSet<Tuple<Integer, Integer>>();
            // Trace in the given direction and it's opposite direction
            Tuple<Integer, Integer> first  = traceDirection(set, data.board, data.player,      x,       y,  dx,  dy);
            Tuple<Integer, Integer> second = traceDirection(set, data.board, data.player, x - dx,  y - dx, -dx, -dy);
            // Add to heuristic data if not empty and the WC is large enough to give a win
            if (!set.isEmpty() && first._1 + first._2 + second._1 + second._2 > 3) {
                int mTW = 4 - first._1 + second._1;

                if (data.player == playerID) {
                    data.mTWCFP1.add(set);
                    if (mTW < data.mTW1) data.mTW1 = mTW;
                }
                else {
                    data.mTWCFP2.add(set);
                    if (mTW < data.mTW2) data.mTW2 = mTW;
                }

            }
        }

        /**
         * Removes MTWCs from the opponent that are 'destroyed' by the latest move.
         */
        private void removeBrokenMTWCs(MTWData data) {
            Set<Set<Tuple<Integer, Integer>>> set;
            if (data.player == playerID) {
                set = data.mTWCFP2;
            } else {
                set = data.mTWCFP1;
            }
            Iterator<Set<Tuple<Integer, Integer>>> combinations = set.iterator();
            while (combinations.hasNext()) {
                Set<Tuple<Integer, Integer>> combination = combinations.next();
                for (Tuple<Integer, Integer> coordinate : combination) {
                    // Remove the combination if it contains the current coordinate
                    // (safely by using the iterator remove)
                    if (coordinate._1.equals(data.column) && coordinate._2.equals(data.row)) {
                        combinations.remove();
                        break;
                    }
                }
            }
        }

        private void updateMTWCs(MTWData data) {
            // Delete old MTWCs and reset the MTW for the current player
            if (data.player == playerID) {
                data.mTWCFP1 = new HashSet<>();
                data.mTW1    = 4;
            } else {
                data.mTWCFP2 = new HashSet<>();
                data.mTW2    = 4;
            }


            // Remove old MTWCs
            removeBrokenMTWCs(data);

            // Trace horizontal
            addMTWCs(data, data.column, data.row, 1, 0);

            // Trace vertical
            addMTWCs(data, data.column, data.row, 0, 1);

            // Trace from left to right
            addMTWCs(data, data.column, data.row, -1, 1);

            // Trace from right to left
            addMTWCs(data, data.column, data.row, -1, -1);
        }

        public Tuple<Float, MTWData> h(LongBoard board, MTWData data) {
            // Calculate and return heuristic value (between -1 and 1)
            float h = (data.mTW1 - data.mTW2 * 1.25f) / 5;
            return new Tuple<>(h, data);
        }

        /**
         * Data for the MTW heuristic containing the board (with the moves) and a list of the possible wins.
         */
        class MTWData implements HeuristicData {
            // The board for the current state
            int board[][] = new int[x][y];
            // Moves to win combinations for player 1 (mTWCFP1)
            Set<Set<Tuple<Integer, Integer>>> mTWCFP1;
            // Moves to win combinations for player 2 (mTWCFP2)
            Set<Set<Tuple<Integer, Integer>>> mTWCFP2;

            int column, row, player, mTW1, mTW2;

            /**
             * Creates a MTWData board with no initial coins set.
             */
            MTWData() {
                mTWCFP1 = new HashSet<>(); mTWCFP2 = new HashSet<>();
                mTW1 = 4; mTW2 = 4;
            }

            /**
             * Constructs a copy of a MTWData object.
             */
            MTWData(MTWData old) {
                for (int i = 0; i < x; i++) {
                    System.arraycopy(old.board[i], 0, board[i], 0, y);
                }
                this.mTWCFP1 = new HashSet<>(old.mTWCFP1);
                this.mTWCFP2 = new HashSet<>(old.mTWCFP2);
                this.mTW1 = old.mTW1;
                this.mTW2 = old.mTW2;
            }

            /**
             * Adds a coin to the current board in the given column.
             */
            public void move(int column, int player) {
                this.column = column;
                this.player = player;
                for (row = 0; row < y; row++) {
                    if (board[column][row] == 0) {
                        board[column][row] = player;
                        break;
                    }
                }
            }
        }
    }

    /**
     * A board that is based on a representation of a single long per player.
     */
    public class LongBoard {
        // -- The following comments are made for Sigurt, who cannot see the errors in his ways -- //
        // -- Jens is ukraine                                                                   -- //
        long boards[]; // Zero-indexed boards for player one (0) and player two (1).
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

        private int getBit(long l, int n){
            return (int)((l >> n) & 1L);
        }

        @Override
        public String toString() {
            StringBuilder sBuff = new StringBuilder();
            for (int i=0; i < SIZE1; i++){
                if ((i+1)%(H1) == 0) continue;
                int oppBoard = getBit(boards[opponentID -1], i);
                int playBoard = getBit(boards[playerID -1], i);
                String slotState = "";
                if (oppBoard == 1) {
                    slotState = "o";
                } else if (playBoard == 1) {
                    slotState = "x";
                } else {
                    slotState = "b";
                }

                sBuff.append(slotState);
                if (i < SIZE1 -2){
                   sBuff.append(",");
                }
            }
            return sBuff.toString();
        }
    }

    //initialize knowledge base from file
    private void initKnowledge(){
        knowledgeBase = new HashMap<>();
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
    }
}
// vim: set ts=4 sw=4 expandtab:
