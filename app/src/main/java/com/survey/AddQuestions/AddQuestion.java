package com.survey.AddQuestions;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.survey.AddNewSurvey.AddSurveyActivity;
import com.survey.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ahmed Magdy on 4/27/2018.
 */

public class AddQuestion {
    Activity mActivity;

    private EditText mQuestion, mWord, mFrom, mTo;
    private Spinner mSurveyAnswerType;
    private Button mUpload;
    private LinearLayout mSlider;
    private String mAnswerFormat, mPhoneNumber, mPlace, mCategory, mSurveyKey;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    public void showDialog(final Activity activity, final String place, final String category, final String surveyKey) {
        final Dialog dialog = new Dialog(activity);
        mActivity = activity;
        mPlace = place;
        mCategory = category;
        mSurveyKey = surveyKey;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_question_activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.setCanceledOnTouchOutside(true);

        mAuth = FirebaseAuth.getInstance();
        mPhoneNumber = mAuth.getCurrentUser().getEmail().substring(0,11);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);


        mQuestion = (EditText) dialog.findViewById(R.id.input_question);
        mWord = (EditText) dialog.findViewById(R.id.input_word);
        mFrom = (EditText) dialog.findViewById(R.id.input_from);
        mTo = (EditText) dialog.findViewById(R.id.input_to);
        mSlider = (LinearLayout) dialog.findViewById(R.id.slider);
        mUpload = (Button) dialog.findViewById(R.id.uploadQuestion);

        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Add Question...");
        mSurveyAnswerType = (Spinner) dialog.findViewById(R.id.questionFormat);
        final String[] mAnswerType = new String[]{
                "Select Answer Format",
                "Single Textbox",
                "Slider"
        };
        final List<String> typeList = new ArrayList<>(Arrays.asList(mAnswerType));
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(activity,R.layout.spinner_item,typeList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSurveyAnswerType.setAdapter(spinnerArrayAdapter);

        mSurveyAnswerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    if(position==2){
                        mSlider.setVisibility(View.VISIBLE);
                    }else{
                        mSlider.setVisibility(View.GONE);
                    }
                    mAnswerFormat = selectedItemText;
//                    Toast.makeText(activity, "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mQuestion.getText().toString().isEmpty()||mFrom.getText().toString().isEmpty()||mTo.getText().toString().isEmpty()){
                    Toast.makeText(activity,"Please Fill Data",Toast.LENGTH_SHORT).show();
                }else{
                    DatabaseReference databaseReference = mDatabase.child("Surveys").child(mPlace).child(mSurveyKey).child("Questions").push();
                    databaseReference.child("question").setValue(mQuestion.getText().toString());
                    databaseReference.child("format").setValue(mAnswerFormat);
                    databaseReference.child("from").setValue(mFrom.getText().toString());
                    databaseReference.child("to").setValue(mTo.getText().toString());
                    if(!mWord.getText().toString().isEmpty()){
                        databaseReference.child("label").setValue(mWord.getText().toString());
                    }else{
                        databaseReference.child("label").setValue("null");
                    }
                }
            }
        });
        dialog.show();
    }
}
