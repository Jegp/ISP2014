/**
 * The cake is a lie.
 */
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
        return gameFinished(gameBoard, lastMoveColumn);
    }

    /**
     * Tests whether the given player has one on a board with the given last move (column and player).
     */
    private static Winner gameFinished(int[][] board, int lastMoveColumn) {
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
            for (int n = 0; n < board[n].length; n++) {
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
                    if (board[n][row] > 0) playerID = board[n][row];
                    coherentFields = 0;
                }
            }

            // Vertical

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
    }

    public int decideNextMove() {
        //TODO Write your implementation for this method
        return 0;
    }

}
