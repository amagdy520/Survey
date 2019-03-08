package com.survey.AddNewSurvey;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.survey.LoginAndSignUpScreen.LoginActivity;
import com.survey.MainScreen.StartScreen;
import com.survey.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ahmed Magdy on 4/25/2018.
 */

public class CreateSurvey {
    private TextInputLayout mSurveyNameLayout, mNumberOfPeopleLayout;
    private EditText mSurveyName, mNumberOfPeople;
    private Spinner mSurveyCategory, mSurveyPlace;
    private TextView mCost;
    private Button mOK;
    Activity mActivity;
    private boolean mValid = false;
    private TextWatcher textWatcher;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String ph, place, category;

    public void showDialog(final Activity activity) {
        final Dialog dialog = new Dialog(activity);
        mActivity = activity;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.activity_create_survey);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.setCanceledOnTouchOutside(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        ph = mAuth.getCurrentUser().getEmail().substring(0,11);
        mSurveyNameLayout = (TextInputLayout) dialog.findViewById(R.id.input_survey_layout);
        mNumberOfPeopleLayout = (TextInputLayout) dialog.findViewById(R.id.input_people_layout);
        mSurveyName = (EditText) dialog.findViewById(R.id.input_survey);
        mNumberOfPeople = (EditText) dialog.findViewById(R.id.input_people_number);
        mCost = (TextView) dialog.findViewById(R.id.cost);
        mSurveyCategory = (Spinner) dialog.findViewById(R.id.category);
        mSurveyPlace = (Spinner) dialog.findViewById(R.id.places);
        mOK = (Button) dialog.findViewById(R.id.create);

        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating...");

        String[] mCategory = new String[]{
                "Select Category",
                "Community",
                "Customer Feedback",
                "Customer Satisfaction",
                "Demographics",
                "Education",
                "Events",
                "Health Care",
                "Human Resource",
                "Market Research",
                "Political",
                "Quiz",
                "Other"
        };

        /**************************************************************************************/
        String[] mCountries = new String[]{"Select Place","Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla",
                "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas",
                "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
                "Bosnia and Herzegowina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "Brunei Darussalam",
                "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands",
                "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia",
                "Comoros", "Congo", "Congo, the Democratic Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire",
                "Croatia (Hrvatska)", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
                "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia",
                "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "France Metropolitan", "French Guiana",
                "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar",
                "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti",
                "Heard and Mc Donald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India",
                "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan",
                "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait",
                "Kyrgyzstan", "Lao, People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya",
                "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia, The Former Yugoslav Republic of", "Madagascar",
                "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius",
                "Mayotte", "Mexico", "Micronesia, Federated States of", "Moldova, Republic of", "Monaco", "Mongolia", "Montserrat",
                "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles",
                "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands",
                "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn",
                "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda",
                "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino",
                "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Seychelles", "Sierra Leone", "Singapore",
                "Slovakia (Slovak Republic)", "Slovenia", "Solomon Islands", "Somalia", "South Africa",
                "South Georgia and the South Sandwich Islands", "Spain", "Sri Lanka", "St. Helena", "St. Pierre and Miquelon",
                "Sudan", "Suriname", "Svalbard and Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic",
                "Taiwan, Province of China", "Tajikistan", "Tanzania, United Republic of", "Thailand", "Togo", "Tokelau", "Tonga",
                "Trinidad and Tobago", "Tunisia", "Türkiye", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine",
                "United Arab Emirates", "United Kingdom", "United States", "United States Minor Outlying Islands", "Uruguay",
                "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands (British)", "Virgin Islands (U.S.)",
                "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"};


        final List<String> countriesList = new ArrayList<>(Arrays.asList(mCountries));
// Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(activity,R.layout.spinner_item,countriesList){
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
        mSurveyPlace.setAdapter(spinnerArrayAdapter);

        mSurveyPlace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    place = selectedItemText;
//                    Toast.makeText(activity, "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /**************************************************************************************/
        final List<String> categoryList = new ArrayList<>(Arrays.asList(mCategory));
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(activity,R.layout.spinner_item,categoryList){
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
        spinnerArrayAdapter2.setDropDownViewResource(R.layout.spinner_item);
        mSurveyCategory.setAdapter(spinnerArrayAdapter2);

        mSurveyCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    category = selectedItemText;
//                    Toast.makeText(activity, "Selected : " + selectedItemText, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        textWatcher = new TextWatcher() {

            public void afterTextChanged(Editable s) {
                int number = Integer.parseInt(mNumberOfPeople.getText().toString());
                double x = number*0.05*2;
                DecimalFormat f = new DecimalFormat("##.00");
                mCost.setText(String.valueOf(f.format(x))+"$");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        };

        mNumberOfPeople.addTextChangedListener(textWatcher);


        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mSurveyName.getText().toString().isEmpty()||mNumberOfPeople.getText().toString().isEmpty()||category.equals("Select Category")||place.equals("Select Place")){
                    Toast.makeText(activity,"Please Complete Data",Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.show();
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    DatabaseReference databaseReference = mDatabase.child("Users").child(ph).child("Surveys").child(place).push();
                                    DatabaseReference databaseReference1= mDatabase.child("Surveys").child(place).push();
                                    databaseReference1.child("name").setValue(mSurveyName.getText().toString());
                                    databaseReference1.child("people_number").setValue(mNumberOfPeople.getText().toString());
                                    databaseReference1.child("category").setValue(category);
                                    databaseReference1.child("place").setValue(place);
                                    databaseReference1.child("points").setValue("1");
                                    databaseReference1.child("cost").setValue(mCost.getText().toString());
                                    databaseReference.child("survey").setValue(databaseReference1.getKey().toString());
                                    Intent mAccount = new Intent(activity, AddSurveyActivity.class);
                                    mAccount.putExtra("SurveyName",mSurveyName.getText().toString());
                                    mAccount.putExtra("SurveyKey",databaseReference1.getKey().toString());
                                    mAccount.putExtra("SurveyKeyUser",databaseReference.getKey().toString());
                                    mAccount.putExtra("Category",category);
                                    mAccount.putExtra("Place",place);
                                    activity.startActivity(mAccount);
                                    progressDialog.dismiss();
                                }
                            },500);
                }

            }
        });
        dialog.show();
    }
}
