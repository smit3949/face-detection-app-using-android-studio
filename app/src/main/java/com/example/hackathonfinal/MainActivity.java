package com.example.hackathonfinal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hackathonfinal.Helper.GraphicOverlay;
import com.example.hackathonfinal.Helper.RactOverlay;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    ImageView imageview;
    Button camera;
    Button done;
    TextView textv,lt1,lt2;
    int count=0;
    public static final int req=9999;
    FusedLocationProviderClient client;
    // private Object MediaStore;
    double lt;
    double lg;
    GraphicOverlay graphicOverlay;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageview=findViewById(R.id.image);
        camera = findViewById(R.id.camera);
        done = findViewById(R.id.done);
        textv=findViewById(R.id.textView);
        lt1 = findViewById(R.id.lt1);
        lt2 = findViewById(R.id.lg1);
        client = LocationServices.getFusedLocationProviderClient(this);
        graphicOverlay=findViewById(R.id.graphic_overlay);





    }

    public void camerab(View view)
    {
        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            getLocation();
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,req);

    }

    private void getLocation() {
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if(location!=null)
                {

                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        lt=addresses.get(0).getLatitude();
                        lg= addresses.get(0).getLongitude();
                        lt1.setText(Html.fromHtml(
                                "<b>Lat:</b><br>"
                                +addresses.get(0).getLatitude()
                        ));
                        lt2.setText(Html.fromHtml(
                                "<b>Log:</b><br>"
                                        +addresses.get(0).getLongitude()
                        ));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(req==requestCode)
        {
            assert data != null;
            Bitmap finalmg = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            imageview.setImageBitmap(finalmg);


            assert finalmg != null;
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(finalmg);

            FirebaseVisionFaceDetectorOptions options =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .build();
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);



                    detector.detectInImage(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onSuccess(List<FirebaseVisionFace> faces) {
                                            // Task completed successfully
                                            // ...

                                            // FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                            int count=0;
                                            int flag=0;
                                            for(FirebaseVisionFace face : faces)
                                            {
                                                Rect bounds = face.getBoundingBox();
                                                RactOverlay rect = new RactOverlay(graphicOverlay,bounds);
                                                graphicOverlay.add(rect);
                                                flag=1;

                                            }
                                            if(flag==1)
                                            {
                                                Toast.makeText(MainActivity.this,String.format("face detected",count),Toast.LENGTH_SHORT).show();
                                                textv.setText("FACEDETECTED");
                                            }
                                            else
                                            {
                                                textv.setText("NOFACEDETECTED");
                                                Toast.makeText(MainActivity.this,String.format("no face detected plz try again",count),Toast.LENGTH_SHORT).show();
                                            }



                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {

                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...

                                        }
                                    });




        }
    }
    public void doneb(View view)
    {
        if(textv.getText()=="FACEDETECTED" && lt-23.0014248<0.001 && lg-72.6323551<0.001)
        {

            FirebaseDatabase.getInstance().getReference().child("SMIT").setValue("OK");
            startActivity(new Intent(MainActivity.this,NewActivity.class));
        }
        else
        {
            FirebaseDatabase.getInstance().getReference().child("SMIT").setValue("NOT OK");
        }
    }
}
