package com.example.budgetkitaapp;

import static com.example.budgetkitaapp.ExpensesFragment.REQUEST_IMAGE_CAPTURE;
import static com.example.budgetkitaapp.ExpensesFragment.REQUEST_IMAGE_GALLERY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class UpdateExpenses extends AppCompatActivity {

    private TextInputEditText transID2, expName2, expAmount2, expUpdateDate;
    private Button updateExp, delExp,  btnInvoice;
    private Spinner expensesListCategory;
    private ImageView ivInvoice;
    private String et1, et2, et3, et4, et5;
    private FirebaseAuth mAuth;
    FirebaseStorage storage;

    ArrayList<Uri> arrayList = new ArrayList<>();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_expenses);
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Expenses Report");

        Expenses expenses = (Expenses) getIntent().getSerializableExtra("expenses");

        //Firebase authentication to identify user
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Expenses").child(mAuth.getCurrentUser().getUid()).child("Expenses");

        //Assign variable
        transID2 = findViewById(R.id.et1);
        expName2 = findViewById(R.id.et2);
        expAmount2 = findViewById(R.id.et3);
        expUpdateDate = findViewById(R.id.btnExpDate);
        updateExp = findViewById(R.id.btnUpdate);
        delExp = findViewById(R.id.btnDelete);
        expensesListCategory = findViewById(R.id.updateExpensesCategory);
        btnInvoice = findViewById(R.id.btnInvoice);
        ivInvoice = findViewById(R.id.ivInvoice);

        //Get data from firebase
        String transID = expenses.getExpensesTransactionID().trim();
        String expName = expenses.getExpensesName().trim();
        String expCat = expenses.getExpensesCategory().trim();
        String expAmount = expenses.getExpensesTotal().trim();
        String expDate = expenses.getExpensesDate().trim();

        //Check if there imageUrl
        String imageUrl = expenses.getInvoiceUrl();
        if(imageUrl != null) {
            imageUrl = imageUrl.trim();
        }

        //DROP-DOWN for spinner
        String[] expenses1 = {"Debt payment", "Giving debt", "Stock purchase", "Donation", "Operating cost", "Savings or investments", "Personal expenses", "Other expenses"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, expenses1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expensesListCategory.setAdapter(adapter);

        //DATE PICKER FUNCTION
        expUpdateDate.setOnClickListener(new View.OnClickListener() {
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
                        UpdateExpenses.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting dateExpenses to our edit text.
                                expUpdateDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

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
        for (int i = 0; i < expensesListCategory.getCount(); i++) {
            if (expensesListCategory.getItemAtPosition(i).toString().equals(expCat)) {
                index = i;
                break;
            }
        }

        //Set spinner selection
        if (index != -1) {
            expensesListCategory.setSelection(index);
        }

        //Set the edit tex
        transID2.setText(transID);
        //Disable the EditText for transaction ID so user cannot edit it
        transID2.setEnabled(false);

        //Set the edit text
        expName2.setText(expName);
        expAmount2.setText(expAmount);
        expUpdateDate.setText(expDate);

        //Set imageview
        Glide.with(this)
                .load(imageUrl)
                .into(ivInvoice);
        ivInvoice.setVisibility(View.VISIBLE);

        //Button to take image for invoice
        btnInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateExpenses.this);

                //Show dialog
                builder.setTitle("Select Image");
                builder.setMessage("Open Camera or Open Gallery?");
                builder.setPositiveButton("Open Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the camera and capture a photo
                        openCamera();
                    }
                });
                builder.setNegativeButton("Open Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the gallery and select an image
                        openGallery();
                    }
                });
                builder.show();
            }
        });

        //Delete expenses
        String finalImageUrl = imageUrl;
        delExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateExpenses.this);

                //Show dialog
                builder.setTitle("DELETE RECORD");
                builder.setMessage("Confirm to delete record?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the gallery and select an image

                        deleteRecord(expenses, finalImageUrl);


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
        String finalImageUrl1 = imageUrl;
        updateExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateExpenses.this);

                //Show dialog
                builder.setTitle("UPDATE RECORD");
                builder.setMessage("Confirm to update record?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the gallery and select an image
                        et1 = transID2.getText().toString().trim();
                        et2 = expName2.getText().toString().trim();
                        et3 = expensesListCategory.getSelectedItem().toString().trim();
                        et4 = expAmount2.getText().toString().trim();
                        et5 = expUpdateDate.getText().toString().trim();

                        if (TextUtils.isEmpty(et2)) {
                            expName2.setError("Please enter");
                        } else if (TextUtils.isEmpty(et4)) {
                            expAmount2.setError("Please enter");
                        }

                        if (finalImageUrl1 != null && !finalImageUrl1.isEmpty()){
                            // calling a method to update our course.
                            // we are passing our object class, course name,
                            // course description and course duration from our edittext field.
                            //updateExpenses(expenses, et1, et2, et3, et4, expDate);

                            //get image selected by user to send it to firebase storage to save it
                            Bitmap bitmap = ((BitmapDrawable) ivInvoice.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            //Create a file name for the image
                            String imageInvoice = "invoice.jpg";
                            //Assign variable and a reference to the file
                            StorageReference imageRef = storage.getInstance().getReference("Invoice").child(mAuth.getCurrentUser().getUid()).child(transID).child(imageInvoice);

                            UploadTask uploadTask = imageRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Handle successful uploads
                                    //Get a reference to the uploaded image
                                    StorageReference fileRef = taskSnapshot.getStorage();

                                    // Get the download URL for the image
                                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageUrl = uri.toString();
                                            // Save the download URL to the database

                                            // Call the next method and pass the image URL as an argument
                                            updateExpenses(expenses, et1, et2, et3, et4, et5, imageUrl);


                                        }
                                    });
                                }
                            });
                        }else{
                            // Call the next method and pass the image URL as an argument
                            updateExpenses(expenses, et1, et2, et3, et4, et5);
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


    //Open camera to take picture
    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(UpdateExpenses.this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If user select camera
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivInvoice.setImageBitmap(imageBitmap);
            ivInvoice.setVisibility(View.VISIBLE);

            //User can click image to see full image
            ivInvoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int width = 500;
                    int height = 500;

                    Glide.with(UpdateExpenses.this)
                            .load(imageBitmap)
                            .override(width, height)
                            .into(ivInvoice);
                }
            });

            //If user select gallery
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            Glide.with(UpdateExpenses.this)
                    .load(selectedImage)
                    .into(ivInvoice);
            ivInvoice.setVisibility(View.VISIBLE);

            //User can click image to see full image
            ivInvoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int width = 500;
                    int height = 500;

                    Glide.with(UpdateExpenses.this)
                            .load(selectedImage)
                            .override(width, height)
                            .into(ivInvoice);
                }
            });
        }
    }

    //Open the gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);

    }

    //Have image
    public void updateExpenses(Expenses expenses, String et1, String et2, String et3, String et4, String et5, String imageUrl){

        Expenses updateExpenses = new Expenses(et1, et2, et3, et4, et5, imageUrl);
        //Update expenses
        databaseReference.child(expenses.getId()).setValue(updateExpenses).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(UpdateExpenses.this, "Success Update!", Toast.LENGTH_SHORT).show();
                    //Go back to transaction  activity
                    Intent home = new Intent(UpdateExpenses.this, HomeActivity.class);
                    startActivity(home);

                }else{
                    Toast.makeText(UpdateExpenses.this, "Failed to Update!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //No image
    public void updateExpenses(Expenses expenses, String et1, String et2, String et3, String et4, String finalExpensesDate){

        Expenses updateExpenses = new Expenses(et1, et2, et3, et4, finalExpensesDate);
        //Update expenses
        databaseReference.child(expenses.getId()).setValue(updateExpenses).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(UpdateExpenses.this, "Success Update!", Toast.LENGTH_SHORT).show();
                    //Go back to transaction  activity
                    Intent home = new Intent(UpdateExpenses.this, HomeActivity.class);
                    startActivity(home);

                }else{
                    Toast.makeText(UpdateExpenses.this, "Failed to Update!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteRecord(Expenses expenses, String imageUrl) {

        //create reference to database
        DatabaseReference DbRef = databaseReference.child(expenses.getId());
        //we referencing child here because we will be delete one record not whole data data in database
        //we will use generic Task here so lets do it..

        if (imageUrl != null && !imageUrl.isEmpty()) {
            //The image name we want to delete
            //Assign variable and a reference to the file
            StorageReference imageRef = storage.getInstance().getReferenceFromUrl(imageUrl);

            DbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(UpdateExpenses.this, "Success Delete!", Toast.LENGTH_SHORT).show();
                                Intent home = new Intent(UpdateExpenses.this, HomeActivity.class);
                                startActivity(home);

                            }
                        });
                    } else {
                        Toast.makeText(UpdateExpenses.this, "Failed Delete!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            DbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UpdateExpenses.this, "Success Delete!", Toast.LENGTH_SHORT).show();
                        Intent home = new Intent(UpdateExpenses.this, HomeActivity.class);
                        startActivity(home);

                    } else {
                        Toast.makeText(UpdateExpenses.this, "Failed Delete!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
        startActivity(new Intent(UpdateExpenses.this, ViewTransactionActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}