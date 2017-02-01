package projetstl.com.imagerecognizerv2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class REST extends AppCompatActivity implements View.OnClickListener {

    private Button btRequest;
    private TextView textViewJson;
    private String url = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";

    private RequestQueue mVolleyRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btRequest = (Button) findViewById(R.id.ServerButton);
       //TODO Standby
        // textViewJson = (TextView) findViewById(R.id.textView);
        btRequest.setOnClickListener(this);
        //mVolleyRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ServerButton:
                downloadFile(url);
        }
    }

    protected void downloadFile(String url){
        //Create Queue
        RequestQueue queue = Volley.newRequestQueue(this);
        //Create CallBack
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        textViewJson.setText("Response is: " + response.substring(0, 500));
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textViewJson.setText("DID NOT WORK :/");
                    }
                }
        );
        queue.add(stringRequest);

    }
}
