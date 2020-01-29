package bd.com.dev.pocketcash.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ActivityUpdateProfileBinding;
import bd.com.dev.pocketcash.model.ReferUser;
import bd.com.dev.pocketcash.utills.CustomProgressDialog;
import bd.com.dev.pocketcash.utills.EmailMatcher;
import bd.com.dev.pocketcash.utills.Status;

public class UpdateProfileActivity extends AppCompatActivity {


    private static final int REQUEST_SELECT_PHOTO = 2;
    private ActivityUpdateProfileBinding binding;
    private ProgressDialog progressDialog;

    private String imageUrl;
    private String userId;
    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_profile);
        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getCurrentUser().getUid();

    }

    public void openGallery(View view) {
        Intent intent_gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent_gallery, REQUEST_SELECT_PHOTO);
    }

    public void updateProfile(View view) {
        String name = binding.nameET.getText().toString();
        String email = binding.emailET.getText().toString();
        String referralCode = binding.referralCodeET.getText().toString();

        if (name.trim().isEmpty() | name.trim().equals("")) {
            binding.nameET.setError("Enter your name.");
            binding.nameET.requestFocus();
        } else if (email.trim().isEmpty() || EmailMatcher.validate(email) == false) {
            binding.emailET.setError("Enter a valid E-mail.");
            binding.emailET.requestFocus();
        } else if (imageUrl == null || imageUrl.equals("") || imageUrl.isEmpty()) {
            Toast.makeText(this, "Select a profile picture.", Toast.LENGTH_LONG).show();
        } else if (!referralCode.trim().isEmpty() && !referralCode.trim().equals("")) {
            binding.updateProfileBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            matchReferralCode(name, email, referralCode);
        } else {
            binding.updateProfileBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            saveToDB(name, email);
        }
    }

    private void matchReferralCode(final String name, final String email, final String referralCode) {
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("users");
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    boolean isMatch = false;
                    String cUserId = "";

                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                        if (data.child("userInfo").child("userId").exists() && data.child("userInfo").child("referralCode").exists()) {
                            cUserId = data.child("userInfo").child("userId").getValue().toString();
                            String cReferralCode = data.child("userInfo").child("referralCode").getValue().toString();
                            if (!cUserId.equals(userId)) {
                                if (cReferralCode.equals(referralCode)) {
                                    isMatch = true;
                                    break;
                                }
                            }

                        }
                    }
                    if (isMatch == true) {
                        saveToDB(name, email);
                        if (!cUserId.equals("")) {
                            DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("sourceUser");
                            Map<String,Object> usermap = new HashMap<>();
                            usermap.put("sourceUserId",cUserId);
                            usermap.put("date",System.currentTimeMillis());
                            usermap.put("status",Status.PENDING);
                            userDB.setValue(usermap);

                            DatabaseReference cUserDB = FirebaseDatabase.getInstance().getReference().child("users").child(cUserId).child("referUsers").child(userId);
                            cUserDB.setValue(new ReferUser(userId, name, Status.PENDING, System.currentTimeMillis()));
                        }
                    } else {
                        binding.updateProfileBtn.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(UpdateProfileActivity.this, "Invalid referral code.", Toast.LENGTH_LONG).show();
                    }

                } else {

                    binding.updateProfileBtn.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveToDB(String name, String email) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("photoUrl", imageUrl);
        userMap.put("userStatus", Status.ACTIVE);
        userMap.put("referralCode", name.replaceAll("\\s+", "").toLowerCase() + new Random().nextInt(50) + 1);

        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("userInfo");
        userDB.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(UpdateProfileActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    binding.updateProfileBtn.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {
            progressDialog = createCustomProgressDialog();

            Uri uri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                saveImage(bitmap);
            }
        }
    }

    private void saveImage(Bitmap bitmap) {

        binding.userProfileImageID.setImageBitmap(bitmap);

        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImage").child(imageUrlMaker());
        filepath.putBytes(convertToByte(bitmap)).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUrl = uri.toString();
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });

    }


    public String imageUrlMaker() {
        long time = System.currentTimeMillis();
        String millis = Long.toString(time);
        String url = millis;
        return url;
    }

    private byte[] convertToByte(Bitmap bitmap) {
        byte[] byteImage;

        float originalWidth = bitmap.getWidth();
        float originalHeight = bitmap.getHeight();
        if (originalWidth > 1200 && originalHeight >= originalWidth) {

            float destWidth = 1200;
            float destHeight = originalHeight / (originalWidth / destWidth);
            Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, Math.round(destWidth), Math.round(destHeight), false);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, baos1);
            byteImage = baos1.toByteArray();

        } else if (originalWidth > 1200 && originalHeight < originalWidth) {
            float destWidth = 1400;
            float destHeight = originalHeight / (originalWidth / destWidth);
            Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, Math.round(destWidth), Math.round(destHeight), false);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, baos1);
            byteImage = baos1.toByteArray();

        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); //decodedFoodBitmap is the bitmap object
            byteImage = baos.toByteArray();

        }

        return byteImage;
    }


    private ProgressDialog createCustomProgressDialog() {
        ProgressDialog progressDialog = CustomProgressDialog.createProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.isIndeterminate();
        return progressDialog;
    }
}
