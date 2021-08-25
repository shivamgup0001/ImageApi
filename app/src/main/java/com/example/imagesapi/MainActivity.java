package com.example.imagesapi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String absolutePathOfImage = null;
    ImageView imageView;
    Button button;
    private int k=0;
    boolean check=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        button=findViewById(R.id.button);
        loadImage();

                if(!isConnected(MainActivity.this)) {
                    showCustomDialog();
                }

        if(check==false)
        Picasso.get().load("https://random.imagecdn.app/500/150").into(imageView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable=(BitmapDrawable)imageView.getDrawable();
                Bitmap bitmap=bitmapDrawable.getBitmap();
                saveToGallery(bitmap);
            }
        });
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please Connect to the internet to proceed further")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        check=true;
                        imageView.setImageBitmap(BitmapFactory.decodeFile(absolutePathOfImage));

                    }
                }).show();
    }

    private boolean isConnected(MainActivity mainActivity) {
        ConnectivityManager connectivityManager= (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);


        NetworkInfo wifiConn=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn!=null&&wifiConn.isConnected())||(mobileConn!=null&&mobileConn.isConnected())){

            return true;
        }
        else {

            return false;

        }
    }

    private void loadImage() {
        Uri uri;
        Cursor cursor;
        int column_index_data;

        uri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String [] projection={MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String orderBy= MediaStore.Images.Media.DATE_TAKEN;
        cursor=getApplicationContext().getContentResolver().query(uri,projection,
                null,null,orderBy+" ASC");
        column_index_data=cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while(cursor.moveToLast()) {

            if(k==0) {
                absolutePathOfImage = cursor.getString(column_index_data);

                break;
            }

        }

    }

    private void saveToGallery(Bitmap bitmap) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Images_" + timeStamp + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "TestFolder");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);
                Toast.makeText(MainActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Image not saved" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}