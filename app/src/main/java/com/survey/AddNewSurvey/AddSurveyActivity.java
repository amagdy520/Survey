package com.survey.AddNewSurvey;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.survey.AddQuestions.AddQuestion;
import com.survey.LoginAndSignUpScreen.LoginActivity;
import com.survey.MainScreen.StartScreen;
import com.survey.R;
import com.survey.UserProfileScreen.UserProfileActivity;
import com.xw.repo.BubbleSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddSurveyActivity extends AppCompatActivity {

    private String mKey, mSurveyKey, mSurveyKeyUser, mPhoneNumber, mCategory, mPlace;
    @BindView(R.id.surveyName) TextView mTitle;
    @BindView(R.id.back) ImageView mBack;
    @BindView(R.id.save) ImageView mSave;
    @BindView(R.id.survey) RecyclerView mSurveyQuestions;
    @BindView(R.id.actionAdd) FloatingActionButton mAddQuestion;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mDataShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_survey);
        ButterKnife.bind(this);
        mKey = getIntent().getStringExtra("SurveyName");
        mSurveyKey = getIntent().getStringExtra("SurveyKey");
        mSurveyKeyUser = getIntent().getStringExtra("SurveyKeyUser");
        mPlace = getIntent().getStringExtra("Place");
        mCategory = getIntent().getStringExtra("Category");
        mTitle.setText(mKey);
        mAuth = FirebaseAuth.getInstance();
        mPhoneNumber = mAuth.getCurrentUser().getEmail().substring(0,11);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mDataShow = FirebaseDatabase.getInstance().getReference().child("Surveys").child(mPlace).child(mSurveyKey).child("Questions");
        mDataShow.keepSynced(true);


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating...");



        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(AddSurveyActivity.this);
                builder1.setMessage("Are you sure you want to discard this!?.");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Discard",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mDatabase.child("Surveys").child(mPlace).child(mSurveyKey).removeValue();
                                mDatabase.child("Users").child(mPhoneNumber).child("Surveys").child(mPlace).child(mSurveyKeyUser).removeValue();
                                Intent mAccount = new Intent(AddSurveyActivity.this, StartScreen.class);
                                startActivity(mAccount);
                            }
                        });

                builder1.setNegativeButton(
                        "Stay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Intent mAccount = new Intent(AddSurveyActivity.this, StartScreen.class);
                                startActivity(mAccount);
                            }
                        },500);
            }
        });
        mAddQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddQuestion addQuestion = new AddQuestion();
                addQuestion.showDialog(AddSurveyActivity.this,mPlace,mCategory,mSurveyKey);
            }
        });

        mSurveyQuestions.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mSurveyQuestions.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(AddSurveyActivity.this);
        builder1.setMessage("Are you sure you want to discard this!?.");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Discard",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mDatabase.child("Surveys").child(mPlace).child(mSurveyKey).removeValue();
                        mDatabase.child("Users").child(mPhoneNumber).child("Surveys").child(mPlace).child(mSurveyKeyUser).removeValue();
                        Intent mAccount = new Intent(AddSurveyActivity.this, StartScreen.class);
                        startActivity(mAccount);
                    }
                });

        builder1.setNegativeButton(
                "Stay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<QuestionAdapter, AddSurveyActivity.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter
                <QuestionAdapter, AddSurveyActivity.BlogViewHolder>(
                QuestionAdapter.class,
                R.layout.question_row,
                AddSurveyActivity.BlogViewHolder.class,
                mDataShow
        ) {
            @Override
            protected void populateViewHolder(final AddSurveyActivity.BlogViewHolder viewHolder, final QuestionAdapter model, int position) {
                viewHolder.setQuestion(model.getQuestion());
                viewHolder.setFormat(model.getFormat());
                viewHolder.setFrom(model.getFrom());
                viewHolder.setTo(model.getTo());
                viewHolder.setLabel(model.getLabel());
            }
        };
        mSurveyQuestions.setAdapter(firebaseRecyclerAdapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        EditText mAnswer;
        LinearLayout mSeek;
        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mAnswer = (EditText) mView.findViewById(R.id.answerTxtBox);
            mSeek = (LinearLayout) mView.findViewById(R.id.seekFormat);
        }
        public void setQuestion(String question){
            TextView mQuestion = (TextView) mView.findViewById(R.id.questionTxt);
            mQuestion.setText(question);
        }
        public void setFormat(String format){
            switch (format){
                case "Single Textbox":
                    mAnswer.setVisibility(View.VISIBLE);
                    break;
                case "Slider":
                    mSeek.setVisibility(View.VISIBLE);
                    break;
            }
        }
        public void setFrom(String from){
            BubbleSeekBar mFrom = (BubbleSeekBar) mView.findViewById(R.id.seekBar);
            mFrom.setLeft(Integer.parseInt(from));
        }
        public void setTo(String to){
            BubbleSeekBar mTo = (BubbleSeekBar) mView.findViewById(R.id.seekBar);
            mTo.setRight(Integer.parseInt(to));
        }
        public void setLabel(String label){
            TextView mLabel = (TextView) mView.findViewById(R.id.txtLabel);
            if(label.equals("null")){
                mLabel.setText("");
            }else{
                mLabel.setText(label);
            }
        }
    }
}
