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

public class ViewExpensesFragment extends Fragment {

    RecyclerView rv2;
    ArrayList<Expenses> expensesArrayList;
    ExpensesAdapter expensesAdapter;
    private FirebaseAuth mAuth;
    DatabaseReference databaseReference,totalExpensesReference;
    TextView dateExpenses;
    String startExpensesDate, endExpensesDate;
    private boolean dateSelected = false;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public ViewExpensesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_view_expenses, container, false);
        rv2 = v.findViewById(R.id.recycler2);

        //Firebase authentication to identify user
        mAuth = FirebaseAuth.getInstance();
        //only the login user can see their data information
        databaseReference = FirebaseDatabase.getInstance().getReference("Expenses").child(mAuth.getCurrentUser().getUid()).child("Expenses");

        expensesArrayList = new ArrayList<>();
        rv2.setHasFixedSize(true);
        rv2.setLayoutManager(new LinearLayoutManager(getActivity()));
        expensesAdapter = new ExpensesAdapter(expensesArrayList, getActivity());
        rv2.setAdapter(expensesAdapter);

        //For total expenses reference
        totalExpensesReference = FirebaseDatabase.getInstance().getReference("Expenses").child(mAuth.getCurrentUser().getUid()).child("Expenses");
        //Calculate sum values of total expenses
        totalExpensesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double totalExpenses = 0;
                for (DataSnapshot expensesSnapshot : dataSnapshot.getChildren()) {
                    String expensesValue = expensesSnapshot.child("expensesTotal").getValue(String.class);
                    if(expensesValue != null) {
                        double expenses = Double.valueOf(expensesValue);
                        totalExpenses += expenses;
                    }
                }
                // Update the TextView
                TextView viewtotalExpenses = v.findViewById(R.id.ttlExpenses);
                viewtotalExpenses.setText("Total Expenses: RM " + Double.toString(totalExpenses));
                viewtotalExpenses.setTextColor(Color.RED);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Error", "Failed to read value.", error.toException());
            }
        });

        //Get all list expenses
        databaseReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                expensesArrayList.clear();

                for(DataSnapshot expensesDatasnap : dataSnapshot.getChildren()) {
                    Expenses expenses = expensesDatasnap.getValue(Expenses.class);
                    expenses.setId(expensesDatasnap.getKey());
                    expensesArrayList.add(expenses);
                }
                Collections.sort(expensesArrayList, new Comparator<Expenses>(){
                    @Override
                    public int compare(Expenses e1, Expenses e2) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            Date date1 = dateFormat.parse(e1.getExpensesDate());
                            Date date2 = dateFormat.parse(e2.getExpensesDate());
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
                expensesAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if we do not get any data or any error we are displaying
                // a toast message that we do not get any data
                Toast.makeText(getActivity(), "Fail to get the data.", Toast.LENGTH_SHORT).show();
            }
        });

        //Select dateExpenses for filter
        dateExpenses = v.findViewById(R.id.txtDate);
        dateExpenses.setOnClickListener(new View.OnClickListener() {
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

                        endExpensesDate = (year2 +"/"+ month2 +"/"+ day2);

                        //Set the dateExpenses
                        if (validateDates() != false){
                            dateExpenses.setText(startExpensesDate +" - "+ endExpensesDate);
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
                        startExpensesDate = (year1+"/"+month1+"/"+day1);
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
                    if(validateDates() != false) {
                        filterExpensesByDate(startExpensesDate, endExpensesDate);
                    }
                }else{
                    filterExpensesByDate(startExpensesDate, endExpensesDate);
                }

            }
        });

        return v;
    }

    //Method to filter the Income by dateExpenses
    private void filterExpensesByDate(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            Query query = databaseReference.orderByChild("expensesDate").startAt(startDate).endAt(endDate);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    expensesArrayList.clear();
                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                        Expenses expenses = snapshot1.getValue(Expenses.class);
                        expensesArrayList.add(expenses);
                    }
                    Collections.sort(expensesArrayList, new Comparator<Expenses>() {
                        @Override
                        public int compare(Expenses expenses1, Expenses expenses2) {
                            try {
                                Date date1 = dateFormat.parse(expenses1.getExpensesDate());
                                Date date2 = dateFormat.parse(expenses2.getExpensesDate());
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });
                    expensesAdapter.notifyDataSetChanged();
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

    //Validate that the start dateExpenses is before the end dateExpenses
    public boolean validateDates() {
        Date start = null;
        try {
            start = dateFormat.parse(startExpensesDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Start date should not be empty.", Toast.LENGTH_SHORT).show();
        }
        Date end = null;
        try {
            end = dateFormat.parse(endExpensesDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "End dateExpenses should not be empty.", Toast.LENGTH_SHORT).show();

        }
        if (start.after(end)) {
            //end dateExpenses is before start dateExpenses
            //display an error message to the user
            Toast.makeText(getActivity(), "End dateExpenses should be greater than start date.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            //dates are valid
            return true;
        }

    }
}