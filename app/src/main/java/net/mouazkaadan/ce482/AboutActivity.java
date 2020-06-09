package net.mouazkaadan.ce482;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    private LinearLayout mFbImg, mTwImg, mEmImg, mLink, mPlay;
    private TextView appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mFbImg = findViewById(R.id.mFb);
        mEmImg = findViewById(R.id.mEm);
        mTwImg = findViewById(R.id.mTw);
        mLink  = findViewById(R.id.mLink);
        mPlay  = findViewById(R.id.mPlay);
        appVersion = findViewById(R.id.app_version);

        appVersion.setText(BuildConfig.VERSION_NAME);

        //Mouaz about set links
        mFbImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://www.facebook.com/mouaz.kaadan"));
                    startActivity(i); } catch (Exception e){
                    Toast.makeText(getBaseContext(), "The link is broken", Toast.LENGTH_SHORT).show();                }
            }
        });

        mLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://www.linkedin.com/in/mouaz-kaadan-727911107/"));
                    startActivity(i); } catch (Exception e){
                    Toast.makeText(getBaseContext(), "The link is broken", Toast.LENGTH_SHORT).show();                }
            }
        });

        mEmImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("message/rfc822");
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"mouaz.kaadan@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Namaz Vakitleri App Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, " ......... ");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "Send mail..."));
                }
            }
        });

        mTwImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://twitter.com/mouazkaadan"));
                    startActivity(i); } catch (Exception e){
                    Toast.makeText(getBaseContext(), "The link is broken", Toast.LENGTH_SHORT).show();                }
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Mouaz+KAADAN"));
                    startActivity(i); } catch (Exception e){
                    Toast.makeText(getBaseContext(), "The link is broken", Toast.LENGTH_SHORT).show();                }
            }
        });


    }
}