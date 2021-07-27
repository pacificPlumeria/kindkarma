package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.kindkarma.models.Comment;
import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.Request;
import edu.neu.madcourse.kindkarma.models.User;

public class CreateActivity extends AppCompatActivity {

    private final String TAG = "Create Activity";
    private final int PICK_PHOTO_CODE = 1234;
    private Uri photoUri;
    public ImageView imageView;
    public EditText etDescription;
    private User signedInUser = null;
    private String signedInUserId;
    FirebaseFirestore firestoreDb;
    StorageReference storageRef;
    StorageReference photoRef;
    Post post;
    Button btnSubmit;
    String downloadImageUrl;
    Spinner spinner;
    Object item;
    Bundle bundle;
    public String description;
    public String image_url;
    public Long creation_time_ms;
    public String postId, posterId;
    //public Parcelable[] comments;
    HashMap<String, Object> dataPost = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        imageView = (ImageView)findViewById(R.id.imageView);
        etDescription = (EditText)findViewById(R.id.etDescription);
        firestoreDb = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        spinner = (Spinner)findViewById(R.id.spinner);

        getSupportActionBar().setTitle("Create Post");

        bundle = getIntent().getExtras();

        if (bundle != null) {

            description = bundle.getString("POST_DESCRIPTION");
            creation_time_ms = bundle.getLong("POST_CREATIONTIMEMS");
            image_url = bundle.getString("POST_IMAGEURL");
            posterId = bundle.getString("POST_POSTERID");
            postId = bundle.getString("POST_POSTID");
            //comments = bundle.getParcelableArray("POST_COMMENTS");
            //photoUri = Uri.parse(image_url);

            post = new Post(description,image_url,creation_time_ms,posterId,postId);
            if(image_url != null) {
                GlideApp.with(this).load(image_url).into(imageView);
            }
            etDescription.setText(description);

        //is it only this post we allow to update so should the spinner not be allowed to update

        }

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        // get the signed in user object from the database
        firestoreDb.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener((userSnapshot) -> {
            signedInUser = userSnapshot.toObject(User.class);
            Log.i(TAG, "Signed In User" + signedInUser);
        }).addOnFailureListener((exception) -> {
            Log.i(TAG, "Failure fetching signed in user" + exception);
        });

        // get the uid of the signed in user
        signedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a spinner that contains all of the signed in user's fulfilled request titles
        List<String> fulfilledRequests = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, fulfilledRequests);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        firestoreDb.collection("users").document(signedInUserId).collection("allFulfilledRequests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String requestTitle = document.getString("fulfilledRequestTitle");
                        fulfilledRequests.add(requestTitle);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        // get the selected item
        item = spinner.getSelectedItem();
    }

    //https://stackoverflow.com/questions/39884661/notify-user-that-the-user-is-not-connected-to-internet-at-any-screen
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void askNetwork() {
        // below line is use to request
        // permission in the current activity.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet");
        builder.setMessage("Please connect to your internet");
        builder.setPositiveButton("Wifi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // below is the intent from which we are redirecting our user.
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));

            }
        });
        builder.setNegativeButton("Mobile Data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity"));
                dialog.cancel();
                startActivity(intent);
            }
        });
        builder.show();

    }


    //https://androidbitmaps.blogspot.com/2015/04/loading-images-in-android-part-iii-pick.html
    public void pickImage() {
        Log.i(TAG, "Open up image picker on device");
        // open any application that handles this intent and we want it to provide us with images
        Intent imagePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imagePickerIntent.setType("image/*");
        // check if application that can handle intent exists
        if (imagePickerIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE);
        }
    }

    public void onSubmit(View view){
        item = spinner.getSelectedItem();
        // if the description is empty, show error toast
        if (etDescription.getText().toString().isEmpty()){
            Log.i("hello", "item empty");
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // if the signed in user is null, show error toast
        if (signedInUser == null){
            Toast.makeText(this, "No signed in user, please wait", Toast.LENGTH_SHORT).show();
            return;
        }
        // if no fulfilled request is chosen in the spinner, display an error
        if (item == null || item.toString().isEmpty()){
            Toast.makeText(this, "No Fulfilled Request Chosen", Toast.LENGTH_SHORT).show();
            return;
        }
        save();
    }

    public void updateProfileImage(View view) {
        requestImagePermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // user has picked image
                // photoUri is location of photo that was selected
                assert data != null;
                photoUri = data.getData();
                // set the image uri of the imageView
                imageView.setImageURI(photoUri);

            } else {
                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // sets up likes for post
    public void setLikes() {

        // gets the post that was just created
        Query query = firestoreDb.collection("posts").whereEqualTo("creation_time_ms",post.getCreation_time_ms()).whereEqualTo("posterId",signedInUserId);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentSnapshot documentSnapshot: documentSnapshots) {

                    // gets the document id for the newly created post
                    String postId = documentSnapshot.getId();

                    // sets the id in the post item object
                    post.setPostId(postId);

                    // adds the postId field to the firestore document
                    HashMap<String, Object> dataPostId = new HashMap<>();
                    dataPostId.put("postId", postId);
                    documentSnapshot.getReference().update(dataPostId);

                    // adds an unlike for the current user for this post
                    HashMap<String, Object> dataUnlikedUser = new HashMap<>();
                    dataUnlikedUser.put("value",false);
                    firestoreDb.collection("likes").document(postId).collection("userLiked").document(signedInUserId).set(dataUnlikedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
                }
            }
        });
    }


    //  https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
//https://www.geeksforgeeks.org/easy-runtime-permissions-in-android-with-dexter/
    private void requestImagePermissions() {
        // below line is use to request
        // permission in the current activity.
        Dexter.withContext(CreateActivity.this)
                // below line is use to request the number of permissions which are required in our app.
                .withPermission(
                        // below is the list of permissions
                        Manifest.permission.READ_EXTERNAL_STORAGE
                        //Manifest.permission.CAMERA

                )
                // after adding permissions we are calling an with listener method.
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        pickImage();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        showSettingsDialog();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }

                }).check();
    }

    // below is the shoe setting dialog method which is use to display a dialogue message.
    private void showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateActivity.this);

        // below line is the title
        // for our alert dialog.
        builder.setTitle("Need Permissions");
        builder.setMessage("To update your image we need this permission");
        //        }
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called on click on positive button and on clicking shit button we
                // are redirecting our user from our app to the settings page of our app.
                dialog.cancel();
                // below is the intent from which we are redirecting our user.
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called when
                // user click on negative button.
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void save() {
        dataPost.put("creation_time_ms",System.currentTimeMillis());
        dataPost.put("description",etDescription.getText().toString());
        dataPost.put("postId",postId);
        dataPost.put("posterId",signedInUserId);
        Log.i("data post", dataPost.toString());
        // if no photo was selected, pass in null for the photo uri
        if (photoUri == null){
            // create a post object
            if (image_url == null) {
                post = new Post(etDescription.getText().toString(), null, System.currentTimeMillis(), signedInUserId);
            } else {
                post = new Post(etDescription.getText().toString(), image_url, System.currentTimeMillis(), signedInUserId);
            }
            // add the post to the database
            if (bundle == null) {
                firestoreDb.collection("posts").add(post);
            } else {
                dataPost.put("image_url",image_url);
                updatePost();
            }
            setLikes();
            // re-enable the submit button
            btnSubmit.setEnabled(true);
            // clear the description edit text
            etDescription.getText().clear();
            // display a success message
            Toast.makeText(CreateActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CreateActivity.this, FeedActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // disable the submit button
        btnSubmit.setEnabled(false);
        // where photo is stored
        // Use Tasks API to handle chained async operations
        photoRef = storageRef.child("images/" + System.currentTimeMillis() + "-photo.jpg");
        photoRef.putFile(photoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            String download_url = uri.toString();
                            Log.i("url",download_url);
                            // Create post object with the image url
                            post = new Post(etDescription.getText().toString(), download_url, System.currentTimeMillis(), signedInUserId);
                            setLikes();
                            // add the post to the database
                            if (bundle == null) {
                                firestoreDb.collection("posts").add(post);
                            } else {
                                dataPost.put("image_url",download_url);
                                updatePost();
                            }
                            // re-enable the submit button
                            btnSubmit.setEnabled(true);
                            // clear the description edit text
                            etDescription.getText().clear();
                            // clear the imageView
                            imageView.setImageResource(0);
                            // display a success message
                            Toast.makeText(CreateActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreateActivity.this, FeedActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }else{
                    Toast.makeText(CreateActivity.this, "Error happened during the upload process", Toast.LENGTH_LONG ).show();
                }
            }
        });
    }

    public void updatePost() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("data post in update", dataPost.toString());
                firestoreDb.collection("posts").document(postId).update(dataPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Also add request to firebase
                        //saveUpdatesToRealtime();
                        Log.i("post updated ", postId);
                        Toast.makeText(CreateActivity.this, "Post updated", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to create post" + e.toString());
                        Toast.makeText(CreateActivity.this, "Post failed, please try again later",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}