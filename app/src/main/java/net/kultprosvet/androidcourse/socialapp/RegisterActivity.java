package net.kultprosvet.androidcourse.socialapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static android.Manifest.permission.READ_CONTACTS;

public class RegisterActivity extends AppCompatActivity {

    private EditText mInputEmail, mInputPassword;
    private Button mBtnSignIn, mBtnRegister;
    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;
    public static final int MIN_PASSWRD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        findViews();

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mInputEmail.getText().toString().trim();
                String password = mInputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_toast_enter_email),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_toast_enter_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < MIN_PASSWRD_LENGTH) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_toast_short_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                //create user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                String result = getString(R.string.toast_auth_completed) + task.isSuccessful();
                                Toast.makeText(RegisterActivity.this,
                                        result,
                                        Toast.LENGTH_SHORT).show();

                                mProgressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    result = getString(R.string.error_toast_auth_failed) + task.getException();
                                    Toast.makeText(RegisterActivity.this,
                                            result,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });

            }
        });
    }

    private void findViews() {
        mBtnSignIn = (Button) findViewById(R.id.sign_in_button);
        mBtnRegister = (Button) findViewById(R.id.register_button);
        mInputEmail = (EditText) findViewById(R.id.email);
        mInputPassword = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }
}