package com.example.kartik.assign2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
IMAGE SOURCES
https://pixabay.com/en/game-minesweeper-flag-icon-sign-27678/

 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button beginner, intermediate, advanced;
    Button howToPlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Get Objects for Buttons
         */
        beginner = (Button) findViewById(R.id.beginner);
        intermediate = (Button) findViewById(R.id.intermediate);
        advanced = (Button) findViewById(R.id.advanced);
        howToPlay = (Button)findViewById(R.id.howToPlay);

        /*
        Set OnClickListeners for buttons
         */
        beginner.setOnClickListener(this);
        intermediate.setOnClickListener(this);
        advanced.setOnClickListener(this);
        howToPlay.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        Intent intent = new Intent("com.example.kartik.assign2.GridActivity");
        switch (v.getId()){
            case R.id.beginner:         //Start the game in Beginner Mode
                GridActivity.MINES_COUNT = 9;
                startActivity(intent);
                break;
            case R.id.intermediate:       //Start the game in Intermediate Mode
                GridActivity.MINES_COUNT = 24;
                startActivity(intent);
                break;
            case R.id.advanced:    //Start the game in Advanced Mode
                GridActivity.MINES_COUNT = 40;
                startActivity(intent);
                break;
            case R.id.howToPlay:    //Instructions
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setMessage("Click on boxes to uncover and try not to land on a mine, Each box conatins count of mines it has in neighbour boxes")
                        .setCancelable(false)
                        .setPositiveButton("Thanks", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.setTitle("How to Play");
                alertDialog.show();     //Show Alert Box
        }
    }
}
