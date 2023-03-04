package com.wzt.yolov5;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WelcomeActivity extends AppCompatActivity {
    private Button yolov5s;
    private EditText location_id;
    private EditText time_interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        location_id=findViewById(R.id.location_id);
        time_interval=findViewById(R.id.time_interval);
        yolov5s = findViewById(R.id.btn_start_detect1);
        yolov5s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.location_id=location_id.getText().toString();
                MainActivity.time_interval= Integer.parseInt(time_interval.getText().toString())*1000;

                MainActivity.USE_MODEL = MainActivity.YOLOV5S;
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                WelcomeActivity.this.startActivity(intent);
            }
        });



    }


}
