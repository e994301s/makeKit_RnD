package com.android.mail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;

public class MainActivity extends AppCompatActivity {

    String user = "pakk7026@gmail.com"; // 보내는 계정의 id
    String password = "kyeongmi7"; // 보내는 계정의 pw

    Button mailBtn;
    EditText email;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());

        mailBtn = findViewById(R.id.btnSend);
        email = findViewById(R.id.email);

        mailBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                SendMail mailServer = new SendMail();

                String code = mailServer.sendSecurityCode(getApplicationContext(), email.getText().toString(), user, password);

                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("password", password);
                intent.putExtra("userRegisterEmail", email.getText().toString());
                intent.putExtra("codeAuth", code);
                startActivity(intent);


            }


        });
    }
}
