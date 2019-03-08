package com.survey.MainScreen;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.survey.AddNewSurvey.AddSurveyActivity;
import com.survey.AddNewSurvey.CreateSurvey;
import com.survey.AddNewSurvey.QuestionAdapter;
import com.survey.AnswerSurvey.AnswerSurvey;
import com.survey.LoginAndSignUpScreen.LoginActivity;
import com.survey.R;
import com.survey.UserProfileScreen.UserProfileActivity;
import com.xw.repo.BubbleSeekBar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartScreen extends AppCompatActivity {

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    @BindView(R.id.profile)
    CardView mProfile;

    @BindView(R.id.addNew)
    CardView mAddNew;

    @BindView(R.id.imageProfile)
    ImageView mImageProfile;

    private DatabaseReference mDatabase, mDataShow;
    private FirebaseAuth mAuth;
    String mCountry;
    String name,key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        switch(getUserCountry(this)){
            case "eg":
                mCountry = "Egypt";
                break;
        }
        mDataShow = FirebaseDatabase.getInstance().getReference().child("Surveys").child(mCountry);
        mDataShow.keepSynced(true);



        String ph = mAuth.getCurrentUser().getEmail().substring(0,11);

        mDatabase.child("Users").child(ph).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image = (String) dataSnapshot.child("profile").getValue();
                if(!image.equals("null")){
                    Picasso.with(StartScreen.this).load(image).into(mImageProfile);
                }else{
                    return;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mAccount = new Intent(StartScreen.this, UserProfileActivity.class);
                startActivity(mAccount);
            }
        });

        mAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateSurvey createSurvey = new CreateSurvey();
                createSurvey.showDialog(StartScreen.this);
            }
        });

        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new GridLayoutManager(this,2));

    }
    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<SurveyAdapter, StartScreen.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter
                <SurveyAdapter, StartScreen.BlogViewHolder>(
                SurveyAdapter.class,
                R.layout.survey_row,
                StartScreen.BlogViewHolder.class,
                mDataShow
        ) {
            @Override
            protected void populateViewHolder(final StartScreen.BlogViewHolder viewHolder, final SurveyAdapter model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setCategory(model.getCategory());
                viewHolder.setPoints(model.getPoints());
                viewHolder.mDatabaseQuestions.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.count.setText((String.valueOf(dataSnapshot.child(mCountry).child(post_key).child("Questions").getChildrenCount()))+"\nQu");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent single = new Intent(StartScreen.this, AnswerSurvey.class);
                        single.putExtra("key",post_key);
                        startActivity(single);
                    }
                });
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView count;
        DatabaseReference mDatabaseQuestions;
        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            count = (TextView)mView.findViewById(R.id.questionNumbers);
            mDatabaseQuestions = FirebaseDatabase.getInstance().getReference().child("Surveys");
        }
        public void setName(String name){
            TextView mName = (TextView) mView.findViewById(R.id.nameOfSurvey);
            mName.setText(name);
        }
        public void setCategory(String category){
            TextView mCategory = (TextView) mView.findViewById(R.id.categoryOfSurvey);
            mCategory.setText(category);
        }
        public void setPoints(String points){
            TextView mPoints = (TextView) mView.findViewById(R.id.pointCount);
            mPoints.setText(points+"\nPt");
        }
    }
}
