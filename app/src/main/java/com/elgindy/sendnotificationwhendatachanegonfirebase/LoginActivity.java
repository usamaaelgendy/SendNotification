package com.elgindy.sendnotificationwhendatachanegonfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_user_email_et)
    EditText loginUserEmailEt;
    @BindView(R.id.login_user_password_et)
    EditText loginUserPasswordEt;
    @BindView(R.id.fragment_login_user_btn)
    Button fragmentLoginUserBtn;
    @BindView(R.id.fragment_login_register_link)
    TextView fragmentLoginRegisterLink;
    @BindView(R.id.progressBar_login)
    ProgressBar progressBar;

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    // var
    private String email, password, studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setupFirebaseAuth();


    }

    @OnClick({R.id.fragment_login_user_btn, R.id.fragment_login_register_link})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fragment_login_user_btn:
                email = loginUserEmailEt.getText().toString();
                password = loginUserPasswordEt.getText().toString();
                signIn(email, password);
                break;
            case R.id.fragment_login_register_link:
                Intent intent = new Intent(LoginActivity.this , RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }
    private void setupFirebaseAuth() {

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setTimestampsInSnapshotsEnabled(true)
                            .build();
                    db.setFirestoreSettings(settings);

                    DocumentReference userRef = db.collection("Users")
                            .document(user.getUid());

                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                User usersData = task.getResult().toObject(User.class);
                                Constants.CurrentOnlineUser = usersData;
                                Toast.makeText(LoginActivity.this, " اهلا  " + studentName + " فى حراج ابل الخليج  ", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn(String email, String password) {
        //check if the fields are filled out
        if (!isEmpty(loginUserEmailEt.getText().toString())
                && !isEmpty(loginUserPasswordEt.getText().toString())) {

            showDialog();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(loginUserEmailEt.getText().toString(),
                    loginUserPasswordEt.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            hideDialog();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("tagmessage", e.getMessage());
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    hideDialog();
                }
            });
        } else {
            Toast.makeText(LoginActivity.this, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show();
        }
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
