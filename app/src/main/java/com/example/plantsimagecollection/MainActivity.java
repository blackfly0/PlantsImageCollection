package com.example.plantsimagecollection;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.layout.simple_spinner_dropdown_item;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String NAME_LABEL = "plant_name";
    private static final String DISEASE_LABEL = "plant_disease";
    private static final String DOWNLOAD_URL = "download_url";
    private static final String GEOPOINT_LABEL = "geopoint";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();

    boolean isFromGallery;
    private int imageUploadProgress;
    private String imageDownloadURL;
    private String currentPhotoName;
    private String currentPhotoPath;
    private Button btn;
    private Button uploadButton;
    private String plantName, diseaseName;
    private Spinner plantNameSpinner,diseaseNameSpinner;
    private boolean imageLoaded = false;
    private ImageView imageview;
    private ImageView placeHolderIV;
    private TextView uploadProgressTextView;
    private static final String IMAGE_DIRECTORY = "/Plants";
    private int GALLERY = 1, CAMERA = 2;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestMultiplePermissions();

        btn = findViewById(R.id.btn);
        uploadButton = findViewById(R.id.uploadButton);
        plantNameSpinner = findViewById(R.id.plantNameSpinner);
        diseaseNameSpinner = findViewById(R.id.diseaseNameSpinner);
        imageview = findViewById(R.id.iv);
        uploadProgressTextView = findViewById(R.id.uploadProgressTextView);
        uploadProgressTextView.setText("");
        placeHolderIV = findViewById(R.id.imageView);
        //imageview.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));

        // Spinner Drop down elements
        List<String> plantNames = new ArrayList<String>();
        plantNames.add("Rice");
        plantNames.add("Corn");
        plantNames.add("Wheat");
        plantNames.add("Mango");

        // Spinner Drop down elements
        final List<String> riceDiseases = new ArrayList<String>();
        riceDiseases.add("Yellow Rice Borer");
        riceDiseases.add("Rice Stemfly");
        riceDiseases.add("Rice Leaf Caterpillar");
        riceDiseases.add("Brown Plant Hopper");
        riceDiseases.add("White Backed Hopper");
        riceDiseases.add("Rice Shell Pest");
        riceDiseases.add("Rice Gall Midge");
        riceDiseases.add("Asiatic Rice Borer");
        riceDiseases.add("Small Brown Hopper");
        riceDiseases.add("Rice Leaf Hopper");
        riceDiseases.add("Paddy Stem Maggot");
        riceDiseases.add("Rice Water Weevil");
        riceDiseases.add("Rice Leaf Roller");
        riceDiseases.add("Grain Spreader Thrips");
        // Spinner Drop down elements
        final List<String> cornDiseases = new ArrayList<String>();
        cornDiseases.add("Grub");
        cornDiseases.add("Aphids");
        cornDiseases.add("WireWorm");
        cornDiseases.add("Mole Cricket");
        cornDiseases.add("Yellow Cutworm");
        cornDiseases.add("Potosiabre Vitarsis");
        cornDiseases.add("Peach Borer");
        cornDiseases.add("Corn Borer");
        cornDiseases.add("White Margined Moth");
        cornDiseases.add("Black Cutworm");
        cornDiseases.add("Red Spider");
        cornDiseases.add("Large Cutworm");
        // Spinner Drop down elements
        final List<String> wheatDiseases = new ArrayList<String>();
        wheatDiseases.add("Wheat Sawfly");
        wheatDiseases.add("Longlegged Spider Mite");
        wheatDiseases.add("Bird Cherry Oatphid");
        wheatDiseases.add("Wheat Phelothrips");
        wheatDiseases.add("Green Bug");
        wheatDiseases.add("Wheat Blossom Midge");
        wheatDiseases.add("English Grain Aphid");
        wheatDiseases.add("Penthaleus Major");
        wheatDiseases.add("Cerodonta Denticornis");
        // Spinner Drop down elements
        final List<String> mangoDiseases = new ArrayList<String>();
        mangoDiseases.add("Sternochetus frigidus");
        mangoDiseases.add("Mango Flatbeak Leafhopper");
        mangoDiseases.add("Cicadellidae");
        mangoDiseases.add("Scirtothrips Dorsalis Hood");
        mangoDiseases.add("Lawana imitata Melichar");
        mangoDiseases.add("Chlumetia Transversa");
        mangoDiseases.add("Salurnis Marginella Guerr");
        mangoDiseases.add("Deporaus Marginatus Pascoe");
        mangoDiseases.add("Dasinuera Sp");
        mangoDiseases.add("Rhytidodera bowrinii white");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, plantNames);
        dataAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
        plantNameSpinner.setAdapter(dataAdapter);
        plantNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                plantName = item;
                if (position == 0) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, riceDiseases);
                    dataAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
                    diseaseNameSpinner.setAdapter(dataAdapter);
                } else if (position == 1) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, cornDiseases);
                    dataAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
                    diseaseNameSpinner.setAdapter(dataAdapter);
                } else if (position == 2) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, wheatDiseases);
                    dataAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
                    diseaseNameSpinner.setAdapter(dataAdapter);
                }else if(position == 3) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, mangoDiseases);
                    dataAdapter.setDropDownViewResource(simple_spinner_dropdown_item);
                    diseaseNameSpinner.setAdapter(dataAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        diseaseNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                diseaseName = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadButtonTapped();
            }
        });
    }


    private void uploadButtonTapped(){

        if(!imageLoaded){
            Toast.makeText(this, "Please upload an image first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Location location = getLocationWithCheckNetworkAndGPS(getApplicationContext());
        if(location == null){
            Toast.makeText(this, "Could not get location!", Toast.LENGTH_SHORT).show();
        }
        // Upload the image
        uploadImage();
    }

    public void uploadData(){

        Location location = getLocationWithCheckNetworkAndGPS(getApplicationContext());
        GeoPoint geoPoint;
        if(location == null){
            geoPoint = new GeoPoint(0,0);
        } else {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            geoPoint = new GeoPoint(longitude, latitude);
        }

        Map<String, Object> entity = new HashMap<>();
        entity.put(NAME_LABEL, plantName);
        entity.put(DISEASE_LABEL, diseaseName);
        entity.put(DOWNLOAD_URL, imageDownloadURL);
        entity.put(GEOPOINT_LABEL, geoPoint);

        String id = db.collection("Images").document().getId();

        db.collection("Images").document(id).set(entity)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Upload SuccessFull!", Toast.LENGTH_SHORT).show();
                        placeHolderIV.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
                        imageview.setImageBitmap(null);
                        imageLoaded = false;
                        uploadProgressTextView.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }

    public static Location getLocationWithCheckNetworkAndGPS(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        assert lm != null;
        boolean isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location networkLoacation = null, gpsLocation = null, finalLoc = null;
        if (isGpsEnabled)
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
        gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (isNetworkLocationEnabled)
            networkLoacation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gpsLocation != null && networkLoacation != null) {

            //smaller the number more accurate result will
            if (gpsLocation.getAccuracy() > networkLoacation.getAccuracy())
                return finalLoc = networkLoacation;
            else
                return finalLoc = gpsLocation;

        } else {

            if (gpsLocation != null) {
                return finalLoc = gpsLocation;
            } else if (networkLoacation != null) {
                return finalLoc = networkLoacation;
            }
        }
        return finalLoc;
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.d(TAG, ex.toString());
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.plantsimagecollection.fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, CAMERA);
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e(TAG, "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            imageLoaded = true;
            placeHolderIV.setImageBitmap(null);
            if (data != null) {
                isFromGallery = true;
                Uri contentURI = data.getData();
                currentPhotoPath = getRealPathFromURI(this, contentURI);
                currentPhotoName = getFileName(contentURI);
                setPic();
            }

        } else if (requestCode == CAMERA && resultCode == RESULT_OK) {
            isFromGallery = false;
            placeHolderIV.setImageBitmap(null);
            imageLoaded = true;
            setPic();
        }
    }

    private void uploadImage(){
        // Create a reference to 'images/mountains.jpg'
        final StorageReference ImagesRef = storageRef.child("Plants_Images/" + currentPhotoName);
        byte[] data = new byte[0];

        Bitmap bitmap;
        Uri photoURI;
        if(isFromGallery){
            photoURI = Uri.fromFile(new File(currentPhotoPath));
        }else {
            File f = new File(currentPhotoPath);
            photoURI = FileProvider.getUriForFile(this,
                    "com.example.plantsimagecollection.fileprovider",
                    f);
        }

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
            bitmap = correctOrientationOfBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        String downloadURL = "";
        UploadTask uploadTask = ImagesRef.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ImagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    final String downloadURLString = downloadUri.toString();
                    setImageDownloadURL(downloadURLString);
                    uploadData();
                    Log.d(TAG, "********* download url:" + downloadUri.toString());
                } else {
                    // Handle failures
                    // ...
                }
            }
        });

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                imageUploadProgress = (int)progress;
                uploadProgressTextView.setText("Uploading " +Integer.toString(imageUploadProgress) + "%");
                if (imageUploadProgress >= 100) {
                    Toast.makeText(MainActivity.this, "Image Upload Successful!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bitmap = null;

    }

    public void setImageDownloadURL(String url){
    imageDownloadURL = url;
    }


    private Bitmap correctOrientationOfBitmap(Bitmap bitmap){
        int orientation = 0;
        try {
            ExifInterface ei = new ExifInterface(currentPhotoPath);
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        currentPhotoName = imageFileName;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageview.getWidth();
        int targetH = imageview.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        bitmap = correctOrientationOfBitmap(bitmap);
        imageview.setImageBitmap(bitmap);
    }

    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            //Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}
