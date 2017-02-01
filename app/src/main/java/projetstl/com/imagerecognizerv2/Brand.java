package projetstl.com.imagerecognizerv2;


import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Brand {
    JSONObject jsonObj;
    String ServerURL = ("http://www-rech.telecom-lille.fr/nonfreesift/");
    String name;
    String classifierFileName;
    List<String> StringList;
    //RequestQueue queue = Volley.newRequestQueue(this);
    TextView tv;


    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, ServerURL, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject json) {
                    try {
                        String name = jsonObj.getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    tv.setText("That didn't work!");
                }
            }
    );

    public Brand() throws MalformedURLException {
    }
}
