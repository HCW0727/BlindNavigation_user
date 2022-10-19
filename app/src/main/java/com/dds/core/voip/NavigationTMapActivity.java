package com.dds.core.voip;

import static java.lang.Math.abs;
import static java.lang.Math.round;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dds.webrtc.R;
import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NavigationTMapActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    public      static String  mApiKey ="cd801069-d783-4402-a170-dcef9b3fe221";   //' "38c7269d-5eb5-4739-b305-9886986b658f"; // 발급받은 appKey
    String      utteranceId=this.hashCode() + "";
    private     final boolean           bTTS_Speech   = true;
    private     TextToSpeech            ttsObj;
    private     TextView                txtAddress = null;
    private     RelativeLayout          contentView = null;
    private     TMapView                mMapView = null;
    private     TMapCircle              circle = null;
    public   	static boolean 	        m_bTrackingMode    = false;
    public      static boolean          m_bSimulation_mode = true;//false;
    private     static boolean          m_bCenterPointMode = false;
    private     int                     iter_simul = 0;
    private     Timer                   timer2      = null;
    private     String                  oldBAddress = null;
    private     String                  newAddress  = null;
    private     ArrayList<TMapPoint>    m_arrayPoint;
    private     ArrayList<TMapPoint>    m_arrayBookMark;
    private     ArrayList<String>       mArrayMarkerID;
    private     int                     mCurMarkerID = 0;
    private     int                     miSelect     = -1;

    private     Context mContext;

    private     double      m_distance = 0;
    private     int lang;
    TMapGpsManager gps = null;
    PermissionManager mPermissionManager = null;
    private     int              time_minute = 1, time_iter = 0;
    private     TMapPoint tMapOrgPoint  = new TMapPoint(0,0);
    private     TMapPoint tMapDstPoint  = new TMapPoint(0,0);


    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData = result.getData();
                    if (resultData != null) {
                        // ArrayList
                        ArrayList candidates =
                                resultData.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        if (candidates.size() > 0) {
                            String   name = (String) candidates.get(0) ;
                            //textView.setText(name);
                            //findAllPoi( name );
                            mCurMarkerID  = -1;
                            String   sCur    = "";
                            String   sSelect = "";
                            for( int i=0;i<mArrayMarkerID.size();i++ ) {

                                sCur = mArrayMarkerID.get( i );
                                if( name.compareToIgnoreCase( sCur  ) == 0 ) {
                                    txtAddress.setText( sCur );
                                    sSelect   = sCur;
                                    mCurMarkerID  = i;
                                    break;
                                }
                            }

                            if( mCurMarkerID==-1 ) {
                                txtAddress.setText( "북마크가 존재하지 않습니다. 다시 입력해주세요" );
                            }
                            String st = sSelect + "을 선택하셨습니다";
                            txtAddress.setText( sSelect );
                            if(bTTS_Speech ==true) ttsObj.speak( st,TextToSpeech.QUEUE_ADD,null, utteranceId);
                            //Intent intent = new Intent(NavigationActivity.this, ActivitySearchTMap.class);
                            //startActivity(intent);
                            //Intent intent = new Intent(MainActivity.this, ActivityBtnTMap.class);
                            //startActivity(intent);
                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  super.setContentView(R.layout.activity_navigation);
        super.setContentView(R.layout.activity_navigation_tmap );
        contentView  = (RelativeLayout)findViewById(R.id.contentView);
        super.onCreate(savedInstanceState);

        ttsObj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    ttsObj.setLanguage(Locale.KOREAN);
                    ttsObj.setPitch(1.f);
                    ttsObj.setSpeechRate(1.5f);
                    ttsObj.getVoice();
                    ttsObj.getVoices();
                }
            }
        });

        mContext          = this;
        m_arrayPoint      = new ArrayList<>();
        m_arrayPoint.clear();

        mArrayMarkerID    = new ArrayList<>();
        //mArrayMarkerID.add("강남역");  //37.498082125962,    127.028007144248
        //mArrayMarkerID.add("교대역");  //35.763624094753,    128.722262767166
        //mArrayMarkerID.add("역삼역");  //37.500665213063,    127.036450800771
       // mArrayMarkerID.add("서울대학교"); //37.459363917109,  126.953125393997
       // mArrayMarkerID.add("고려대학교"); //37.589543800333,  127.03233919086
        //mArrayMarkerID.add("한양대학교"); //37.55724296836, 127.046421568243
        //mArrayMarkerID.add("회사");    //37.500665213063,    127.036450800771
        //mArrayMarkerID.add("복지관");  //37.57015760429,    127.033394841404
        //mArrayMarkerID.add("서울역");  //37.554714113903,    126.970706216557
        mArrayMarkerID.add("행신역");
        mArrayMarkerID.add("고양종합운동장");
        mArrayMarkerID.add("대화역");
        mArrayMarkerID.add("대화도서관");
        mArrayMarkerID.add("주엽고등학교");
        mArrayMarkerID.add("일산호수공원");
        mArrayMarkerID.add("일산문화공원");
        m_arrayBookMark   = new ArrayList<TMapPoint>();
        m_arrayBookMark.clear();
        m_arrayBookMark.add( 0,  new TMapPoint(37.612175933,  126.834157342) );
        m_arrayBookMark.add( 1,  new TMapPoint(37.676369, 126.743082 ) );
        m_arrayBookMark.add( 2,  new TMapPoint( 37.676200421, 126.747511311) );
        m_arrayBookMark.add( 3,  new TMapPoint( 37.681148,  126.753599) );
        m_arrayBookMark.add( 4,  new TMapPoint( 37.675896, 126.754744) );
        m_arrayBookMark.add( 5,  new TMapPoint(37.656702, 126.766197) );
        m_arrayBookMark.add( 6,  new TMapPoint(37.6589462, 126.770719  ) );
        //m_arrayBookMark.add( 7,  new TMapPoint(37.57015760429,    127.033394841404) );



        Button button1 =  findViewById(R.id.btn1);
        Button button2 =  findViewById(R.id.btn2);
        Button button3 =  findViewById(R.id.btn3);
        Button button4 =  findViewById(R.id.btn4);

        txtAddress     =  findViewById(R.id.TextView01);
        String     sCur = mArrayMarkerID.get( mCurMarkerID );
        txtAddress.setText( sCur );


        button1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu1();
            }
        });

        button2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu2();
            }
        });

        button3.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu3();
            }
        });

        button4.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Menu4();
            }
        });

        // Gps Open
        gps = new TMapGpsManager(NavigationTMapActivity.this);
        mPermissionManager = new PermissionManager();
        gps.setMinTime(1000);
        gps.setMinDistance(2);
        gps.setProvider(gps.NETWORK_PROVIDER);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }
        // TmapView setting
        mMapView = new TMapView(this);
        mMapView.setSKTMapApiKey(mApiKey);
        mMapView.setCenterPoint( 0,0 );

        addView(mMapView);  // 지도 뷰 생성
        StartGuidance();
        addTMapCircle();

        // Insert A SKT Logo on the Tmap.
        mMapView.setTMapLogoPosition(TMapView.TMapLogoPositon.POSITION_BOTTOMRIGHT);

        //----
        mMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> markerlist, ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
                String   str ="Press Btn :" + String.valueOf(point.getLatitude()) + " , " + String.valueOf(point.getLongitude());

                //    circle.setCenterPoint(point);
                //    tMapDstPoint  = point;
                //     mMapView.setLocationPoint(point.getLongitude(), point.getLatitude());
                //    mMapView.setCenterPoint(point.getLongitude(), point.getLatitude());
                Log.i( str,"press up--- mMapView.setCenterPoint-" );
                //-----
                //  getLocationPoint( );
                //-----

                return false;
            }

            //  @Override
            public boolean onPressDnEvent(ArrayList<TMapMarkerItem> markerlist, ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
                String   str ="Press Btn :" + String.valueOf(point.getLatitude()) + " , " + String.valueOf(point.getLongitude());
                Log.i( str,"press dn" );
                //    circle.setCenterPoint(point);
                //    tMapDstPoint  = point;
                return false;
            }

            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> markerlist, ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
                String   str ="Press Btn :" + String.valueOf(point.getLatitude()) + " , " + String.valueOf(point.getLongitude());
                Log.i( str,"press dn" );
                //    circle.setCenterPoint(point);
                //    tMapDstPoint  = point;
                return false;
            }
        });


    }//onCreate -------------------

    /**    경로 그리기 메소드 */
    public void StartGuidance() {
        Log.i("huh","StartGuidance() : 1"  );
        m_bTrackingMode = true;
        setTrackingModeT( m_bTrackingMode );
        Log.i("huh","StartGuidance() : End"  );
    }
    /**
     * setTrackingMode 화면중심을 단말의 현재위치로 이동시켜주는 트래킹모드로 설정한다.
     */
    public void setTrackingModeT(boolean isShow) {
        Log.i("setTrackingModeT()", "tracking Function Before" );
        mMapView.setTrackingMode(isShow);
        Log.i("setTrackingModeT()", "tracking Function After");
        if (isShow) {
            Log.i("setTrackingMode()isShow", "tracking");
            mPermissionManager.request(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new PermissionManager.PermissionListener()
            {
                @Override
                public void granted() {
                    Log.i("request-granted()", "tracking");
                    if (gps != null) {
                        gps.setMinTime(1000);
                        gps.setMinDistance(5);
                        gps.setProvider(gps.GPS_PROVIDER);
                        gps.OpenGps();
                        gps.setProvider(gps.NETWORK_PROVIDER);
                        gps.OpenGps();
                        Log.i("gps.OpenGps()--", "tracking");
                    }
                }
                @Override
                public void denied() {
                    Log.i("request-denied()", "tracking");
                    Toast.makeText(NavigationTMapActivity.this, "위치정보 수신에 동의하지 않으시면 현재위치로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
            //locationBtn.setBackgroundResource(R.drawable.location_btn_sel);
            mMapView.setCenterPoint( mMapView.getLocationPoint().getLongitude(),
                    mMapView.getLocationPoint().getLatitude());
            Log.i( "oint-","center" );
            Toast.makeText(NavigationTMapActivity.this, "trackingmode-mMapView.setCenterPoint()", Toast.LENGTH_SHORT).show();
        } else {
            if (gps != null) {
                gps.CloseGps();
            }
            //locationBtn.setBackgroundResource(R.drawable.location_btn);
        }
    }

    @Override
    public void setContentView(int res)  {
        contentView.removeAllViews();
        LayoutInflater inflater;
        inflater = LayoutInflater.from(this);

        View item = inflater.inflate(res, null);
        contentView.addView(item, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

    }



    @Override
    public void setContentView(View view) {
        contentView.removeAllViews();
        contentView.addView(view, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
    }
    public void addView(View v)
    {
        contentView.removeAllViews();
        contentView.addView(v, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
    }


    //-----------------------------
    public void   Menu1( ) {
        //String  st = "음성북마크";
        //if(bTTS_Speech ==true) ttsObj.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
        speech( );
        //curPos에서 찾은것 넣기
        //찾앗다고 플래그 설정
    }
    //-----------------------------
    public void   Menu2( ) {
        // 현재 위치에서 인덱스 증가시키기
        //curPos에서 찾은것 넣기
        //찾앗다고 플래그 설정
        mCurMarkerID = mCurMarkerID += 1;
        if( mCurMarkerID >= mArrayMarkerID.size() ) mCurMarkerID = 0;
        String   sCur = mArrayMarkerID.get( mCurMarkerID );
        txtAddress.setText( sCur );

        String  st = sCur + " 을 선택하셨습니다";
        if(bTTS_Speech ==true) ttsObj.speak( st, TextToSpeech.QUEUE_FLUSH,null, utteranceId);
    }
    //-----------------------------
    public void   Menu3( ) {

        m_bTrackingMode = false;
        setTrackingModeT( m_bTrackingMode );
        String   sCur = mArrayMarkerID.get( mCurMarkerID );
        txtAddress.setText( sCur );
        String  st = sCur + " 으로 가는 경로를 찾았습니다";
        if(bTTS_Speech ==true) ttsObj.speak( st, TextToSpeech.QUEUE_FLUSH,null, utteranceId);


        TMapPoint   tmapCur = m_arrayBookMark.get( mCurMarkerID );

        //  String  st = "경로찾기";
        //  if(bTTS_Speech ==true) ttsObj.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
        //  Log.i( "Start-1", "btnMenu1( ) " );

//        double latitude1  = 35.837927205794486; //mMapView.getCenterPoint().getLatitude();
//        double longitude1 = 128.61044835627567; //mMapView.getCenterPoint().getLongitude();
        double latitude1  = 37.669303; //mMapView.getCenterPoint().getLatitude();
        double longitude1 = 126.741319; //mMapView.getCenterPoint().getLongitude();

//        double latitude1  = tMapOrgPoint.getLatitude();
//        double longitude1 = tMapOrgPoint.getLongitude();
        tMapDstPoint = tmapCur;
//        tMapDstPoint.setLatitude( 35.83366264669403 );
//        tMapDstPoint.setLongitude( 128.61342430114746 );

        double latitude2  = tMapDstPoint.getLatitude();
        double longitude2 = tMapDstPoint.getLongitude();

        TMapPoint tMapPoint1 = new TMapPoint(latitude1, longitude1);
        TMapPoint tMapPoint2 = new TMapPoint(latitude2, longitude2);

        tMapDstPoint  = tMapPoint2;

        mMapView.setLocationPoint(longitude1, latitude1);
        mMapView.setCenterPoint(longitude1, latitude1);

        CalcPathFinder( tMapPoint1, tMapPoint2 );
        //----------
        //---- Timer 사용하기
        m_bSimulation_mode = true;
        mMapView.setTrackingMode(false);
        iter_simul = 0;
        mMapView.setZoomLevel(17);

        if( timer2 == null  ) {
            timer2 = new Timer();
            timer2.schedule(simul, 0, 100 * 13);
        }
    }
    //-----------------------------
    public void   Menu4( ) {
        String  st;
        st = "네비게이션 기능을 종료합니다";
        if(bTTS_Speech ==true) ttsObj.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
        iter_simul = 1000;
        finish( );
        return;
    }
    //-----------------------------
    TimerTask simul = new TimerTask() {
        public void run() {
            //----- 타이머에 따라서 진행하기
            int  ptSize = m_arrayPoint.size();
            if( iter_simul >= ptSize )
            {
                m_bSimulation_mode = false;
                //mMapView.setTrackingMode(true);
                return;
            }
            //private double bearing(Location startPoint, Location endPoint) {
            Log.i(String.valueOf(iter_simul),"simulation");

            TMapPoint  tMapPoint = m_arrayPoint.get(iter_simul);
            circle.setCenterPoint(tMapPoint);
            circle.setRadius(40);
            mMapView.setLocationPoint(tMapPoint.getLongitude(), tMapPoint.getLatitude());
            mMapView.setCenterPoint(tMapPoint.getLongitude(), tMapPoint.getLatitude());
            if( iter_simul > 0 && iter_simul < ptSize-1 ) {
                HelpPathFnder( iter_simul );
            }
            iter_simul ++;

            System.gc();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String   st = "onKeyDown-----" + String.valueOf( keyCode );
        Log.i("--", st);
        if( keyCode == 25 )  Menu1( );
        if( keyCode == 24 )  Menu2( );
        if( keyCode == 96 )  Menu3( );
        if( keyCode == 97 )  Menu4( );
        if( keyCode ==  4 )  Menu4( );
        return true;
    }
    public void addTMapCircle() {
        circle = new TMapCircle();
        circle.setRadius(100);
        circle.setLineColor(Color.BLUE);
        circle.setAreaAlpha(50);
        circle.setCircleWidth((float) 10);
        circle.setRadiusVisible(true);

        mMapView.addTMapCircle("picking", circle);
    }

    //----------------------------------------
    private void speech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if (lang == 0) {
            // 한국어
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREA.toString());
        } else if (lang == 1) {
            // 영어
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString());
        } else if (lang == 2) {
            // Off line mode
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        }

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "음성입력");

        try {
            // undo
            resultLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            //    textView.setText(R.string.error);
        }
    }

    @Override
    public void onLocationChange(Location location) {
        /*
        LogManager.printLog("onLocationChange :::> " + location.getLatitude() + " " +
                location.getLongitude() + " " + location.getSpeed() + " " + location.getAccuracy());

        if(m_bTrackingMode && m_bSimulation_mode == true)
        {
            location.setLatitude( 35.837927205794486 ); //mMapView.getCenterPoint().getLatitude();
            location.setLongitude(  128.61044835627567 ); //mMapView.getCenterPoint().getLongitude();
        }
        else {
        }
        mMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        mMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        Log.i( " Point-","center" );
        */
    }
    ////CalcPathFinder //////////////////////////////////////////////////////////
    public int CalcPathFinder( TMapPoint tMapPoint1, TMapPoint tMapPoint2 ) {
        Log.i("Start-1", "CalcPathFinder( ) ");
        mMapView.removeTMapPath();
        TMapCircle tMapCircle = new TMapCircle();
        tMapCircle.setCenterPoint(tMapPoint1);
        tMapCircle.setRadius(40);
        tMapCircle.setCircleWidth(2);
        tMapCircle.setLineColor(Color.BLUE);
        tMapCircle.setAreaColor(Color.GRAY);
        tMapCircle.setAreaAlpha(100);
        mMapView.addTMapCircle("circle1", tMapCircle);

        TMapData tmapdata = new TMapData();
        tmapdata.findPathDataWithType( TMapData.TMapPathType.PEDESTRIAN_PATH, tMapPoint1,tMapPoint2,
                new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine polyLine) {
                        if( polyLine.getDistance() < 1 ) return;
                        ArrayList<TMapPoint> arrayPoint = polyLine.getLinePoint();
                        if( arrayPoint.size() < 2 ) return;

                        polyLine.setLineColor(Color.BLUE);
                        mMapView.addTMapPath(polyLine);
                        m_distance = polyLine.getDistance();

                        m_arrayPoint.clear();
                        m_arrayPoint.add(  arrayPoint.get(0) );
                        for (int i = 1; i < arrayPoint.size(); i++) {
                            TMapPoint tMapPoint0 = arrayPoint.get(i-1);
                            TMapPoint tMapPoint1 = arrayPoint.get(i);
                            double dist = calDistance( tMapPoint0,tMapPoint1 );
                            if( dist <= 1 ) continue;
                            m_arrayPoint.add( tMapPoint1);

                            double latitude1  = tMapPoint1.getLatitude();
                            double longitude1 = tMapPoint1.getLongitude();
                            Log.i(String.valueOf(i) + ":" + String.valueOf(latitude1)+" + "+String.valueOf(longitude1), "Polygon");
                        }
                        int  ptSize = m_arrayPoint.size();
                        TMapPoint  tMapPoint1 = m_arrayPoint.get(0);
                        TMapPoint  tMapPoint2 = m_arrayPoint.get(ptSize-1);
                        double longitude1 = tMapPoint1.getLongitude();
                        double latitude1  = tMapPoint1.getLatitude();

                        Bitmap start = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_start);
                        Bitmap end = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_end);
                        mMapView.setTMapPathIcon(start, end);
                        mMapView.setLocationPoint(longitude1, latitude1);
                        mMapView.setCenterPoint(longitude1,   latitude1);


                        mMapView.zoomToTMapPoint(tMapPoint1 ,tMapPoint2 );

                        Log.i(String.valueOf(m_arrayPoint.size()), "절점 갯수 : ");
                        Log.i(String.valueOf(m_distance), "절점 거리 : ");
                        Log.i( " Pos","view centerpos");
                    }
                });
        return 1;
    }

    public void HelpPathFnder( int index ) {
        int i = index;
        Location Pt1;
        Location Pt2;
        Location Pt3;
        TMapPoint tMapPoint1 = m_arrayPoint.get(i - 1);
        TMapPoint tMapPoint2 = m_arrayPoint.get(i);
        TMapPoint tMapPoint3 = m_arrayPoint.get(i + 1);

        double angle1 = bearing(tMapPoint1, tMapPoint2);
        double angle2 = bearing(tMapPoint2, tMapPoint3);
        double dist1 = calDistance(tMapPoint1, tMapPoint2);
        double dist2 = calDistance(tMapPoint2, tMapPoint3);

        String toS = String.valueOf(i);// + "좌회전";
        double res_angle = angle1 - angle2;
        if (res_angle < -180) res_angle += 360;
        if (res_angle > 180) res_angle -= 360;
        Log.i(String.valueOf(i) + ":" + String.valueOf(res_angle) + ">>"
                + String.valueOf(angle1) + " + " + String.valueOf(angle2), "angle");
        if (abs(res_angle) > 40 && dist1 > 0.1 && dist2 > 0.1) {
            TMapCircle tMapCircle = new TMapCircle();
            tMapCircle.setRadius(30);
            tMapCircle.setCircleWidth(1);
            tMapCircle.setLineColor(Color.RED);
            tMapCircle.setAreaAlpha(100);

            double dangle = res_angle / 10.;
            long iAngle = round(dangle);
            iAngle = abs(iAngle) * 10;
            toS = String.valueOf(iAngle);

            Log.i(String.valueOf(i) + ":" + String.valueOf(dist1) + " + " + String.valueOf(dist2), "dist");
            if (res_angle < 0) {
                tMapCircle.setAreaColor(Color.RED);
                // toS += String.valueOf( iAngle );
                toS += "도 우회전";
                //img_direction.setImageResource(R.drawable.direction_13);
            } else {
                tMapCircle.setAreaColor(Color.BLUE);
                toS += "도 좌회전";
                //img_direction.setImageResource(R.drawable.direction_12);
            }
            if(bTTS_Speech==true) ttsObj.speak(toS, TextToSpeech.QUEUE_ADD, null, utteranceId);
            Log.i("HelpPathFnder", toS);
        }
    }
    /*==========
    [ 경도 위도로 거리 구하기 ]
    ==========*/
    private double calDistance(TMapPoint startPoint, TMapPoint endPoint) {
        double lon1 = startPoint.getLongitude();
        double lat1 = Math.toRadians(startPoint.getLatitude());

        double lon2 = endPoint.getLongitude();
        double lat2 = Math.toRadians(endPoint.getLatitude());

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        if( abs(dist) > 1 ) return 0;
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환
        return dist;
    }

    public double calDistance(double lat1, double lon1, double lat2, double lon2){

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        if( abs(dist) > 1 ) return 0;
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }


    private double bearing(TMapPoint startPoint, TMapPoint endPoint) {
        double longitude1 = startPoint.getLongitude();
        double latitude1 = Math.toRadians(startPoint.getLatitude());

        double longitude2 = endPoint.getLongitude();
        double latitude2 = Math.toRadians(endPoint.getLatitude());

        double longDiff = Math.toRadians(longitude2 - longitude1);

        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return Math.toDegrees(Math.atan2(y, x));
    }
    private double bearing(Location startPoint, Location endPoint) {
        double longitude1 = startPoint.getLongitude();
        double latitude1 = Math.toRadians(startPoint.getLatitude());

        double longitude2 = endPoint.getLongitude();
        double latitude2 = Math.toRadians(endPoint.getLatitude());

        double longDiff = Math.toRadians(longitude2 - longitude1);

        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return Math.toDegrees(Math.atan2(y, x));
    }


}