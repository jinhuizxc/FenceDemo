package com.example.jh.fencedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
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

public class MainActivity extends AppCompatActivity {


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
}
