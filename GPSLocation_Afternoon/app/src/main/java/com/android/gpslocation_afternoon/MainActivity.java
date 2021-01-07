package com.android.gpslocation_afternoon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.PlacesListener;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, PlacesListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private Marker setMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
//    private static final int UPDATE_INTERVAL_MS = 10000;  // 10초 단위 시간 갱신
//
//    private static final int FASTEST_UPDATE_INTERVAL_MS = 5000; // 5초 단위 갱신


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    LatLng currentPosition;
    List<Marker> previous_marker = null;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    EditText search;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)
    // 현재값 가져오기 위해
    List<Address> addresses;

    // 1/5 test
    double lat;
    double lng;
    ArrayList<Address> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.layout_main);
        search = findViewById(R.id.search_map);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                .setInterval(UPDATE_INTERVAL_MS)
//                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);


        //Geocoder geocoder = new Geocoder(this);

        // 주변 위치 검색
        previous_marker = new ArrayList<Marker>();

        Button b4 = findViewById(R.id.button4);
        b4.setOnClickListener(mClickListener);

    }

    // (검색) 장소 입력 후 버튼 클릭 시 이벤트
    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mMap.clear();//지도 클리어
            sellerLocation();
            String str = search.getText().toString().trim();

            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> list = null;
            data = new ArrayList<Address>();


                try {
                    list = geocoder.getFromLocationName
                            (str, // 지역 이름
                                    10); // 읽을 개수

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
                }


            if (list != null) {
                if (list.size() == 0) {
                    Toast.makeText(MainActivity.this, "해당되는 주소 정보는 없습니다", Toast.LENGTH_SHORT).show();
                } else {
                    // 해당되는 주소로 인텐트 날리기
                    Address addr = list.get(0);
                    double lat = addr.getLatitude();
                    double lon = addr.getLongitude();

                    LatLng setLatLng = new LatLng(lat, lon);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(setLatLng);
                    markerOptions.title("위치 보내기");
                    //markerOptions.snippet(list.get(0).get);
                    markerOptions.draggable(true);


                    setMarker = mMap.addMarker(markerOptions);

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(setLatLng);
                    mMap.moveCamera(cameraUpdate);




//                        String sss = String.format("geo:%f,%f", lat, lon);
//
//                        // 구글 지도로 이동
//                        Intent intent = new Intent(
//                                Intent.ACTION_VIEW,
//                                Uri.parse(sss));
//                        startActivity(intent);
                }
            }
        }
    };

           //showPlaceInformation(currentPosition);
//            addressMarker();
            ///////////////////////////////////////
            ////////////////////////////////////////
            // 주소로 가져오기   ( 위치 받아서 넣기)
            ////////////////////////////////////////
            ///////////////////////////////////////

            ///////////////////////////////////////
            // 판매자 위치 정보 마커 표시
//           Geocoder geocoder = new Geocoder(getBaseContext());
//            List<Address> list = null;
//            data = new ArrayList<Address>();
//
//            ArrayList<String> address = new ArrayList<String>();
//                address.add("서울특별시 강남구 강남대로 402");
//                address.add("서울특별시 강남구 강남대로서 510-1");
//                address.add("경기도 성남시 대왕판교로 477");
//                address.add("서울특별시 서초구 잠원동 39-12");
//
//            try {
//                for(int i=0; i<address.size(); i++) {
//                    list = geocoder.getFromLocationName
//                            (address.get(i), // 지역 이름
//                                    10); // 읽을 개수
//                    data.add(list.get(0));
//                    Log.v("here", String.valueOf(list));
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
//            }
//
//
//            if (list != null) {
//                if (list.size() == 0) {
//                    Toast.makeText(MainActivity.this, "해당되는 주소 정보는 없습니다", Toast.LENGTH_SHORT).show();
//                } else {
//                    // 해당되는 주소로 인텐트 날리기
//                    Address addr = list.get(0);
//                    for(int i=0; i<data.size(); i++) {
//                        lat = data.get(i).getLatitude();
//                        lng = data.get(i).getLongitude();
//
//                        LatLng setLatLng = new LatLng(lat, lng);
//
//                        MarkerOptions markerOptions = new MarkerOptions();
//                        markerOptions.position(setLatLng);
//                        markerOptions.title("위치" + (i+1));
//                        //markerOptions.snippet(list.get(0).get);
//                        markerOptions.draggable(true);
//
//
//                        setMarker = mMap.addMarker(markerOptions);
//                    }
///////////////////////////////////////


//                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition);
//                    mMap.moveCamera(cameraUpdate);



//    // db 등록된 주소 마커 표시 하기
//    private void addressMarker(){
//        Geocoder geocoder = new Geocoder(getBaseContext());
//        List<Address> userAddresslist = null;
//
//        ArrayList<String> address = new ArrayList<String>();
//        address.add("서울특별시 강남구 강남대로 402");
//        address.add("서울특별시 강남구 강남대로서 510-1");
//        address.add("경기도 성남시 대왕판교로 477");
//        address.add("서울특별시 서초구 잠원동 39-12 ");
//
//       try {
//
//            for (int j=0; j<address.size(); j++) {
//                userAddresslist = geocoder.getFromLocationName
//                        (address.get(j), // 지역 이름
//                                10); // 읽을 개수
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
//        }
//
//        if (userAddresslist != null) {
//            if (userAddresslist.size() == 0) {
//                Toast.makeText(MainActivity.this, "해당되는 주소 정보는 없습니다", Toast.LENGTH_SHORT).show();
//            } else {
//                // 해당되는 주소로 인텐트 날리기
//                double lat = 0;
//                double lon = 0;
//
//                for (int i = 0; i < userAddresslist.size(); i++) {
//
//                    Address addr = userAddresslist.get(i);
//                    lat = addr.getLatitude();
//                    lon = addr.getLongitude();
//
//                    LatLng setLatLng = new LatLng(lat, lon);
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(setLatLng);
//                    markerOptions.title("판매자" + i);
//                    //markerOptions.snippet(list.get(0).get);
//                    markerOptions.draggable(true);
//
//
//                    setMarker = mMap.addMarker(markerOptions);
//                }
//
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition);
//                mMap.moveCamera(cameraUpdate);
//            }
//        }
//                Address addr = userAddresslist.get(0);
//                lat = addr.getLatitude();
//                lon = addr.getLongitude();
//
//                LatLng setLatLng = new LatLng(lat, lon);
//
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(setLatLng);
//                markerOptions.title("위치 보내기");
//                //markerOptions.snippet(list.get(0).get);
//                markerOptions.draggable(true);
//
//
//                setMarker = mMap.addMarker(markerOptions);
//
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(setLatLng);
//                mMap.moveCamera(cameraUpdate);
//
//
//    }



    // 판매자 위치 표시 메소드
    private void sellerLocation(){
        /////////////////////////////////////////////////
        // 판매자 위치 표시    (메인화면 - GPS)
        //////////////////////////////////////////////////
        Geocoder geocoder1 = new Geocoder(getBaseContext());
        List<Address> list = null;
        data = new ArrayList<Address>();

        ArrayList<String> address = new ArrayList<String>();
        address.add("서울특별시 강남구 강남대로 402");
        address.add("서울특별시 강남구 강남대로서 510-1");
        address.add("경기도 성남시 대왕판교로 477");
        address.add("서울특별시 서초구 잠원동 39-12");

        try {
            for(int i=0; i<address.size(); i++) {
                list = geocoder1.getFromLocationName
                        (address.get(i), // 지역 이름
                                10); // 읽을 개수
                data.add(list.get(0));
                Log.v("here", String.valueOf(list));
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
        }


        if (list != null) {
            if (list.size() == 0) {
                Toast.makeText(MainActivity.this, "해당되는 주소 정보는 없습니다", Toast.LENGTH_SHORT).show();
            } else {
                // 해당되는 주소로 인텐트 날리기
                Address addr = list.get(0);
                for (int i = 0; i < data.size(); i++) {
                    lat = data.get(i).getLatitude();
                    lng = data.get(i).getLongitude();

                    LatLng setLatLng = new LatLng(lat, lng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(setLatLng);
                    markerOptions.title("위치" + (i + 1));
                    //markerOptions.snippet(list.get(0).get);
                    markerOptions.draggable(true);


                    setMarker = mMap.addMarker(markerOptions);
                }
            }
        }
    }


    // map 초기 설정
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        // 말풍선 클릭 시 이벤트
        mMap.setOnInfoWindowClickListener(this);
        Geocoder geocoder = new Geocoder(this);

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();


        //////////////////////////////////////////////////
        // 판매자 위치 표시    (메인화면 - GPS)
        //////////////////////////////////////////////////
        Geocoder geocoder1 = new Geocoder(getBaseContext());
        List<Address> list = null;
        data = new ArrayList<Address>();

        ArrayList<String> address = new ArrayList<String>();
        address.add("서울특별시 강남구 강남대로 402");
        address.add("서울특별시 강남구 강남대로서 510-1");
        address.add("경기도 성남시 대왕판교로 477");
        address.add("서울특별시 서초구 잠원동 39-12");

        try {
            for(int i=0; i<address.size(); i++) {
                list = geocoder1.getFromLocationName
                        (address.get(i), // 지역 이름
                                10); // 읽을 개수
                data.add(list.get(0));
                Log.v("here", String.valueOf(list));
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
        }


        if (list != null) {
            if (list.size() == 0) {
                Toast.makeText(MainActivity.this, "해당되는 주소 정보는 없습니다", Toast.LENGTH_SHORT).show();
            } else {
                // 해당되는 주소로 인텐트 날리기
                Address addr = list.get(0);
                for (int i = 0; i < data.size(); i++) {
                    lat = data.get(i).getLatitude();
                    lng = data.get(i).getLongitude();

                    LatLng setLatLng = new LatLng(lat, lng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(setLatLng);
                    markerOptions.title("위치" + (i + 1));
                    //markerOptions.snippet(list.get(0).get);
                    markerOptions.draggable(true);


                    setMarker = mMap.addMarker(markerOptions);
                }
            }
        }



//        showPlaceInformation(currentPosition);
//        //카메라 이동 시작
//        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
//            @Override
//            public void onCameraMoveStarted(int i) {
//                Log.d("set>>","start");
//            }
//        });
//        // 카메라 이동 중
//        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
//            @Override
//            public void onCameraMove() {
//                Log.d("set>>","move");
//            }
//        });

        //////////////////////////////////////////////
        // 맵이 준비 후 시작될떄 실행
        // 초기화면을 마커의 위도경도로 지정하고 카메라를 이동
        //previous_marker.clear();

//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(dfsfa);
//        mMap.moveCamera(cameraUpdate);

        // marker 클릭 이벤트
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                LatLng latLng
//                        = new LatLng(marker.getPosition().latitude
//                        , marker.getPosition().longitude);
//
//               String clickAddress =  getCurrentAddress(latLng);
//               search.setText(clickAddress);
//                Toast.makeText(MainActivity.this, clickAddress, Toast.LENGTH_SHORT).show();
//               MarkerOptions markerOptions = new MarkerOptions();
//               markerOptions.position(latLng);
//               markerOptions.title("위치 전송하기");
//               String geoAddress = String.format("geo:%f,%f", marker.getPosition().latitude, marker.getPosition().longitude);
//
//                        // 구글 지도로 이동
////                        Intent intent = new Intent(
////                                Intent.ACTION_VIEW,
////                                Uri.parse(geoAddress));
////                        startActivity(intent);
//                return true;
//            }
//        });

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            startLocationUpdates(); // 3. 위치 업데이트 시작


        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }


        // 내 위치 버튼튼
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 지도 위 확대/축소 버튼
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // 손으로 줌 설정
        mMap.getUiSettings().setZoomControlsEnabled(true);



        // map 클릭 시 신규 마커 생성
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :");

               // 기존 마커 정리
                googleMap.clear();

                // 클릭한 위치로 지도 이동하기
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // 신규 마커 추가
                MarkerOptions newMarker=new MarkerOptions();
                newMarker.position(latLng);
                newMarker.title("선택 위치정보 보내기");
                googleMap.addMarker(newMarker);

            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                ///////////////////////////////////////////////////////
                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);


            }


        }

    };



    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
           mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);

        }


    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }



    // 현 위치 gps를  주소 전환
    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);

        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    // 현 위치 설정
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title("내 위치");
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);


        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

    }

    // 디폴트 위치 설정
    public void setDefaultLocation() {


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }

    //////////////////////////////////////////////////
    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    //////////////////////////////////////////////////
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }



    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(List<Place> places) {

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                for (noman.googleplaces.Place place : places) {
//
//
//                            LatLng latLng
//                            = new LatLng(place.getLatitude()
//                            , place.getLongitude());
//
//                    String markerSnippet = getCurrentAddress(latLng);
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(latLng);
//                    markerOptions.title(place.getName());
//                    markerOptions.snippet(markerSnippet);
//                    //주변 위치 표시 마커 아이콘 변경
//                   //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                    Marker item = mMap.addMarker(markerOptions);
//                    previous_marker.add(item);
//
//                }
//
//                //중복 마커 제거
//                HashSet<Marker> hashSet = new HashSet<Marker>();
//                hashSet.addAll(previous_marker);
//                previous_marker.clear();
//                previous_marker.addAll(hashSet);
//
//            }
//        });
    }

    @Override
    public void onPlacesFinished() {

    }

    // 마커 위 말풍선 클릭 시 이벤
    @Override
    public void onInfoWindowClick(Marker marker) {

        LatLng latLng
                = new LatLng(marker.getPosition().latitude
                , marker.getPosition().longitude);

        String markerSnippet = getCurrentAddress(latLng);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("해당 위치 전송");
        markerOptions.snippet(markerSnippet);
        //주변 위치 표시 마커 아이콘 변경
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        Marker item = mMap.addMarker(markerOptions);
        previous_marker.add(item);



        search.setText(markerSnippet.substring(4));
        Toast.makeText(this, "주소는 " + markerSnippet.substring(4),
                Toast.LENGTH_SHORT).show();


    }

    public void onMapClick(LatLng latLng){
        MarkerOptions mar
    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//
//        Toast.makeText(this, marker.getTitle() + "\n" + marker.getPosition(), Toast.LENGTH_SHORT).show();
//
//
//       return true;
//    }
}
