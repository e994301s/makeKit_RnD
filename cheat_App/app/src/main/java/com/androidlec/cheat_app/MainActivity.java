package com.androidlec.cheat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
    Handler handler;
    Thread thread;
    ArrayList<ChattingBean> chattingJudge;
    boolean isRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editText = findViewById(R.id.chat_edit);
        insertButton = findViewById(R.id.chat_btn);
        listView = findViewById(R.id.chat_lv);

        urlAddr = "http://192.168.0.4:8080/chat/";



        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        chattingContents.clear();
                        chattingJudge.clear();
                        connectGetData();
                        adapter = new ChattingAdapter(MainActivity.this, R.layout.chatting_layout, chattingContents);
                        listView.setAdapter(adapter);
                        break;
                    case 1:
                        break;
                }
            }
        };

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (isRun){
                        connectGetData();
                        Thread.sleep(100);
                        Log.v("chatting", "채팅 사이즈 : "+chattingContents.size());
                        if(judgement()==chattingContents.size()){
                            Message msg = handler.obtainMessage();
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }else {
                            Message msg = handler.obtainMessage();
                            msg.what = 0;
                            handler.sendMessage(msg);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

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
        chattingJudge = new ArrayList<ChattingBean>();
        chattingContents = new ArrayList<ChattingBean>();
        isRun = true;
        connectGetData();
        adapter = new ChattingAdapter(MainActivity.this, R.layout.chatting_layout, chattingContents);
        listView.setAdapter(adapter);
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRun = false;
        try {
            thread.join();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void connectGetData(){
        try {
            NetworkTask networkTask = new NetworkTask(MainActivity.this, urlAddr+"chatting.jsp");
            Object obj = networkTask.execute().get();
            chattingContents = (ArrayList<ChattingBean>) obj;


            chattingJudge.addAll(chattingContents);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int judgement(){
        int j = 0;
        for(int i =0 ; i<chattingContents.size(); i++){
            int contents = chattingContents.get(i).getChattingNo();
            int judge = chattingJudge.get(i).getChattingNo();
            if(contents == judge){
                j++;
                Log.v("chatting", "판단 : "+j);
            }else {
            }
        }
        return j;
    }
}