import java.util.ArrayList;

public class GLaDOS implements IGameLogic {

    private int x = 0, y = 0, lastMoveColumn = -1, lastMovePlayer = -1;
    private int playerID;
    private int[][] gameBoard;
    
    public GLaDOS() {
        //TODO Write your implementation for this method
    }

    //TODO expand from center
    private ArrayList<Integer> generateActions(int[][] state) {
	ArrayList<Integer> result = new ArrayList();
	for (int i=0; i < x; i++){
		if (gameBoard[i][0] == 0) {
			result.add(i);
		}
	}
    }

    private Winner gameFinnished(int[][] state, int lastmoveCol) {

    }

    private int utility(int[][] state, int lastMove){
	    Winner win = gameFinnished(state, lastMove);
	    if (win == Winner.TIE) {
		    return 0;
	    } else if (win.ordinal() == playerID) {
		    return 1;
	    } else if (win == Winner.NOT_FINISHED) {
		    throw new IlligalArgumentException("Faggot");
	    } else {
		   return -1;
	    }
    }

    private int max(int[][] state, int action) {
	
    }

    private int min(int[][] state, int action) {

    }

    private int minimax(int[][] state) {
	
    }

    public void initializeGame(int x, int y, int playerID) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        gameBoard = new int[x][y];
        //TODO Write your implementation for this method
    }
	
    public Winner gameFinished() {
        if (lastMoveColumn > 0) {
            return Winner.NOT_FINISHED;
        } else {
            return Winner.NOT_FINISHED;
        }
    }


    public void insertCoin(int column, int playerID) {
        lastMoveColumn = column;
        lastMovePlayer = playerID;
        //TODO Write your implementation for this method	
    }

    public int decideNextMove() {
        //TODO Write your implementation for this method
        return 0;
    }

}

