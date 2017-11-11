package com.indigo.vincent.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.ThemedSpinnerAdapter;

import com.indigo.vincent.Bean.Article;
import com.indigo.vincent.Util.GeneratePictureView;
import com.indigo.vincent.Util.LocationUtil;
import com.indigo.vincent.Util.ToastDebug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 卡片内容分享、保存页面
 */
public class Activity_Capture extends AppCompatActivity implements SensorEventListener {

    private static final String SAVE_PIC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SimpleWord";
    // private static final String SAVE_PIC_PATH=Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/SimpleWord" : "";
    //保存
    private static final int SAVE = 0;
    //分享
    private static final int SHARE = 1;
    //操作失败
    private static final int FAILE = 2;
    GeneratePictureView gpv;
    ProgressDialog pd;
    private Handler mHandler = new Handler_Capture(this);

    Article article;
    private boolean isShake = false;
    private static final String TAG = "Activity_Capture";
    //传感器，震动
    private static final int START_SHAKE = 3;
    private static final int END_SHAKE = 4;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Vibrator mVibrator;//手机震动

    //读取用户名信息
    SharedPreferences myPreference;
    String user_name;
    String tag;
    String add_tag;
    String[] tags;

    Handler uiHandler;
    //获取位置
    LocationManager locationManager;
    LocationListener locationListener;
    String address;
    LocationUtil locate;

    @Override
    protected void onStart() {
        super.onStart();
        //获取 SensorManager 负责管理传感器
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        if (mSensorManager != null) {
            //获取加速度传感器
            Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                mSensorManager.registerListener((SensorEventListener) this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        // 务必要在pause中注销 mSensorManager
        // 否则会造成界面退出后摇一摇依旧生效的bug
        if (mSensorManager != null) {
            mSensorManager.unregisterListener((SensorEventListener) this);
        }
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math
                    .abs(z) > 17) && !isShake) {
                isShake = true;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Log.d(TAG, "onSensorChanged: 摇动");
                            //开始震动
                            mHandler.obtainMessage(START_SHAKE).sendToTarget();
                            Thread.sleep(500);
                            mHandler.obtainMessage(END_SHAKE).sendToTarget();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            // 关闭程序时将监听器移除
            locationManager.removeUpdates(locationListener);
        }
    }

    class Handler_Capture extends Handler {
        WeakReference<Activity> weakReference;

        private Activity_Capture mActivity;

        public Handler_Capture(Activity activity) {
            weakReference = new WeakReference<>(activity);
            if (weakReference != null)
                mActivity = (Activity_Capture) weakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference.get() != null) {
                Bundle data = null;
                switch (msg.what) {
                    case SAVE:
                        pd.dismiss();
                        ToastDebug.showToast(getApplicationContext(), "图片已保存在相册!");
                        break;
                    case SHARE:
                        data = msg.getData();
                        pd.dismiss();
                        shareMsg("分享卡片", "分享卡片title", "分享卡片内容", data.getString("path"));
                        break;
                    case FAILE:
                        String strData = (String) msg.obj;
                        ToastDebug.showToast(getApplicationContext(), strData);
                        break;
                    case START_SHAKE:
                        mActivity.mVibrator.vibrate(300);
                        Save(gpv);
                        break;
                    case END_SHAKE:
                        mActivity.isShake = false;
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_cap);
        gpv = (GeneratePictureView) findViewById(R.id.gpv);
        //获取Vibrator震动服务
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        article = (Article) getIntent().getSerializableExtra("data");
        final String data = article.strData;
        final String title = article.strTitle;

        //获取用户信息
        myPreference = getSharedPreferences("myPreference", Context.MODE_PRIVATE);
        user_name = myPreference.getString("user_name", "");

        //获取位置
        locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {
                // 更新当前设备的位置信息
                address = locate.getAddress(location);
            }
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        new Thread(locateTask).start();

        pd = new ProgressDialog(this);
        pd.setMessage("请稍后...");

        initRadioButton(data, title);
        //初始化控件填充内容
        gpv.init(data, title, user_name, "via Vicent Woo");
        //标签赋值
        uiHandler = new Handler();
        new Thread(getTags).start();
    }

    //tag的UI Runnable接口
    Runnable uiRunnable = new Runnable() {
        @Override
        public void run() {
            // 初始化tag标签
            Spinner spinner = (Spinner) findViewById(R.id.category);
            // 建立数据源
            // 建立Adapter并且绑定数据源，simple_spinner_item/simple_spinner_dropdown_item系统默认布局
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Activity_Capture.this, android.R.layout.simple_spinner_item, tags);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //绑定 Adapter到控件
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    String[] languages = tags;
                    tag = languages[pos];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                }
            });
        }
    };

    Runnable addTags = new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("ID", user_name).add("METHOD", "addTags").add("TAG", add_tag);
            RequestBody formBody = builder.build();
            String host = getResources().getString(R.string.hostname);
            Request request = new Request.Builder()
                    .url(host + "/Tag")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String text = response.body().string();
                    Log.i(TAG, text);
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            new Thread(getTags).start();
        }
    };

    //获取tags
    Runnable getTags = new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("ID", user_name).add("METHOD", "getTags");
            RequestBody formBody = builder.build();
            String host = getResources().getString(R.string.hostname);
            Request request = new Request.Builder()
                    .url(host + "/Tag")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String text = response.body().string();
                    Log.i(TAG, text);
                    text = text.substring(1, text.length() - 1);
                    tags = text.split(",");
                    for (int i = 0; i < tags.length; i++) {
                        tags[i] = tags[i].trim();
                        Log.i(TAG, tags[i]);
                    }
                    uiHandler.post(uiRunnable);
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    //获取经纬度，定位
    Runnable locateTask = new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            List<String> providerList = locationManager.getProviders(true);
            String provider;
            if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                provider = LocationManager.GPS_PROVIDER;
            } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
            } else {
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            locate = new LocationUtil(locationManager);
            if (location != null) {
                address = locate.getAddress(location);
            }
            locationManager.requestLocationUpdates(provider, 5000, 5,
                    locationListener);
            Looper.loop();
        }
    };

    /**
     * 初始化卡片主题切换按钮
     *
     * @param data  选取的内容
     * @param title 页面标题
     */
    private void initRadioButton(final String data, final String title) {
        final RadioButton rb_day = (RadioButton) findViewById(R.id.rb_day);
        final RadioButton rb_night = (RadioButton) findViewById(R.id.rb_night);

        rb_day.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rb_night.setChecked(false);
                rb_day.setChecked(true);
                gpv.changeDay(data, title, user_name, address);
            }
        });
        rb_night.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rb_day.setChecked(false);
                rb_night.setChecked(true);
                gpv.changeNight(data, title, user_name, address);
            }
        });
    }

    /**
     * 新建标签
     *
     * @param v
     */
    public void addTag(final View v) {
        final EditText inputServer = new EditText(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("建立标签").setView(inputServer)
                .setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                add_tag = inputServer.getText().toString().trim();
                if (add_tag.length() != 0)
                    new Thread(addTags).start();
            }
        });
        builder.show();
    }

    /**
     * 保存
     *
     * @param v
     */
    public void Save(View v) {
        if (TextUtils.isEmpty(SAVE_PIC_PATH))
            return;
        pd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
                    final File realFile = saveBitmap(tag + "_" + sdf.format(new Date()) + ".jpg");
                    if (realFile == null) {
                        Message message = mHandler.obtainMessage(FAILE);
                        message.obj = "保存失败,文件过大!";
                        message.sendToTarget();
                    } else {
//                        // 其次把文件插入到系统图库
//                    try {
//                        MediaStore.Images.Media.insertImage(Activity_Capture.this.getContentResolver(),
//                                realFile.getAbsolutePath(), realFile.getName(), null);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                        realFile.delete();

                        Message message = mHandler.obtainMessage(SAVE);
                        Bundle data = new Bundle();
                        data.putString("path", realFile.getAbsolutePath());
                        message.setData(data);
                        message.sendToTarget();
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    Message message = mHandler.obtainMessage(FAILE);
                    message.obj = "保存失败," + e.getMessage();
                    message.sendToTarget();
                } finally {
                    pd.dismiss();
                }
            }
        }).start();
    }

    /**
     * 分享
     *
     * @param view
     */
    public void Share(View view) {
        if (TextUtils.isEmpty(SAVE_PIC_PATH))
            return;
        pd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final File realFile = saveBitmap("share.jpg");
                    if (realFile == null) {
                        Message message = mHandler.obtainMessage(FAILE);
                        message.obj = "分享失败,文件过大!";
                        message.sendToTarget();
                    } else {
                        Message message = mHandler.obtainMessage(SHARE);
                        Bundle data = new Bundle();
                        data.putString("path", realFile.getAbsolutePath());
                        message.setData(data);
                        message.sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = mHandler.obtainMessage(FAILE);
                    message.obj = "分享失败," + e.getMessage();
                    message.sendToTarget();
                } finally {
                    pd.dismiss();
                }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 分享功能
     *
     * @param activityTitle Activity的名字
     * @param msgTitle      消息标题
     * @param msgText       消息内容
     * @param imgPath       图片路径，不分享图片则传null
     */
    public void shareMsg(String activityTitle, String msgTitle, String msgText,
                         String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, activityTitle));
    }

    /**
     * 保存图片到文件
     *
     * @param fileName 文件名称
     * @return
     * @throws Exception
     */
    private File saveBitmap(String fileName) throws Exception {
        Bitmap bitmap = gpv.getScreen();
        if (bitmap == null)
            return null;
        File file = new File(SAVE_PIC_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File realFile = new File(file, fileName);
        if (!realFile.exists()) {
            realFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(realFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc(); // 通知系统回收
        }
        return realFile;
    }
}
