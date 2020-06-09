package net.mouazkaadan.ce482;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    private Button camera, gallery, about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camera_button);
        gallery = findViewById(R.id.gallery_button);
        about = findViewById(R.id.about_button);

        File dir = new File(Environment.getExternalStorageDirectory(), "CE482");
        if(!dir.exists()) {
            dir.mkdirs();
        }

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            try {
                Uri imageUri = data.getData();
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("imageUri", imageUri + "");
//                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//                intent.putExtra("imageBitmap", byteArray);
                startActivity(intent);

            } catch (NullPointerException ex){
                Toast.makeText(this, "you didn't choose a picture", Toast.LENGTH_LONG).show();
                Log.v("Error", ex.getMessage() + "  ");
//                } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.v("Error", e.getMessage() + "  ");

            }

        }
    }
}
