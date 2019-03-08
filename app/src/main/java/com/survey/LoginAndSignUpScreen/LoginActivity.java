package com.survey.LoginAndSignUpScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.survey.MainScreen.StartScreen;
import com.survey.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";

    @BindView(R.id.logo) ImageView mLogo;
    @BindView(R.id.header) TextView mHeader;
    @BindView(R.id.dataEnter) LinearLayout mData;

    @BindView(R.id.input_phone_layout) TextInputLayout mPhoneLayout;
    @BindView(R.id.input_phone) EditText mPhoneNumber;
    @BindView(R.id.input_password_layout) TextInputLayout mPasswordLayout;
    @BindView(R.id.input_password) EditText mPassword;
    @BindView(R.id.login) Button mLogin;
    @BindView(R.id.activate) Button mActivate;
    @BindView(R.id.dataEnter2) LinearLayout mData2;
    @BindView(R.id.input_code_layout) TextInputLayout mCodeLayout;
    @BindView(R.id.input_code) EditText mCode;


    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;

    boolean valid = true;

    private String mail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //CHECKING USER PRESENCE
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if ((user != null)) {
                    Intent mAccount = new Intent(LoginActivity.this, StartScreen.class);
                    startActivity(mAccount);
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mLogo.setVisibility(View.INVISIBLE);
        mHeader.setVisibility(View.INVISIBLE);
        mData.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                mLogo.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, android.R.anim.fade_in));
                mLogo.setVisibility(View.VISIBLE);
                mHeader.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, android.R.anim.fade_in));
                mHeader.setVisibility(View.VISIBLE);
                mData.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, android.R.anim.fade_in));
                mData.setVisibility(View.VISIBLE);
            }
        }, 500);


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()){
                    mail = mPhoneNumber.getText().toString() + "@survey.com";
                    final String pass = mPassword.getText().toString().trim();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                    ref.orderByChild("phone").equalTo(mPhoneNumber.getText().toString()).addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot){
                            if(dataSnapshot.exists()) {
                                //username exist
                                mAuth.signInWithEmailAndPassword(mail, pass)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Intent mAccount = new Intent(LoginActivity.this, StartScreen.class);
                                                    startActivity(mAccount);
                                                }else{
                                                    Toast.makeText(LoginActivity.this, "Unable to login user", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }else{
                                sendCode();
                                mData.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, android.R.anim.fade_out));
                                mData.setVisibility(View.INVISIBLE);
                                mData2.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, android.R.anim.fade_in));
                                mData2.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        mActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });

    }


    public boolean validate() {
        String phone = mPhoneNumber.getText().toString();
        String password = mPassword.getText().toString();

        if (phone.isEmpty() || phone.length() < 11 || phone.length() > 11) {
            mPhoneNumber.setError("Must be 11 number");
            valid = false;
        } else {
            mPhoneNumber.setError(null);
        }
        if (password.isEmpty() || password.length() < 6 || password.length() > 15) {
            mPassword.setError("Must be between 6 & 15 character");
            valid = false;
        } else {
            mPassword.setError(null);
        }


        return valid;
    }

    public void sendCode() {

        String phoneNumber = mPhoneNumber.getText().toString();

        setUpVerificatonCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks);


    }

    private void setUpVerificatonCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {

                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d(TAG, "Invalid credential: "
                                    + e.getLocalizedMessage());
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        phoneVerificationId = verificationId;
                        resendToken = token;
                    }
                };
    }

    public void verifyCode() {

        String code = mCode.getText().toString();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        String email = mPhoneNumber.getText().toString() + "@survey.com";
        mAuth.createUserWithEmailAndPassword(email,mPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent mAccount = new Intent(LoginActivity.this, StartScreen.class);
                            startActivity(mAccount);
                            DatabaseReference current = mDatabase.child(mPhoneNumber.getText().toString());
                            current.child("name").setValue("null");
                            current.child("points").setValue("null");
                            current.child("profile").setValue("null");
                            current.child("phone").setValue(mPhoneNumber.getText().toString());
                            current.child("address").setValue(getUserCountry(LoginActivity.this));
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
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

    public void resendCode() {

        String phoneNumber = mPhoneNumber.getText().toString();

        setUpVerificatonCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }

    public void signOut(View view) {
        mAuth.signOut();
    }
}
