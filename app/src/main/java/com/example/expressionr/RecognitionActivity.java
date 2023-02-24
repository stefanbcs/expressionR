package com.example.expressionr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.tensorflow.lite.Interpreter;

public class RecognitionActivity extends AppCompatActivity {

    //variables and android elements declarations
    ImageView chosenImage;

    public static final int IMG_GALLERY_REQUEST = 20;
    public static final int CAMERA_PERM_CODE=101;
    public static final int CAMERA_REQUEST_CODE = 102;

    TextView scoreInput;
    TextView timeInput ;
    TextView classInput;


    private static String MODEL_NAME;
    private static  int IMG_HEIGHT;
    private static  int IMG_WIDTH;
    private static int NUM_CHANNEL;
    private static int NUM_CLASSES;
    private static String LABEL;
    public static String chosenModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        //image view variable
        chosenImage = findViewById(R.id.chosen_image);
        //upload button
        ImageButton upload_button = findViewById(R.id.upload);
        //camera button
        ImageButton camera_button = findViewById(R.id.camera);

        scoreInput = (TextView) findViewById(R.id.scoreInput);
        timeInput = (TextView)findViewById(R.id.timeInput);
        classInput = (TextView)findViewById(R.id.classInput);

        //select model Spinner
        Spinner mySpinner = findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(RecognitionActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.modele));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);


        //upload method
        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                //where do we want to find data?
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String pictureDirectoryPath = pictureDirectory.getPath();
                //get a URI representation
                Uri data = Uri.parse(pictureDirectoryPath);
                //set the data and type. Get all image type
                photoPickerIntent.setDataAndType(data,"image/*");
                //we will invoke this activity and get something back from it
                startActivityForResult(photoPickerIntent, IMG_GALLERY_REQUEST);
            }
        });

        //camera button function
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();    
            }

        });

        //get_results button
        Button get_results = findViewById(R.id.recognize_button);
        //when clicked the specified function is called
        get_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get value from spinnner in a string variable
                chosenModel = mySpinner.getSelectedItem().toString();
                //set parameters for recognition activity based on chosenModel
                switch(chosenModel){
                    case "L-CNN_FER2013":
                        MODEL_NAME="l_cnn_fer2013.tflite";
                        IMG_HEIGHT=48;
                        IMG_WIDTH=48;
                        NUM_CHANNEL=1;
                        NUM_CLASSES = 7;
                        LABEL="labels_FER2013.txt";
                        break;

                    case "NL-CNN_FER2013":
                        MODEL_NAME= "nl_cnn_fer2013.tflite";
                        IMG_HEIGHT=48;
                        IMG_WIDTH=48;
                        NUM_CHANNEL=1;
                        NUM_CLASSES = 7;
                        LABEL="labels_FER2013.txt";
                        break;

                    case "MobileNet_FER2013":
                        MODEL_NAME="mobilenet_fer2013.tflite";
                        IMG_HEIGHT=48;
                        IMG_WIDTH=48;
                        NUM_CHANNEL=3;
                        NUM_CLASSES = 7;
                        LABEL="labels_FER2013.txt";
                        break;

                    case "L-CNN_JAFFE":
                        MODEL_NAME="l_cnn_jaffe.tflite";
                        IMG_HEIGHT=128;
                        IMG_WIDTH=128;
                        NUM_CHANNEL=3;
                        NUM_CLASSES = 7;
                        LABEL="labels_JAFFE.txt";
                        break;

                    case "NL-CNN_JAFFE":
                        MODEL_NAME="nl_cnn_jaffe.tflite";
                        IMG_HEIGHT=128;
                        IMG_WIDTH=128;
                        NUM_CHANNEL=3;
                        NUM_CLASSES = 7;
                        LABEL="labels_JAFFE.txt";
                        break;

                    case "MobileNet_JAFFE":
                        MODEL_NAME="mobilenet_jaffe.tflite";
                        IMG_HEIGHT=128;
                        IMG_WIDTH=128;
                        NUM_CHANNEL=3;
                        NUM_CLASSES = 7;
                        LABEL="labels_JAFFE.txt";
                        break;
                }
                //Toast.makeText(RecognitionActivity.this,MODEL_NAME, Toast.LENGTH_SHORT).show();
                try {
                    getRecognizeResults();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    // camera capture method
    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else{
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            }else{
                Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CAMERA_REQUEST_CODE){
            Bundle bundle = data.getExtras();
            Bitmap image = (Bitmap) bundle.get("data");
            chosenImage.setImageBitmap(image);
        }

        //upload from gallery second method
        if(resultCode == RESULT_OK){
            if(requestCode == IMG_GALLERY_REQUEST){

                Uri imageUri = data.getData();

                InputStream inputStream;

                try{
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    chosenImage.setImageBitmap(image);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);// si ptr camera si ptr upload

    }

    private void getRecognizeResults() throws IOException {
        //check if the imageview contains an image
        if(chosenImage.getDrawable()==null){
            //display warning message
            Toast.makeText(this,"There's no image selected",Toast.LENGTH_SHORT).show();
        }
        else{
            //toast for testing the change of parameters
            //Toast.makeText(this,"Value of chosenModel:" + getChosenModel(),Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,"model ales: "+ LABEL,Toast.LENGTH_SHORT).show();
            Interpreter.Options options = new Interpreter.Options();
            float[][] mResult = new float[1][NUM_CLASSES];
            Interpreter mInterpreter = new Interpreter(Classifier.loadModelFile(this, MODEL_NAME), options);
            List<String> labelList = Classifier.loadLabelList(this, LABEL);
            Bitmap bmp = ((BitmapDrawable) chosenImage.getDrawable()).getBitmap();
            //resize bmp to the dimensions that the model used in training
            Bitmap resBmp = Bitmap.createScaledBitmap(bmp,IMG_WIDTH,IMG_HEIGHT,false);
            //transform resBMP in a byte buffer
            ByteBuffer buffImg = Classifier.convertBitmapToByteBuffer(resBmp,IMG_HEIGHT,IMG_WIDTH,NUM_CHANNEL);
            long startTime = System.currentTimeMillis();
            mInterpreter.run(buffImg, mResult);
            long endTime = System.currentTimeMillis();
            long timeCost = (endTime - startTime);
            String timeCost2 = String.valueOf(timeCost);
            final Result result = new Result(mResult[0], timeCost);
            //show the results in textviews
            scoreInput.setText(Float.toString(result.getProbability() * 100)+" %");
            timeInput.setText(timeCost2+" ms");
            classInput.setText(labelList.get(result.getNumber()));
        }
    }
}
