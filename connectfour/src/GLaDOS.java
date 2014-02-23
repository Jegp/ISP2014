
public class GLaDOS implements IGameLogic {

    private int x = 0, y = 0, lastMoveColumn = -1, lastMovePlayer = -1;
    private int playerID;
    private int[][] gameBoard;
    
    public GLaDOS() {
        //TODO Write your implementation for this method
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
        int r = gameBoard[column].length-1;
        while(gameBoard[column][r]!=0) r--;
        gameBoard[column][r]=playerID;	
        lastMoveColumn = column;
        lastMovePlayer = playerID;
    }

    public int decideNextMove() {
        //TODO Write your implementation for this method
        return 0;
    }

}
