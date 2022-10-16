package com.dds;

import static java.lang.Thread.State.WAITING;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dds.data.FrameToRecord;
import com.dds.data.RecordFragment;
import com.dds.util.CameraHelper;
import com.dds.util.MiscUtils;
import com.dds.webrtc.R;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public class FFmpegRecordActivity extends AppCompatActivity implements
        TextureView.SurfaceTextureListener, View.OnClickListener, SensorEventListener {
    String      utteranceId=this.hashCode() + "";
    private     final boolean           bTTS_Speech   = true;
    private     TextToSpeech tts;
    private     long startTime = 0, endTime = 0, timeGap = 0;
    private static final String LOG_TAG = FFmpegRecordActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS = 1;

    private static final int PREFERRED_PREVIEW_WIDTH = 480;
    private static final int PREFERRED_PREVIEW_HEIGHT = 640;

    // both in milliseconds
    private static final long MIN_VIDEO_LENGTH = 1 * 1000;
    private static final long MAX_VIDEO_LENGTH = 300 * 1000;

    private FixedRatioCroppedTextureView mPreview;
    private Button mBtnResumeOrPause;
    private Button mBtnDone;
    private Button mBtnSwitchCamera;
    private Button mBtnReset;
    private Button mBtn5;

    private int mCameraId;
    private Camera mCamera;
    private FFmpegFrameRecorder mFrameRecorder;
    private VideoRecordThread mVideoRecordThread;
    private volatile boolean mRecording = false;
    private File mVideo, mSensorValues;
    private LinkedBlockingQueue<FrameToRecord> mFrameToRecordQueue;
    private LinkedBlockingQueue<FrameToRecord> mRecycledFrameQueue;
    private int mFrameToRecordCount;
    private int mFrameRecordedCount;
    private long mTotalProcessFrameTime;
    private Stack<RecordFragment> mRecordFragments;

    /* The sides of width and height are based on camera orientation.
    That is, the preview size is the size before it is rotated. */
    private int mPreviewWidth = PREFERRED_PREVIEW_WIDTH;
    private int mPreviewHeight = PREFERRED_PREVIEW_HEIGHT;
    // Output video size
    private int videoWidth = 480;
    private int videoHeight = 640;
    private int videoBitrate = 400000;
    private int frameRate = 30;
    private int frameDepth = Frame.DEPTH_UBYTE;
    private int frameChannels = 2;

    // Workaround for https://code.google.com/p/android/issues/detail?id=190966
    private Runnable doAfterAllPermissionsGranted;

    private float[] acc = {-1f, -1f, -1f}, mag = {-1f, -1f, -1f}, gyro = {-1f, -1f, -1f};
    private LinkedBlockingQueue<String> sensorValueLines = new LinkedBlockingQueue<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_record);


        mPreview = findViewById(R.id.camera_preview);
        mBtnResumeOrPause = findViewById(R.id.btn_resume_or_pause);
        mBtnDone = findViewById(R.id.btn_done);
        mBtnSwitchCamera = findViewById(R.id.btn_switch_camera);
        mBtn5 = findViewById(R.id.btn5);


        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(1.4f);
                }
            }
        });

       // mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        setPreviewSize(mPreviewWidth, mPreviewHeight);
        mPreview.setCroppedSizeWeight(videoWidth, videoHeight);
        mPreview.setSurfaceTextureListener(this);
        mBtnResumeOrPause.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
        mBtnSwitchCamera.setOnClickListener(this);
        mBtn5.setOnClickListener(this);

        // At most buffer 10 Frame
        mFrameToRecordQueue = new LinkedBlockingQueue<>(10);
        // At most recycle 2 Frame
        mRecycledFrameQueue = new LinkedBlockingQueue<>(2);
        mRecordFragments = new Stack<>();

        // SensorManager
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            int rate = SensorManager.SENSOR_DELAY_FASTEST;
            Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, accelSensor, rate);
            sensorManager.registerListener(this, gyroSensor, rate);
            sensorManager.registerListener(this, magSensor, rate);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String   st = "onKeyDown" + String.valueOf( keyCode );
        Log.i("-------------------", st);
        if( keyCode == 25 )
        {
            Menu1( );
        }
        if( keyCode == 24 )
        {
            Menu2( );
        }
        if( keyCode == 96 )
        {
            Menu3( );
        }
        if( keyCode == 97 ) {
            Menu4( );
        }
        if( keyCode ==  4 )  Menu4( );

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecorder();
        releaseRecorder(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (doAfterAllPermissionsGranted != null) {
            doAfterAllPermissionsGranted.run();
            doAfterAllPermissionsGranted = null;
        } else {
            String[] neededPermissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
            List<String> deniedPermissions = new ArrayList<>();
            for (String permission : neededPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                }
            }
            if (deniedPermissions.isEmpty()) {
                // All permissions are granted
                doAfterAllPermissionsGranted();
            } else {
                String[] array = new String[deniedPermissions.size()];
                array = deniedPermissions.toArray(array);
                ActivityCompat.requestPermissions(this, array, REQUEST_PERMISSIONS);
            }
        }
    }
    //-----------------------------
    public void   Menu1( ) {
        resumeRecording();
        startTime = System.currentTimeMillis();
        Log.i("-------------------", "resumeRecording()");
        String  st = "녹화를 시작합니다";
        if(bTTS_Speech ==true) tts.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
    }
    //-----------------------------
    public void   Menu2( ) {
        pauseRecording();
        endTime = System.currentTimeMillis();
        timeGap = endTime - startTime;
        new FinishRecordingTask().execute();
        String   timegap = String.valueOf(timeGap);
        Log.i("----", timegap );
        String  st = "녹화를 중지합니다";
        if(bTTS_Speech ==true) tts.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
    }
    //-----------------------------
    public void   Menu3( ) {
        pauseRecording();
        if (timeGap < MIN_VIDEO_LENGTH) {
            Toast.makeText(this, R.string.video_too_short, Toast.LENGTH_SHORT).show();
            String  st = "녹화가 너무 짧습니다";
            Log.i("FFPMPEG", st);
            if(bTTS_Speech ==true) tts.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
            return;
        }
        String  st = "미리보기를 시작합니다";
        if(bTTS_Speech ==true) tts.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
        Intent intent = new Intent(FFmpegRecordActivity.this, PlaybackActivity.class);
        intent.putExtra(PlaybackActivity.INTENT_NAME_VIDEO_PATH, mVideo.getPath());
        startActivity(intent);
    }
    //-----------------------------
    public void   Menu5( ) {
        Intent intent = new Intent(FFmpegRecordActivity.this, PlaybackActivity.class);
        intent.putExtra(PlaybackActivity.INTENT_NAME_VIDEO_PATH,
                "android.resource://" + getPackageName() + "/" + R.raw.blackbox3 );
        startActivity(intent);
    }
    //-----------------------------
    public void   Menu4( ) {
        Log.i("-------------------", "Finish()");
        tts.stop();
        String  st;
        st = "블랙박스 기능을 종료합니다";
        if(bTTS_Speech ==true) tts.speak( st, TextToSpeech.QUEUE_ADD,null, utteranceId);
        finish( );
        return;
        /*
        Intent i = new Intent();
        i.setType("video/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

    //    Uri uri = Uri.parse( Environment.getExternalStorageDirectory().getPath()+ "/Download/");
        Uri uri =  Uri.parse( "/storage/emulated/0/Pictures/com.github.crazyorr.ffmpegrecorder/"  );



        Log.i("------video path",   mVideo.getPath() );
        Log.i("------env getpath",  Environment.getExternalStorageDirectory().getPath() );

        i.setDataAndType(uri, "video/*");

        // pass the constant to compare it
        // with the returned requestCode
        int SELECT_PICTURE = 200;
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);


        //intent.putExtra(PlaybackActivity.INTENT_NAME_VIDEO_PATH, mVideo.getPath());
        */
    }
    //-----------------------------

    @Override
    protected void onPause() {
        super.onPause();
        pauseRecording();
        stopRecording();
        stopPreview();
        releaseCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean permissionsAllGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionsAllGranted = false;
                    break;
                }
            }
            if (permissionsAllGranted) {
                doAfterAllPermissionsGranted = new Runnable() {
                    @Override
                    public void run() {
                        doAfterAllPermissionsGranted();
                    }
                };
            } else {
                doAfterAllPermissionsGranted = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FFmpegRecordActivity.this, R.string.permissions_denied_exit, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                };
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
        startPreview(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_resume_or_pause) {
            /*
            if (mRecording) {
                pauseRecording();
            } else {
                resumeRecording();
            }*/
            Menu1( );

        } else if (i == R.id.btn_done) {
            /*
            pauseRecording();
            // check video length
            if (calculateTotalRecordedTime(mRecordFragments) < MIN_VIDEO_LENGTH) {
                Toast.makeText(this, R.string.video_too_short, Toast.LENGTH_SHORT).show();
                return;
            }
            new FinishRecordingTask().execute();
             */
            Menu2( );

        } else if (i == R.id.btn_switch_camera) {
            /*
            Intent intent = new Intent(FFmpegRecordActivity.this, PlaybackActivity.class);
            intent.putExtra(PlaybackActivity.INTENT_NAME_VIDEO_PATH, mVideo.getPath());
            startActivity(intent);
             */
            Menu3( );

            /* huh camera는 항상 front
            final SurfaceTexture surfaceTexture = mPreview.getSurfaceTexture();
            new ProgressDialogTask<Void, Integer, Void>(R.string.please_wait) {

                @Override
                protected Void doInBackground(Void... params) {
                    stopRecording();
                    stopPreview();
                    releaseCamera();

                    mCameraId = (mCameraId + 1) % 2;

                    acquireCamera();
                    startPreview(surfaceTexture);
                    startRecording();
                    return null;
                }
            }.execute();
             */

        }
        else if (i == R.id.btn5) {
            Menu5( );
        }
        /*else if (i == R.id.btn_reset) {
            pauseRecording();
            new ProgressDialogTask<Void, Integer, Void>(R.string.please_wait) {

                @Override
                protected Void doInBackground(Void... params) {
                    stopRecording();
                    stopRecorder();

                    startRecorder();
                    startRecording();
                    return null;
                }
            }.execute();
        }
        */
    }

    private void doAfterAllPermissionsGranted() {
        acquireCamera();
        SurfaceTexture surfaceTexture = mPreview.getSurfaceTexture();
        if (surfaceTexture != null) {
            // SurfaceTexture already created
            startPreview(surfaceTexture);
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mFrameRecorder == null) {
                    initRecorder();
                    startRecorder();
                }
                startRecording();
            }
        }).start();
    }

    private void setPreviewSize(int width, int height) {
        if (MiscUtils.isOrientationLandscape(this)) {
            mPreview.setPreviewSize(width, height);
        } else {
            // Swap width and height
            mPreview.setPreviewSize(height, width);
        }
    }

    private void startPreview(SurfaceTexture surfaceTexture) {
        if (mCamera == null) {
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = CameraHelper.getOptimalSize(previewSizes,
                PREFERRED_PREVIEW_WIDTH, PREFERRED_PREVIEW_HEIGHT);
        // if changed, reassign values and request layout
        if (mPreviewWidth != previewSize.width || mPreviewHeight != previewSize.height) {
            mPreviewWidth = previewSize.width;
            mPreviewHeight = previewSize.height;
            setPreviewSize(mPreviewWidth, mPreviewHeight);
            mPreview.requestLayout();
        }
        parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
//        parameters.setPreviewFormat(ImageFormat.NV21);
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(parameters);

        mCamera.setDisplayOrientation(CameraHelper.getCameraDisplayOrientation(
                this, mCameraId));

        // YCbCr_420_SP (NV21) format
        byte[] bufferByte = new byte[mPreviewWidth * mPreviewHeight * 3 / 2];
        mCamera.addCallbackBuffer(bufferByte);
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {

            private long lastPreviewFrameTime;

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                long thisPreviewFrameTime = System.currentTimeMillis();
                if (lastPreviewFrameTime > 0) {
                    Log.d(LOG_TAG, "Preview frame interval: " + (thisPreviewFrameTime - lastPreviewFrameTime) + "ms");
                }
                lastPreviewFrameTime = thisPreviewFrameTime;

                // get video data
                if (mRecording) {
                    // pop the current record fragment when calculate total recorded time
                    RecordFragment curFragment = mRecordFragments.pop();
                    long recordedTime = calculateTotalRecordedTime(mRecordFragments);
                    // push it back after calculation
                    mRecordFragments.push(curFragment);
                    long curRecordedTime = System.currentTimeMillis()
                            - curFragment.getStartTimestamp() + recordedTime;
                    // check if exceeds time limit
                    if (curRecordedTime > MAX_VIDEO_LENGTH) {
                        pauseRecording();
                        new FinishRecordingTask().execute();
                        return;
                    }

                    long timestamp = 1000 * curRecordedTime;
                    Frame frame;
                    FrameToRecord frameToRecord = mRecycledFrameQueue.poll();
                    if (frameToRecord != null) {
                        frame = frameToRecord.getFrame();
                        frameToRecord.setTimestamp(timestamp);
                    } else {
                        frame = new Frame(mPreviewWidth, mPreviewHeight, frameDepth, frameChannels);
                        frameToRecord = new FrameToRecord(timestamp, frame);
                    }
                    ((ByteBuffer) frame.image[0].position(0)).put(data);

                    if (mFrameToRecordQueue.offer(frameToRecord)) {
                        mFrameToRecordCount++;
                    }
                }
                mCamera.addCallbackBuffer(data);
            }
        });

        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mCamera.startPreview();
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
        }
    }

    private void acquireCamera() {
        try {
            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void initRecorder() {
        Log.i(LOG_TAG, "init mFrameRecorder");

        String recordedTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mVideo = CameraHelper.getOutputMediaFile(recordedTime, CameraHelper.MEDIA_TYPE_VIDEO);
        Log.i(LOG_TAG, "Output Video: " + mVideo);
        mSensorValues = new File(mVideo.getAbsolutePath() + ".txt");
        try {
            mSensorValues.createNewFile();
            Log.i(LOG_TAG, "Output Text: " + mSensorValues);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mFrameRecorder = new FFmpegFrameRecorder(mVideo, videoWidth, videoHeight);
        mFrameRecorder.setFormat("mp4");
        mFrameRecorder.setVideoBitrate(videoBitrate);
        mFrameRecorder.setFrameRate(frameRate);

        // Use H264
        mFrameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        // See: https://trac.ffmpeg.org/wiki/Encode/H.264#crf
        /*
         * The range of the quantizer scale is 0-51: where 0 is lossless, 23 is default, and 51 is worst possible. A lower value is a higher quality and a subjectively sane range is 18-28. Consider 18 to be visually lossless or nearly so: it should look the same or nearly the same as the input but it isn't technically lossless.
         * The range is exponential, so increasing the CRF value +6 is roughly half the bitrate while -6 is roughly twice the bitrate. General usage is to choose the highest CRF value that still provides an acceptable quality. If the output looks good, then try a higher value and if it looks bad then choose a lower value.
         */
        mFrameRecorder.setVideoOption("crf", "28");
        mFrameRecorder.setVideoOption("preset", "superfast");
        mFrameRecorder.setVideoOption("tune", "zerolatency");

        Log.i(LOG_TAG, "mFrameRecorder initialize success");
    }

    private void releaseRecorder(boolean deleteFile) {
        if (mFrameRecorder != null) {
            try {
                mFrameRecorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            mFrameRecorder = null;

            if (deleteFile) {
                mVideo.delete();
            }
        }
    }

    private void startRecorder() {
        try {
            mFrameRecorder.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecorder() {
        if (mFrameRecorder != null) {
            try {
                mFrameRecorder.stop();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        mRecordFragments.clear();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void startRecording() {
        mVideoRecordThread = new VideoRecordThread();
        mVideoRecordThread.start();
    }

    private void stopRecording() {
        if (mVideoRecordThread != null) {
            if (mVideoRecordThread.isRunning()) {
                mVideoRecordThread.stopRunning();
            }
        }

        try {
            if (mVideoRecordThread != null) {
                mVideoRecordThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mVideoRecordThread = null;


        mFrameToRecordQueue.clear();
        mRecycledFrameQueue.clear();
    }

    private void resumeRecording() {
        if (!mRecording) {
            RecordFragment recordFragment = new RecordFragment();
            recordFragment.setStartTimestamp(System.currentTimeMillis());
            mRecordFragments.push(recordFragment);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnSwitchCamera.setEnabled(false);
                //    mBtnResumeOrPause.setText(R.string.pause);
                }
            });
            mRecording = true;
        }
    }

    private void pauseRecording() {
        if (mRecording) {
            mRecordFragments.peek().setEndTimestamp(System.currentTimeMillis());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnSwitchCamera.setEnabled(true);
                //    mBtnResumeOrPause.setText(R.string.resume);
                }
            });
            mRecording = false;
        }
    }

    private long calculateTotalRecordedTime(Stack<RecordFragment> recordFragments) {
        long recordedTime = 0;
        for (RecordFragment recordFragment : recordFragments) {
            recordedTime += recordFragment.getDuration();
        }
        return recordedTime;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                acc = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyro = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mag = sensorEvent.values.clone();
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class RunningThread extends Thread {
        boolean isRunning;

        public boolean isRunning() {
            return isRunning;
        }

        public void stopRunning() {
            this.isRunning = false;
        }
    }

    class VideoRecordThread extends RunningThread {
        @Override
        public void run() {
            int previewWidth = mPreviewWidth;
            int previewHeight = mPreviewHeight;

            List<String> filters = new ArrayList<>();
            // Transpose
            String transpose = null;
            String hflip = null;
            String vflip = null;
            String crop = null;
            String scale = null;
            int cropWidth;
            int cropHeight;
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    switch (info.orientation) {
                        case 270:
                            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                transpose = "transpose=clock_flip"; // Same as preview display
                            } else {
                                transpose = "transpose=cclock"; // Mirrored horizontally as preview display
                            }
                            break;
                        case 90:
                            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                transpose = "transpose=cclock_flip"; // Same as preview display
                            } else {
                                transpose = "transpose=clock"; // Mirrored horizontally as preview display
                            }
                            break;
                    }
                    cropWidth = previewHeight;
                    cropHeight = cropWidth * videoHeight / videoWidth;
                    crop = String.format("crop=%d:%d:%d:%d",
                            cropWidth, cropHeight,
                            (previewHeight - cropWidth) / 2, (previewWidth - cropHeight) / 2);
                    // swap width and height
                    scale = String.format("scale=%d:%d", videoHeight, videoWidth);
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    switch (rotation) {
                        case Surface.ROTATION_90:
                            // landscape-left
                            switch (info.orientation) {
                                case 270:
                                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                        hflip = "hflip";
                                    }
                                    break;
                            }
                            break;
                        case Surface.ROTATION_270:
                            // landscape-right
                            switch (info.orientation) {
                                case 90:
                                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                        hflip = "hflip";
                                        vflip = "vflip";
                                    }
                                    break;
                                case 270:
                                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                        vflip = "vflip";
                                    }
                                    break;
                            }
                            break;
                    }
                    cropHeight = previewHeight;
                    cropWidth = cropHeight * videoWidth / videoHeight;
                    crop = String.format("crop=%d:%d:%d:%d",
                            cropWidth, cropHeight,
                            (previewWidth - cropWidth) / 2, (previewHeight - cropHeight) / 2);
                    scale = String.format("scale=%d:%d", videoWidth, videoHeight);
                    break;
                case Surface.ROTATION_180:
                    break;
            }
            // transpose
            if (transpose != null) {
                filters.add(transpose);
            }
            // horizontal flip
            if (hflip != null) {
                filters.add(hflip);
            }
            // vertical flip
            if (vflip != null) {
                filters.add(vflip);
            }
            // crop
            if (crop != null) {
                filters.add(crop);
            }
            // scale (to designated size)
            if (scale != null) {
                filters.add(scale);
            }

            FFmpegFrameFilter frameFilter = new FFmpegFrameFilter(TextUtils.join(",", filters),
                    previewWidth, previewHeight);
            frameFilter.setPixelFormat(avutil.AV_PIX_FMT_NV21);
            frameFilter.setFrameRate(frameRate);
            try {
                frameFilter.start();
            } catch (FrameFilter.Exception e) {
                e.printStackTrace();
            }

            isRunning = true;
            FrameToRecord recordedFrame;

            while (isRunning || !mFrameToRecordQueue.isEmpty()) {
                try {
                    recordedFrame = mFrameToRecordQueue.take();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    try {
                        frameFilter.stop();
                    } catch (FrameFilter.Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

                if (mFrameRecorder != null) {
                    long timestamp = recordedFrame.getTimestamp();
                    if (timestamp > mFrameRecorder.getTimestamp()) {
                        mFrameRecorder.setTimestamp(timestamp);
                    }
                    long startTime = System.currentTimeMillis();
//                    Frame filteredFrame = recordedFrame.getFrame();
                    Frame filteredFrame = null;
                    try {
                        frameFilter.push(recordedFrame.getFrame());
                        filteredFrame = frameFilter.pull();
                    } catch (FrameFilter.Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        mFrameRecorder.record(filteredFrame);

                        float[] tmpAcc = acc.clone(), tmpMag = mag.clone(), tmpGyro = gyro.clone();
                        String timeValue = String.valueOf(timestamp / 1000000f);
                        String accValues = String.format("%f %f %f", tmpAcc[0], tmpAcc[1], tmpAcc[2]);
                        String gyroValues = String.format("%f %f %f", tmpGyro[0], tmpGyro[1], tmpGyro[2]);
                        String magValues = String.format("%f %f %f", tmpMag[0], tmpMag[1], tmpMag[2]);
                        String sensorLine = String.format("%s %s %s %s\r\n", timeValue, accValues, gyroValues, magValues);
                        sensorValueLines.put(sensorLine);

                    } catch (FFmpegFrameRecorder.Exception | InterruptedException e) {
                        e.printStackTrace();
                    }
                    long endTime = System.currentTimeMillis();
                    long processTime = endTime - startTime;
                    mTotalProcessFrameTime += processTime;
                    Log.d(LOG_TAG, "This frame process time: " + processTime + "ms");
                    long totalAvg = mTotalProcessFrameTime / ++mFrameRecordedCount;
                    Log.d(LOG_TAG, "Avg frame process time: " + totalAvg + "ms");
                }
                Log.d(LOG_TAG, mFrameRecordedCount + " / " + mFrameToRecordCount);
                mRecycledFrameQueue.offer(recordedFrame);
            }
        }

        public void stopRunning() {
            super.stopRunning();
            if (getState() == WAITING) {
                interrupt();
            }
        }
    }

    abstract class ProgressDialogTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

        private int promptRes;
        private ProgressDialog mProgressDialog;

        public ProgressDialogTask(int promptRes) {
            this.promptRes = promptRes;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(FFmpegRecordActivity.this,
                    null, getString(promptRes), true);
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            super.onProgressUpdate(values);
//            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
        }
    }

    class FinishRecordingTask extends ProgressDialogTask<Void, Integer, Void> {

        public FinishRecordingTask() {
            super(R.string.processing);
        }

        @Override
        protected Void doInBackground(Void... params) {
            stopRecording();
            stopRecorder();
            releaseRecorder(false);

            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(mSensorValues, false));
                while (!sensorValueLines.isEmpty()) {
                    String sensorLine = sensorValueLines.poll();
                    printWriter.printf("%s", sensorLine);
                }
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//huh
            /*
            Intent intent = new Intent(FFmpegRecordActivity.this, PlaybackActivity.class);
            intent.putExtra(PlaybackActivity.INTENT_NAME_VIDEO_PATH, mVideo.getPath());
            startActivity(intent);
             */
        }
    }
}
