package com.android.smssend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    String smsCode = createSMSCode();
    EditText codeNum, phone;
    LinearLayout layoutSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeNum = findViewById(R.id.sendNum_findId);
        phone = findViewById(R.id.phone_findId);
        layoutSMS = findViewById(R.id.linearSMS_findId);

        findViewById(R.id.btnSendMsg_findId).setOnClickListener(mClickListener);
        findViewById(R.id.btnFindId_findId).setOnClickListener(mClickListener);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Log.d(TAG, "=== sms전송을 위한 퍼미션 확인 ===" );

            // For device above MarshMallow
            boolean permission = getWritePermission();
            if(permission) {
                // If permission Already Granted
                // Send You SMS here
                Log.d(TAG, "=== 퍼미션 허용 ===" );
            }
        }
        else{
            // Send Your SMS. You don't need Run time permission
            Log.d(TAG, "=== 퍼미션 필요 없는 버전임 ===" );
        }
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.btnSendMsg_findId:
                    sendMessage(phone.getText().toString());
                    layoutSMS.setVisibility(View.VISIBLE);
                    break;

                case R.id.btnFindId_findId:
                    String code = codeNum.getText().toString();
                    checkCode(code);
                    break;
            }
        }
    };


    // 문자 인증
    public boolean getWritePermission(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 10);
        }
        return hasPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 10: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission is Granted
                    // Send Your SMS here
                }
            }
        }
    }

    // 문자 발송
    private void sendMessage(String phoneNo){
        try {
            Log.d(TAG, "=== 문자 전송 시작 ===" );

            //전송
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, "[주소록]의 인증번호는 "+getSMSCode() +"입니다.", null, null); //SMSContents앞서 전역변수로 입력한, 번호 [랜덤숫자 생성] 포스팅의 메서드를 활용하여 넣으면, 랜덤으로 숫자가 보내진다.
            //
            Log.d(TAG, "=== 문자 전송 완료 ===" );

            //countDownTimer(); [카운트다운 시간재기]포스팅에서 확인할 수 있다.


        } catch (Exception e) {
            Log.d(TAG, "=== 문자 전송 실패 === 에러코드 e : "+e );
            e.printStackTrace();

//            sendCan=false;
//            Log.d(TAG, "=== sendCan === :" +sendCan );
        }
    }


    // 문자 랜덤 코드
    public String getSMSCode() {
        return smsCode;
    } //생성된 인증코드 반환

    private String createSMSCode() { // 인증코드 생성
        String[] str = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        String newCode = new String();

        for (int x = 0; x < 6; x++) {
            int random = (int) (Math.random() * str.length);
            newCode += str[random];
        }

        return newCode;
    }

    private void checkCode(String code){
        if(code.equals(smsCode)){
            Toast.makeText(MainActivity.this, "일치", Toast.LENGTH_SHORT).show();
            finish();

        } else{
            codeNum.setText("");
            Toast.makeText(MainActivity.this, "인증코드 다시 입력해주세요.", Toast.LENGTH_SHORT).show();

        }
    }

}