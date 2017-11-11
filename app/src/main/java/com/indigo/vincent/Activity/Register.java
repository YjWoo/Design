package com.indigo.vincent.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
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

/**
 * Created by Miracle on 2017/11/2.
 */

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //提交数据
        Button button_submit = (Button) findViewById(R.id.button_submit);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(networkTask).start();
            }
        });
    }

    /**
     * 网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            EditText uid = (EditText) findViewById(R.id.register_input_user);
            EditText pwd = (EditText) findViewById(R.id.register_input_password);
            EditText pwdrp = (EditText) findViewById(R.id.register_input_password_repeat);
            String id = uid.getText().toString().trim();
            String pw = pwd.getText().toString().trim();
            String pwrp = pwdrp.getText().toString().trim();
            if (!pw.equals(pwrp)) {
                Looper.prepare();
                Toast.makeText(Register.this, "密码错误！请重新输入", Toast.LENGTH_SHORT).show();
                Looper.loop();
                pwd.setText("");
                pwdrp.setText("");
                return;
            }
            if (id.length() == 0 || pw.length() == 0) {
                Looper.prepare();
                Toast.makeText(Register.this, "用户名和密码不能为空！请重新输入！", Toast.LENGTH_SHORT).show();
                Looper.loop();
                pwd.setText("");
                pwdrp.setText("");
                return;
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
                .url(host + "/Register")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        String tempResponse = response.body().string();

        if (response.isSuccessful()) {
            Looper.prepare();
            Log.i("Response", "SendByHttpClient: " + tempResponse);
            if (tempResponse.equals("exists")) {
                //用户名存在
                Toast.makeText(this, "用户已存在，请重新输入！", Toast.LENGTH_SHORT).show();
            } else if (tempResponse.equals("1")) {
                Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(Register.this, Login.class);
                startActivity(it);
            }
            Looper.loop();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }
}
