package org.opencv.samples.facedetect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import static android.os.Environment.getExternalStorageDirectory;
import static org.opencv.samples.facedetect.FdActivity.copyFile;

public class Display extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener {
    public Context mContext;
    ImageView mImage;
    public Bitmap bitmap;

    int train_e=0,test_e=0,counter=0;
    private String train_data = "";
    double[] train_data_val = new double[25];
    double[] test_data = new double[25];
    private static String train_file= "imgdata";
    private static String open_file= "secret";


    String db_Path = getExternalStorageDirectory().getAbsolutePath() + "/opencv_data/";
    TextView touch_coordinates;
    TextView touch_color;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mContext = this;
        touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
        touch_color = (TextView) findViewById(R.id.touch_color);
        mImage= (ImageView)findViewById(R.id.imgview);
        Button lock = (Button) findViewById(R.id.lock_train);
        lock.setOnClickListener(this);
        lock.setEnabled(true);
        Button unlock = (Button) findViewById(R.id.unlock);
        unlock.setOnClickListener(this);
        unlock.setEnabled(true);
        mImage.setOnTouchListener(Display.this);
        File f= new File(db_Path+"rainbow.jpg");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mImage.setImageBitmap(bitmap);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lock_train:   train_e=1; clearFile();
                Arrays.fill(train_data_val,0.0); counter=0;  break;
            case R.id.unlock:       test_e=1;
                Arrays.fill(test_data,0.0); counter=0; break;
            default:break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        int pixel = bitmap.getPixel(x,y);
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        touch_coordinates.setText("X: " + Double.valueOf(x) + ", Y: " + Double.valueOf(y));
        touch_color.setText("R:"+redValue+" "+"G:"+greenValue+" "+"B:"+blueValue+" ");
//        Imgproc.rectangle(mRgba, touchedRect.tl(), touchedRect.br(), FACE_RECT_COLOR, 1);


        if(train_e==1){
            train_data+=String.valueOf(x)+";"+String.valueOf(y) + ";"+String.valueOf(redValue)+ ";"+String.valueOf(greenValue) +";"+String.valueOf(blueValue+";");
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
            test_data[counter]=redValue;counter++;
            test_data[counter]=greenValue;counter++;
            test_data[counter]=blueValue;counter++;

            if(counter>=25) {
                test_e = 0;
                counter = 0;
                try {
                    BufferedReader in = new BufferedReader(new FileReader(db_Path + train_file));
                    String str;
                    int i=0;
                    str = in.readLine();
                    String[] ar=str.split(";");
                    for(i=0;i<=24;i++){train_data_val[counter]=Double.parseDouble(ar[i]);counter++;}
                    in.close();
                    int j=0;
                    for(i=0;i<25;i++){
                        double diff=test_data[i]-train_data_val[i];
                        if(-100<=diff & diff<=100){ j=1; }
                        else { j=0;}
                        if(j==0){break;}
                    }
                    if(j==1){Toast.makeText(mContext, "Matching! Now you can view the secret info", Toast.LENGTH_SHORT).show();display_data();}
                    if(j==0){Toast.makeText(mContext, "not matching", Toast.LENGTH_SHORT).show();}
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }



        return false;
    }

    private void clearFile() {
        File myFile = new File(db_Path + train_file);
        if (myFile.delete()) {
            Toast.makeText(mContext, "Files Deleted", Toast.LENGTH_LONG).show();
        }
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

    public void display_data() {

        File destFile = new File(db_Path + open_file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(destFile);
        intent.setDataAndType(uri, "text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

}
