package com.dds;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by JWCha on 2017-03-31.
 * 권한요청 결과는 activity 의 onRequestPermissionsResult 함수로 넘어온다.
 * 따라서 이 클래스를 맴버변수로 선언하고 activity 의 onRequestPermissionsResult 함수에서
 * 이 클래스의 setResponse 함수를 호출해줘야 한다.
 * ( ACCESS_FINE_LOCATION 과 ACCESS_COARSE_LOCATION 두개를 함께 요청하면 승인요청 창이 한번만 뜨고 결과도 ACCESS_FINE_LOCATION 만 grant 로 넘어옴 )
 * ( ACCESS_FINE_LOCATION 과  ACCESS_COARSE_LOCATION 이 같은 권한그룹이라 물어보는 창도 한번 뜨고 결과도 1개만 Grant 로 오는듯? / 추후 테스트 필요 )
 */

public class PermissionManager {

    private static final int REQUEST_CODE = 1;

    PermissionListener mPermissionListener = null;

    public interface PermissionListener {
        void granted();
        void denied();
    }

    public void request(Activity activity, String[] permissions, PermissionListener listener ) {

        this.mPermissionListener = listener; // 권한 승인 결과 리스너
        int cntGrant = 0; // 권한 승인 개수

        if (Build.VERSION.SDK_INT >= 23) {
            // 권한 요청 ( 요청 결과는 activity 의 onRequestPermissionsResult 함수로 넘어온다. )
            // 따라서 이 클래스를 맴버변수로 선언하고 onRequestPermissionsResult 함수에서
            // 이 클래스의 setResponse 함수를 호출해줘야 한다.
            for(int i=0; i<permissions.length; i++) {
                if( activity.checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_GRANTED ) {
                    // 해당 권한이 승인된 경우
                    cntGrant++;
                }
            }

            if( permissions.length != cntGrant ) {
                // 필요한 권한 개수와 승인된 권한 개수가 다르다면 권한 요청
                activity.requestPermissions(permissions, REQUEST_CODE);
            }
            else {
                listener.granted();
            }
        }
        else {
            // SDK 23 미만은 설치하기 전에 권한요청 함 ( 따라서 이미 권한 있음 )
            listener.granted();
        }
    }

    public void setResponse(int requestCode, int[] grantResults) {
        int cntGrant = 0;
        if( requestCode == REQUEST_CODE ) {
            if ( grantResults.length > 0 ) {
                for( int i=0; i<grantResults.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        cntGrant++;
                    }
                }

                if( grantResults.length == cntGrant ) {
                    // 권한 승인 허가
                    mPermissionListener.granted();
                }
                else {
                    // 권한 승인 거부
                    mPermissionListener.denied();
                }
            } else {
                // 권한 승인 거부
                mPermissionListener.denied();
            }
            return;
        }
    }
}
