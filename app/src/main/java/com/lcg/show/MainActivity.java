package com.lcg.show;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lcg.show.media.h264data;
import com.lcg.show.rtsp.RtspServer;
import com.lcg.show.screen.Constant;
import com.lcg.show.screen.ScreenRecord;

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * MediaProjectionManager流程
 * 1.调用Context.getSystemService()方法即可获取MediaProjectionManager实例
 * 2.调用MediaProjectionManager对象的createScreenCaptureIntent()方法创建一个屏幕捕捉的Intent
 * 3.调用startActivityForResult()方法启动第2步得到的Intent，这样即可启动屏幕捕捉的Intent
 * 4.重写onActivityResult()方法，在该方法中通过MediaProjectionManager对象来获取MediaProjection对象，在该对象中即可获取被捕获的屏幕
 **/
public class MainActivity extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE_A = 10001;
    Button start_record, stop_record;
    private TextView line2, w, h, r;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRecord mScreenRecord;
    private static int queuesize = 30;
    public static ArrayBlockingQueue<h264data> h264Queue = new ArrayBlockingQueue<>(queuesize);
    private RtspServer mRtspServer;
    private String RtspAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitView();
        InitMPManager();
        RtspAddress = displayIpAddress();
        if (RtspAddress != null) {
            line2.setText(RtspAddress);
        }
        w.setText(Constant.VIDEO_WIDTH + "");
        h.setText(Constant.VIDEO_HEIGHT + "");
        r.setText(Constant.VIDEO_BITRATE + "");
    }

    private ServiceConnection mRtspServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRtspServer = ((RtspServer.LocalBinder) service).getService();
            mRtspServer.addCallbackListener(mRtspCallbackListener);
            mRtspServer.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private RtspServer.CallbackListener mRtspCallbackListener = new RtspServer.CallbackListener() {

        @Override
        public void onError(RtspServer server, Exception e, int error) {
            // We alert the user that the port is already used by another app.
            e.printStackTrace();
            if (error == RtspServer.ERROR_BIND_FAILED) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Port already in use !")
                        .setMessage("You need to choose another port for the RTSP server !")
                        .show();
            }
        }

        @Override
        public void onMessage(RtspServer server, int message) {
            Log.i("fast", "onMessage:" + message);
            if (message == RtspServer.MESSAGE_STREAMING_STARTED) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "RTSP STREAM STARTED", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (message == RtspServer.MESSAGE_STREAMING_STOPPED) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "RTSP STREAM STOPPED", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };


    public static void putData(byte[] buffer, int type, long ts) {
        if (h264Queue.size() >= queuesize) {
            h264Queue.poll();
        }
        h264data data = new h264data();
        data.data = buffer;
        data.type = type;
        data.ts = ts;
        h264Queue.add(data);
    }

    /**
     * 初始化View
     **/
    private void InitView() {
        start_record = findViewById(R.id.start_record);
        start_record.setOnClickListener(this);
        stop_record = findViewById(R.id.stop_record);
        stop_record.setOnClickListener(this);
        line2 = findViewById(R.id.line2);
        w = findViewById(R.id.w);
        h = findViewById(R.id.h);
        r = findViewById(R.id.r);
    }

    /**
     * 初始化MediaProjectionManager
     **/
    private void InitMPManager() {
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }


    /**
     * 开始截屏
     **/
    private void StartScreenCapture() {
        if (RtspAddress != null && !RtspAddress.isEmpty()) {
            bindService(new Intent(this, RtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE_A);
        } else {
            Toast.makeText(this, "网络连接异常！", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 停止截屏
     **/
    private void StopScreenCapture() {
        stop_record.setEnabled(false);
        start_record.setEnabled(true);
        mScreenRecord.release();
        if (mRtspServer != null)
            mRtspServer.removeCallbackListener(mRtspCallbackListener);
        unbindService(mRtspServiceConnection);
    }


    /**
     *
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                Toast.makeText(this, "程序发生错误:MediaProjection@1", Toast.LENGTH_SHORT).show();
                return;
            }
            mScreenRecord = new ScreenRecord(mediaProjection);
            mScreenRecord.start();
            stop_record.setEnabled(true);
            start_record.setEnabled(false);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.act_no);
            line2.startAnimation(animation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        Constant.VIDEO_BITRATE = Integer.parseInt(r.getText().toString());
        Constant.VIDEO_WIDTH = Integer.parseInt(w.getText().toString());
        Constant.VIDEO_HEIGHT = Integer.parseInt(h.getText().toString());
        switch (view.getId()) {
            case R.id.start_record:
                StartScreenCapture();
                break;
            case R.id.stop_record:
                StopScreenCapture();
                break;
        }
    }

    /**
     * 先判断网络情况是否良好
     */
    private String displayIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ipaddress = "";
        if (info != null && info.getNetworkId() > -1) {
            int i = info.getIpAddress();
            String ip = String.format(Locale.ENGLISH, "%d.%d.%d.%d", i & 0xff, i >> 8 & 0xff, i >> 16 & 0xff, i >> 24 & 0xff);
            ipaddress += "rtsp://";
            ipaddress += ip;
            ipaddress += ":";
            ipaddress += RtspServer.DEFAULT_RTSP_PORT;
        }
        return ipaddress;
    }
}
