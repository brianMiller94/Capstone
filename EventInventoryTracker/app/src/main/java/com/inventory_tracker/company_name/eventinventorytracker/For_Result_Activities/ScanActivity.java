package com.inventory_tracker.company_name.eventinventorytracker.For_Result_Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.inventory_tracker.company_name.eventinventorytracker.R;

import java.io.File;
import java.io.FileNotFoundException;

public class ScanActivity extends Activity {
    //Widgets
    Button mScanButton;
    EditText mBarcodeTextEdit;

    //Internal Variables
    private Uri mImageUri;
    private BarcodeDetector mBarcodeDetector;

    //Constants
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    private static final String LOG_TAG = "Barcode Scanner API";
    private static final int PHOTO_REQUEST = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.utility_scan);

        //Setting Controls
        mBarcodeTextEdit = (EditText) findViewById(R.id.barcodeEditText);
        mScanButton = (Button) findViewById(R.id.scanButton);
        wireScanButton();

        //Saved instance state setup

        if (savedInstanceState != null) {
            mImageUri = Uri.parse(savedInstanceState.getString("uri"));
        }

        //Setup detector
        mBarcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.CODE_128)
                .build();
        if (!mBarcodeDetector.isOperational()) {
            Toast.makeText(this, "Barcode Detector Not Functional", Toast.LENGTH_LONG).show();
        }

    }


    private void wireScanButton() {
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(ScanActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION
                );
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode){
            case 20:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePicture();
                }else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                }

        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        mImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mImageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, mImageUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(mImageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK){
            launchMediaScanIntent();
            try{
                Bitmap bitmap = decodeBitmapUri(this,mImageUri);
                if(mBarcodeDetector.isOperational() && bitmap != null){
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodeArray = mBarcodeDetector.detect(frame);
                    Barcode barcode  = barcodeArray.valueAt(0);
                    mBarcodeTextEdit.setText(barcode.displayValue);
                }
            }catch (Exception e){
                Toast.makeText(this,"Failed to load image", Toast.LENGTH_LONG).show();
            }
        }
    }
}
