package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {   
        int MOVE = AB(currentBoard, true, player, 1, -789, 789); // Aiming for "GRADE C", having conditions: MinMax with DFS and alpha-beta pruning stopping at pre - defined depth level (>4). 
         return MOVE;
    }
   
    public int AB(GameState currentBoard, boolean maxPlayer, int player, int depth, int A, int B) {
        // A -> Alpha (highest value found for maxPlayer)
        // B -> Beta (lowest value found for minPlayer)
        int val = 0;    
        int MOVE = 0;
        Integer eval;          
        
        if (depth == 7 || currentBoard.gameEnded()) // if it reached an end, evaluating the score.
        {
            
            if (player == 1) // for maxPlayer
            {
             return (currentBoard.getScore(1) - currentBoard.getScore(2));
            }
            
            else  //for minPlayer
            {
             return (currentBoard.getScore(2) - currentBoard.getScore(1));   
            }
//            if(player == 1)
//            {   
//                return( currentBoard.getScore(Conv(maxPlayer)) - currentBoard.getScore(Conv(!maxPlayer)+1) );
//            }
//            
//            else
//            {
//                return( currentBoard.getScore(Conv(!maxPlayer) + 1) - currentBoard.getScore(Conv(maxPlayer)) );
//            }
        }

        if (maxPlayer) // for MaxPlayer
        { 
            eval = -789; //giving it an infinite value
            for (int i = 1; i < 7; i++) { // checking all possible moves from the current state
                if (currentBoard.moveIsPossible(i)) { //to check if its a legal move
                           
                    GameState newState = currentBoard.clone(); // cloning the GameState everytime 
                    newState.makeMove(i); // making moves on the new cloned GameState
                               
                    if(newState.getNextPlayer() == player) // to check if its a free turn
                    {
                        maxPlayer = true;
                    }
                    else
                    {
                        maxPlayer = false; 
                    }
                   
                    val = AB(newState, maxPlayer, player, depth + 1, A, B); // recursive calling for extra turn; if not, continuing the gameplay
                    if (val > eval) {    
                        eval = val;
                        MOVE = i; //setting highest value to eval and returning the respective move to make.
                    }
                    
                        A = MAX(A, eval); //updating alpha value
                    

                    if (B <= A) // comparing alpha and beta values
                    { 
                        break;
                    }
                }
               
            }
        } else { // for MinPlayer
            eval = 789; //giving it an infinite value
            int j;
            for ( j = 1; j < 7; j++) { // checking all possible moves from the current state
                if (currentBoard.moveIsPossible(j)) { //to check if its a legal move
               
                    
                    GameState newBoard = currentBoard.clone(); // cloning the GameState everytime 
                    newBoard.makeMove(j); // making moves on the new cloned GameState
                                                           
                    if(newBoard.getNextPlayer() == player) // to check if its a free turn
                    { 
                       maxPlayer = true;
                    }
                    else
                    { 
                       maxPlayer = false;                     
                    } 
                   
                    val = AB(newBoard, maxPlayer, player, depth + 1, A, B); // recursive calling for extra turn; if not, continuing the gameplay
                    if (val < eval) {  
                        eval = val;
                        MOVE = j; //setting highest value to eval and returning the respective move to make.
                    }
                   
                        B = MIN(B, eval); //updating beta value
                    

                    if (B <= A) { // comparing alpha and beta values
                        break;
                    }
                }
            }
          
        }
       
        if (depth != 1) { // returning MOVE instead of eval when depth is 1,else compute eval and return it 
            MOVE = eval;
        }
       
        return MOVE;
    }
    private int MAX(int X, int Y){ // to calculate maximum of two values
        if(X > Y)
        {  
            return(X);
        }
        else
        {
            return(Y);
        }
    }
    
    private int MIN(int X, int Y){ // to calculate minimum of two values
        if(X < Y)
        {  
            return(Y);
        }
        else
        {
            return(X);
        }
    }
//    private int Conv(boolean bool) { // to convert boolean to int
//         if(bool)
//         {
//             return(1);
//         }
//         else
//         {
//             return(0);
//         }
    
    }
   
