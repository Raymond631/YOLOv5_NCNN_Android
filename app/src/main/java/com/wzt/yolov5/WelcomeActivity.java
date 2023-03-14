package com.wzt.yolov5;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WelcomeActivity extends AppCompatActivity {
    private Button yolov5s;
    private Button advice_button;
    private EditText location_id;
    private EditText people_num;
    private EditText complete_time;
    private EditText detect_speed;
    private EditText waiting_time;
    private TextView advice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        location_id=findViewById(R.id.location_id);
        people_num=findViewById(R.id.people_num);
        complete_time=findViewById(R.id.complete_time);
        detect_speed=findViewById(R.id.detect_speed);
        waiting_time=findViewById(R.id.waiting_time);
        advice=findViewById(R.id.advice);

        advice_button = findViewById(R.id.advice_button);
        advice_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num= Integer.parseInt(people_num.getText().toString());
                int time= Integer.parseInt(complete_time.getText().toString())*60;
                double speed= 60.0/Integer.parseInt(detect_speed.getText().toString());
                int wait= Integer.parseInt(waiting_time.getText().toString());
                double spot_num=num*(1+speed*wait)*1.0/(time*wait*speed*speed);
                advice.setText("建议：至少需要设置"+(int)Math.ceil(spot_num)+"个检测点");
                System.out.println("建议："+spot_num);
            }
        });

        yolov5s = findViewById(R.id.btn_start_detect1);
        yolov5s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.location_id=location_id.getText().toString();
                MainActivity.USE_MODEL = MainActivity.YOLOV5S;
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                WelcomeActivity.this.startActivity(intent);
            }
        });
    }
}
