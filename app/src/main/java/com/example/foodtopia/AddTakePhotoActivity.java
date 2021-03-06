package com.example.foodtopia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foodtopia.add.Upload;
import com.example.foodtopia.databinding.ActivityAddTakePhotoBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class AddTakePhotoActivity extends AppCompatActivity {

    ActivityAddTakePhotoBinding binding;

    public static final String TAG = MainActivity.class.getSimpleName()+"My";

    Uri uri ;
    String imgURL;
    String mealtime;
    ImageView imageView;
    ProgressDialog progressDialog;
    StorageReference storageRef;
    private DatabaseReference mDatabase;

    private String mPath = "";//??????????????????????????????
    public static final int CAMERA_PERMISSION = 100;//?????????????????????
    public static final int REQUEST_HIGH_IMAGE = 101;//??????????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTakePhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        imageView = findViewById(R.id.cameraImageView);

        Intent intent=getIntent();
        mealtime = intent.getStringExtra("choice");

        /*??????????????????*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
        }

        /*??????????????????*/
        binding.buttonHigh.setOnClickListener(v->{
            Intent highIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //???????????????????????????
            if (highIntent.resolveActivity(getPackageManager()) == null) return;
            //?????????????????????URI???????????????????????????
            File imageFile = getImageFile();
            if (imageFile == null) return;
            //?????????????????????URI??????
            uri = FileProvider.getUriForFile(
                    this,
                    "com.example.foodtopia.CameraEx",//????????????AndroidManifest.xml??????authorities ??????
                    imageFile
            );
            binding.cameraImageView.setImageURI(uri);
            highIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
            startActivityForResult(highIntent,REQUEST_HIGH_IMAGE);//????????????
        });

        /*??????????????????*/
        binding.cameraPhotoUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
//                Toast.makeText(TakePhoto.this,"????????????",Toast.LENGTH_SHORT).show();
            }
        });
        /*??????????????????*/
        binding.cameraBackFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTakePhotoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void uploadImage() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("?????????...");
        progressDialog.show();

        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.TAIWAN);
        String date = formatter.format(now);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.TAIWAN);
        String time = timeFormatter.format(now);

        storageRef = FirebaseStorage.getInstance().getReference("meals");
        final StorageReference fileReference = storageRef.child(time
                + "." + getFileExtension(uri));
        UploadTask uploadTask = fileReference.putFile(uri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    imgURL = downloadUri.toString();

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference("uploads");
                    //new node
                    String uploadID = mDatabase.push().getKey();
                    Upload photo = new Upload(uid, date, mealtime, imgURL);

                    mDatabase.child(uploadID).setValue(photo);

                    if (progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }

                } else {
                    Toast.makeText(AddTakePhotoActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddTakePhotoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //???????????????
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /**?????????????????????URI???????????????????????????*/
    private File getImageFile() {
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time+"_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            //?????????????????????????????????
            File imageFile = File.createTempFile(fileName,".jpg",dir);
            //???????????????????????????????????????????????????????????????
            mPath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    /*??????????????????*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*???????????????????????????????????????requestCode?????????????????????resultCode???-1??????????????????0????????????????????????*/
        Log.d(TAG, "onActivityResult: requestCode: "+requestCode+", resultCode "+resultCode);

        /*?????????????????????????????????*/
        if (requestCode == REQUEST_HIGH_IMAGE && resultCode == -1){
            ImageView imageHigh = binding.cameraImageView;
            new Thread(()->{
                //???BitmapFactory????????????URI???????????????????????????????????????AtomicReference<Bitmap>???????????????????????????
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
//                matrix.setRotate(90f);//???90???
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    //???Glide????????????(?????????????????????????????????????????????LAG????????????????????????Thread?????????)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(imageHigh);
                });
            }).start();
        }
        else{
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
        }

    }
}