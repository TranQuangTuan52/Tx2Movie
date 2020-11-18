package com.example.tx2movie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadThumbnail extends AppCompatActivity implements View.OnClickListener {
    private Uri videoThumbUri;
    private String thumbUrl;
    private ImageView imgvThumb;
    private StorageReference storageRefThumb;
    private DatabaseReference databaseRef;
    private TextView tvSelected;
    private RadioButton rbNoType, rbLatest, rbPopular, rbSlide;
    private StorageTask storageTask;

    private Button btnUploadThumb, btnUpload;

    private DatabaseReference updateRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_thumbnail);

        init();
    }

    private void init() {
        imgvThumb =  findViewById(R.id.imgThumbnail);
        tvSelected = findViewById(R.id.tvThumbnailSelected);
        rbNoType = findViewById(R.id.rbNoType);
        rbLatest = findViewById(R.id.rbLatesMovie);
        rbPopular = findViewById(R.id.rbBestPopularMovie);
        rbSlide = findViewById(R.id.rbSlideMovie);
        btnUpload = findViewById(R.id.btnUpload);
        btnUploadThumb = findViewById(R.id.uploadThumbnail);
        //
        rbLatest.setOnClickListener(this);
        rbSlide.setOnClickListener(this);
        rbPopular.setOnClickListener(this);
        rbNoType.setOnClickListener(this);
        btnUploadThumb.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

        storageRefThumb = FirebaseStorage.getInstance().getReference().child("VideoThumbnails");
        databaseRef = FirebaseDatabase.getInstance().getReference().child("videos");

        String currentUid = getIntent().getExtras().getString("currentuid");
        updateRef = FirebaseDatabase.getInstance().getReference("videos").child(currentUid);



    }

    @Override
    public void onClick(View v) {
        String type = "";
        switch (v.getId())
        {
            case R.id.rbBestPopularMovie:
                    type = rbPopular.getText().toString();
                    updateRef.child("video_type").setValue(type);
                    updateRef.child("video_slide").setValue("");
                break;
            case R.id.rbLatesMovie:
                     type = rbLatest.getText().toString();
                    updateRef.child("video_type").setValue(type);
                    updateRef.child("video_slide").setValue("");
                break;
            case R.id.rbNoType:
                    type = rbNoType.getText().toString();
                    updateRef.child("video_type").setValue("");
                    updateRef.child("video_slide").setValue("");
                break;
            case R.id.rbSlideMovie:
                    type = rbSlide.getText().toString();
                    updateRef.child("video_type").setValue(type);
                    updateRef.child("video_slide").setValue("");
                break;
            case R.id.uploadThumbnail:
                showImage();
                break;
            case R.id.btnUpload:
                uploadFileToFireBase();
                break;
        }
    }

    private void uploadFileToFireBase() {
            if(tvSelected.getText().equals("No Thumbnail selected"))
            {
                Toast.makeText(this, "pls select image", Toast.LENGTH_SHORT).show();
            }else{
                if(storageTask != null && storageTask.isInProgress())
                    Toast.makeText(this, "Upload files is allready in progress", Toast.LENGTH_SHORT).show();
                else
                    uploadFile();
            }
    }

    public void showImage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 102 && resultCode == RESULT_OK && data.getData() != null){
            videoThumbUri = data.getData();
            try {
                String thumbName = getFileName(videoThumbUri);
                tvSelected.setText(thumbName);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), videoThumbUri);
                imgvThumb.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileName(Uri uri){
        String result = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if(result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut !=-1)
                result = result.substring(cut + 1);
        }
        return result;

    }
    private void uploadFile(){
        if(videoThumbUri != null){
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("wait for upload thumbnail...");
            progressDialog.show();
            String video_title = getIntent().getExtras().getString("video_title");
            Log.d("name", video_title + "." + getFileNameExtension(videoThumbUri));
            StorageReference storageRef  = storageRefThumb.child(video_title + "." + getFileNameExtension(videoThumbUri));
            storageRef.putFile(videoThumbUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            thumbUrl = uri.toString();
                            updateRef.child("video_thumb").setValue(thumbUrl);
                            progressDialog.dismiss();
                            Toast.makeText(UploadThumbnail.this, "upppppppppp", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadThumbnail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressDialog.setMessage("Upload "+ ((int)progress)+"...");
                }
            });
        }
    }

    private String getFileNameExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

}