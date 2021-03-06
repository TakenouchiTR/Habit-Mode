package habit_mode.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import habit_mode.model.sudoku.SudokuPuzzle;

import org.zeromq.ZContext;

/**
 * A proper implementation of the server communication protocol indicated by the 
 * ServerCommunicator abstract class. 
 * Uses ZeroMQ and Gson to facilitate proper communication. 
 *  
 * @author Team 1
 * @version Spring 2022
 */
public class ServerServerCommunicator extends ServerCommunicator {
    private static final String REQUEST_TYPE = "request_type";
    private static final String REQUEST_TYPE_REGISTER_USER = "register_user";
    private static final String REQUEST_TYPE_LOGIN = "login";
    private static final String REQUEST_TYPE_ADD_HABIT = "add_habit";
    private static final String REQUEST_TYPE_REMOVE_HABIT = "remove_habit";
    private static final String REQUEST_TYPE_MODIFY_HABIT = "modify_habit";
    private static final String REQUEST_TYPE_COMPLETE_HABIT = "complete_habits";
    private static final String REQUEST_TYPE_RETRIEVE_DATA = "retrieve_data";
    private static final String REQUEST_TYPE_GENERATE_PUZZLE = "generate_sudoku_puzzle";
    private static final String REQUEST_TYPE_UPDATE_PUZZLE = "update_sudoku_puzzle";
    private static final String REQUEST_TYPE_BUY_HINT = "buy_hint";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static final String COINS = "coins";
    private static final String SUCCESS_CODE = "success_code";
    private static final String AUTHENTICATION_TOKEN = "authentication_token";
    private static final String FIELDS = "fields";
    private static final String HABITS = "habits";
    private static final String PUZZLE = "sudoku_puzzle";
    private static final String HABIT_NAME = "habit_name";
    private static final String HABIT_FREQ = "habit_frequency";
    private static final String HABIT_ID = "habit_id";
    private static final String HABIT_IDS = "habit_ids";
    private static final String NUMBERS = "numbers";
    private static final String NUMBER = "number";
    private static final String ROW = "row";
    private static final String COL = "col";
    private static final String LOCKS = "number_locks";
    
    
    private static final ZContext CONTEXT = new ZContext();
    private static final Type TYPE = new TypeToken<HashMap<String, Object>>() { } .getType();

    private SuccessCode successCode;
    private ZMQ.Socket socket;
    private Gson gson;
    private HashMap<String, Object> message;
    private String jsonMessage;
    private HashMap<String, Object> response;
    private String authenticationToken;
    private String jsonResponse;
    private String[] fields;
    private String tcpAddress;
    private int coins;

    /**
     * The default constructor for ServerServerCommunicator. 
     * 
     * @precondition none
     * @postcondition this.socket.getSocketType == SocketType.REQ &&
     *                this.getGson() == new Gson() &&
     *                this.getMessage() == {} &&
     *                this.authenticationToken == ""
     *                this.fields.length == 1;
     * 
     */
    public ServerServerCommunicator() {
        this.socket = CONTEXT.createSocket(SocketType.REQ);
        this.gson = new Gson();
        this.message = new HashMap<String, Object>();
        this.tcpAddress = "tcp://127.0.0.1:5555";
        this.authenticationToken = "";
        this.fields = new String[1];
        this.coins = 0;
    }

    /**
     * An overloaded constructor for testing purposes. 
     * 
     * @precondition tcpAddress != null 
     * @postcondition this.socket.getSocketType == SocketType.REQ &&
     *                this.getGson() == new Gson() &&
     *                this.getMessage() == {} &&
     *                this.authenticationToken == ""
     *                this.fields.length == 1;
     * 
     * @param tcpAddress The address for the client to connect to.
     */
    public ServerServerCommunicator(String tcpAddress) {
        this();
        this.tcpAddress = tcpAddress;        
    }

    /**
     * Simple getter for the message hashmap.
     * 
     * @return The message hashmap.
     */
    public HashMap<String, Object> getMessage() {
        return this.message;
    }

    /**
     * Simple getter for the gson.
     * 
     * @return The gson as a Gson object.
     */
    public Gson getGson() {
        return this.gson;
    }

    /**
     * Simple getter for the formatted json message.
     * 
     * @return The jsonMessage as a string formatted in json notation.
     */
    public String getJsonMessage() {
        return this.jsonMessage;
    }

    /**
     * Simple getter for the socket in the current context.
     * 
     * @return The socket inside the current context.
     */
    public ZMQ.Socket getSocket() {
        return this.socket;
    }

    /**
     * Simple getter for the current ZContext.
     * 
     * @return The current context as a ZContext object.
     */
    public ZContext getContext() {
        return CONTEXT;
    }

    /**
     * Simple getter for the authentication token.
     * 
     * @return The authentication token.
     */
    public String getToken() {
        return this.authenticationToken;
    }

    /**
     * Simple setter for the authentication token.
     * 
     * @precondition token != null 
     * 
     * @param token The string to set as the new authentication token.
     */
    public void setToken(String token) {
        if (token == null) {
            return;
        }
        this.authenticationToken = token;
    }

    @Override
    public int[] buyHint() {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_BUY_HINT);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);

        this.sendMessage();

        int[] hint = new int[4];

        hint[0] = ((Double) this.response.get(NUMBER)).intValue();
        hint[1] = ((Double) this.response.get(ROW)).intValue();
        hint[2] = ((Double) this.response.get(COL)).intValue();
        hint[3] = ((Double) this.response.get(COINS)).intValue();

        return hint;
    }

    @Override
    public SuccessCode registerCredentials(String username, String password, String email) {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_REGISTER_USER);
        this.message.put(USERNAME, username);
        this.message.put(PASSWORD, password);
        this.message.put(EMAIL, email);

        this.sendMessage();

        this.successCode = SuccessCode.checkValues(this.response.get(SUCCESS_CODE));

        return this.successCode;
    }

    @Override
    public SuccessCode validateLogin(String username, String password) {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_LOGIN);
        this.message.put(USERNAME, username);
        this.message.put(PASSWORD, password);
        
        this.sendMessage();

        this.authenticationToken = (String) this.response.get(AUTHENTICATION_TOKEN);

        return SuccessCode.checkValues(this.response.get(SUCCESS_CODE));
    }

    @Override
    public int getCoins() {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_RETRIEVE_DATA);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        this.fields[0] = COINS;

        this.message.put(FIELDS, this.fields);

        this.sendMessage();

        Double coins = (Double) this.response.get(COINS);
        this.setCoins(coins.intValue());
        

        return this.coins;
    }
    
    @Override
    public List<Habit> getHabits() {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_RETRIEVE_DATA);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        
        this.fields[0] = HABITS;

        this.message.put(FIELDS, this.fields);

        this.sendMessage();
       
        return this.parseRetrieveHabitsResponse();
    }

    @Override
    public SudokuPuzzle getSudokuPuzzle() {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_RETRIEVE_DATA);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        this.fields[0] = PUZZLE;
        this.message.put(FIELDS, this.fields);

        this.sendMessage();
        if (this.response.get("sudoku_puzzle") == null) {
            return null;
        }
        
        return this.parseSudokuPuzzleResponse();
    }

    @Override
    public boolean setCoins(int amount) {
        this.coins = amount;
        if (this.coins == amount) {
            return true;
        }
        return false;
    }

    @Override
    public SuccessCode addHabit(Habit habit) {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_ADD_HABIT);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        this.message.put(HABIT_NAME, habit.getText());
        this.message.put(HABIT_FREQ, habit.getFrequency().ordinal());

        this.sendMessage();

        return SuccessCode.checkValues(this.response.get(SUCCESS_CODE));
    }

    @Override
    public SuccessCode removeHabit(Habit habit) {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_REMOVE_HABIT);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        this.message.put(HABIT_ID, habit.getId());
        
        this.sendMessage();

        return SuccessCode.checkValues(this.response.get(SUCCESS_CODE));
    }

    @Override
    public SuccessCode modifyHabit(Habit habit) {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_MODIFY_HABIT);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        this.message.put(HABIT_NAME, habit.getText());
        this.message.put(HABIT_FREQ, habit.getFrequency().ordinal());
        this.message.put(HABIT_ID, habit.getId());
        
        this.sendMessage();

        return SuccessCode.checkValues(this.response.get(SUCCESS_CODE));
    }

    @Override
    public SuccessCode completeHabit(Habit habit) {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_COMPLETE_HABIT);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        int[] ids = {habit.getId()};

        this.message.put(HABIT_IDS, this.gson.toJsonTree(ids));

        this.sendMessage();
        Double coins = (Double) this.response.get(COINS);
        this.setCoins(coins.intValue());

        return SuccessCode.checkValues(this.response.get(SUCCESS_CODE));
    }

    @Override
    public SuccessCode updateSudokuPuzzle(SudokuPuzzle puzzle) {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_UPDATE_PUZZLE);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);
        this.message.put(NUMBERS, puzzle.getNumbers());

        this.sendMessage();

        SuccessCode code = SuccessCode.checkValues(this.response.get(SUCCESS_CODE));

        return code;
    }

    @Override 
    public SudokuPuzzle generateSudokuPuzzle() {
        this.message.put(REQUEST_TYPE, REQUEST_TYPE_GENERATE_PUZZLE);
        this.message.put(AUTHENTICATION_TOKEN, this.authenticationToken);

        this.sendMessage();

        return this.parseSudokuPuzzleResponse();
    }

    private void sendMessage() {
        this.socket.connect(this.tcpAddress);

        this.jsonMessage = this.gson.toJson(this.message);

        this.socket.send(this.jsonMessage);

        this.jsonResponse = this.socket.recvStr();
        this.response = this.gson.fromJson(this.jsonResponse, TYPE);

        this.socket.disconnect(this.tcpAddress);
        this.message.clear();
    }

    private List<Habit> parseRetrieveHabitsResponse() {
        List<Habit> habits = new ArrayList<Habit>();
        ArrayList<LinkedTreeMap<String, Object>> map = (ArrayList<LinkedTreeMap<String, Object>>) this.response.get(HABITS);
       
        for (LinkedTreeMap<String, Object> habMap : map) {
            Double freque = (Double) habMap.get("frequency");
            Double idd = (Double) habMap.get("id");
            Frequency frequency;
            switch(freque.intValue()) {
                case 0: frequency = Frequency.DAILY;
                        break;
                case 1: frequency = Frequency.WEEKLY;
                        break;
                case 2: frequency = Frequency.MONTHLY;
                        break;
                default: frequency = Frequency.DAILY;
            }
            Habit habit = new Habit((String) habMap.get("name"), frequency);
            habit.setId(idd.intValue());
            habit.completionProperty().set((boolean) habMap.get("is_complete"));
            habits.add(habit);
        }
        return habits;
    }

    private SudokuPuzzle parseSudokuPuzzleResponse() {
        LinkedTreeMap<String, Object> puzzleMap = (LinkedTreeMap<String, Object>) this.response.get(PUZZLE);
        ArrayList<ArrayList<Double>> numberLists = (ArrayList<ArrayList<Double>>) puzzleMap.get(NUMBERS);
        ArrayList<ArrayList<Boolean>> numberLocks = (ArrayList<ArrayList<Boolean>>) puzzleMap.get(LOCKS);
        
        int[][] numbers = new int[numberLists.size()][numberLists.size()];
        boolean[][] locks = new boolean[numberLists.size()][numberLists.size()];

        for (int row = 0; row < numberLists.size(); row++) {
            for (int col = 0; col < numberLists.get(row).size(); col++) {
                numbers[row][col] = numberLists.get(row).get(col).intValue();
            }
        }

        for (int row = 0; row < numberLocks.size(); row++) {
            for (int col = 0; col < numberLocks.get(row).size(); col++) {
                locks[row][col] = numberLocks.get(row).get(col).booleanValue();
            }
        }
        
        SudokuPuzzle puzzle = new SudokuPuzzle(numbers, locks);
        return puzzle;
    }

}