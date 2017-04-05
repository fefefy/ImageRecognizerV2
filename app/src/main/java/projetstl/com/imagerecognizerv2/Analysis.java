package projetstl.com.imagerecognizerv2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

/**
 * This Activity permits to handle the result View and all that it implies
 */
public class Analysis extends AppCompatActivity {
    String WebSiteURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_analysis);
        ImageView AnalysisImageView = (ImageView) findViewById(R.id.AnalysisImageView);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        WebSiteURL = extras.getString("URL");

        System.out.println(" Ceci est un test ");

        Uri result = extras.getParcelable("URI");
        AnalysisImageView.setImageURI(result);
        Log.i("image : ", result.toString());
        Button bWebsite = (Button) findViewById(R.id.WebsiteButton);

        bWebsite.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebSiteURL));
                startActivity(webIntent);
            }
        });
    }
}