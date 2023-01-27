package com.example.budgetkitaapp;
import static android.content.Intent.getIntent;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import pub.devrel.easypermissions.EasyPermissions;

public class IncomeFragment extends Fragment  {

    private Button btnIncomeSave;
    private TextInputEditText editIncomeTransaction, editIncomeName, editIncomeTotal, dateEdt;
    private Spinner incomeListCategory;
    private ProgressBar progressBarIncome;
    private FirebaseAuth mAuth;

    ArrayList<Uri> arrayList = new ArrayList<>();
    DatabaseReference incomeReference, transactionRef;

    public IncomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_income, container, false);

        //Assign variable
        editIncomeTransaction = v.findViewById(R.id.incomeTransactionID);
        editIncomeName = v.findViewById(R.id.incomeName);
        editIncomeTotal = v.findViewById(R.id.totalIncome);
        btnIncomeSave = v.findViewById(R.id.saveIncomeBtn);
        dateEdt = v.findViewById(R.id.idBtnIncomePickDate);
        progressBarIncome = v.findViewById(R.id.progressBarIncome);
        incomeListCategory = v.findViewById(R.id.incomeCategory);

        //Firebase authentication to identify user
        mAuth = FirebaseAuth.getInstance();
        //only the login user can see and edit their data information
        incomeReference = FirebaseDatabase.getInstance().getReference("Income").child(mAuth.getCurrentUser().getUid()).child("Income");

        // Generate a new, unique transaction ID
        transactionRef = FirebaseDatabase.getInstance().getReference("Income").child(mAuth.getCurrentUser().getUid()).child("Income").push();
        // Generate a new, unique transaction ID
        String transactionId = transactionRef.getKey();
        //set the unique id to layout
        editIncomeTransaction.setText(transactionId);
        //Disable the EditText for transaction ID so user cannot edit it
        editIncomeTransaction.setEnabled(false);

        //DROP-DOWN for spinner
        String[] incomes = { "Capital Increase","Loan", "Debt payment" , "Grant", "Operation cost", "Sale", "Services revenue", "Other income"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, incomes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        incomeListCategory.setAdapter(adapter);

        // on below line we are adding click listener
        // for our pick dateExpenses button
        dateEdt.setOnClickListener(new View.OnClickListener() {
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
                        getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // format the date string as "yyyy/MM/dd"
                                String date = String.format("%04d/%02d/%02d", year, monthOfYear + 1, dayOfMonth);
                                // on below line we are setting dateExpenses to our edit text.
                                dateEdt.setText(date);

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



        //BUTTON SAVE INCOME DATA
        btnIncomeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertIncomeData();
            }
        });

        return v;
    }

    private void insertIncomeData() {

        //Get data from EditText
        String incomeID = editIncomeTransaction.getText().toString().trim();
        String incomeName = editIncomeName.getText().toString().trim();
        String incomeCategory = incomeListCategory.getSelectedItem().toString().trim();
        String incomeTotal = editIncomeTotal.getText().toString().trim();
        String incomeDate = dateEdt.getText().toString().trim();

        //Check if all field have been field
        //If not it will point to empty field
        if (incomeID.isEmpty()) {
            editIncomeTransaction.setError("Transaction ID is required!");
            editIncomeTransaction.requestFocus();
            return;
        }
        if (incomeName.isEmpty()) {
            editIncomeName.setError("Income name is required!");
            editIncomeName.requestFocus();
            return;
        }

        if (incomeTotal.isEmpty()) {
            editIncomeTotal.setError("Amount is required!");
            editIncomeTotal.requestFocus();
            return;
        }

        //If dateExpenses not selected, the save button will fail and it will ask the user to select dateExpenses
        if (incomeDate.isEmpty()) {
            dateEdt.setError("Amount is required!");
            dateEdt.requestFocus();
            return;
        } else {
            //Create new income to store into firebase
            Income income = new Income(incomeID, incomeName, incomeCategory, incomeTotal, incomeDate);
            incomeReference.push().setValue(income).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }

            });
            Intent home = new Intent(getActivity(), HomeActivity.class);
            startActivity(home);
        }

        // Get a Calendar instance
        Calendar calendar = Calendar.getInstance();

        // Get the current dateExpenses and time
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
    }
}