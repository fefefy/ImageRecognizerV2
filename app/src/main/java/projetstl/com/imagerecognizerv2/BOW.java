package projetstl.com.imagerecognizerv2;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_nonfree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;

public class BOW extends AppCompatActivity {

    // SIFT keypoint features
    private static final int N_FEATURES = 0;
    private static final int N_OCTAVE_LAYERS = 3;
    private static final double CONTRAST_THRESHOLD = 0.04;
    private static final double EDGE_THRESHOLD = 10;
    private static final double SIGMA = 1.6;

    public opencv_core.Mat img;
    private opencv_nonfree.SIFT SiftDesc;

    private String filePath;

    private ImageView imageView;
    private Bitmap inputImage;

    private File imageFile;
    private String CurrentPhotoPath;
    private static int LOAD_IMAGE = 1;
    private String ImageString;
    private String temp;
    private int READ_PERMISSION = 1;
    private Handler h = new Handler();
    private AssetManager assetManager;
    private ArrayList<String> listImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //prepare BOW descriptor extractor from the vocabulary already computed

        final String pathToVocabulary = "vocabulary.yml"; // to be define
        final opencv_core.Mat vocabulary;

        System.out.println("read vocabulary from file... ");
//        Loader.load(opencv_core.class);

        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(this.ToCache(this,
                "Data_BOW/vocabulary.yml", "vocabulary.yml").getAbsolutePath(), null, opencv_core.CV_STORAGE_READ);
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);
        System.out.println("vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(storage);


        //create SIFT feature point extracter
        final opencv_nonfree.SIFT detector;
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new opencv_nonfree.SIFT(0, 3, 0.04, 10, 1.6);

        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        final opencv_features2d.FlannBasedMatcher matcher;
        matcher = new opencv_features2d.FlannBasedMatcher();


        //create BoF (or BoW) descriptor extractor
        final opencv_features2d.BOWImgDescriptorExtractor bowide;
        bowide = new opencv_features2d.BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        //Set the dictionary with the vocabulary we created in the first step
        bowide.setVocabulary(vocabulary);
        System.out.println("Vocab is set");

        int classNumber = 3;
        String[] class_names;
        class_names = new String[classNumber];

        class_names[0] = "Coca";
        class_names[1] = "Pepsi";
        class_names[2] = "Sprite";


        final opencv_ml.CvSVM[] classifiers;
        classifiers = new opencv_ml.CvSVM[classNumber];
        for (int i = 0; i < classNumber; i++) {
            //System.out.println("Ok. Creating class name from " + className);
            //open the file to write the resultant descriptor
            classifiers[i] = new opencv_ml.CvSVM();
            classifiers[i].load("Data_BOW/classifiers/" + class_names[i] + ".xml");
            System.out.println("Classifiers : " + class_names[i]);
        }

        opencv_core.Mat response_hist = new opencv_core.Mat();
        opencv_features2d.KeyPoint keypoints = new opencv_features2d.KeyPoint();
        opencv_core.Mat inputDescriptors = new opencv_core.Mat();


        opencv_core.MatVector imagesVec;

        File root = new File("Data_BOW/TestImage");

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };


/*        File[] imageFiles = root.listFiles(imgFilter);
        imagesVec = new MatVector(imageFiles.length);

          Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
          IntBuffer labelsBuf = labels.createBuffer();*/

        System.out.println("Fin");
        return;
    }

    public static File ToCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(Path);
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
