package edu.neu.madcourse.kindkarma;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import edu.neu.madcourse.kindkarma.models.Request;

public class CreateRequest extends AppCompatActivity {
    //https://www.youtube.com/watch?v=hwe1abDO2Ag
    //https://github.com/mitchtabian/DatePickerDialog
    private static final String TAG = "CreateRequest";
    public TextInputEditText title;
    public TextInputEditText summary;
    public TextView date1;
    public TextView date2;
    private final int PICK_PHOTO_CODE = 1234;
    final HashMap<String, Object> requestInfo = new HashMap<>();
    Request newRequest;
    private TextView[] editTextCatArray;
    private String signedInUserUID;
    int maxSkills = 3;
    int counter = 0;
    Calendar firstDate;
    Calendar endDate;
    FirebaseFirestore firestoreDb;
    CollectionReference requestsReference;
    private Uri photoUri;
    StorageReference photoRef;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private String id;
    Button makeRequest;
    boolean titleFilled = false;
    boolean skillsFilled = false;
    String requestTitle;
    String requestBody;
    String requestImageUrl;
    Long requestCreationTimeMs;
    int requestLastSpinnerLocation;
    String requestStatus;
    ArrayList<String> requestFavorite;
    HashMap<String,String> requestCategories;
    String requesterId;
    Boolean showRequestee;
    String requestId;
    Request theRequest;
    String requestStartDate;
    String requestEndDate;
    ImageView requestImage;
    Bundle bundle;
    boolean[] usersClicked;
    HashMap<String, String> allCategories;
    Boolean online;
    CompoundButton onlineToggle;
    Boolean requestOnline;
    String orientationTitle;
    String orientationSummary;
    String orientationDate1;
    String orientationDate2;
    boolean orientationOnline;
    boolean[] orientationSkills;
    Uri orientationImageUrl;

    private HashMap<String,String> selectedSkills;
    private FirebaseDatabase firebaseDb;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    // Please add the server key from your firebase console in the follwoing format "key=<serverKey>"
    private static final String SERVER_KEY = "key=AAAAQq6C_Wo:APA91bF-83JCqjVbED5GJ2zfC5xKJeQ5clZNbMz8fWM8x3jVEjjHcDFzEse1Da0dkjrClUajd-IwnUgVCQIuMK4Rjupt7vnRbqiRRAMvZ8XQgLSEQO44E4K_Eitoa4rBrnt_0ZTRyNZF";

    // This is the client registration token
    //private static final String CLIENT_REGISTRATION_TOKEN = "dW584Ab0JoY:APA91bGv2dltU6sR5_cM3ii1Y5iQVaxMdk4Mn7b7OPtsIhssBmNzpxk9t9kB-iKziXUic1z2TGPYJgbGSu0aGLyA3d4cwsyj3IRJr4pzEXT6TiGmFXF3BllxAnquEEgTPFZ0ffDU6KYO";
    private String CLIENT_REGISTRATION_TOKEN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);
        date1  = (TextView) findViewById(R.id.firstDate);
        date2 = (TextView) findViewById(R.id.secondDate);
        firstDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        makeRequest = findViewById(R.id.createReq);
        onlineToggle = (CompoundButton)findViewById(R.id.switch1);
        getSupportActionBar().setTitle("Create Request");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        editTextCatArray = new TextView[]{
                findViewById(R.id.skill1),
                findViewById(R.id.skill2),
                findViewById(R.id.skill3)};

        allCategories = new LinkedHashMap<>();
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

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        //https://github.com/codingdemos/MultichoiceTutorial

        //tells if a box is checked -> true, or unchecked -> false and will track the changes when
        // editted
        usersClicked = new boolean[allCategories.size()];

        bundle = getIntent().getExtras();
        //Serializable theRequestSerial = bundle.getSerializable("REQUEST");
        //Request theRequest = theRequestSerial;

        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();

        // get the signed in user uid
        signedInUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // want to get to requests collection
        requestsReference = firestoreDb.collection("requests");
        title = (TextInputEditText) findViewById(R.id.Request_Title);
        TextInputLayout titleLayout = findViewById(R.id.titleInputLayout) ;
        requestImage = (ImageView) findViewById(R.id.reqPhoto);
        summary = (TextInputEditText) findViewById(R.id.Request_Summary);

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

        /*if(savedInstanceState!=null) {
            orientationTitle = savedInstanceState.getString("title");
            orientationSummary = savedInstanceState.getString("summary");
            orientationDate1 = savedInstanceState.getString("date1");
            orientationDate2 = savedInstanceState.getString("date2");
            orientationOnline = savedInstanceState.getBoolean("online");
            orientationSkills = savedInstanceState.getBooleanArray("selected skills");
            orientationImageUrl = savedInstanceState.getParcelable("imageUri");
            //Log.i("orientation url", orientationImageUrl.toString());

            if (orientationTitle != null) {
                title.setText(orientationTitle);
            }
            if (orientationSummary != null) {
                summary.setText(orientationSummary);
            }
            if (orientationDate1 != null) {
                date1.setText(orientationDate1);
            }
            if (orientationDate2 != null) {
                date2.setText(orientationDate2);
            }
            onlineToggle.setChecked(orientationOnline);
            int x = 0;
            int i = 0;
            for (Map.Entry<String, String> theEntry : allCategories.entrySet()) {
                Log.i("orientation item", theEntry.getValue());
                if (orientationSkills[x] == true){
                    Log.i("orientation item chosen", theEntry.getValue());
                    editTextCatArray[i].setText(theEntry.getValue());
                    i++;
                }
                x++;
            }
            if (orientationImageUrl != null) {
                GlideApp.with(this).load(photoUri).into(requestImage);
            }

        }*/
        if (bundle != null){
            makeRequest.setEnabled(true);
            requestTitle = bundle.getString("REQUEST_TTILE");
            requestBody = bundle.getString("REQUEST_BODY");
            requestCreationTimeMs = bundle.getLong("REQUEST_CREATIONTIMEMS");
            requestImageUrl = bundle.getString("REQUEST_IMAGEURL");
            requestLastSpinnerLocation = bundle.getInt("REQUEST_LASTSPINNERLOCATION");
            requestStatus = bundle.getString("REQUEST_STATUS");
            requestFavorite = bundle.getStringArrayList("REQUEST_FAVORITE");
            requestCategories = (HashMap<String,String>) bundle.getSerializable("REQUEST_REQUESTCATEGORIES");
            requesterId = bundle.getString("REQUEST_REQUESTERID");
            showRequestee = bundle.getBoolean("REQUEST_SHOWREQUESTEE");
            requestId = bundle.getString("REQUEST_REQUESTID");
            requestStartDate = bundle.getString("REQUEST_STARTDATE");
            Log.i("request start", requestStartDate);
            requestEndDate = bundle.getString("REQUEST_ENDDATE");
            Log.i("request end", requestEndDate);
            requestOnline = bundle.getBoolean("REQUEST_ONLINE");

            theRequest = new Request(requestTitle,requestBody,requestImageUrl,requestCreationTimeMs,requesterId,requestCategories,requestStatus,true,requestId,requestFavorite,requestStartDate,requestEndDate,requestOnline);
            newRequest = theRequest;
            requestInfo.put("status", theRequest.getStatus());
            requestInfo.put("requesterId", signedInUserUID);
            requestInfo.put("lastSpinnerPosition", theRequest.getLastSpinnerPosition());
            requestInfo.put("showRequestee", theRequest.getShowRequestee());
            requestInfo.put("image_url", theRequest.getImage_url());
            requestInfo.put("title", theRequest.getTitle());
            title.setText(theRequest.getTitle());
            GlideApp.with(this).load(theRequest.getImage_url()).into(requestImage);
            requestInfo.put("body", theRequest.getBody());
            summary.setText(requestBody);
            requestInfo.put("creation_time_ms", System.currentTimeMillis());
            selectedSkills = requestCategories;
            date1.setText(theRequest.getStartDate());
            date2.setText(theRequest.getEndDate());
            titleFilled = true;
            onlineToggle.setChecked(requestOnline);

            int x = 0;
            for (Map.Entry<String, String> theEntry: requestCategories.entrySet()){
                usersClicked[Integer.parseInt(theEntry.getKey())] = true;
                editTextCatArray[x].setText(theEntry.getValue());
                counter++;
                x++;
            }
            Log.i("skills counter start", String.valueOf(counter));
            skillsFilled = counter > 0;

        } else {
            makeRequest.setEnabled(false);
            onlineToggle.setChecked(false);
            requestInfo.put("online", false);
            newRequest = new Request();

            id = firestoreDb.collection("requests").document().getId();
            //        this.title = title;
            //        this.body = body;

            // Instantiate firebase db
            firebaseDb = FirebaseDatabase.getInstance();

            newRequest.setStatus("active");
            requestInfo.put("status", "active");

            newRequest.setRequesterId(signedInUserUID);
            requestInfo.put("requesterId", signedInUserUID);

            requestInfo.put("favorite", new ArrayList<String>());
            requestInfo.put("lastSpinnerPosition", 0);
            requestInfo.put("showRequestee", false);
            requestInfo.put("image_url", "");

        }

        //https://stackoverflow.com/questions/8543449/how-to-use-the-textwatcher-class-in-android
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Called before the changes have been applied to the text.
                //The s parameter is the text before any change is applied.
                //The start parameter is the position of the beginning of the changed part in the text.
                //The count parameter is the length of the changed part in the s sequence since the start position.
                //And the after parameter is the length of the new sequence which will replace the part
                // of the s sequence from start to start+count.
                if (after < count || s.length() < 1) {
                    Log.i("sequence before change", s.toString());
                    Log.i("start", String.valueOf(start));
                    Log.i("count", String.valueOf(count));
                    Log.i("after", String.valueOf(after));
                    // delete character action have done
                    // do what ever you want
                    titleFilled = after >= 1;
                    enableButton();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("start", String.valueOf(start));
                Log.i("before", String.valueOf(before));
                Log.i("count", String.valueOf(count));
                // called after the text changes.
                //The s parameter is the text after changes have been applied.
                //The start parameter is the same as in the beforeTextChanged method.
                //The count parameter is the after parameter in the beforeTextChanged method.
                //And the before parameter is the count parameter in the beforeTextChanged method.
                titleFilled = s.length() > 1;
                enableButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //  You can change the text in the TextView from this method.
                String tempTitle = title.getText().toString().trim();
                requestInfo.put("title", tempTitle);
                titleFilled = tempTitle.length() > 1;
                enableButton();
            }

        });

        Log.i("enabled title value", String.valueOf(titleFilled));
        Log.i("enabled skills value", String.valueOf(skillsFilled));

        onlineToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                    requestInfo.put("online", isChecked);
            }
        });
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

    //button check to enable it or not
    private void enableButton(){
        makeRequest.setEnabled(titleFilled && skillsFilled);
    }


    public void onClick(View view){
        if (view.getId() == R.id.firstDate) {
            getDate(date1, 22);
        } else if (view.getId() == R.id.secondDate) {
            getDate(date2,44);

        } else if(view.getId() == R.id.addSkills){
            addSkills();
            skillsFilled = counter > 0;
            enableButton();
        } else if(view.getId() == R.id.attachPhoto){
            updateProfileImage(view);
        }  else if (view.getId() == R.id.createReq) {
            saveUpdatesToDatabase();
        } else if (view.getId() == R.id.cancelReq) {
            Intent intent = new Intent(this, RequestsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //https://www.geeksforgeeks.org/calendar-compareto-method-in-java-with-examples/
    private void getDate(TextView dateEdit, int num) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Log.i(TAG, "todays cal date :  " + month + " " + day + " " + year);

        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                String date = (month + 1) + "/" + day + "/" + year;
                if (num == 22) {
                    firstDate.set(year, month, day);
                    firstDate.getTimeInMillis();
                    Log.d("first date", month + " " + day + " " + year);
                    int compare = firstDate.compareTo(cal);
                    Log.i(TAG, "firstdate comparission " + compare);
                    if (compare < 0) {
                        Toast.makeText(CreateRequest.this, "Date has already passed",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        dateEdit.setText(date);
                        requestInfo.put("startDate", date);
                        newRequest.setStartDate(date);
                    }

                } else if (num == 44) {
                    endDate.set(year, month, day + 1);
                    dateEdit.setText(date);
                    requestInfo.put("endDate", date);
                    newRequest.setEndDate(date);
                }

            }
        };
        //UI dialog
        DatePickerDialog dialog = new DatePickerDialog(
                CreateRequest.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateSetListener,
                year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    //https://androidbitmaps.blogspot.com/2015/04/loading-images-in-android-part-iii-pick.html
    public void pickImage() {
        Log.i(TAG, "Open up image picker on device");
        // open any application that handles this intent and we want it to provide us with images
        Intent imagePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imagePickerIntent.setType("image/*");
        // check if application that can handle intent exists
        if (imagePickerIntent.resolveActivity(getPackageManager()) != null) {
            Log.i("*******starting image picker", "hi");
            startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE);
        }

    }

    public void save() {
        // if no photo was selected, pass in null for the photo uri
        if (photoUri == null) {
            //finish();
            return;
        }
        Log.i("*******photouri", String.valueOf(photoUri));
        // where photo is stored
        // Use Tasks API to handle chained async operations
        photoRef = storageRef.child("request/" + System.currentTimeMillis() + "-photo.jpg");
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
                            requestInfo.put("image_url", download_url);
                            newRequest.setImage_url(download_url);
                            // display a success message
                        }
                    });
                } else {
                    Toast.makeText(CreateRequest.this, "Error happened during the upload process",
                            Toast.LENGTH_LONG).show();
                }
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
                // set the image uri of the imageView
                save();
                //requestImage.setImageURI(photoUri);
                GlideApp.with(this).load(photoUri).into(requestImage);
            } else {
                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void addSkills() {

        //what will show up for the UI
        String[] cs = {"Automotive", "Childcare", "Crafting", "Education", "Garden",
                "HouseHold", "Labor", "Legal", "Pets", "Tech"};

        //building the UI dialog
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CreateRequest.this);
        mBuilder.setTitle("Pick up to "+maxSkills+" skills");
        Log.i("skills picked array", Arrays.toString(usersClicked));
        mBuilder.setMultiChoiceItems(cs, usersClicked,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        // if user neewly clicks a box isChecked will be true, if they are
                        // unchecking a box it will be false so update the boolean array
                        Log.i("skills counter", String.valueOf(counter));
                        Log.i("position", String.valueOf(position));
                        //Log.i("skills picked array after click", Arrays.toString(usersClicked));
                        Log.i("skills counter isChecked", String.valueOf(isChecked));
                        if(counter < maxSkills && isChecked == true) {
                            usersClicked[position] = isChecked;
                            counter++;
                            // if unchecking set to false
                        }else if(isChecked == false){
                            counter--;
                            usersClicked[position] = false;
                        } else{
                            //counter--;
                            usersClicked[position] = false;
                            Toast.makeText(CreateRequest.this , "Max 3 skills", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // adding selected skills to users (object) skills if not already there, also add
                // it to the UI for skills
                skillsFilled = counter > 0;
                enableButton();
                selectedSkills = new HashMap<>();
                Log.i("skills picked array after click", Arrays.toString(usersClicked));
                int positionCount = 0;
                for (int i = 0; i < usersClicked.length; i++) {
                    Log.i("***posCount", String.valueOf(positionCount));
                    //index value in string form to be able to find key in hashmap
                    String keyVal = String.valueOf(i);

                    // add the new categories to users request
                    if (usersClicked[i]) {
                        String skillVal = allCategories.get(keyVal);
                        selectedSkills.put(keyVal , skillVal);
                        Log.i("skill position count", String.valueOf(positionCount));
                        Log.i("skill val", String.valueOf(skillVal));
                        editTextCatArray[positionCount].setText(skillVal);
                        positionCount++;
                    }
                }

                //possible that people had skills and changed there mind so have to rest those
                // values to null/empty string( had 4 skills changed to 3 skills total)
                Log.i("***countteerrr", String.valueOf(counter));
                /*while(counter < maxSkills) {
                    editTextCatArray[counter++].setText("");
                    Log.i("skill counter", String.valueOf(counter));
                }*/

                while(positionCount < maxSkills) {
                    editTextCatArray[positionCount++].setText("");
                    Log.i("skill counter", String.valueOf(counter));
                }
                //updating users object and the info for db update,
                requestInfo.put("requestCategories",selectedSkills);
                newRequest.setRequestCategories(selectedSkills);
            }
        });
        // no changes will be saved or made
        mBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                skillsFilled = counter > 0;
                enableButton();
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }


    private void saveUpdatesToDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TextInputEditText summary = (TextInputEditText) findViewById(R.id.Request_Summary);
                newRequest.setTitle(title.getText().toString());
                //summary can be empty
                if(summary.getText() != null){
                    String tempSummary = summary.getText().toString().trim();
                    Log.i("**************",tempSummary);
                    requestInfo.put("body", tempSummary);

                }else{ // still have to put the place in the database
                    requestInfo.put("body","");
                }
                requestInfo.put("creation_time_ms", System.currentTimeMillis());
                Log.i("string",newRequest.getRequestCategories().toString());

                for (String key : newRequest.getRequestCategories().keySet()) {
                    if (key.equals("0")){
                        sendMessageToAutomotive(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }

                    if (key.equals("1")) {
                        sendMessageToChildcare(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }

                    if (key.equals("2")){
                        sendMessageToCrafting(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }

                    if (key.equals("3")){
                        Log.i("string", key);
                        sendMessageToEducation(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }
                    if (key.equals("4")){
                        Log.i("string", key);
                        sendMessageToGarden(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }
                    if (key.equals("5")){
                        Log.i("string", key);
                        sendMessageToHousehold(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }
                    if (key.equals("6")){
                        Log.i("string", key);
                        sendMessageToLabor(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }
                    if (key.equals("7")){
                        Log.i("string", key);
                        sendMessageToLegal(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }
                    if (key.equals("8")){
                        Log.i("string", key);
                        sendMessageToPets(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }
                    if (key.equals("9")){
                        Log.i("string", key);
                        sendMessageToTech(newRequest.getTitle(), newRequest.getBody(), newRequest.getRequestCategories().get(key));
                    }
                }

                if (bundle == null) {

                    firestoreDb.collection("requests").document(id).set(requestInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Also add request to firebase
                            //saveUpdatesToRealtime();
                            Toast.makeText(CreateRequest.this, "Request created", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to create request" + e.toString());
                            Toast.makeText(CreateRequest.this, "Request failed, please try again later",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    firestoreDb.collection("requests").document(requestId).update(requestInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Also add request to firebase
                            //saveUpdatesToRealtime();
                            Toast.makeText(CreateRequest.this, "Request created", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to create request" + e.toString());
                            Toast.makeText(CreateRequest.this, "Request failed, please try again later",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                Intent intent = new Intent(CreateRequest.this,RequestsActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }

    private void saveUpdatesToRealtime() {
        StringBuilder categories = new StringBuilder();
        for (String value : selectedSkills.values()) {
            categories.append(" ").append(value);
        }
        DatabaseReference reqRef = firebaseDb.getReference("requests");
        reqRef.child(id).child("categories").setValue(categories.toString());
        Intent intent = new Intent(this, RequestsActivity.class);
        startActivity(intent);
        finish();
    }

    public void updateProfileImage(View view) {
        requestImagePermissions();
    }

    public void sendMessageToDevice(View type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO send message to selected user
            }
        }).start();
    }

    //  https://www.androidhive.info/2017/12/android-easy-runtime-permissions-with-dexter/
//https://www.geeksforgeeks.org/easy-runtime-permissions-in-android-with-dexter/
    private void requestImagePermissions() {
        // below line is use to request
        // permission in the current activity.
        Dexter.withContext(CreateRequest.this)
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
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateRequest.this);

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

    private void sendMessageToEducation(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Education");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            //Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToPets(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Pets");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            //Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToHousehold(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Household");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToTech(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Tech");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToCrafting(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Crafting");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToChildcare(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Childcare");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToAutomotive(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Automotive");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToLabor(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Labor");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            //Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToLegal(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Legal");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            //Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
    }

    private void sendMessageToGarden(String title, String body, String category){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", "Someone posted an " + category + " request!");
                    jNotification.put("message", "This is a Firebase Cloud Messaging topic " + "\"" + category + "\"" +" message!");
                    jNotification.put("body", "Help them out with the request titled: " + title);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    // Populate the Payload object.
                    // Note that "to" is a topic, not a token representing an app instance
                    Log.i("category",category);
                    jPayload.put("to", "/topics/"+ "Garden");
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    // Open the HTTP connection and send the payload
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
                            //Toast.makeText(getApplicationContext(),"response was: " + resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e(TAG,"sendMessageToNews threw error",e);
                }
            }
        }).start();
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

    /*protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title.getText().toString());
        outState.putString("summary", summary.getText().toString());
        outState.putString("date1", date1.getText().toString());
        outState.putString("date2", date2.getText().toString());
        outState.putBoolean("online", onlineToggle.isChecked());
        outState.putBooleanArray("selected skills", usersClicked);
        Log.i("orientation url", String.valueOf(photoUri));
        outState.putParcelable("imageUri", photoUri);
    }*/
}
