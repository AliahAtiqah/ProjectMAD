package com.example.budgetkitaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.database.FirebaseDatabase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private TextView welcomeRegister, registerUser;
    private TextInputEditText editTextFullName, editTextPassword, editTextEmail, editTextPhone, editTextCompanyName;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //HIDE TOOLBAR
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        //Assign variable and set listener
        welcomeRegister = (TextView) findViewById(R.id.wkmTitleLinkRegister);
        welcomeRegister.setOnClickListener(this);

        //Assign variable and set listener
        registerUser = (Button) findViewById(R.id.registerBtn);
        registerUser.setOnClickListener(this);

        //Assign variable
        editTextFullName = (TextInputEditText) findViewById(R.id.registerUsername);
        editTextPassword = (TextInputEditText) findViewById(R.id.registerPassword);
        editTextEmail = (TextInputEditText) findViewById(R.id.registerEmail);
        editTextPhone = (TextInputEditText) findViewById(R.id.registerPhone);
        editTextCompanyName = (TextInputEditText) findViewById(R.id.registerCompany);

        progressBar = (ProgressBar) findViewById(R.id.progressBarRegister);

        /*enable arrow icon to go back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wkmTitleLinkRegister:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.registerBtn:
                registerUser();
                break;
        }
    }

    //this method is to make sure the user "wajib" fill in the txt field
    private void registerUser() {
        //Get the input data from the EditText
        String username = editTextFullName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String company = editTextCompanyName.getText().toString().trim();

        //To check if all field have been fill
        //If null, it will point to empty field
        if (username.isEmpty()) {
            editTextFullName.setError("Full name is required!");
            editTextFullName.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6 || !isValidPassword(password)) {
            editTextPassword.setError("Min password length should be 6 characters with combination of uppercase, alphanumeric and special character");
            editTextPassword.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please provide valid email");
            editTextEmail.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            editTextPhone.setError("Phone number is required!");
            editTextPhone.requestFocus();
            return;
        }
        if (company.isEmpty()) {
            editTextCompanyName.setError("Business name is required!");
            editTextCompanyName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //Create a new user
        //User need email and password to sign in
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    //Create new user in firebase
                    User user = new User(username, email, phone, company);

                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Register.this, "User has been registered successfully!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.VISIBLE);

                                //redirected to login layout!
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);

                            }else{
                                Toast.makeText(Register.this, "Failed to register! Try again!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }else{
                    Toast.makeText(Register.this, "Failed to register! Try again!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    //To check password strength
    //Must satisfy the requirement which is alphanumeric, uppercase and special character
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
        finish();
        startActivity(new Intent(Register.this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}


