package com.indigo.vincent.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {
    //保存用户名、密码
    SharedPreferences myPreference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button button_login = (Button) findViewById(R.id.button_login);
        myPreference = getSharedPreferences("myPreference", Context.MODE_PRIVATE);
        EditText uid = (EditText) findViewById(R.id.input_user);
        uid.setText(myPreference.getString("user_name", ""));
        EditText pwd = (EditText) findViewById(R.id.input_password);
        pwd.setText(myPreference.getString("user_password", ""));

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(networkTask).start();
            }
        });

        Button button_register = (Button) findViewById(R.id.button_register);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Login.this, Register.class);
                startActivity(it);
            }
        });
    }


    /**
     * 网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            // TODO
            // 在这里进行 http request.网络请求相关操作
            EditText uid = (EditText) findViewById(R.id.input_user);
            EditText pwd = (EditText) findViewById(R.id.input_password);
            String id = uid.getText().toString().trim();
            String pw = pwd.getText().toString().trim();

            //测试代码
            if (id.equals("") && pw.equals("")) {
                Intent it = new Intent(Login.this, MainActivity.class);
                startActivity(it);
            }

            try {
                SendByHttpClient(id, pw);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void SendByHttpClient(String id, String pw) throws Exception {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ID", id).add("PW", pw);
        RequestBody formBody = builder.build();
        String host = getResources().getString(R.string.hostname);
        Request request = new Request.Builder()
                .url(host + "/Login")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        String tempResponse = response.body().string();
        if (response.isSuccessful()) {
            Log.i("Response", "SendByHttpClient: " + tempResponse);
            if (tempResponse.equals("yes")) {
                //验证成功，登录
                Intent it = new Intent(Login.this, MainActivity.class);
                //保存用户名
                SharedPreferences.Editor editor = myPreference.edit();
                editor.putString("user_name", id);
                editor.putString("user_password", pw);
                editor.commit();
                startActivity(it);
            } else {
                Looper.prepare();
                Toast.makeText(this, "用户不存在，请重新输入！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }


}
