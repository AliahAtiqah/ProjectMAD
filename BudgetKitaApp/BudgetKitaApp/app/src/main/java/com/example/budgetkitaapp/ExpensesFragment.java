package com.example.budgetkitaapp;

import static android.app.Activity.RESULT_OK;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import androidx.fragment.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class ExpensesFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 3;
    static final int REQUEST_IMAGE_GALLERY = 4;
    private Button btnExpensesSave, btnInvoice;
    private Spinner expensesListCategory;
    private TextInputEditText editExpensesTransaction, editExpensesName, editExpensesTotal,editDateExpenses;
    private FirebaseAuth mAuth;
    FirebaseStorage storage;
    private ImageView ivInvoice;

    ArrayList<Uri> arrayList = new ArrayList<>();
    DatabaseReference expensesReference, transactionRef, invoiceReference;

    public ExpensesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_expenses, container, false);

        //Assign variable
        editExpensesTransaction = v.findViewById(R.id.expensesTransactionID);
        editExpensesName = v.findViewById(R.id.expensesName);
        expensesListCategory = v.findViewById(R.id.expensesCategory);
        editExpensesTotal = v.findViewById(R.id.totalExpenses);
        editDateExpenses = v.findViewById(R.id.btnExpensesPickDate);
        btnExpensesSave = v.findViewById(R.id.saveExpensesBtn);
        btnInvoice = v.findViewById(R.id.btnInvoice);

        //Set imageDrawable as null
        ivInvoice = v.findViewById(R.id.ivInvoice);
        ivInvoice.setImageDrawable(null);

        //Firebase authentication to identify the current user
        mAuth = FirebaseAuth.getInstance();

        //only the login user can see and edit their data
        //Reference when we want to insert data to firebase
        expensesReference = FirebaseDatabase.getInstance().getReference("Expenses").child(mAuth.getCurrentUser().getUid()).child("Expenses");

        // Generate a new, unique transaction ID
        transactionRef = FirebaseDatabase.getInstance().getReference("Expenses").child(mAuth.getCurrentUser().getUid()).child("Expenses").push();
        // Generate a new, unique transaction ID
        String transactionId = transactionRef.getKey();
        //set the unique id to layout
        editExpensesTransaction.setText(transactionId);
        //Disable the EditText for transaction ID so user cannot edit it
        editExpensesTransaction.setEnabled(false);

        //DROP-DOWN for spinner
        String[] expenses = {"Debt payment", "Giving debt", "Stock purchase","Donation" ,"Operating cost","Savings or investments","Personal expenses", "Other expenses"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, expenses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expensesListCategory.setAdapter(adapter);

        //DATE PICKER FUNCTION
        editDateExpenses.setOnClickListener(new View.OnClickListener() {
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
                                editDateExpenses.setText(date);

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

        //Button to take image for invoice
        btnInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

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

        //BTN SAVE EXPENSES
        btnExpensesSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertExpenses();
            }
        });
        return v;
    }


    public void insertExpenses() {

        //Get data from EditText
        String expensesID = editExpensesTransaction.getText().toString().trim();
        String expensesName = editExpensesName.getText().toString().trim();
        String expenseCategory = expensesListCategory.getSelectedItem().toString().trim();
        String expensesTotal = editExpensesTotal.getText().toString().trim();
        String expensesDate = editDateExpenses.getText().toString().trim();

        //Check if all field have been field
        //If not it will point to empty field
        if (expensesName.isEmpty()) {
            editExpensesName.setError("Expenses name is required!");
            editExpensesName.requestFocus();
            return;
        }

        if (expensesTotal.isEmpty()) {
            editExpensesTotal.setError("Amount is required!");
            editExpensesTotal.requestFocus();
            return;
        }

        String finalExpensesDate = null;
        //if image not null
        if (null == ivInvoice.getDrawable()) {
            //Imageview is null but user still can insert into database
            //Create new expenses to store into firebase

            finalExpensesDate = expensesDate;
            String finalExpensesDate1 = finalExpensesDate;

            saveToExpensesReference(expensesID, expensesName, expenseCategory, expensesTotal, finalExpensesDate1);

        } else {
            //get image selected by user to send it to firebase storage to save it
            Bitmap bitmap = ((BitmapDrawable) ivInvoice.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            //Create a  file name for the image
            String imageInvoice = "invoice.jpg";

            //Assign variable and a reference to the file
            StorageReference imageRef = storage.getInstance().getReference("Invoice").child(mAuth.getCurrentUser().getUid()).child(expensesID).child(imageInvoice);

            UploadTask uploadTask = imageRef.putBytes(data);
            finalExpensesDate = expensesDate;
            String finalExpensesDate1 = finalExpensesDate;
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
                            //expensesReference.child("imageUrl").setValue(imageUrl);

                            // Call the next method and pass the image URL as an argument
                            saveImageToExpensesReference(expensesID, expensesName, expenseCategory, expensesTotal, finalExpensesDate1, imageUrl);

                        }
                    });
                }
            });
        }
    }

    private void saveToExpensesReference(String expensesID, String expensesName, String expenseCategory, String expensesTotal, String finalExpensesDate1) {

        //Create new expenses to store into firebase
        Expenses expenses = new Expenses(expensesID, expensesName, expenseCategory, expensesTotal, finalExpensesDate1);

        expensesReference.push().setValue(expenses).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Go to home activity
        Intent home = new Intent(getActivity(), HomeActivity.class);
        startActivity(home);
    }


    // This is the method that will be called after the image URL has been retrieved
    private void saveImageToExpensesReference(String expensesID, String expensesName, String expenseCategory, String expensesTotal,String finalExpensesDate,String imageUrl) {

        //Create new expenses to store into firebase
        Expenses expenses = new Expenses(expensesID, expensesName, expenseCategory, expensesTotal, finalExpensesDate, imageUrl);

        expensesReference.push().setValue(expenses).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Go to home activity
        Intent home = new Intent(getActivity(), HomeActivity.class);
        startActivity(home);
    }

    //Open camera to take picture
    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If user select camera
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

                    Glide.with(getContext())
                            .load(imageBitmap)
                            .override(width, height)
                            .into(ivInvoice);
                }
            });

            //If user select gallery
        }else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            Glide.with(getContext())
                    .load(selectedImage)
                    .into(ivInvoice);
            ivInvoice.setVisibility(View.VISIBLE);

            //User can click image to see full image
            ivInvoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int width = 500;
                    int height = 500;

                    Glide.with(getContext())
                            .load(selectedImage)
                            .override(width,height)
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
}













