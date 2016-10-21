package org.xerrard.wifiapdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView mStatusTv;
    private Button mSendBtn;
    private Button mOpenApBtn;
    private Button mConnectApBtn;
    private NetworkManger mNetworkManger = NetworkManger.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusTv = (TextView) findViewById(R.id.tv_status);
        mSendBtn = (Button) findViewById(R.id.btn_send_file);
        mOpenApBtn = (Button) findViewById(R.id.btn_open_ap);
        mConnectApBtn = (Button) findViewById(R.id.btn_connect_ap);
        mNetworkManger.init(getApplicationContext());
        mSendBtn.setOnClickListener(this);
        mConnectApBtn.setOnClickListener(this);
        mOpenApBtn.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        mNetworkManger.close();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_open_ap) {
            if (mNetworkManger.createAccessPoint()) {
                mStatusTv.setText("create ap ok");
            } else {
                mStatusTv.setText("create ap fail");
            }

        } else if (v.getId() == R.id.btn_connect_ap) {
            if (mNetworkManger.connectAccessPoint()) {
                mStatusTv.setText("connect ap ok");
            } else {
                mStatusTv.setText("connect ap fail");
            }
        }
    }
}
