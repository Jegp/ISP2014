import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    private int startDepth = 8;
    private Heuristic H;

    private ArrayList<Integer> generateActions(LongBoard state) {
    ArrayList<Integer> result = new ArrayList<Integer>();
    int middle = x/2;
  //TODO choose random when x is even
    if (state.height[middle] == (byte) (state.H1 * middle)) {
        result.add(middle);
    }
    for (int i=1; i <= x/2; i++){
        if(middle + i < x) {
            if (state.height[middle + i] == (byte) (state.H1 * (middle + i))) {
                result.add(middle + i);
            }
        }
        if(middle - i > -1) {
            if (state.height[middle - i] == (byte) (state.H1 * (middle - i))) {
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
        if(win != Winner.NOT_FINISHED) return utility(win);

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
            H = new MovesToWin();
        }
    }

    public Winner gameFinished() {
        return gameFinished(gameBoard);
    }

    public static Winner gameFinished(LongBoard board) {
        return board.hasWon() ?
                (board.player == 0 ? Winner.PLAYER1 : Winner.PLAYER2) :
                (board.player == board.SIZE - 1 ? Winner.TIE : Winner.NOT_FINISHED);
    }
    
    public void insertCoin(int column, int playerID) {
        gameBoard.move(column);
    }

    public int decideNextMove() {
        return knowledgeSearch();
    }

    public interface Heuristic {
        public float h(LongBoard state, Integer lastMove);
    }

    private class baseLookUp implements Heuristic {
        public float h(LongBoard state, Integer ignored_var) {
            return 1f;
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
        int player;
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
            long board = boards[player & 1];
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
            boards[player & 1] ^= 1L << height[column];
            player++;
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
