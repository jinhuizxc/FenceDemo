package com.example.jh.fencedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, GeoFenceListener {


    private View lyOption;

    private TextView tvGuide;
    private TextView tvResult;

    private EditText etCustomId;
    private EditText etRadius;

    private CheckBox cbAlertIn;
    private CheckBox cbAlertOut;
    private CheckBox cbAldertStated;

    private Button btAddFence;
    private Button btOption;

    /**
     * 用于显示当前的位置
     * <p>
     * 示例中是为了显示当前的位置，在实际使用中，单独的地理围栏可以不使用定位接口
     * </p>
     */
    private AMapLocationClient mlocationClient;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClientOption mLocationOption;

    private MapView mMapView;
    private AMap mAMap;

    // 中心点坐标
    private LatLng centerLatLng = null;
    // 中心点marker
    private Marker centerMarker;
    private BitmapDescriptor ICON_YELLOW = BitmapDescriptorFactory
            .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
    private BitmapDescriptor ICON_RED = BitmapDescriptorFactory
            .defaultMarker(BitmapDescriptorFactory.HUE_RED);
    private MarkerOptions markerOption = null;
    private List<Marker> markerList = new ArrayList<Marker>();
    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

    // 地理围栏客户端
    private GeoFenceClient fenceClient = null;
    // 要创建的围栏半径
    private float fenceRadius = 0.0F;
    // 触发地理围栏的行为，默认为进入提醒
    private int activatesAction = GeoFenceClient.GEOFENCE_IN;
    // 地理围栏的广播action
    private static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence.round";

    // 记录已经添加成功的围栏
    private HashMap<String, GeoFence> fenceMap = new HashMap<String, GeoFence>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.roundGeoFence);
        // 初始化地理围栏
        fenceClient = new GeoFenceClient(getApplicationContext());

        lyOption = findViewById(R.id.ly_option);
        btAddFence = (Button) findViewById(R.id.bt_addFence);
        btOption = (Button) findViewById(R.id.bt_option);
        tvGuide = (TextView) findViewById(R.id.tv_guide);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvResult.setVisibility(View.GONE);
        etCustomId = (EditText) findViewById(R.id.et_customId);
        etRadius = (EditText) findViewById(R.id.et_radius);

        cbAlertIn = (CheckBox) findViewById(R.id.cb_alertIn);
        cbAlertOut = (CheckBox) findViewById(R.id.cb_alertOut);
        cbAldertStated = (CheckBox) findViewById(R.id.cb_alertStated);

        mMapView = (MapView) findViewById(R.id.map);
        // 创建map
        mMapView.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
//            mAMap.getUiSettings().setRotateGesturesEnabled(false);
//            mAMap.moveCamera(CameraUpdateFactory.zoomBy(6));
            setUpMap();
        }
        btOption.setVisibility(View.VISIBLE);
        btOption.setText(getString(R.string.hideOption));
        resetView_round();

        btAddFence.setOnClickListener(this);
        btOption.setOnClickListener(this);
        cbAlertIn.setOnCheckedChangeListener(this);
        cbAlertOut.setOnCheckedChangeListener(this);
        cbAldertStated.setOnCheckedChangeListener(this);

        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);


        /**
         * 创建pendingIntent
         */
        fenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        fenceClient.setGeoFenceListener(this);
        /**
         * 设置地理围栏的触发行为,默认为进入
         */
        fenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN);
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0 :
                    StringBuffer sb = new StringBuffer();
                    sb.append("添加围栏成功");
                    String customId = (String)msg.obj;
                    if(!TextUtils.isEmpty(customId)){
                        sb.append("customId: ").append(customId);
                    }
                    Toast.makeText(getApplicationContext(), sb.toString(),
                            Toast.LENGTH_SHORT).show();
                    drawFence2Map();
                    break;
                case 1 :
                    int errorCode = msg.arg1;
                    Toast.makeText(getApplicationContext(),
                            "添加围栏失败 " + errorCode, Toast.LENGTH_SHORT).show();
                    break;
                case 2 :
                    String statusStr = (String) msg.obj;
                    tvResult.setVisibility(View.VISIBLE);
                    tvResult.append(statusStr + "\n");
                    break;
                default :
                    break;
            }
        }
    };

    private void drawFence2Map() {
    }

    /**
     * 接收触发围栏后的广播,当添加围栏成功之后，会立即对所有围栏状态进行一次侦测，如果当前状态与用户设置的触发行为相符将会立即触发一次围栏广播；
     * 只有当触发围栏之后才会收到广播,对于同一触发行为只会发送一次广播不会重复发送，除非位置和围栏的关系再次发生了改变。
     */
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                String customId = bundle
                        .getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                //status标识的是当前的围栏状态，不是围栏行为
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                StringBuffer sb = new StringBuffer();
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL :
                        sb.append("定位失败");
                        break;
                    case GeoFence.STATUS_IN :
                        sb.append("进入围栏 ");
                        break;
                    case GeoFence.STATUS_OUT :
                        sb.append("离开围栏 ");
                        break;
                    case GeoFence.STATUS_STAYED :
                        sb.append("停留在围栏内 ");
                        break;
                    default :
                        break;
                }
                if(status != GeoFence.STATUS_LOCFAIL){
                    if(!TextUtils.isEmpty(customId)){
                        sb.append(" customId: " + customId);
                    }
                    sb.append(" fenceId: " + fenceId);
                }
                String str = sb.toString();
                Message msg = Message.obtain();
                msg.obj = str;
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }
    };




    private void resetView_round() {
        etRadius.setHint("围栏半径");
        etRadius.setVisibility(View.VISIBLE);
        tvGuide.setBackgroundColor(getResources().getColor(R.color.red));
        tvGuide.setText("请点击地图选择围栏的中心点");
        tvGuide.setVisibility(View.VISIBLE);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        // 停止定位
        deactivate();
    }

    /**
     * 停止定位
     */
    private void deactivate() {
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        
    }

    @Override
    public void onGeoFenceCreateFinished(List<GeoFence> list, int i, String s) {
        
    }
}
