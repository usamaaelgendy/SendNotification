package com.elgindy.sendnotificationwhendatachanegonfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.register_email_ET)
    EditText registerEmailET;
    @BindView(R.id.register_password_ET)
    EditText registerPasswordET;
    @BindView(R.id.register_user_btn)
    Button registerUserBtn;
    @BindView(R.id.login_link)
    TextView loginLink;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    // firebase
    private FirebaseFirestore mDb;
    private FirebaseAuth auth;

    private String email, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mDb = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();

    }

    @OnClick({R.id.register_user_btn, R.id.login_link})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.register_user_btn:
                createAccount();
                break;
            case R.id.login_link:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void createAccount() {
        // get input fields in the string
        email = registerEmailET.getText().toString();
        password = registerPasswordET.getText().toString();

        // check if the user name or password or phone field empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write your email....", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write your password....", Toast.LENGTH_SHORT).show();
        } else {
            registerNewEmail(email, password);
        }

    }

    private void registerNewEmail(final String email, final String password) {
        showDialog();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userId = firebaseUser.getUid();


                            //insert some default data
                            User user = new User();
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setuId(userId);

                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                    .setTimestampsInSnapshotsEnabled(true)
                                    .build();
                            mDb.setFirestoreSettings(settings);

                            DocumentReference newUserRef = mDb
                                    .collection("Users")
                                    .document(FirebaseAuth.getInstance().getUid());

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    hideDialog();

                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "check your internet ", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this, "this email already used or used valid email like name@gmail.com", Toast.LENGTH_LONG).show();
                            hideDialog();
                        }
                    }
                });
    }


    private void showDialog() {
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

}
