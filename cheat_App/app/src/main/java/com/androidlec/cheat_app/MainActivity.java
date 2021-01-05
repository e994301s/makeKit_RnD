package com.androidlec.cheat_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String urlAddr = null;
    ArrayList<ChattingBean> chattingContents;
    ChattingAdapter adapter;
    ListView listView;
    EditText editText;
    Button insertButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.chat_lv);
        editText = findViewById(R.id.chat_edit);
        insertButton = findViewById(R.id.chat_btn);


        urlAddr = "http://192.168.0.79:8080/chat/";





        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkTask networkTask = new NetworkTask(MainActivity.this, urlAddr+"insertChatting.jsp?userid=asdf&contents="+editText.getText().toString());
                networkTask.execute();
                connectGetData();
                editText.setText("");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Background thread = 
        connectGetData();
    }
    private void connectGetData(){
        try {
            NetworkTask networkTask = new NetworkTask(MainActivity.this, urlAddr+"chatting.jsp");
            Object obj = networkTask.execute().get();
            chattingContents = (ArrayList<ChattingBean>) obj;

            adapter = new ChattingAdapter(MainActivity.this, R.layout.chatting_layout, chattingContents);
            listView.setAdapter(adapter);


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}