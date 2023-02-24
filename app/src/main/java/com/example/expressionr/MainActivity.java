package com.example.expressionr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //exit button
        ImageButton exit_button = findViewById(R.id.exit);
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityExit();
            }
        });

        //change activity to recognition button
        Button button = findViewById(R.id.start_recog);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecognitionActivity();
            }
        });

    }

    private void openActivityExit() {
        finish();
        System.exit(0);
    }

    public void openRecognitionActivity(){
        Intent intent = new Intent(this,RecognitionActivity.class);
        startActivity(intent);
    }

}
