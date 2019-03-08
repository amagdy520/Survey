package com.survey.UserProfileScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.survey.LoginAndSignUpScreen.LoginActivity;
import com.survey.MainScreen.StartScreen;
import com.survey.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfileActivity extends AppCompatActivity {

    @BindView(R.id.setting)
    ImageView mSetting;
    @BindView(R.id.logout)
    ImageView mLogout;
    @BindView(R.id.photo)
    CircleImageView mProfilePicture;
    @BindView(R.id.username)
    TextView mUsername;
    @BindView(R.id.address)
    TextView mAddress;
    @BindView(R.id.survey)
    TextView mSurvey;
    @BindView(R.id.PhoneNumber)
    TextView mPhoneNumber;
    @BindView(R.id.points)
    TextView mPoints;
    @BindView(R.id.recyclerSurveys)
    RecyclerView mRecycler;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private final int GALLERY_REQUEST=1;
    private Uri mImageUri = null;

    String ph;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase.keepSynced(true);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ph = mAuth.getCurrentUser().getEmail().substring(0,11);

        mDatabase.child("Users").child(ph).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                if(!name.equals("null")){
                    mUsername.setText(name);
                }
                String points = (String) dataSnapshot.child("points").getValue();
                if(!points.equals("null")){
                    mPoints.setText(points);
                }
                String address = (String) dataSnapshot.child("address").getValue();
                if(!address.equals("null")){
                    if(address.equals("eg")) {
                        mAddress.setText("Egypt");
                    }
                }
                String image = (String) dataSnapshot.child("profile").getValue();
                if(!image.equals("null")){
                    Picasso.with(UserProfileActivity.this).load(image).into(mProfilePicture);
                }
                mPhoneNumber.setText(ph);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });


        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(UserProfileActivity.this);
                builder1.setMessage("Are you sure you want to logout!?.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAuth.signOut();
                                Intent mAccount = new Intent(UserProfileActivity.this, LoginActivity.class);
                                startActivity(mAccount);
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK ) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mProfilePicture.setImageURI(mImageUri);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                            StorageReference filepath = mStorage.child("Users").child(ph).child(mImageUri.getLastPathSegment());
                            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    mDatabase.child("Users").child(ph).child("profile").setValue(downloadUrl.toString());
                                }
                            });
                        } catch (Exception e) {

                        }
                    }
                }).start();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
