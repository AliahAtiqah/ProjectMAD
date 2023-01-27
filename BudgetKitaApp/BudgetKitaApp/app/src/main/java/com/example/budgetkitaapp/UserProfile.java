package com.example.budgetkitaapp;

import static com.example.budgetkitaapp.ExpensesFragment.REQUEST_IMAGE_CAPTURE;
import static com.example.budgetkitaapp.ExpensesFragment.REQUEST_IMAGE_GALLERY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UserProfile extends AppCompatActivity {//coding for UserProfile


    //Initialize variable
    private TextInputEditText editAccCompany, editUsername, editAccPhoneNo, editAccEmail;
    private Button btnSave;
    private ImageView ivStore;
    FirebaseStorage storage;
    private FirebaseAuth mAuth;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setTitle("Profile");
        //Firebase authentication to identify the current user
        mAuth = FirebaseAuth.getInstance();

        // Reference to an image file in Firebase Storage
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getInstance().getReference("Business Images").child(mAuth.getCurrentUser().getUid()).child("Business_Image.jpg");

        // Assign variable
        ivStore = findViewById(R.id.imageStore);

        //Download the image from the Firebase Storage
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            //If success load the picture
            public void onSuccess(Uri uri) {
                Glide.with(UserProfile.this).load(uri).into(ivStore);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            //If there no picture in database load default
            public void onFailure(@NonNull Exception exception) {
                ivStore.setImageResource(R.drawable.storeicon);
            }
        });

        //When user click the imageview, it will show option to change picture
        ivStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);

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

        //Firebase authentication to identify user so only login user can see their information
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            //Retrieve business name from firebase
            DatabaseReference BusinessRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            BusinessRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String businessName = snapshot.child("company").getValue(String.class);

                    //Assign variable
                    editAccCompany = (TextInputEditText) findViewById(R.id.editAccCompany);
                    //Set the business name with the data from firebase
                    editAccCompany.setText(businessName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Retrieve username from firebase
            DatabaseReference UsernameRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            UsernameRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String username = snapshot.child("username").getValue(String.class);

                    //Assign variable
                    editUsername = (TextInputEditText) findViewById(R.id.editAccUsername);
                    //Set the username with the data from firebase
                    editUsername.setText(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Retrieve phone number from firebase
            DatabaseReference PhoneRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            PhoneRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String phone = snapshot.child("phone").getValue(String.class);

                    //Assign variable
                    editAccPhoneNo = (TextInputEditText) findViewById(R.id.editAccPhoneNo);
                    //Set the phone with the data from firebase
                    editAccPhoneNo.setText(phone);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Retrieve email from firebase
            DatabaseReference EmailRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            EmailRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String email = snapshot.child("email").getValue(String.class);

                    //Assign variable
                    editAccEmail = (TextInputEditText) findViewById(R.id.editAccEmail);
                    //Set the email with the data from firebase
                    editAccEmail.setText(email);
                    //Disable the EditText for email so user cannot edit it
                    editAccEmail.setEnabled(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Assign variable
            btnSave = (Button) findViewById(R.id.btnSave);

            //return to home screen & save the data data
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Get the data as strings
                    String user = editUsername.getText().toString();
                    String phone = editAccPhoneNo.getText().toString();
                    String company = editAccCompany.getText().toString();

                    //check if box empty
                    if (user.isEmpty() ||  phone.isEmpty() || company.isEmpty())
                        Toast.makeText(UserProfile.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    else {

                        //Firebase authentication to identify the current user
                        mAuth = FirebaseAuth.getInstance();

                        //Overwrite already exist data for Business name, Username, Phone number even there no change to it
                        DatabaseReference businessRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("company");
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("username");
                        DatabaseReference phoneRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("phone");

                        //Overwrite for Business name
                        businessRef.setValue(company);
                        //Overwrite for username
                        userRef.setValue(user);
                        //Overwrite for phone
                        phoneRef.setValue(phone);


                        //get image selected by user to send it to firebase storage to save it
                        Bitmap bitmap = ((BitmapDrawable) ivStore.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();


                        //Assign variable and location in firebase storage
                        StorageReference imagesRef = storage.getInstance().getReference("Business Images").child(mAuth.getCurrentUser().getUid());
                        //Name of the files when we save it to storage
                        StorageReference imageRef = imagesRef.child("Business_Image.jpg");

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
                            }
                        });

                        //go back to Home activity
                        Intent userProfile = new Intent(UserProfile.this, HomeActivity.class);
                        Toast.makeText(UserProfile.this, "Success Update", Toast.LENGTH_SHORT).show();
                        startActivity(userProfile);
                    }

                }
            });

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void into(ImageView ivStore) {
    }

    //Open camera to take picture
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Convert the picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivStore.setImageBitmap(imageBitmap);
        }
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                ivStore.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Open the gallery
    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);

    }

    public boolean onCreateOptionsMenu (Menu menu){
        return true;
    }

    //Display the arrow on top to go back to previous activity
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
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(UserProfile.this, HomeActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}





