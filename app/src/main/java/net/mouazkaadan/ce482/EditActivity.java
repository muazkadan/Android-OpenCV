package net.mouazkaadan.ce482;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static org.opencv.core.CvType.CV_32F;

public class EditActivity extends AppCompatActivity {

    private ImageView photo;
    private Spinner filtersSpinner;
    private Bitmap imageBitmap, editedBitmap, backUpBitmap, sunGlass;
    private String filtersList[]   = {"Gray Filter", "Canny Filter", "Detect Faces", "Şifrele"};
    private ArrayAdapter<String> filtersListAdapter;
    private String fileName;
    private int filterIndex;
    Paint rectPaint = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        OpenCVLoader.initDebug();

        Bundle extras = getIntent().getExtras();
        Uri imageUri = Uri.parse(extras.getString("imageUri"));
        byte[] byteArray = getIntent().getByteArrayExtra("imageBitmap");
//        imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//        backUpBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        photo = findViewById(R.id.photo);
        filtersSpinner = findViewById(R.id.filters_spinner);
        filtersListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filtersList);
        filtersSpinner.setAdapter(filtersListAdapter);
        filtersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {            }
        });

        try{
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            backUpBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

        }catch (IOException e){
            e.printStackTrace();
        }

        photo.setImageBitmap(imageBitmap);

        Button button = findViewById(R.id.apply_effect_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filterIndex == 0){
                    convertToGray();
                }else if(filterIndex == 1){
                    convertToCanny();
                }else if (filterIndex == 2){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable=true;
                    Bitmap myBitmap = ((BitmapDrawable)photo.getDrawable()).getBitmap();

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
                    if(!faceDetector.isOperational()){
                        new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
                        return;
                    }

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
                    photo.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
                }else if(filterIndex == 3){
                        cyrpt();
                }
            }
        });
    }

    public void cyrpt(){
        Mat rgba = new Mat();
        Mat ciphered = new Mat();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = 4;

        BitmapDrawable drawable = (BitmapDrawable) photo.getDrawable();

        editedBitmap = Bitmap.createBitmap(drawable.getBitmap().getWidth(), drawable.getBitmap().getHeight(), Bitmap.Config.RGB_565);

        rgba.convertTo(rgba, CV_32F);
        int rows = (int) rgba.size().height;
        int cols = (int) rgba.size().width;
        double[][] table = new double[rows][cols];

        for(int i = 0 ;i < rows; ++i)
            for(int j = 0; j < cols; ++j)
                table[i][j]= rgba.get(i,j)[0];

        int c = 0;

//        int a = 0;
//        String ci = "";
//        for (int b = 0; b < table[a].length; b++)
//            for (a = table.length; a > 0; a--) {
//                ci += table[a - 1][b];
//            }
//
//        System.out.println(ci);

        Log.v("So", Arrays.deepToString(table)
                .replace("],", "\n").replace(",", "\t| ")
                .replaceAll("[\\[\\]]", " "));

        Utils.bitmapToMat(drawable.getBitmap(), rgba);

        Imgproc.cvtColor(rgba, ciphered, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(ciphered, editedBitmap);

        photo.setImageBitmap(editedBitmap);
    }

    public void convertToCanny(){
        Mat rgba = new Mat();
        Mat cannyMat = new Mat();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = 4;

        BitmapDrawable drawable = (BitmapDrawable) photo.getDrawable();



        editedBitmap = Bitmap.createBitmap(drawable.getBitmap().getWidth(), drawable.getBitmap().getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(drawable.getBitmap(), rgba);

        Imgproc.Canny(rgba, cannyMat, 50, 150);

        Utils.matToBitmap(cannyMat, editedBitmap);

        photo.setImageBitmap(editedBitmap);
    }

    public void convertToGray(){
        Mat rgba = new Mat();
        Mat grayMat = new Mat();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inSampleSize = 4;

        BitmapDrawable drawable = (BitmapDrawable) photo.getDrawable();

        editedBitmap = Bitmap.createBitmap(drawable.getBitmap().getWidth(), drawable.getBitmap().getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(drawable.getBitmap(), rgba);

        Imgproc.cvtColor(rgba, grayMat, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(grayMat, editedBitmap);

        photo.setImageBitmap(editedBitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_save:
                setFileNameAndSave();
                break;

            case R.id.action_undo:
                photo.setImageBitmap(backUpBitmap);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFileNameAndSave() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("File Name ");

        // Setting Dialog Message
        alertDialog.setMessage("Set a name to the saved file : ");
        final EditText input = new EditText(EditActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        // Write your code here to execute after dialog
                        try {
                            fileName = input.getText().toString();
                            String extStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CE482";
                            OutputStream outStream = null;
                            File file = new File(extStorageDirectory, fileName + ".PNG");
                                outStream = new FileOutputStream(file);
                                photo.invalidate();
                                BitmapDrawable drawable = (BitmapDrawable) photo.getDrawable();
                                drawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, outStream);
                                outStream.flush();
                                outStream.close();
                                Toast.makeText(EditActivity.this, "Photo Saved Seccesfully", Toast.LENGTH_LONG).show();
                            } catch(Exception e) {
                                Toast.makeText(EditActivity.this, "Failed to Save The Photo", Toast.LENGTH_LONG).show();
                            }
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }
}
