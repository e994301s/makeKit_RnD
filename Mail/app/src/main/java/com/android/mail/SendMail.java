package com.android.mail;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import androidx.appcompat.app.AppCompatActivity;

public class SendMail extends AppCompatActivity {
//    String user = "pakk7026@gmail.com"; // 보내는 계정의 id
//    String password = "kyeongmi7"; // 보내는 계정의 pw

    public String sendSecurityCode(Context context, String sendTo, String user, String password) {
        String code = "";
        try {
            GMailSender gMailSender = new GMailSender(user, password);
            code = gMailSender.getEmailCode();
            String body = "인증번호는 "  + code +"입니다." ;
            gMailSender.sendMail("이메일 인증코드 발송 메일입니다.", body, sendTo);
            Toast.makeText(context, "이메일을 성공적으로 보냈습니다.", Toast.LENGTH_SHORT).show();


        } catch (SendFailedException e) {
            Toast.makeText(context, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (MessagingException e) {
            Toast.makeText(context, "인터넷 연결을 확인해주십시오", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }
}

