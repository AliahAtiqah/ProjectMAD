package com.example.budgetkitaapp;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ViewIncomeFragment extends Fragment {

    RecyclerView rv1;
    ArrayList<Income> incomeArrayList;
    IncomeAdapter incomeAdapter;
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference, totalIncomeReference;
    TextView dateIncome;
    String startIncomeDate, endIncomeDate;
    private boolean dateSelected = false;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public ViewIncomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_income, container, false);
        rv1 = v.findViewById(R.id.recycler1);

        //Firebase authentication to identify user
        mAuth = FirebaseAuth.getInstance();

        incomeArrayList = new ArrayList<>();
        rv1.setHasFixedSize(true);
        rv1.setLayoutManager(new LinearLayoutManager(getActivity()));
        incomeAdapter = new IncomeAdapter(incomeArrayList, getActivity());
        rv1.setAdapter(incomeAdapter);

        //only the login user can see their data information
        databaseReference = FirebaseDatabase.getInstance().getReference("Income").child(mAuth.getCurrentUser().getUid()).child("Income");

        //For total income reference
        totalIncomeReference = FirebaseDatabase.getInstance().getReference("Income").child(mAuth.getCurrentUser().getUid()).child("Income");
        //Calculate sum values of total expenses
        totalIncomeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double totalIncome = 0;
                for (DataSnapshot incomeSnapshot : dataSnapshot.getChildren()) {
                    String incomeValue = incomeSnapshot.child("totalIncome").getValue(String.class);
                    if(incomeValue != null) {
                        double income = Double.valueOf(incomeValue);
                        totalIncome += income;
                    }
                }
                // Update the TextView
                TextView viewtotalExpenses = v.findViewById(R.id.ttlIncome);
                viewtotalExpenses.setText("Total Income: RM " + Double.toString(totalIncome));
                viewtotalExpenses.setTextColor(Color.GREEN);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Error", "Failed to read value.", error.toException());
            }
        });

        //Get all list incomes
        databaseReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                incomeArrayList.clear();

                for(DataSnapshot incomeDatasnap : dataSnapshot.getChildren()){
                    Income income = incomeDatasnap.getValue(Income.class);
                    income.setId(incomeDatasnap.getKey());
                    incomeArrayList.add(income);
                }
                Collections.sort(incomeArrayList, new Comparator<Income>() {
                    @Override
                    public int compare(Income income1, Income income2) {
                        try {
                            Date date1 = dateFormat.parse(income1.getDateIncome());
                            Date date2 = dateFormat.parse(income2.getDateIncome());
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
                incomeAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if we do not get any data or any error we are displaying
                // a toast message that we do not get any data
                Toast.makeText(getActivity(), "Fail to get the data.", Toast.LENGTH_SHORT).show();
            }
        });

        //Select dateIncome for filter
        dateIncome = v.findViewById(R.id.txtDate);
        dateIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show dialog to get dateExpenses from user
                Calendar calendar2 = Calendar.getInstance();
                DatePickerDialog datePickerDialog2 = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Pad single digit day values with a leading zero
                        String day2 = String.valueOf(dayOfMonth);
                        if (dayOfMonth < 10) {
                            day2 = "0" + day2;
                        }
                        // Pad single digit month values with a leading zero
                        String month2 = String.valueOf(month + 1);
                        if (month < 9) {
                            month2 = "0" + month2;
                        }
                        String year2 = String.valueOf(year);

                        endIncomeDate = (year2 +"/"+ month2 +"/"+ day2);

                        //Set the dateExpenses
                        if (validateDates() != false){
                            dateIncome.setText(startIncomeDate +" - "+ endIncomeDate);
                        }
                    }
                }, calendar2.get(Calendar.YEAR), calendar2.get(Calendar.MONTH), calendar2.get(Calendar.DAY_OF_MONTH));
                datePickerDialog2.show();

                Calendar calendar1 = Calendar.getInstance();
                DatePickerDialog datePickerDialog1 = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Pad single digit day values with a leading zero
                        String day1 = String.valueOf(dayOfMonth);
                        if (dayOfMonth < 10) {
                            day1 = "0" + day1;
                        }
                        // Pad single digit month values with a leading zero
                        String month1 = String.valueOf(month + 1);
                        if (month < 9) {
                            month1 = "0" + month1;
                        }
                        String year1 = String.valueOf(year);
                        startIncomeDate = (year1 +"/"+ month1 +"/"+ day1);
                    }
                }, calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH), calendar1.get(Calendar.DAY_OF_MONTH));
                datePickerDialog1.show();
            }
        });


        //Filter button
        Button filterButton = v.findViewById(R.id.btnFilter);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dateSelected) {
                    dateSelected = true;
                    if(validateDates() != false){
                        filterIncomeByDate(startIncomeDate, endIncomeDate);
                    }
                }else{
                    filterIncomeByDate(startIncomeDate, endIncomeDate);
                }
            }
        });
        return v;
    }

    //Method to filter the Income by dateIncome
    private void filterIncomeByDate(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            Query query = databaseReference.orderByChild("dateIncome").startAt(startDate).endAt(endDate);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    incomeArrayList.clear();
                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                        Income income = snapshot1.getValue(Income.class);
                        incomeArrayList.add(income);
                    }
                    Collections.sort(incomeArrayList, new Comparator<Income>() {
                        @Override
                        public int compare(Income income1, Income income2) {
                            try {
                                Date date1 = dateFormat.parse(income1.getDateIncome());
                                Date date2 = dateFormat.parse(income2.getDateIncome());
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });
                    incomeAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Item failed, log a message
                    Log.w("Error", "loadIncome:onCancelled", databaseError.toException());
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    //Validate that the start dateIncome is before the end dateIncome
    public boolean validateDates() {
        Date start = null;
        try {
            start = dateFormat.parse(startIncomeDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Start date should not be empty.", Toast.LENGTH_SHORT).show();
        }
        Date end = null;
        try {
            end = dateFormat.parse(endIncomeDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "End date should not be empty.", Toast.LENGTH_SHORT).show();

        }
        if (start.after(end)) {
            //end dateExpenses is before start dateExpenses
            //display an error message to the user
            Toast.makeText(getActivity(), "End date should be greater than start date.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            //dates are valid
            return true;
        }
    }
}