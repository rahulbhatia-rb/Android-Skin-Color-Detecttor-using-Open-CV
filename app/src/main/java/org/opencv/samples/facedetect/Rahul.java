package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

import static android.os.Environment.getExternalStorageDirectory;

public class Rahul extends Activity implements OnTouchListener, CvCameraViewListener2,View.OnClickListener {

    private static final String    TAG                 = "OCVSample::Activity";
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private Scalar FACE_RECT_COLOR  = new Scalar(255, 255, 255, 255);
    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    double x = -1;
    double y = -1;
    TextView touch_coordinates;
    TextView touch_color;

    //code added by me
    String db_Path = getExternalStorageDirectory().getAbsolutePath() + "/opencv_data/";
    public Context mContext;
    int train_e=0,test_e=0,counter=0;
    private String train_data = "";
    //private String test_data = "";
    double[] train_data_val = new double[10];
    double[] test_data = new double[10];
    private static String train_file= "opencvd";
    ImageView  mImage;
    public static int cols=0;
    public static int rows=0;

    //till here

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Rahul.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Rahul() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        setContentView(R.layout.activity_bharath);
        touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
        touch_color = (TextView) findViewById(R.id.touch_color);

        /*Button lock = (Button) findViewById(R.id.lock_train);
        lock.setOnClickListener(this);
        lock.setEnabled(true);
        Button unlock = (Button) findViewById(R.id.unlock);
        unlock.setOnClickListener(this);
        unlock.setEnabled(true);
        Button img = (Button) findViewById(R.id.image_s);
        img.setOnClickListener(this);
        img.setEnabled(true);
*/
        File folder = new File(db_Path);
        boolean success = false;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (!success) {
            Log.d(TAG, "Folder not created.");
        } else {
            Log.d(TAG, "Folder created!");
        }
        LoadData(mContext);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setMaxFrameSize(640,480);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
  /*          case R.id.lock_train:   train_e=1; clearFile();   break;
            case R.id.unlock:       test_e=1;   break;
            case R.id.image_s:      bring_image(); break;
    */        default:break;
        }
    }


    public void bring_image(){
        Intent intent = new Intent(this, Display.class);
        startActivity(intent);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        cols = mRgba.cols();
        rows = mRgba.rows();
        x = event.getX();
        y = event.getY();
        double yLow = (double)mOpenCvCameraView.getHeight() * 0.2401961;
        double yHigh = (double)mOpenCvCameraView.getHeight() * 0.7696078;
        double xScale = (double)cols / (double)mOpenCvCameraView.getWidth();
        double yScale = (double)rows / (yHigh - yLow);
        y = y - yLow;
        x = x * xScale;
        y = y * yScale;
        if((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
        touch_coordinates.setText("X: " + Double.valueOf(x) + ", Y: " + Double.valueOf(y));

      /*  //code added by me
        if(train_e==1){
            train_data+=String.valueOf(x)+";"+String.valueOf(y) + ";";
            counter++;
            if(counter>=5){
                train_e=0;
                counter=0;
                writeData(train_data);
                File destFile = new File(db_Path + train_file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(destFile);
                intent.setDataAndType(uri, "text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }

        if(test_e==1){
            test_data[counter]=x;counter++;
            test_data[counter]=y;counter++;
            if(counter>=9) {
                test_e = 0;
                counter = 0;
                try {
                    BufferedReader in = new BufferedReader(new FileReader(db_Path + train_file));
                    String str;
                    int i=0;
                    str = in.readLine();
                    String[] ar=str.split(";");
                    for(i=0;i<=9;i++){train_data_val[counter]=Double.parseDouble(ar[i]);counter++;}
                    in.close();
                    int j=0;
                    for(i=0;i<10;i++){
                        double diff=test_data[i]-train_data_val[i];
                        if(-150<=diff & diff<=150){ j=1; }
                        else { j=0;}
                        if(j==0){break;}
                    }
                    if(j==1){Toast.makeText(mContext, "matching", Toast.LENGTH_SHORT).show();}
                    if(j==0){Toast.makeText(mContext, "not matching", Toast.LENGTH_SHORT).show();}
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

*/
        //till here
        Rect touchedRect = new Rect();

        touchedRect.x = (int)x;
        touchedRect.y = (int)y;

        touchedRect.width = 8;
        touchedRect.height = 8;

        Mat touchRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        touch_color.setText("Color: #" + String.format("%02X", (int)mBlobColorRgba.val[0])
                + String.format("%02X", (int)mBlobColorRgba.val[1])
                + String.format("%02X", (int)mBlobColorRgba.val[2]));

        touch_color.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                (int) mBlobColorRgba.val[1],

                (int) mBlobColorRgba.val[2]));

        FACE_RECT_COLOR  = new Scalar((int) mBlobColorRgba.val[0], (int) mBlobColorRgba.val[1], (int) mBlobColorRgba.val[2], 255);

//        Log.d("Color Values", String.valueOf((int) mBlobColorRgba.val[0]));

        touch_color.setBackgroundColor(Color.rgb(255 - (int) mBlobColorRgba.val[0],
                255 - (int) mBlobColorRgba.val[1],
                255 - (int) mBlobColorRgba.val[2]));



        return false;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);

    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
     //   Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2BGR);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }
        Mat mask1;
        Rect[] facesArray = faces.toArray();
       for (int i = 0; i < facesArray.length; i++) {

           mask1= mRgba.submat(facesArray[i]);
           //Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, -1);
           Imgproc.cvtColor(mask1,mask1,Imgproc.COLOR_RGBA2BGRA);
           /*Imgproc.cvtColor(mask1,mask1,Imgproc.COLOR_GRAY2RGB);
           int row_s=Math.round(facesArray[i].x-facesArray[i].height/2-facesArray[i].width/2);
           int row_e=Math.round(facesArray[i].x+facesArray[i].height/2-facesArray[i].width/2);
           int col_s=Math.round(facesArray[i].x-facesArray[i].height/2+facesArray[i].width/2);
           int col_e=Math.round(facesArray[i].x+facesArray[i].height/2+facesArray[i].width/2);*/
           // int ht=facesArray[0,0];
           //Imgproc.cvtColor(mRgba,mRgba,Imgproc.COLOR_RGB2GRAY);
           //Imgproc.cvtColor(mRgba,mRgba,Imgproc.COLOR_GRAY2RGB);
           //mask1.copyTo(mRgba.rowRange(row_s, row_e).colRange(col_s, col_e));
            }

        // mask = mRgba.submat(facesArray[0]);
        //Imgproc.cvtColor(mask,mask,Imgproc.COLOR_BGR2GRAY);
        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }




    //code added by me
    private void LoadData(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = assetManager.open(filename);
                File outFile = new File(db_Path, filename);
                out = new FileOutputStream(outFile);
                copyFile2(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;

            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    //    }
    private void copyFile2(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    /*TO copy the db into a folder programatically without root access*/
    public static void copyFile(File in, File out)
            throws IOException {
        FileChannel inChannel = new
                FileInputStream(in).getChannel();
        FileChannel outChannel = new
                FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(),
                    outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
        }
    }

    private void clearFile() {
        File myFile = new File(db_Path + train_file);
        if (myFile.delete()) {
            Toast.makeText(mContext, "Files Deleted", Toast.LENGTH_LONG).show();
        }
    }

    public void display_data() {

        File destFile = new File(db_Path + train_file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(destFile);
        intent.setDataAndType(uri, "text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    public void writeData(String data) {
        writeToFile(data, mContext);
        this.train_data = "";
        //  display_data();
        Toast.makeText(mContext, "Saved", Toast.LENGTH_SHORT).show();
    }
    private void writeToFile(String data, Context context) {
        File myFile = new File(mContext.getFilesDir().toString() + "/" + train_file);
        File destFile = new File(db_Path + train_file);

        try {
            String separator = System.getProperty("line.separator");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(train_file, Context.MODE_ENABLE_WRITE_AHEAD_LOGGING));
            outputStreamWriter.write(data);
            outputStreamWriter.write(separator);
            outputStreamWriter.close();
            copyFile(myFile, destFile);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
