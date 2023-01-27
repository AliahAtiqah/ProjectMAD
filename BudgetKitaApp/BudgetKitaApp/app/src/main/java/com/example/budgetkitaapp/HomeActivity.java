package com.example.budgetkitaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private TextView transactionLink, transactionViewLink;
    private ImageView mapsLink;
    private CardView addTransactionCard, viewIncomeExpensesCard, mapsCard;
    private FirebaseAuth mAuth;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    double totalIncome = 0;
    double totalExpenses = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // to make the Navigation drawer icon always appear on the action bar
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.navigation_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase authentication to identify the current user
        mAuth = FirebaseAuth.getInstance();
        // Connect to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Get income reference
        DatabaseReference incomeRef = database.getReference("Income").child(mAuth.getCurrentUser().getUid()).child("Income");
        //Get expenses reference
        DatabaseReference expensesRef = database.getReference("Expenses").child(mAuth.getCurrentUser().getUid()).child("Expenses");

        // Add income or expenses to total
        incomeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                try {
                    Date currentDate = dateFormat.parse(dateFormat.format(new Date()));
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String dateIncomeString = child.child("dateIncome").getValue(String.class);
                        if (dateIncomeString != null) {
                            Date dateIncome = dateFormat.parse(dateIncomeString);
                            if (currentDate.equals(dateIncome)) {
                                String income = child.child("totalIncome").getValue(String.class);
                                double incomeDouble = Double.parseDouble(income);
                                totalIncome += incomeDouble;
                            }
                        }
                    }
                } catch (ParseException e) {
                    Log.e("Income", "Error while parsing date: " + e.getMessage());
                }
                // Update the TextView
                TextView totalIncomeTodayTextView = findViewById(R.id.earnedTotalInHome);
                totalIncomeTodayTextView.setText("RM" + Double.toString(totalIncome));
                totalIncomeTodayTextView.setTextColor(Color.GREEN);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        // Add income or expenses to total
        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                try {
                    Date currentDate = dateFormat.parse(dateFormat.format(new Date()));
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String dateExpenseString = child.child("expensesDate").getValue(String.class);
                        if (dateExpenseString != null) {
                            Date dateExpense = dateFormat.parse(dateExpenseString);
                            if (currentDate.equals(dateExpense)) {
                                String expense = child.child("expensesTotal").getValue(String.class);
                                double expenseDouble = Double.parseDouble(expense);
                                totalExpenses += expenseDouble;

                            }
                        }
                    }
                } catch (ParseException e) {
                    Log.e("Expense", "Error while parsing date: " + e.getMessage());
                }
                // Update the TextView
                TextView totalExpensesTodayTextView = findViewById(R.id.spentTotalInHome);
                totalExpensesTodayTextView.setText("RM" + Double.toString(totalExpenses));
                totalExpensesTodayTextView.setTextColor(Color.RED);

                // Update the TextView
                TextView incomeExpensesDifferenceTextView = findViewById(R.id.numProfitTotal);
                TextView txtprofitLoss = findViewById(R.id.txtProfit);

                double incomeExpensesDifference = totalIncome - totalExpenses;
                String formattedDifference;
                if (incomeExpensesDifference > 0) {
                    formattedDifference = String.format("%.2f", incomeExpensesDifference);
                    incomeExpensesDifferenceTextView.setText("RM " + formattedDifference);

                    //Set text green colour
                    incomeExpensesDifferenceTextView.setTextColor(Color.parseColor("#00ff00"));
                    txtprofitLoss.setText("Total Profit Today is ");

                } else if (incomeExpensesDifference == 0) {
                    formattedDifference = String.format("%.2f", incomeExpensesDifference);
                    incomeExpensesDifferenceTextView.setText("RM " + formattedDifference);

                    //Set text grey colour
                    incomeExpensesDifferenceTextView.setTextColor(Color.parseColor("#808080"));

                    txtprofitLoss.setText("Total Profit Today is ");

                } else {
                    double negativeDifference = incomeExpensesDifference * -1;
                    formattedDifference = String.format("%.2f", negativeDifference);
                    incomeExpensesDifferenceTextView.setText("RM " + formattedDifference);

                    //Set text red colour
                    incomeExpensesDifferenceTextView.setTextColor(Color.parseColor("#ff0000"));
                    txtprofitLoss.setText("Total Loss Today is ");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        //Make textview clickable and link it to next Transaction Activity
        transactionLink = (TextView) findViewById(R.id.transactionLinkPage);
        transactionLink.setOnClickListener(view -> {
            Intent send = new Intent(HomeActivity.this, TransactionActivity.class);
            startActivity(send);
        });

        //Make cardview clickable and link it to next Transaction Activity
        addTransactionCard = (CardView) findViewById(R.id.cardTotalTransaction);
        addTransactionCard.setOnClickListener(view -> {
            Intent send = new Intent(HomeActivity.this, TransactionActivity.class);
            startActivity(send);
        });


        //Make textview clickable and link it to next View Transaction Activity
        transactionViewLink = (TextView) findViewById(R.id.viewIncomeExpensesLinkPage);
        transactionViewLink.setOnClickListener(view -> {
            Intent reportView = new Intent(HomeActivity.this, ViewTransactionActivity.class);
            startActivity(reportView);
        });

        //Make cardview clickable and link it to next View Transaction Activity
        viewIncomeExpensesCard = (CardView) findViewById(R.id.cardViewIncomeExpenses);
        viewIncomeExpensesCard.setOnClickListener(view -> {
            Intent reportView = new Intent(HomeActivity.this, ViewTransactionActivity.class);
            startActivity(reportView);
        });

        //Make imageview clickable and link it to Maps
        mapsLink = (ImageView) findViewById(R.id.mapsLink);
        mapsLink.setOnClickListener(view -> {
            Intent reportView = new Intent(HomeActivity.this, RestAPI.class);
            startActivity(reportView);
        });

        //Make cardview clickable and link it to Maps
        mapsCard = (CardView) findViewById(R.id.cardMaps);
        mapsCard.setOnClickListener(view -> {
            Intent reportView = new Intent(HomeActivity.this, RestAPI.class);
            startActivity(reportView);
        });

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            // Set the navigation item selected listener
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.userProfilePage:
                            Intent profileIntent = new Intent(HomeActivity.this, UserProfile.class);
                            startActivity(profileIntent);
                            return true;
                        case R.id.logoutPage:
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);

                            //Show dialog
                            builder.setTitle("LOGOUT");
                            builder.setMessage("Confirm to logout?");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent logoutIntent = new Intent(HomeActivity.this, MainActivity.class);
                                    startActivity(logoutIntent);
                                    finish();
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
                    return false;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}


