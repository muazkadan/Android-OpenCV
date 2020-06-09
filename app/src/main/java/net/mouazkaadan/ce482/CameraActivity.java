package net.mouazkaadan.ce482;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.io.ByteArrayOutputStream;
import java.util.logging.Filter;

public class CameraActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2{

    private JavaCameraView javaCameraView;
    private Mat mat1, mat2, mat3;
    private ImageButton captureButton, frontCameraButton;
    private float x1, x2;
    boolean isGray = false, isBackCamera = true;
    private int filterCode = 0;
    private String[] filtersArray = {"None", "Gray", "Canny", "Summer", "Pink", "Invert", "Face Detection"};
    private int currentOrientation;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("", "OpenCV loaded successfully");
                    javaCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        OpenCVLoader.initDebug();

        OrientationEventListener myOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            public void onOrientationChanged(int arg0) {
                if (arg0>=315 || arg0<45){
                    currentOrientation = Surface.ROTATION_90;
                }else if (arg0>=45 && arg0<135){
                    currentOrientation = Surface.ROTATION_180;
                }else if (arg0>=135 && arg0<225){
                    currentOrientation = Surface.ROTATION_270;
                }else if (arg0>=225 && arg0<315){
                    currentOrientation = Surface.ROTATION_0;
                }
            }
        };
        myOrientationEventListener.enable();

        javaCameraView = findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        if (x1 > x2) {
                            if(filterCode != 0)
                                filterCode--;
                            Toast.makeText(CameraActivity.this, filtersArray[filterCode], Toast.LENGTH_SHORT).show();
                            javaCameraView.setCvCameraViewListener(CameraActivity.this);
                        } else if (x2 > x1) {
                            isGray = false;
                            if(filterCode != filtersArray.length -1)
                                filterCode++;
                            Toast.makeText(CameraActivity.this, filtersArray[filterCode], Toast.LENGTH_SHORT).show();
                            javaCameraView.setCvCameraViewListener(CameraActivity.this);
                        }
                        return true;
                }
                return false;
            }
        });

        captureButton = findViewById(R.id.capture_button);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, EditActivity.class);

                if (currentOrientation == Surface.ROTATION_90) {
                    Core.flip(mat1.t(), mat1, 1);
                } else if(currentOrientation == Surface.ROTATION_180){
                    Core.flip(mat1, mat1, -1);
                }else if(currentOrientation == Surface.ROTATION_270){
                    Core.flip(mat1.t(), mat1, 0);
                }

                Bitmap bitmap = Bitmap.createBitmap(mat1.width(), mat1.height(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(mat1, bitmap);
                intent.putExtra("imageUri", getImageUri(CameraActivity.this, bitmap) + "");
                startActivity(intent);
            }
        });

        frontCameraButton = findViewById(R.id.front_camera);
        frontCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBackCamera) {
                    javaCameraView.setCameraIndex(1);
                    javaCameraView.disableView();
                    javaCameraView.enableView();
                    isBackCamera = false;
                }else {
                    javaCameraView.setCameraIndex(0);
                    javaCameraView.disableView();
                    javaCameraView.enableView();
                    isBackCamera = true;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug())
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        else
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width, height, CvType.CV_8UC4);
        mat2 = new Mat(width, height, CvType.CV_8UC3);
        mat3 = new Mat(width, height, CvType.CV_8UC3);
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();
        mat2.release();
        mat3.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(filterCode == 0) {
            mat1 = inputFrame.rgba();
            return mat1;
        }else if(filterCode == 1) {
            mat1 = grayCamera(inputFrame.rgba());
            return mat1;
        }else if(filterCode == 2) {
            mat1 = cannyCamera(inputFrame.rgba());
            return mat1;
        }else if(filterCode == 3) {
            Imgproc.cvtColor(inputFrame.rgba(), mat1, Imgproc.COLOR_RGBA2RGB);
            mat2 = summerCamera(mat1);
            return mat2;
        }else if(filterCode == 4){
            Imgproc.cvtColor(inputFrame.rgba(), mat1, Imgproc.COLOR_RGBA2RGB);
            mat2 = pinkCamera(mat1);
            return mat2;
        }else if(filterCode == 5){
            Core.bitwise_not( inputFrame.rgba(), mat1 );
            return mat1;
        }else if(filterCode == 6){
            mat1 = inputFrame.rgba();
            Bitmap myBitmap = Bitmap.createBitmap(mat1.cols(), mat1.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat1, myBitmap);

                Paint myRectPaint = new Paint();
                myRectPaint.setStrokeWidth(5);
                myRectPaint.setColor(Color.RED);
                myRectPaint.setStyle(Paint.Style.STROKE);

                Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
                Canvas tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                FaceDetector faceDetector = new
                        FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                        .build();
                Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Face> faces = faceDetector.detect(frame);

                for(int i=0; i<faces.size(); i++) {
                    Face thisFace = faces.valueAt(i);
                    float x1 = thisFace.getPosition().x;
                    float y1 = thisFace.getPosition().y;
                    float x2 = x1 + thisFace.getWidth();
                    float y2 = y1 + thisFace.getHeight();
                    tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
                }
            Utils.bitmapToMat(tempBitmap, mat2);
            return mat2;
        }

        return inputFrame.rgba();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Mat grayCamera(Mat cameraMat){
        Mat grayMat = new Mat();
        Imgproc.cvtColor(cameraMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        return grayMat;
    }

    private Mat cannyCamera(Mat cameraMat){
        Mat cannyMat = new Mat();
        Imgproc.Canny(cameraMat, cannyMat, 50, 150);
        return cannyMat;
    }

    private Mat summerCamera(Mat cameraMat){
        Mat summerMat = new Mat();
        Imgproc.applyColorMap(cameraMat, summerMat, Imgproc.COLORMAP_SUMMER);
        return summerMat;
    }

    private Mat pinkCamera(Mat cameraMat){
        Mat pinkMat = new Mat();
        Imgproc.applyColorMap(cameraMat, pinkMat, Imgproc.COLORMAP_PINK);
        return pinkMat;
    }
}
