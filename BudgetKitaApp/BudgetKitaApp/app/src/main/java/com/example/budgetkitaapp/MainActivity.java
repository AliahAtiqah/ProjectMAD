package com.example.budgetkitaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView register,passReset;
    private TextInputEditText editTextEmail, editTextPassword;
    private Button signIn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();  //HIDE TOOLBAR
        //getSupportActionBar().setTitle("String"); //string is custom name you want

        //Assign variable and set on click listener for signup text
        register = (TextView) findViewById(R.id.titleSignUpLink);//INITIALIZE REGISTER LINK IN LOGIN PAGE
        register.setOnClickListener(this);

        //Assign variable and set on click listener for sign in button
        signIn = (Button) findViewById(R.id.loginBtn);
        signIn.setOnClickListener(this);

        //Assign variable
        editTextEmail = (TextInputEditText) findViewById(R.id.loginEmail);
        editTextPassword = (TextInputEditText) findViewById(R.id.loginPassword);

        progressBar = (ProgressBar) findViewById(R.id.progressBarRegister);

        //Check if current user exist in firebase
        mAuth = FirebaseAuth.getInstance();

        //Make Forgot password text clickable and link it to next Forgot Password Activity
        passReset = (TextView) findViewById(R.id.forgetPasswordLink);
        passReset.setOnClickListener(view -> {
            //go to ForgotPassword activity using intent
            Intent reset = new Intent(MainActivity.this, ForgotPassword.class);
            startActivity(reset);
        });
    }

    @Override
    public void onClick(View view) {
        //switch to differentiate if user click sign in or signup
        switch (view.getId()){
            case R.id.titleSignUpLink:
                startActivity(new Intent(this, Register.class));
                break;
            case R.id.loginBtn:
                userLogin();
                break;
        }
    }

    private void userLogin() {
        //get the user input for email and password
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //Check the field to make sure the field have been filled
        //if empty it will show error and highlight where the empty field
        if(email.isEmpty()){
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
            return;
        }if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please provide valid email");
            editTextEmail.requestFocus();
            return;
        }if(password.isEmpty()){
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }if(password.length() < 6 || !isValidPassword(password)){
            editTextPassword.setError("Min password length should be 6 characters with combination of uppercase, alphanumeric and special character");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //Authenticate the user using the email and password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    //redirect to home page
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                }else{
                    //if user not exist in firebase
                    Toast.makeText(MainActivity.this, "Failed to login! Please check your credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //Check the password strength
    // Must satisfy the requirement which is must have alphanumeric, uppercase and special char
    public static boolean isValidPassword(final String editTextPassword) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(editTextPassword);

        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
