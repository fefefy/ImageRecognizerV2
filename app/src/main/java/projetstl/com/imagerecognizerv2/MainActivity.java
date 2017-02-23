package projetstl.com.imagerecognizerv2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.soundcloud.android.crop.Crop;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_READ;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.cvAttrList;
import static org.bytedeco.javacpp.opencv_core.cvOpenFileStorage;
import static org.bytedeco.javacpp.opencv_core.cvReadByName;
import static org.bytedeco.javacpp.opencv_core.cvReleaseFileStorage;
import static org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import static org.bytedeco.javacpp.opencv_highgui.imread;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1;
    private static int RESULT_LOAD_IMAGE = 2;
    private ImageView imageView;
    private ProgressBar progressBar;
    String currentPhotoPath;
    File vocab;

    String urlRequest = "http://www-rech.telecom-lille.fr/nonfreesift/";
    List<Brand> brandsList = new ArrayList<>();
    RequestQueue queue;

    boolean mustBeRotated = false;
    SIFT detector;
    FlannBasedMatcher matcher;
    BOWImgDescriptorExtractor bowide;
    String[] class_names;
    CvSVM[] classifiers;
    Mat response_hist = new Mat();
    KeyPoint keypoints = new KeyPoint();
    Mat inputDescriptors = new Mat();

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    //cf. OnCreate, pb with permissions with gallery
    @TargetApi(Build.VERSION_CODES.M)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    //for the camera picture
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //Camera picture Intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);




        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File from taken picture
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            // if successfull :
            if (photoFile != null) {
                System.out.println("Récupération de l'URI photo");
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                System.out.println("Début activité");
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }
String test;

    //2 Crop parts from :
   // https://github.com/jdamcd/android-crop
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).withMaxSize(imageView.getHeight(),imageView.getWidth()).start(this);
        System.out.println("Crop OK");
    }
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Used by Camera & Gallery
    private void ImageViewPrint() {

        // Get View's dimensions
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();

        // Get bitmap's dimensions
        BitmapFactory.Options BitmapOpt = new BitmapFactory.Options();
        BitmapOpt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, BitmapOpt);
        int photoWidth = BitmapOpt.outWidth;
        int photoHeight = BitmapOpt.outHeight;

        // Scaling image ratio
        int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

        // Setting up bitmap parameters
        BitmapOpt.inJustDecodeBounds = false;
        BitmapOpt.inSampleSize = scaleFactor;
        BitmapOpt.inPurgeable = true;
        System.out.println("Decoding ok");

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, BitmapOpt);
        imageView.setVisibility(View.VISIBLE);

        //Rotating image if photo is from camera then print
        System.out.println("valeur de must be rotated " + mustBeRotated);
        if (mustBeRotated = false){
            imageView.setImageBitmap(bitmap);
            System.out.println("on est dans le if");
        }
        else if (mustBeRotated = true){
            System.out.println("On est dans le else");
            imageView.setRotation(90);
            imageView.setImageBitmap(bitmap);
        }
        else System.out.println("Error rotating");
    }

    //Methode to create files from data
    public static File writeToFile(String data, String fileName)
    {
        // Get the directory for the user's public pictures directory.
        final File path = Environment.getExternalStorageDirectory();
        if(!path.exists())
        {
            // Create dir if it does not exist
            path.mkdirs();
        }
        final File file = new File(path, fileName);
        // Save your stream, don't forget to flush() it before closing it.

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public Uri getImageUri(Context context, Bitmap inputImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inputImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Images.Media.insertImage(context.getContentResolver(), inputImage, "Title", null);
        return Uri.parse(path);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        progressBar = (ProgressBar) findViewById(R.id.pBar);
        progressBar.setVisibility(View.GONE);
        imageView = (ImageView) findViewById(R.id.imageView);

        //Asking permission to access user's gallery
        if (shouldAskPermissions()) {
            askPermissions();
        }
        //
        Button camera_button = (Button) findViewById(R.id.CameraButton);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        Button gallery_button = (Button) findViewById(R.id.galleryButton);
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, urlRequest+"index.json", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        //traitement du fichier json
                        try {
                            String vocabs = json.getString("vocabulary");
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRequest+vocabs, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //	Display	the	first	500	characters	of	the	response	string.
                                    vocab = writeToFile(response, "vocabulary.yml");

                                }
                            },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                        }
                                    });
                            queue.add(stringRequest);
                            JSONArray brands = json.getJSONArray("brands");
                            for (int i = 0; i<brands.length(); i++){
                                JSONObject x = brands.getJSONObject(i);
                                brandsList.add(new Brand(x.getString("brandname"), x.getString("url"),x.getString("classifier")));
                                JSONArray imgs = x.getJSONArray("images");
                                for (int j = 0; j<imgs.length(); j++){
                                    String y = imgs.getString(j);
                                    brandsList.get(i).setImgNames(y);
                                }
                                brandsList.get(i).setClassifier(queue, urlRequest);
                                brandsList.get(i).setImage(queue, urlRequest);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        queue.add(jsonRequest);

        //getClassifier part

        Button Analysis_Button = (Button) findViewById(R.id.Analyse_Button);
        Analysis_Button.setOnClickListener(this);

        Button Crop_Button = (Button) findViewById(R.id.Crop_Button);
        Crop_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri cropURI = Uri.parse("file:///" + currentPhotoPath);
                imageView.setImageDrawable(null);
                beginCrop(cropURI);

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            System.out.println("Into Activity Result");
            mustBeRotated = true;
            ImageViewPrint();
            System.out.println("Set pic OK ");
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            currentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            imageView.setRotation(0);
            mustBeRotated = false;
            ImageViewPrint();
        }

        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                final Mat MatVocab;
                //Loader.load(opencv_core.class);
                opencv_core.CvFileStorage storage = cvOpenFileStorage(vocab.getAbsolutePath(), null, CV_STORAGE_READ);
                Pointer p = cvReadByName(storage, null, "vocabulary", cvAttrList());
                opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
                MatVocab = new Mat(cvMat);
                Log.i("Mat Vocab", "vocabulary loaded " + MatVocab.rows() + " x " + MatVocab.cols());
                cvReleaseFileStorage(storage);
                //create SIFT feature point extracter

                detector = new SIFT(0, 3, 0.04, 10, 1.6);
                //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
                matcher = new FlannBasedMatcher();

                //create BoF (or BoW) descriptor extractor
                bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);
                //Set the dictionary with the vocabulary we created in the first step
                bowide.setVocabulary(MatVocab);
                Log.i("vocab status", "vocab is set");
                class_names = new String[brandsList.size()];

                for (int i = 0; i< brandsList.size(); i++){
                    class_names[i] = brandsList.get(i).getName();
                }
                classifiers = new CvSVM[brandsList.size()];

                for (int i = 0; i < brandsList.size(); i++) {
                    Log.i("Ca marche", "Ok. Creating class name from " + class_names[i]);
                    //open the file to write the resultant descriptor
                    classifiers[i] = new CvSVM();
                    classifiers[i].load(brandsList.get(i).getClassifier().getAbsolutePath());
                }
                Log.i("Je valide", "ok");
                Mat imageTest = imread(currentPhotoPath, 1);
                System.out.println("Lecture image ok");

                // Using detector to get keypoints
                detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
                System.out.println("Detect ok");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Detection ok ! Best match : ",Toast.LENGTH_LONG).show();
                    }
                });

                bowide.compute(imageTest, keypoints, response_hist);
                // Finding best match
                System.out.println("Compute ok");
                float minf = Float.MAX_VALUE;
                String bestMatch = null;
                long timePrediction = System.currentTimeMillis();
                // loop for all classes
                for (int j = 0; j < brandsList.size(); j++) {
                    // classifier prediction based on reconstructed histogram
                    float res = classifiers[j].predict(response_hist, true);
                    //System.out.println(class_names[i] + " is " + res);
                    if (res < minf) {
                        minf = res;
                        bestMatch = class_names[j];
                    }
                }

                timePrediction = System.currentTimeMillis() - timePrediction;
                Log.i("Test", currentPhotoPath + "  predicted as " + bestMatch + " in " + timePrediction + " ms");
                final Context context = getApplicationContext();
                for (int i =0; i<brandsList.size(); i++){
                    if (brandsList.get(i).getName() == bestMatch){
                        Intent analysisIntent = new Intent(MainActivity.this, Analysis.class);
                        brandsList.get(i).setUri(getImageUri(context, brandsList.get(i).getImage()));
                        Bundle extras = new Bundle();
                        extras.putString("URL", brandsList.get(i).getUrl());
                        extras.putParcelable("URI", brandsList.get(i).getUri());
                        analysisIntent.putExtras(extras);
                        MainActivity.this.startActivity(analysisIntent);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
        Toast.makeText(getApplicationContext(),"Calculating...",Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        thread.start();
    }
}