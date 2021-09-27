package com.example.covid_19symptomstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

public class SecondScreen extends AppCompatActivity {

    DatabaseHelper databaseHelper;
    TextView dbContent;
    String selectedItem;
    float ratingValue;
    Cursor mCursor;
    String column;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);

        databaseHelper = DatabaseHelper.getInstance(this);

        dbContent = findViewById(R.id.textView3);

        Button saveBtn = findViewById(R.id.button6);

        showData();


        Spinner spinner = findViewById(R.id.spinner);
        RatingBar ratingBar = findViewById(R.id.ratingBar);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(SecondScreen.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedItem = spinner.getSelectedItem().toString();
                ratingValue = ratingBar.getRating();
                if(selectedItem.equals("Nausea")){
                    Log.i("spinnner", "SAVING nausea");
                    column = "nausea";
                } else if(selectedItem.equals("Headache")){
                    column = "headache";
                } else if(selectedItem.equals("Diarrhea")){
                    Log.i("spinnner", "Saving diarrhea");
                    column = "diarrhea";
                } else if(selectedItem.equals("Soar Throat")){
                    column = "soarThroat";
                } else if(selectedItem.equals("Fever")){
                    column = "fever";
                } else if(selectedItem.equals("Muscle Ache")){
                    column = "muscleAche";
                } else if(selectedItem.equals("Loss of Smell or Taste")){
                    column = "LST";
                } else if(selectedItem.equals("Cough")){
                    column = "cough";
                } else if(selectedItem.equals("Shortness of Breath")){
                    column = "shortnessBreath";
                } else if(selectedItem.equals("Feeling tired")){
                    column = "feelingTired";
                }

                mCursor = databaseHelper.getData();

                if(mCursor.getCount() == 0){
                    if(databaseHelper.insertData(column, ratingValue)){
                        Toast.makeText(SecondScreen.this, selectedItem + " saved to database", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SecondScreen.this, selectedItem + " faild to save", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if(databaseHelper.updateData(column, ratingValue)){
                        Toast.makeText(SecondScreen.this, selectedItem + " saved to database", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SecondScreen.this, selectedItem + " faild to save", Toast.LENGTH_SHORT).show();
                    }
                }


                showData();

            }
        });
    }

    public void showData() {
        mCursor = databaseHelper.getData();
        if(mCursor.moveToFirst()){
            dbContent.setText("Data: \nName: " + mCursor.getString(mCursor.getColumnIndex("Name")) + " \nHRrate: " + mCursor.getFloat(mCursor.getColumnIndex("HRrate")) + "   RRrate: " + mCursor.getFloat(mCursor.getColumnIndex("RRrate")) + "   \nnausea: " + mCursor.getFloat(mCursor.getColumnIndex("nausea")) +
                    "\nheadache: " + mCursor.getFloat(mCursor.getColumnIndex("headache")) + "   diarrhea: " + mCursor.getFloat(mCursor.getColumnIndex("diarrhea")) +
                    "\nsoar throat: " + mCursor.getFloat(mCursor.getColumnIndex("soarThroat")) + "   Fever: " + mCursor.getFloat(mCursor.getColumnIndex("fever")) +
                    "\nMuscle Ache: " + mCursor.getFloat(mCursor.getColumnIndex("muscleAche")) + "   LST: " + mCursor.getFloat(mCursor.getColumnIndex("LST")) +
                    "\nCough: " + mCursor.getFloat(mCursor.getColumnIndex("cough")) + "   Shortness: " + mCursor.getFloat(mCursor.getColumnIndex("shortnessBreath")) +
                    "\nFeeling tired: " + mCursor.getFloat(mCursor.getColumnIndex("feelingTired")));
        } else {
            Toast.makeText(SecondScreen.this, "No data in cursor", Toast.LENGTH_SHORT).show();
        }
    }
}