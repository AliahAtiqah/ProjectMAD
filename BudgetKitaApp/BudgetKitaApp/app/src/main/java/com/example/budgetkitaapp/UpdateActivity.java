package com.example.budgetkitaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

public class UpdateActivity extends AppCompatActivity {

    private TextInputEditText transID1, incName1, incAmount1, date;
    private Button updateInc, delInc;
    private Spinner incomeListCategory;
    private String et1, et2, et3, et4;
    private FirebaseAuth mAuth;

    ArrayList<Uri> arrayList = new ArrayList<>();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Income Report");

        Income income = (Income) getIntent().getSerializableExtra("course");

        //Firebase authentication to identify user
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Income").child(mAuth.getCurrentUser().getUid()).child("Income");

        //Assign variable
        transID1 = findViewById(R.id.tvTransID);
        incName1 = findViewById(R.id.tvName);
        incAmount1 = findViewById(R.id.incUpdateAmount);
        updateInc= findViewById(R.id.btnUpdate);
        delInc = findViewById(R.id.btnDelete);
        date = findViewById(R.id.btnDate);
        incomeListCategory = findViewById(R.id.incomeCategory);

        //Get data from firebase
        String transID = income.getTransactionID().trim();
        String incName = income.getIncomeName().trim();
        String incCat = income.getIncomeCategory().trim();
        String incAmount = income.getTotalIncome().trim();
        String incDate = income.getDateIncome().trim();

        //DROP-DOWN for spinner
        String[] incomes = {"Capital Increase", "Loan", "Debt payment" , "Grant", "Operation cost", "Sale", "Services revenue", "Other income"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, incomes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incomeListCategory.setAdapter(adapter);

        //DATE PICKER FUNCTION
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for dateExpenses picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        UpdateActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // format the date string as "yyyy/MM/dd"
                                String date1 = String.format("%04d/%02d/%02d", year, monthOfYear + 1, dayOfMonth);
                                // on below line we are setting dateExpenses to our edit text.
                                date.setText(date1);

                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected dateExpenses in our dateExpenses picker.
                        year, month, day);

                // at last we are calling show to
                // display our dateExpenses picker dialog.
                datePickerDialog.show();
            }
        });

        //Check the category name in spinner and set it to spinner
        int index = -1;
        for(int i = 0; i < incomeListCategory.getCount(); i++){
            if(incomeListCategory.getItemAtPosition(i).toString().equals(incCat)){
                index = i;
                break;
            }
        }

        //Set spinner selection
        if(index != -1){
            incomeListCategory.setSelection(index);
        }

        //Set the edit text with the data from firebase
        transID1.setText(transID);
        //Disable the EditText for transaction ID so user cannot edit it
        transID1.setEnabled(false);

        //Set the edit text with the data from firebase
        incName1.setText(incName);
        incAmount1.setText(incAmount);
        date.setText(incDate);


        //Delete income
        delInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);

                //Show dialog
                builder.setTitle("DELETE RECORD");
                builder.setMessage("Confirm to delete record?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the gallery and select an image
                        deleteRecord(income);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the camera and capture a photo
                        return;
                    }
                });
                builder.show();
            }
        });

        //Update income
        updateInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);

                //Show dialog
                builder.setTitle("UPDATE RECORD");
                builder.setMessage("Confirm to update record?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the gallery and select an image
                        et1 = transID1.getText().toString().trim();
                        et2 = incName1.getText().toString().trim();
                        et3 = incomeListCategory.getSelectedItem().toString().trim();
                        et4 = incAmount1.getText().toString().trim();

                        //Remove the previous "Date:" before sent the data to firebase
                        String incomeDate = date.getText().toString().trim();
                        //incomeDate = incomeDate.replace("Date: ", "");


                        if (TextUtils.isEmpty(et1)) {
                            transID1.setError("Please enter");
                        } else if (TextUtils.isEmpty(et2)) {
                            incName1.setError("Please enter");
                        } else if (TextUtils.isEmpty(et4)) {
                            incAmount1.setError("Please enter");
                        } else {
                            // calling a method to update our course.
                            // we are passing our object class, course name,
                            // course description and course duration from our edittext field.
                            updateIncome(income, et1, et2, et3, et4, incomeDate);
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the camera and capture a photo
                        return;
                    }
                });
                builder.show();
            }
        });
    }

    public void updateIncome(Income income, String et1, String et2, String et3, String et4, String incomeDate){

        Income updateIncome = new Income(et1, et2, et3, et4, incomeDate);
        databaseReference.child(income.getId()).setValue(updateIncome).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(UpdateActivity.this, "Success Update!", Toast.LENGTH_SHORT).show();

                    //Go back to transaction  activity
                    Intent home = new Intent(UpdateActivity.this, HomeActivity.class);
                    startActivity(home);

                }else{
                    Toast.makeText(UpdateActivity.this, "Failed to Update!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteRecord(Income income){

        //create reference to database
        DatabaseReference DbRef = databaseReference.child(income.getId());
        //we referencing child here because we will be delete one record not whole data data in database
        //we will use generic Task here so lets do it..

        DbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(UpdateActivity.this, "Success Delete!", Toast.LENGTH_SHORT).show();
                    Intent home = new Intent(UpdateActivity.this, HomeActivity.class);
                    startActivity(home);

                }else{
                    Toast.makeText(UpdateActivity.this, "Failed Delete!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //BACK BUTTON
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(UpdateActivity.this, ViewTransactionActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}