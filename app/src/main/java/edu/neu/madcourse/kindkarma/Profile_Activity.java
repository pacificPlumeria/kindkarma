

package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.widget.TooltipCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.textclassifier.ConversationActions;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import edu.neu.madcourse.kindkarma.models.Community;
import edu.neu.madcourse.kindkarma.models.User;


//implements LocationListener


public class Profile_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private String currentUser;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    // Please add the server key from your firebase console in the follwoing format "key=<serverKey>"
    private static final String SERVER_KEY = "key=AAAAQq6C_Wo:APA91bF-83JCqjVbED5GJ2zfC5xKJeQ5clZNbMz8fWM8x3jVEjjHcDFzEse1Da0dkjrClUajd-IwnUgVCQIuMK4Rjupt7vnRbqiRRAMvZ8XQgLSEQO44E4K_Eitoa4rBrnt_0ZTRyNZF";

    // This is the client registration token
    //private static final String CLIENT_REGISTRATION_TOKEN = "dW584Ab0JoY:APA91bGv2dltU6sR5_cM3ii1Y5iQVaxMdk4Mn7b7OPtsIhssBmNzpxk9t9kB-iKziXUic1z2TGPYJgbGSu0aGLyA3d4cwsyj3IRJr4pzEXT6TiGmFXF3BllxAnquEEgTPFZ0ffDU6KYO";
    private String CLIENT_REGISTRATION_TOKEN;

    //https://stackoverflow.com/questions/1748977/making-textview-scrollable-on-android
    //https://stackoverflow.com/questions/3013791/live-character-count-for-edittext

    //https://ziptasticapi.com/32825
    //https://www.zipcodeapi.com/API

    //https://github.com/mahalkarshubham/CurrentLocation-Country-State-City-PIN-Address/blob/master/app/src/main/java/com/shubham/location/MainActivity.java

    // UI
    private TextView community,aboutSection,username,userHours;

    public ImageView profilePhoto , helpSkills, helpCommunity;
    public ImageButton save;
    public String topicCommunity = "Kind Karma";

    //Database
    FirebaseFirestore firestoreDb;
    // Create a Cloud Storage reference from the app
    StorageReference storageRef;
    private String signedInUserUID;

    private Location location; // gives geographic location. It contains latitude and longitude etc.
    protected LocationManager locationManager;
    private static final int REQUEST_LOCATION = 1;
    private static final int LOCATION_REQUEST_CODE = 101;
    StorageReference photoRef;
    private Uri photoUri;

    private User signedInUser;
    private HashMap<String, String> skills;
    private TextView[] editTextCatArray;
    HashMap<String, Object> userUpdates = new HashMap<>();
    private final int PICK_PHOTO_CODE = 1234;
    private final int MAX_CHAR_COUNT = 100;
    private final String TAG = "UserProfile";

    private Community usersCommunity;
    private boolean onlineCommunity = false;


    Toolbar toolbar;
    TextView signedInUserName, signedInUserNameCommunity;
    ImageView signedInUserImageHeader;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

//       outState.putString("about",aboutSection.getText().toString());
//        outState.putString("community" , community.getText().toString());
//        outState.putParcelable("photo",photoUri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);

        // UI of users profile
        profilePhoto = findViewById(R.id.profileImage);
        aboutSection = findViewById(R.id.aboutYouEditable);
        username = findViewById(R.id.username);
        userHours = findViewById(R.id.userHours);
        save = findViewById(R.id.saveButton);
        save.setEnabled(false);

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        if(savedInstanceState!=null){
           if(savedInstanceState.getString("about") != null){
            aboutSection.setText( savedInstanceState.getString("about"));
           }
           if(savedInstanceState.getString("community") !=null){
            community.setText(savedInstanceState.getString("community"));
           }
            if(savedInstanceState.getParcelable("photo") !=null){
                profilePhoto.setImageURI(savedInstanceState.getParcelable("photo"));
            }


        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get token from instance ID
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                CLIENT_REGISTRATION_TOKEN = instanceIdResult.getToken();
                String msg = getString(R.string.msg_token_fmt, CLIENT_REGISTRATION_TOKEN);
                Log.e("Token",CLIENT_REGISTRATION_TOKEN);
                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        community = findViewById(R.id.usersCommunity);
        editTextCatArray = new TextView[]{
                findViewById(R.id.skill1),
                findViewById(R.id.skill2),
                findViewById(R.id.skill3),
                findViewById(R.id.skill4),
                findViewById(R.id.skill5),
                findViewById(R.id.skill6),
                findViewById(R.id.skill7),
                findViewById(R.id.skill8),
                findViewById(R.id.skill9),
                findViewById(R.id.skill10)};

        for(int i = 0; i<editTextCatArray.length;i++){
            editTextCatArray[i].setText("");
        }

        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutProfile);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_profile);
        navigationView.setNavigationItemSelectedListener(Profile_Activity.this);

        signedInUserImageHeader = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageUserNameHeader);
        signedInUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.personName);
        signedInUserNameCommunity = (TextView) navigationView.getHeaderView(0).findViewById(R.id.communityName);

        // get the signed in user object from the database
        firestoreDb.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener((userSnapshot) -> {
            signedInUser = userSnapshot.toObject(User.class);
            GlideApp.with(Profile_Activity.this).load(signedInUser.getProfileImage()).into(signedInUserImageHeader);
            signedInUserNameCommunity.setText(signedInUser.getCommunity());
            signedInUserName.setText(signedInUser.getUsername());
            Log.i(TAG, "Signed In User" + signedInUser);
        }).addOnFailureListener((exception) -> {
            Log.i(TAG, "Failure fetching signed in user" + exception);
        });

        // get the user id of the logged in user
        signedInUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // get the reference for the storage portion of the db -> for images
        storageRef = FirebaseStorage.getInstance().getReference();

        DocumentReference docRef = firestoreDb.collection("users").document(signedInUserUID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.i("hi", "hi");
            }

            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                //snapshot should be the signed in user it will be a long data structure so
                // putting it in a User objects makes getting and manipluating without going back
                // the db easier
                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, source + " data: " + snapshot.getData());
                    signedInUser = snapshot.toObject(User.class);

                    String check = signedInUser.getProfileImage();
                    //check to make sure that user profile photo is not null
                    if (check == null || check.isEmpty() || check.length() < 2) {
                        String urlTemp = getProfileImageUrl(signedInUser.getUsername());
                        GlideApp.with(Profile_Activity.this).load(urlTemp).into(profilePhoto);
                        signedInUser.setProfileImage(urlTemp);
                        userUpdates.put("profileImage", urlTemp);

                    } else { //load the users profile image
                        GlideApp.with(Profile_Activity.this).load(signedInUser.getProfileImage()).into(profilePhoto);
                    }
                    //   Picasso.get().load(snapshot.getString("profilePicture")).into(profilePhoto);
                    //setting the UI
                    username.setText(signedInUser.getUsername());
                    aboutSection.setText(signedInUser.getAbout());
                    community.setText(String.valueOf(signedInUser.getCommunity()));
                    userHours.setText(String.valueOf(signedInUser.getTotalHoursWorked()));
                    skills = signedInUser.getCategories();
                    int count = 0;
                    //key set is the place it is located in the hashmap, so we need the value

                    // which is the actual skill/user categories
                    for (String i : skills.keySet()) {
                        //get the value
                        editTextCatArray[count].setText(skills.get(i));
                        count++;
                    }
                    if(count > 0){
                        save.setEnabled(true);
                    }
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });


        // question icon
        helpSkills = (ImageView) findViewById(R.id.helpSkills);
        TooltipCompat.setTooltipText(helpSkills, "It is required to have at least 1 skill in " +
                "order to get the most out of Kind Karma");

        helpCommunity = (ImageView) findViewById(R.id.helpCommunity);
        TooltipCompat.setTooltipText(helpCommunity, " Kind Karma users who do not join a community will be " +
                "automatically enrolled in online group.");

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



    /*To save the state information override onSaveInstanceState() method and add key-value pairs
    to the Bundle object that is saved in the event that your activity is destroyed unexpectedly.
    This method gets called before onStop().*/
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("about",aboutSection.getText().toString());
        outState.putString("community" , community.getText().toString());
        outState.putParcelable("photo",photoUri);

    }



    // show menu posts and profile in actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_posts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // tells us which item the user selected in the menu
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_requests){
            Intent intent = new Intent(this, RequestsActivity.class);
            intent.putExtra("EXTRA_USERID",signedInUserUID);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, Profile_Activity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_communities) {
            Intent intent = new Intent(this, CommunitiesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_messages) {
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
            // If logout option is chosen, sign out user and go to login activity
        } else if (id == R.id.nav_signout) {
            // Logout the user
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.nav_feed) {
            // Logout the user
            Intent intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutProfile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onClickHelpSkills(View view){
        view.performLongClick();
    }

    public void onClickHelpCommunity(View view){
        view.performLongClick();
    }



        public void saveUpdatesToDatabase(View view) {
             if(community.getText().toString().equals("")) {
                 onlineCommunity = true;
                 userUpdates.put("community", "Kind Karma");
                 userUpdates.put("state", "Online");
                 userUpdates.put("zipcode", 0);
                 userUpdates.put("city", "Online");
                 community.setText("Kind Karma");
                 updateCommunity();
             }
            //sendMessageToNews(view, community.getText().toString());
            subscribeToChannels();
            //sendMessageToNews();
            userUpdates.put("token" , CLIENT_REGISTRATION_TOKEN);
            firestoreDb.collection("users").document(signedInUserUID).update(userUpdates);
            Toast.makeText(Profile_Activity.this, "Saved to database", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
        }


        /**
         * when the user wants to edit/update their summary/about section, it pops up as a dialog and
         * count characters to limit the amount one can write, this updates as the user types via
         * textwatcher
         */
        public void editAboutSection(View view) {
            final EditText edittext = new EditText(Profile_Activity.this);
            final TextView tv = new TextView(Profile_Activity.this);
            tv.setGravity(Gravity.RIGHT);

            //Set Length filter Restricting to max_char_count
            edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHAR_COUNT)});

            // if there is already something in the about section that can be editted
            String check = signedInUser.getAbout();
            if (check != null || check.length() != 0) {
                //place already saved data into the edit text and update the char count
                edittext.setText(check);
                tv.setText(edittext.length() + "/" + MAX_CHAR_COUNT);
            } else {
                edittext.setHint(" type here ");
            }
            //https://stackoverflow.com/questions/55684053/edittext-in-alert-dialog-android
            // allows the live character count to display
            final TextWatcher mTextEditorWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    tv.setText(s.length() + "/" + MAX_CHAR_COUNT);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
            //set what needs to have the textwatcher watching for the character count in this case
            edittext.addTextChangedListener(mTextEditorWatcher);
            // Alert popup so user can edit text
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Title")
                    .setMessage("Message");

            //layout of the dialog that will display the edit about section dialog
            LinearLayout alertLayout = new LinearLayout(this);
            alertLayout.setOrientation(LinearLayout.VERTICAL);
            alertLayout.addView(edittext);
            alertLayout.addView(tv);
            builder.setView(alertLayout);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String editTextInput = edittext.getText().toString();

                    Log.d("onclick", "editext value is: " + editTextInput);
                    //updating the textview and adding to the map to be saved to db later
                    aboutSection = (TextView) findViewById(R.id.aboutYouEditable);
                    aboutSection.setText(editTextInput);
                    signedInUser.setAbout(editTextInput);
                    userUpdates.put("about", signedInUser.getAbout());
                }
            })
                    .setNegativeButton("Cancel", null)
                    .create();
            builder.show();
        }


        public void updateProfileImage(View view) {
            requestImagePermissions();
        }


    //  https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
//https://www.geeksforgeeks.org/easy-runtime-permissions-in-android-with-dexter/
    private void requestImagePermissions() {
        // below line is use to request
        // permission in the current activity.
        Dexter.withContext(Profile_Activity.this)
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
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile_Activity.this);

        // below line is the title
        // for our alert dialog.
        builder.setTitle("Need Permissions");
//            if(messageType == 1){
//                builder.setMessage("To update your image from files we need this permission");
//            }
//            else if(messageType == 2){
//                builder.setMessage("To use your camera we need this permission");
//            }
//            else if(messageType == 3) {
        // below line is our message for our dialog
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
        // below line is used
        // to display our dialog
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

        //        public void cameraShot(){
//            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(cameraIntent, MY_CAMERA_PERMISSION_CODE);
//        }
        public void save() {
            // if no photo was selected, pass in null for the photo uri
            if (photoUri == null) {
                // create a post object
                getProfileImageUrl(signedInUserUID);
                finish();
                return;
            }
            // where photo is stored
            // Use Tasks API to handle chained async operations
            photoRef = storageRef.child("profile/" + System.currentTimeMillis() + "-photo.jpg");
            Log.i("photoRef**********", String.valueOf(photoRef));
            photoRef.putFile(photoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url = uri.toString();
                                Log.i("url", download_url);
                                // Add the url from storage to the logged in users document in the
                                // firestore db
                                firestoreDb.collection("users").document(signedInUserUID).update("profileImage",
                                        download_url);
                                userUpdates.put("profileImage", download_url);
                                signedInUser.setProfileImage(download_url);

                                // display a success message
                                Toast.makeText(Profile_Activity.this, "Success!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Profile_Activity.this, "Error happened during the upload process",
                                Toast.LENGTH_LONG).show();
                    }
                    //subscribeToChannels();
                }
            });
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
                    save();
                    // set the image uri of the imageView
                    profilePhoto.setImageURI(photoUri);

                } else {
                    Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show();
                }
            }if(requestCode == 101){
                getUserLocation();
            }
        }


        // uses MD5Util class which takes in the username to generate a hash which is added to a gravatar url
        // this url is used to assign a random profile pic to a user
        public String getProfileImageUrl(String userName) {
            String hash = MD5Util.md5Hex(userName);
            //signedInUser.setProfileImage("https://www.gravatar.com/avatar/" + hash + "?d=identic ");
            return "https://www.gravatar.com/avatar/" + hash + "?d=identicon";
        }

    public String getColorCode(String community)
    {
        // uses MD5Util class which takes in the username to generate a hash which then takesx
        // the first 6 characters for a hex color
        String hash = MD5Util.md5Hex(community);
        return "#"+hash.substring(0,6).toLowerCase();
    }


        /**
         * checks the permission to see if the user has allowed access to their location if not will
         * display an alertdialog with details why we are asking for the permission. If user agrees
         * next is to check the phones connections if that is okay we call the next function to get
         * the actual location
         */

        public void communityUpdate(View view) {
                requestLocationPermissions();
        }


        //  https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
        //https://www.geeksforgeeks.org/easy-runtime-permissions-in-android-with-dexter/
        private void requestLocationPermissions() {
            // below line is use to request permission in the current activity.
            Dexter.withContext(Profile_Activity.this)
                    // below line is use to request the number of permissions which are required in our app.
                    .withPermissions(
                            // below is the list of permissions
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION

                    )
                    // after adding permissions we are calling an with listener method.
                    .withListener(new MultiplePermissionsListener() {

                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            // this method is called when all permissions are granted
                            String perm1 =  multiplePermissionsReport.getGrantedPermissionResponses().get(0).getPermissionName();
                            Log.i("prem1******" , perm1);
                            if (perm1.equals("android.permission.ACCESS_FINE_LOCATION") || perm1.equals("android.permission.ACCESS_COARSE_LOCATION") ) {
                                // do you work now
                                Toast.makeText(Profile_Activity.this, "Getting your community",
                                        Toast.LENGTH_SHORT).show();
                                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                getUserLocation();
                            }

                            else   {
                                // permission is denied permanently, we will show user a dialog
                                // message.
                                showLocationSettingsDialog();
                            }
                        }
                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            // this method is called when user grants some permission and denies some of them.
                            permissionToken.continuePermissionRequest();
                        }
                    }).withErrorListener(new PermissionRequestErrorListener() {
                // this method is use to handle error in runtime permissions
                @Override
                public void onError(DexterError error) {
                    // we are displaying a toast message for error message.
                    Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                }
            })
                    // below line is use to run the permissions on same thread and to check the permissions
                    .onSameThread().check();
        }



    // below is the show setting dialog method which is use to display a

    // dialogue message.
    private void showLocationSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile_Activity.this);

        // below line is the title for our alert dialog.
        builder.setTitle("Need Permissions");

        // below line is our message for our dialog
        builder.setMessage("This app needs location permission to access the community " +
                "feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called on click on positive button and on clicking stop
                // button we are redirecting our user from our app to the settings page of our app.
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
                // this method is called when user click on negative button.
                dialog.cancel();
            }
        });
//            builder.setNegativeButton("Enter zipcode", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//
//
//                }
//            });
        // below line is used to display our dialog
        builder.show();
    }


//    public void removeCommunityDialog(){
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(Profile_Activity.this);
//                builder.setTitle("Edit Community");
//                builder.setMessage("Do you want to update your local community or join the online" +
//                        "community");
//                builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        onlineCommunity = false;
//
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // this method is called when user click on negative button.
//                        dialog.cancel();
//                    }
//                });
//                builder.setNegativeButton("ONLINE", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        onlineCommunity = true;
//                        removeCommunity();
//                        updateCommunity();
//
//                    }
//                });
//                builder.show();
//                }
//            }).start();
//            }
//



        private void getUserLocation() {
            Log.i("**** community",signedInUser.getCommunity());

            //cant be null as we will remove their current community
//            if(signedInUser.getCommunity() != null) {
//                removeCommunityDialog();
//            }

            boolean locationFound = false;
            if (locationManager != null) {
                @SuppressLint("MissingPermission") Location networkLocation =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (networkLocation != null) {
                    getLocation();
                    //set the boolean to used so we know we got the users location already
                    locationFound = true;
                }
                //try the gps location if network location fails
                if (!locationFound) {
                    @SuppressLint("MissingPermission") Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //Taking location and loading to UI so viewer can see
                    if (locationGPS != null) {
                        getLocation();
                    }
                    // couldn't find location either through the network or the gps
                    else {
                        Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }



        /**
         * Running on a new thread so its not bogging down the main/UI thread. This tries to get the
         * users last know location, then using Geocoder library we get the approximate location
         * (country,state,county,city,zipcode,address,etc.) via the Address library.
         */

          private void getLocation() {
              Log.i("**** getlocation",signedInUser.getCommunity());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                          //  community.setText(addresses.get(0).getSubAdminArea());

                            //if the user updates their community
                            Log.i(" signedin user.getcommunity ",signedInUser.getCommunity());
                            Log.i("gotten location", addresses.get(0).getSubAdminArea());
                            if (!signedInUser.getCommunity().equals(addresses.get(0).getSubAdminArea())) {
                                // Subtract the users hours from the past community, subtract one
                                // from the member,  then add those users hours to the new community
                                // and add one member
                                if (!signedInUser.getCommunity().equals("")) {
                                    removeCommunity();
                                }
                            }
                            if (!signedInUser.getCommunity().equals(addresses.get(0).getSubAdminArea()) || signedInUser.getCommunity().equals("")) {
                                Log.i("**** getlocation2", signedInUser.getCommunity());
                                //updating signed in users object object
                                signedInUser.setZipcode(Integer.parseInt(addresses.get(0).getPostalCode()));
                                signedInUser.setCommunity(addresses.get(0).getSubAdminArea());
                                signedInUser.setState(addresses.get(0).getAdminArea());
                                signedInUser.setCity(addresses.get(0).getLocality());
                                updateCommunity();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }



        private void removeCommunity(){
            Log.i("**** removelocation",signedInUser.getCommunity());
            new Thread(new Runnable() {
                @Override
                public void run() {

                    // want to get to requests collection
                    //if (!signedInUser.getCommunity().equals("")) {
                        DocumentReference docRef ;
                        if(signedInUser.getState().equals("Online")){
                           docRef = firestoreDb.collection("community").document("Kind " +
                                            "KarmaOnline");
                        }else {
                            // take the current users info from there old community
                            docRef = firestoreDb.collection("community").document(signedInUser.getCommunity() + signedInUser.getState());
                            Log.i("users current community", signedInUser.getCommunity());
                        }
                       docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()) {

                                        Community comm = documentSnapshot.toObject(Community.class);
                                        long members = comm.getTotalUsers();
                                        Log.i("***Members prior to removal" ,
                                                String.valueOf(members));

                                        ArrayList<String> membersList = comm.getMembersList();
                                        if(membersList.contains(CLIENT_REGISTRATION_TOKEN)){
                                            membersList.remove(CLIENT_REGISTRATION_TOKEN);
                                            docRef.update("membersList" , membersList);
                                        }

                                        if (members > 0) {
                                            members -= 1;
                                            docRef.update("totalUsers", members);
                                            Log.i("***Members after removal" ,
                                                    String.valueOf(members));

                                            long communityHours = comm.getTotalCommunityHours();

                                            Log.i("*** Memebers hours prior to removal" ,
                                                    String.valueOf(communityHours));

                                            communityHours -= signedInUser.getTotalHoursWorked();
                                            docRef.update("totalCommunityHours",
                                                    communityHours);
                                            Log.i("&&& members hours post removal" ,
                                                    String.valueOf(communityHours));

                                            Log.i("**** removelocation2",signedInUser.getCommunity());
                                        }
                                    }
                                }
                            }
                        });
                   // }

                }
            }).start();
        }


    private void updateCommunity() {
        Log.i("**** update location",signedInUser.getCommunity());
        topicCommunity = signedInUser.getCommunity();
              // onlineCommunity = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                DocumentReference docRef2;
                        if(onlineCommunity){
                           docRef2 = firestoreDb.collection("community").document("Kind " +
                                            "KarmaOnline");
                        }else {
                // take the current users info from there old community
                docRef2 = firestoreDb.collection("community").document(signedInUser.getCommunity() + signedInUser.getState());
                  }
                Log.i("made it to docRef2 should be new community", signedInUser.getCommunity() );

                docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                  if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            // this is the state existing
                            if (document.exists()) {
                                Community comm = document.toObject(Community.class);
                                long members = comm.getTotalUsers();
                                Log.i("&&& Members prior to add",String.valueOf(members));

                                long totalHours = comm.getTotalCommunityHours();
                                Log.i("&&& Members hours prior to add",String.valueOf(totalHours));

                                ArrayList<String> membersList = comm.getMembersList();
                                if(!membersList.contains(CLIENT_REGISTRATION_TOKEN)){
                                    membersList.add(CLIENT_REGISTRATION_TOKEN);
                                    docRef2.update("membersList" , membersList);
                                }

                                members += 1;
                                totalHours += signedInUser.getTotalHoursWorked();
//                                comm.setTotalCommunityHours(totalHours + signedInUser.getTotalHoursWorked());
//                                comm.setTotalUsers(members + 1);

                                docRef2.update("totalCommunityHours", totalHours);
                                Log.i("Members hours post add",String.valueOf(totalHours));


                                docRef2.update("totalUsers",members);
                                Log.i("&&&mMembers post add",String.valueOf(members));
                            } else {
                                Log.d(TAG, "Document does not exist!");
                                Map<String, Object> newCom = new HashMap<>();
                                newCom.put("totalUsers", 1);

                                newCom.put("totalCommunityHours",
                                        signedInUser.getTotalHoursWorked());
                                newCom.put("userTokens" , CLIENT_REGISTRATION_TOKEN);

                                String community =
                                        signedInUser.getCommunity() +" "+ signedInUser.getState();
                                String color = getColorCode(community);
                                newCom.put("communityColor",color);
                                
                                String county = signedInUser.getCommunity();
                                String newCounty = county.replace("County", "");

                                String[] newCounty1 = community.split(" ");
                                StringBuilder abbrev = new StringBuilder();
                                      while (abbrev.length() > 2){
                                    for (String s : newCounty1) {
                                        char temp = s.charAt(0);
                                        abbrev.append(temp);
                                    }
                                }
                                newCom.put("abbreviation", abbrev.toString());

                                docRef2.update(newCom);

                            }
                        } else {
                            Log.d(TAG, "Failed with: ", task.getException());
                        }
                    }
                });
                if (onlineCommunity) {
                    userUpdates.put("community", "Kind Karma");
                    userUpdates.put("state", "Online");
                    userUpdates.put("zipcode", 0);
                    userUpdates.put("city", "Online");
                    community.setText("Kind Karma");

                    topicCommunity = "Kind Karma";
                } else {
                    // put this in the map for updating the database when the user presses save
                    userUpdates.put("zipcode", signedInUser.getZipcode());
                    userUpdates.put("community", signedInUser.getCommunity());
                    userUpdates.put("state", signedInUser.getState());
                    userUpdates.put("city", signedInUser.getCity());
                    community.setText(signedInUser.getCommunity());
                    topicCommunity = signedInUser.getCommunity();
                }
                //subscribeToNews();
                Log.i("string", signedInUser.getCategories().keySet().toString());
                Log.i("hello", "This is a Firebase Cloud Messaging topic " + "\"" + topicCommunity + "\"" +" message!");
            }
        }).start();
    }

    private void sendMessageToDevice(String targetToken) {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();
        JSONObject jdata = new JSONObject();
        try {
            jNotification.put("title", "Message Title");
            jNotification.put("body", "Message body ");
            jNotification.put("sound", "default");
            jNotification.put("badge", "1");
            /*
            // We can add more details into the notification if we want.
            // We happen to be ignoring them for this demo.
            jNotification.put("click_action", "OPEN_ACTIVITY_1");
            */
            jdata.put("title","data title");
            jdata.put("content","data content");

            /***
             * The Notification object is now populated.
             * Next, build the Payload that we send to the server.
             */

            // If sending to a single client
            jPayload.put("to", targetToken); // CLIENT_REGISTRATION_TOKEN);

            /*
            // If sending to multiple clients (must be more than 1 and less than 1000)
            JSONArray ja = new JSONArray();
            ja.put(CLIENT_REGISTRATION_TOKEN);
            // Add Other client tokens
            ja.put(FirebaseInstanceId.getInstance().getToken());
            jPayload.put("registration_ids", ja);
            */

            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);
            jPayload.put("data",jdata);


            /***
             * The Payload object is now populated.
             * Send it to Firebase to send the message to the appropriate recipient.
             */
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());
            outputStream.close();

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);

            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: " + resp);
                    Toast.makeText(getApplicationContext(),resp,Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper function
     * @param is
     * @return
     */
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

    public void subscribeToChannels(){
        Log.i("string", signedInUser.getCategories().keySet().toString());
        for (String key : signedInUser.getCategories().keySet()) {
            if (key.equals("0")){
                subscribeToAutomotive();
            }

            if (key.equals("1")) {
                subscribeToChildcare();
            }

            if (key.equals("2")){
                subscribeToCrafting();
            }

            if (key.equals("3")){
                Log.i("string", key);
                subscribeToEducation();
            }
            if (key.equals("4")){
                Log.i("string", key);
                subscribeToGarden();
            }
            if (key.equals("5")){
                Log.i("string", key);
                subscribeToHousehold();
            }
            if (key.equals("6")){
                Log.i("string", key);
                subscribeToLabor();
            }
            if (key.equals("7")){
                Log.i("string", key);
                subscribeToLegal();
            }
            if (key.equals("8")){
                Log.i("string", key);
                subscribeToPets();
            }
            if (key.equals("9")){
                Log.i("string", key);
                subscribeToTech();
            }
        }
    }

    public void subscribeToAutomotive(){
        FirebaseMessaging.getInstance().subscribeToTopic("Automotive")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to automotive", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToChildcare(){
        FirebaseMessaging.getInstance().subscribeToTopic("Childcare")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to childcare", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToCrafting(){
        FirebaseMessaging.getInstance().subscribeToTopic("Crafting")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to crafting", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToEducation(){
        FirebaseMessaging.getInstance().subscribeToTopic("Education")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to education", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToGarden(){
        FirebaseMessaging.getInstance().subscribeToTopic("Garden")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to garden", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToHousehold(){
        FirebaseMessaging.getInstance().subscribeToTopic("Household")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to labor", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToLabor(){
        FirebaseMessaging.getInstance().subscribeToTopic("Labor")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to labor", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToLegal(){
        FirebaseMessaging.getInstance().subscribeToTopic("Legal")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to legal", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToPets(){
        FirebaseMessaging.getInstance().subscribeToTopic("Pets")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to pets", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void subscribeToTech(){
        FirebaseMessaging.getInstance().subscribeToTopic("Tech")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), "subscribed to tech", Toast.LENGTH_LONG).show();
                    }
                });
    }



    /**
         * Allows user to edit/update their skills by checking boxes on a dialog and then displaying
         * them on the UI and updating the signed in users , user object
         */
        public void onClickSkills(View view) {

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
                    //https://github.com/codingdemos/MultichoiceTutorial
                    HashMap<String, String> allCategories = new LinkedHashMap<>();
                    allCategories.put("0", "Automotive");
                    allCategories.put("1", "Childcare");
                    allCategories.put("2", "Crafting");
                    allCategories.put("3", "Education");
                    allCategories.put("4", "Garden");
                    allCategories.put("5", "Household");
                    allCategories.put("6", "Labor");
                    allCategories.put("7", "Legal");
                    allCategories.put("8", "Pets");
                    allCategories.put("9", "Tech");

                    //tells if a box is checked -> true, or unchecked -> false and will track the changes when
                    // editted
                    boolean[] usersClicked = new boolean[allCategories.size()];

                    // get the users skills that was gotten from the database from onCreate, find out where that
                    // skill is in the list of allSkills and set that associated indexs boolean value to true
                    for (String keyVal : skills.keySet()) {
                        // get the key which is a string and convert it to it's integer form
                        int checked = Integer.parseInt(keyVal);
                        usersClicked[checked] = true;
                    }
                    //what will show up for the UI
                    String[] cs = {"Automotive", "Childcare", "Crafting", "Education", "Garden",
                            "HouseHold", "Labor", "Legal", "Pets", "Tech"};

                    //building the UI dialog
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(Profile_Activity.this);
                    mBuilder.setTitle("Pick Skills");
                    mBuilder.setMultiChoiceItems(cs, usersClicked,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                                    // if user neewly clicks a box isChecked will be true, if they are
                                    // unchecking a box it will be false so update the boolean array
                                    usersClicked[position] = isChecked;
                                }
                            });

                    mBuilder.setCancelable(false);
                    mBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            // adding selected skills to users (object) skills if not already there, also add
                            // it to the UI for skills
                            for (int i = 0; i < usersClicked.length; i++) {

                                //index value in string form to be able to find key in hashmap
                                String keyVal = String.valueOf(i);

                                // add the new categories to users set of categories
                                if (usersClicked[i] && !skills.containsKey(keyVal)) {
                                    String skillVal = allCategories.get(keyVal);
                                    skills.put(keyVal, skillVal);
                                }
                                // need to remove the unclicked categories
                                else if (!usersClicked[i] && skills.containsKey(keyVal)) {
                                    skills.remove(keyVal, allCategories.get(keyVal));
                                }
                            }
                            int editTextLengthCurrently = editTextCatArray.length;

                            //resetting the UI for any changes
                            int count = 0;
                            for (String cat : skills.keySet()) {
                                editTextCatArray[count].setText(skills.get(cat));
                                count++;
                            }
                            save.setEnabled(count > 0);
                            //possible that people had skills and changed there mind so have to rest those
                            // values to null/empty string( had 4 skills changed to 3 skills total)
                            if (editTextLengthCurrently > skills.size()) {
                                for (int after = skills.size(); after < editTextLengthCurrently; after++) {
                                    editTextCatArray[after].setText("");
                                }
                            }
                            //updating users object and the info for db update
                            signedInUser.setCategories(skills);
                            userUpdates.put("categories", skills);
                        }
                    });
                    // no changes will be saved or made
                    mBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();

//                }
//            }).start();
        }

    }


//
//     //was the api version of finding the location
//        private void showZipCodeDialog() {
//            // create new alert
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Input Zipcode: ");
//            // create the input for the alert edittext so user can enter in input
//            final EditText edit_dialog = new EditText(this) ;
//            edit_dialog.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });
//            edit_dialog.setHint("Zipcode");
//            // Specify the type of input expected type variation
//            edit_dialog.setInputType(InputType.TYPE_CLASS_NUMBER);
//            //@todo
//            // make sure its 5 digits ( 6 if canada and that if it starts with a  zero that it
//            // saves that value
//            //Layout
//            LinearLayout alertLayout = new LinearLayout(this);
//            alertLayout.setOrientation(LinearLayout.VERTICAL);
//            alertLayout.addView(edit_dialog);
//            builder.setView(alertLayout);
//            // Set up add button
//            builder.setPositiveButton("Join ", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int i) {
//                    String urlString = String.valueOf(Integer.valueOf(String.valueOf((edit_dialog.getText()))));
//                    //get user input
//                    InfoRetrieval zip = new InfoRetrieval();
//                    try{
//                        zip.execute("https://ziptasticapi.com/"+urlString);
//                        Log.i("**************** getting location " ,
//                                "https://ziptasticapi.com/"+urlString);
//                    }
//                    catch (Exception e){
//                        Toast.makeText(getApplication(),e.toString(),Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//            // cancel button, if users wants to cancel and not add url
//            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            builder.show();
//        }
//    }
//        private class InfoRetrieval () {
//                new  Thread(new Runnable() {
//                @Override
//                public void run() {
//                Log.i("***************Doing background async task", String.valueOf(zipcodeUrl));
//                String[] userLocation = new String[3];
//                try {
//                    // publishProgress(0);
//                    URL url = new URL(zipcodeUrl[0]);
//                    //open connection and pass input
//                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//                    conn.setRequestMethod("GET");
//                    //tell the connection to connect
//                    conn.connect();
//                    // Read response.
//                    InputStream inputStream = conn.getInputStream();
//                    final String resp = convertStreamToString(inputStream);
//                    // string to json object from the response
//                    JSONObject communityObj = new JSONObject(resp);
//                    //getting data fields we need the name is the key part of the key value pair
//                    //{"country":"US","state":"FL","city":"ORLANDO"}
//                    userLocation[0] = communityObj.getString("country");
//                    userLocation[1] = communityObj.getString("state");
//                    userLocation[2] = communityObj.getString("city");
//                    Log.i("*********************** country : ", userLocation[0]);
//                    Log.i("*********************** city : ", userLocation[1]);
//                    Log.i("*********************** city : ", communityObj.getString("city"));
//                    //validZipcode = true;
//                    inputStream.close();
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                }
////                return userLocation;
//
//            }
//        }).start();
//
//        }
////             The onProgressUpdate() method, however, is called each time a call is made to the
////              publishProgress() method from within the doInBackground() method and can be used
////              to update the user interface with progress information.* /
////            @Override
////            protected void onProgressUpdate( String... update) {
////               // @TODO show toast for status update
////               // Toast.makeText(this, update ,Toast.LENGTH_SHORT).show();
////                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
////            }
//////             The onPostExecute() method is called when the tasks performed within the
//////              doInBackground() method complete.
//////
//////              This method is passed the value returned by the doInBackground() method and runs
//////              within the main thread allowing user interface updates to be made.* /
////            protected void onPostExecute(String[] userLocation) {
////                super.onPostExecute(userLocation);
////                if(!validZipcode){
////                    community.setText("Enter valid zipcode");
////                }
////                else {
////                    community.setText(userLocation[2]);
////                }
////                //@todo add community and zipcode and maybe state to users profile/database
////            }
//
////              Helper function to convert stream to a string
//
//            private String convertStreamToString(InputStream is) {
//                Scanner s = new Scanner(is).useDelimiter("\\A");
//                return s.hasNext() ? s.next().replace(",", ",\n") : "";
//            }
//
//
//
//}



