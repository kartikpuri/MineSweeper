package com.example.kartik.assign2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class GridActivity extends AppCompatActivity implements View.OnClickListener {

    //Drawable array for images
    int[] myImageList = new int[]{R.drawable.open0, R.drawable.open1, R.drawable.open2, R.drawable.open3,
            R.drawable.open4, R.drawable.open5, R.drawable.open6, R.drawable.open7, R.drawable.open8};

    public GridView gv;                                 //Grid View Object
    int stepCount = 0;                                  //Variable to count number of steps
    static int MINES_COUNT = 0;                         //Gets value from Main Activity
    int flagsRemaining;                                 //Varaible to hold count of flags left
    public int[] items = new int[81];                   //2D array that contains contains count of all the elements
    public char[] dataBase = new char[81];              //A database that conatins current status of each sqaure
    int timerCount = 0;                                 //Clock Variable
    HashSet<Integer> listOfMines = new HashSet<Integer>();    //Holds the postions of all the mines

    TextView steps;                 //Shows value of stepCounts Varaible
    TextView timer;                 //Shows value of timerCount Variable
    TextView flagCount;             //Shows flags remaining
    ImageView smiley;               //Shows animation with smileys
    Button reset;                   //Reset Button object

    Thread th;                      //Thread to run the timer


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        flagCount = (TextView) findViewById(R.id.flagCount);        //Get Flag Counter Object

        flagsRemaining = MINES_COUNT;
        flagCount.setText("Flags left: " +flagsRemaining);          //Set  Flag Counter
        reset = (Button) findViewById(R.id.reset);                  //Get Reset button object
        reset.setOnClickListener(this);
        if(MINES_COUNT == 9){ //Beginner Mode
            reset.setText("Reset Beginner Mode");
        }
        else if(MINES_COUNT == 24){ //Intermediate Mode
            reset.setText("Reset Intermediate Mode");
        }
        else{ //Advanced Mode
            reset.setText("Reset Advanced Mode");
        }

        timer = (TextView) findViewById(R.id.timer);                //Get Timer Object
        smiley = (ImageView) findViewById(R.id.smiley);             //Get Simley Object



        steps = (TextView) findViewById(R.id.stepCount);

        th = new Thread() {
            public void run() { //Timer Thread
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timerCount++;
                                timer.setText("" + timerCount + " sec");

                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };//#Timer Thread
        th.start();                     //#Start The Timer Thread


        mineGenerator();                //Call to generate random mines
        countNeighbours();              //Call to calculate mines in neighbour cells

        for (int i = 0; i < 81; i++) { //Intialize database to blank cells
            dataBase[i] = 'b';
        }


        gv = (GridView) findViewById(R.id.mygrid);      //Grid View Object
        CustomGridAdapter customGridAdapter = new CustomGridAdapter(GridActivity.this, items);
        gv.setAdapter(customGridAdapter);

        /*
        Click on Grid Elements
         */
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                stepCount++;
                steps.setText("" + stepCount + " steps");

                if (items[position] == 0 && dataBase[position] == 'b') { //If an empty cell us presesed
                    View selectedItem = gv.getChildAt(position);
                    ImageView iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    /*
                    https://stackoverflow.com/questions/9481334/how-to-replace-r-drawable-somestring
                     */
                    Log.v("CheckValue", "Value we are checking is: " + "open" + items[position]);
                    iv.setImageResource(myImageList[items[position]]);
                    iv.setEnabled(false);
                    dataBase[position] = '0';
                    ArrayList<Integer> zeroList = new ArrayList<Integer>();
                    zeroList.add(position);
                    clearZeros(zeroList);
                }//#If an empty cell us presesed
                else if (items[position] == -1 && dataBase[position] == 'b') { //If a mines is pressed
                    gameLost(position);
                }//#If a mines is pressed
                else {
                    if (dataBase[position] == 'b') {
                        View selectedItem = gv.getChildAt(position);
                        ImageView iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                        iv.setImageResource(myImageList[items[position]]);
                        iv.setEnabled(false);
                        dataBase[position] = (char) items[position];
                    }
                }

                //Update the flag counter
                checkFlags();
                //After every move from user, The application nees to check if user has won or not
                checkForWin();
            }
        });
        /*
        For Long click from user
         */
        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                View selectedItem = gv.getChildAt(position);
                ImageView iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                switch (dataBase[position]) {
                    case 'b':           //Add Flag
                        if(flagsRemaining >0 ) {
                            iv.setImageResource(R.drawable.bombflagged);
                            dataBase[position] = 'f';
                            iv.setEnabled(false);
                            flagsRemaining--;
                            flagCount.setText("Flags left: " +flagsRemaining);
                        }
                        else{           //User has used all the flags
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GridActivity.this);
                            alertBuilder.setMessage("Please remove some flags to continue or open uncovered boxes to check for win!")
                                    .setCancelable(false)
                                    .setPositiveButton("Thanks", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                            AlertDialog alertDialog = alertBuilder.create();
                            alertDialog.setTitle("All flags used");
                            alertDialog.show();
                        }
                        break;
                    case 'f':           //Remove a flag
                        iv.setEnabled(true);
                        iv.setImageResource(R.drawable.blank);
                        dataBase[position] = 'b';
                        flagsRemaining++;
                        flagCount.setText("Flags left: " +flagsRemaining);
                }
                //Return true otherwise OnItemClickListener will also be called with this method
                return true;
            }
        });
    }

    /*
    CREATING MINES RANDOMLY
    Use hashset to store mines postions
    So that one postions holds one mine only
     */
    public void mineGenerator() {
        Log.d("mineGenerator", "Mine Generator called");
        Random rn = new Random();
        while (listOfMines.size() != MINES_COUNT) {
            listOfMines.add(rn.nextInt(81));
        }

        for (int mine : listOfMines) {
            items[mine] = -1;
        }
    }

    /*
    COUNT THE NEIGHBOURS
     */
    public void countNeighbours() {
        Log.d("countNeighbours", "Count Neigghbours called");
        for (int element = 0; element < 81; element++) {

            if (items[element] == -1) {
                continue;
            }

            //Get value of columns and rows for items 2D array
            int col = element % 9;
            int row = element / 9;

            if (col > 0 && row > 0 && items[element - 10] == -1) { //top left element
                items[element]++;
            }
            if (row > 0 && items[element - 9] == -1) { //top element
                items[element]++;
            }
            if (col < 8 && row > 0 && items[element - 8] == -1) { //top right element
                items[element]++;
            }
            if (col > 0 && items[element - 1] == -1) { //left element
                items[element]++;
            }
            if (col < 8 && items[element + 1] == -1) { //right element
                items[element]++;
            }
            if (col > 0 && row < 8 && items[element + 8] == -1) { //bottom left element
                items[element]++;
            }
            if (row < 8 && items[element + 9] == -1) { //bottom element
                items[element]++;
            }
            if (col < 8 && row < 8 && items[element + 10] == -1) { //bottom right element
                items[element]++;
            }
        }
    }

    /*
    CLEARNING EMPLTY POSITIONS/ZEROES
    A recurssive call helps to clear the zeroes
     */
    public void clearZeros(ArrayList<Integer> zeroList) {
        if (zeroList.size() == 0) { //Base Condition, until list is not empty
            return;
        } else {
            int pos = zeroList.get(0);
            int col = pos % 9;
            int row = pos / 9;
            zeroList.remove(0);
            View selectedItem;
            ImageView iv;

            if (items[pos] == 0) { //Enter if the element has no mine in neighbour

                if (col > 0 && row > 0) { //top left element
                    selectedItem = gv.getChildAt(pos - 10);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos - 10] == 0 ) {
                        zeroList.add(pos - 10);
                    }
                    iv.setImageResource(myImageList[items[pos - 10]]);
                    iv.setEnabled(false);
                    dataBase[pos - 10] = (char) items[pos - 10];
                }//#top left element
                if (row > 0) { //top element
                    selectedItem = gv.getChildAt(pos - 9);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos - 9] == 0) {
                        zeroList.add(pos - 9);
                    }
                    iv.setImageResource(myImageList[items[pos - 9]]);
                    iv.setEnabled(false);
                    dataBase[pos - 9] = (char) items[pos - 9];
                }//#top element
                if (col < 8 && row > 0) { //top right element
                    selectedItem = gv.getChildAt(pos - 8);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos - 8] == 0) {
                        zeroList.add(pos - 8);
                    }
                    iv.setImageResource(myImageList[items[pos - 8]]);
                    iv.setEnabled(false);
                    dataBase[pos - 8] = (char) items[pos - 8];
                }//#top right element
                if (col > 0) { //left element
                    selectedItem = gv.getChildAt(pos - 1);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos - 1] == 0) {
                        zeroList.add(pos - 1);
                    }
                    iv.setImageResource(myImageList[items[pos - 1]]);
                    iv.setEnabled(false);
                    dataBase[pos - 1] = (char) items[pos - 1];
                }//#left element
                if (col < 8) { //right element
                    selectedItem = gv.getChildAt(pos + 1);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos + 1] == 0) {
                        zeroList.add(pos + 1);
                    }
                    iv.setImageResource(myImageList[items[pos + 1]]);
                    iv.setEnabled(false);
                    dataBase[pos + 1] = (char) items[pos + 1];
                }//#right element
                if (col > 0 && row < 8) { //bottom left element
                    selectedItem = gv.getChildAt(pos + 8);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos + 8] == 0) {
                        zeroList.add(pos + 8);
                    }
                    iv.setImageResource(myImageList[items[pos + 8]]);
                    iv.setEnabled(false);
                    dataBase[pos + 8] = (char) items[pos + 8];
                }//#bottom left element
                if (row < 8) { //bottom element
                    selectedItem = gv.getChildAt(pos + 9);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos + 9] == 0) {
                        zeroList.add(pos + 9);
                    }
                    iv.setImageResource(myImageList[items[pos + 9]]);
                    iv.setEnabled(false);
                    dataBase[pos + 9] = (char) items[pos + 9];
                }//#bottom element
                if (col < 8 && row < 8) { //bottom right element
                    selectedItem = gv.getChildAt(pos + 10);
                    iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
                    if (iv.isEnabled() == true && items[pos + 10] == 0) {
                        zeroList.add(pos + 10);
                    }
                    iv.setImageResource(myImageList[items[pos + 10]]);
                    iv.setEnabled(false);
                    dataBase[pos + 10] = (char) items[pos + 10];
                }//#bottom right element
            }
            clearZeros(zeroList);
        }
    }

    /*
    CHECK IF USER CLICKED ON A MINE
     */
    public void gameLost(int pos) {
        View selectedItem = gv.getChildAt(pos);
        ImageView iv = (ImageView) selectedItem.findViewById(R.id.gridImage);

        //Display the mine that user has clicked on
        iv.setImageResource(R.drawable.bombdeath);
        iv.setEnabled(false);
        listOfMines.remove(pos);
        //Display rest of the mines
        for (int item : listOfMines) {
            selectedItem = gv.getChildAt(item);
            iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
            iv.setImageResource(R.drawable.bombrevealed);
            iv.setEnabled(false);
        }

        //Change the smiley
        smiley.setImageResource(R.drawable.facedead);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GridActivity.this);
        alertBuilder.setMessage("Exit or restart")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reset();
                    }
                });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setTitle("You Lost, You found a mine");
        alertDialog.show();         //Display alert box and ask whether user wants to exit or restart
    }

    /*
    CHECK IF USER HAS COMPLETED THE GAME
     */
    public void checkForWin() {
        int minesLeft = 0;
        for (char c : dataBase) {
            if (c == 'b' || c == 'f') {     //Check if only mined sqauares are left
                minesLeft++;
            }
        }
        Log.d("Mines Count", "Mines left is: " + minesLeft);
        if (minesLeft == MINES_COUNT) {
            smiley.setImageResource(R.drawable.facepirate);
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GridActivity.this);
            alertBuilder.setMessage("Exit or restart")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    });
            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.setTitle("Congratulations, You Won!!!");
            alertDialog.show();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reset) {
            //Ask for Confirmation
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(GridActivity.this);
            alertBuilder.setMessage("Are you sure that tou want to reset?")
                    .setCancelable(false)
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            reset();
                        }
                    });
            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.setTitle("Reset");
            alertDialog.show();

        }
    }

    /*
    RESET THE GAME
     */
    public void reset() {
        smiley.setImageResource(R.drawable.facesmile);

        listOfMines = new HashSet<Integer>();       //Empty the list of mines
        mineGenerator();                            //Create mines for new game
        countNeighbours();                          //Calculate new Neighbours

        timerCount = 0;                             //Reset Timer
        stepCount = 0;                              //Reset Steps Count Varaible

        View selectedItem;
        ImageView iv;
        for (int i = 0; i < 81; i++) {
            selectedItem = gv.getChildAt(i);
            iv = (ImageView) selectedItem.findViewById(R.id.gridImage);
            iv.setEnabled(true);

            iv.setImageResource(R.drawable.blank);  //Hide all the sqaures
            dataBase = new char[81];                //Empty the current status database
            Arrays.fill(dataBase, 'b');             //Fill the the current status database with blank values
            steps.setText(""+stepCount +" steps");  //Set Counter to zero
            timer.setText("" +timerCount+ " sec");  //Set timer to zero
        }
    }

    void checkFlags()
    {
        int tempFlagCount = 0;
        for(char c : dataBase){
            if(c == 'f'){
                tempFlagCount++;
            }
        }
        flagsRemaining = MINES_COUNT - tempFlagCount;
        flagCount.setText("Flags left: " +flagsRemaining);

    }

}