package com.example.tx2movie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tx2movie.model.VideoUploadDetail;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Uri videoUri;
    TextView tvVideoSelected;
    String videoCategory, videoTitle, currenuid;
    StorageReference storageRef;
    StorageTask uploadTask;
    DatabaseReference databaseRef;
    EditText edtVideoDescription;
    Spinner spinner;
    Button btnUpload ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        btnUpload = findViewById(R.id.btnUploadMovie);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFileToFireBase(v);
            }
        });
    }

    private void initView() {
        tvVideoSelected = findViewById(R.id.tvVideoSelected);
        edtVideoDescription = findViewById(R.id.edtDesMovie);
        spinner = findViewById(R.id.spinnerCategory);

        databaseRef = FirebaseDatabase.getInstance().getReference().child("videos");
        storageRef = FirebaseStorage.getInstance().getReference().child("videos");

        List<String> categories = new ArrayList<>();
        categories.add("Action");
        categories.add("Romantic");
        categories.add("Adventure");
        categories.add("Sports");
        categories.add("Comedy");
        categories.add("Cartoon ");

        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, categories);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                videoCategory = spinnerAdapter.getItem(position).toString();
                Toast.makeText(MainActivity.this, videoCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void openVideoFiles(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null){
            Toast.makeText(this, "yeah", Toast.LENGTH_SHORT).show();
            videoUri = data.getData();

            String path = null;
            Cursor cursor;
            int colum_index_data;
            String [] projection = {MediaStore.MediaColumns.DATA,MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails.DATA};
            final String orderby = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
            cursor = MainActivity.this.getContentResolver().query(videoUri, projection, null, null, orderby);
            //colum_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

            while(cursor.moveToNext()){
                //path = cursor.getString();
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                Log.d("title", "path: "+ path);
                videoTitle = FilenameUtils.getBaseName(path);
            }
            tvVideoSelected.setText(videoTitle);
            Log.d("title", "title: "+ videoTitle);
        }
    }
    public void uploadFileToFireBase(View view){
        if(tvVideoSelected.getText().equals("No video selected")){
            Toast.makeText(this, "pls select a video, ok? You idiot!!!", Toast.LENGTH_SHORT).show();
        }
        else{
            if(uploadTask != null && uploadTask.isInProgress())
            {
                Toast.makeText(this, "video upload is all ready in pogress", Toast.LENGTH_SHORT).show();
            }
            else{
                uploadFile();
            }
        }
    }

    private void uploadFile() {
        if(videoUri != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("video is uploading....");
            progressDialog.show();
            final StorageReference storageReference = storageRef.child(videoTitle);
            uploadTask = storageReference.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String video_url = uri.toString();
                            VideoUploadDetail videoUploadDetail = new VideoUploadDetail("",
                                    "", "", video_url, videoTitle,
                                    edtVideoDescription.getText().toString(), videoCategory);
                            String uploadsid = databaseRef.push().getKey();
                            databaseRef.child(uploadsid).setValue(videoUploadDetail);
                            currenuid = uploadsid;
                            progressDialog.dismiss();
                            if(currenuid.equals(uploadsid))
                                startThumbnailActivity();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressDialog.setMessage("uploaded " + (int)progress + "%...");
                }
            });
        }else{
            Toast.makeText(this, "No Video selected upload ", Toast.LENGTH_SHORT).show();
        }
    }

    public void startThumbnailActivity(){
        Intent intent = new Intent(this, UploadThumbnail.class);
        intent.putExtra("currentuid", currenuid);
        intent.putExtra("video_title", videoTitle);
        startActivity(intent);
        Toast.makeText(this, "video upload  ís successfully", Toast.LENGTH_SHORT).show();



    }
}