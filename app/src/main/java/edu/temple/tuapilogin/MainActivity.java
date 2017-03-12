package edu.temple.tuapilogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.*;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import static android.widget.Toast.*;

public class MainActivity extends AppCompatActivity {


    public String url1, url2, user, pass, tuId; //3 major strings for our application, the username, password and tuId
    EditText user1, pass1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        url1 ="https://prd-mobile.temple.edu/banner-mobileserver/api/2.0/grades/";
        url2 = "https://prd-mobile.temple.edu/banner-mobileserver/api/2.0/security/getUserInfo";
        final TextView textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());


        AndroidNetworking.initialize(getApplicationContext());
        final Button button = (Button) findViewById(R.id.button);

        user = sharedPref.getString("username", "");
        pass = sharedPref.getString("password", "");

        user1 = (EditText) findViewById(R.id.tu_username);
        pass1 = (EditText) findViewById(R.id.editText2);

        user1.setText(user);
        pass1.setText(pass);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                user = user1.getText().toString();
                pass = pass1.getText().toString();

                AndroidNetworking.get(url2)
                        .addHeaders("Authorization",
                                "Basic " + Base64.encodeToString((user + ":" + pass).getBytes(), Base64.NO_WRAP))
                        .build()
                        .getAsString(new StringRequestListener() {

                            @Override
                            public void onResponse(String response) {

                                // Respond to request
                                Log.v(MainActivity.class.getSimpleName(), response);

                                JSONObject obj = null; //Parses json data to get user's TUID from their username and password
                                try {
                                    obj = new JSONObject(response);
                                    tuId = obj.getString("userId");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                // Handle error
                                Log.v(MainActivity.class.getSimpleName(), anError.getErrorDetail());
                                textView.setText("Unvalid Username/Password");
                            }
                        });

                AndroidNetworking.get(url1 + tuId)
                        .addHeaders("Authorization",
                                "Basic " + Base64.encodeToString((user1.getText() + ":" + pass1.getText()).getBytes(), Base64.NO_WRAP))
                        .build() // NEEDED TO FINALIZE REQUEST
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {

                                //Save
                                SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();

                                editor.putString("username", user1.getText().toString());
                                editor.putString("password", pass1.getText().toString());
                                editor.apply();

                                // Respond to request
                                Log.v(MainActivity.class.getSimpleName(), response);
                                textView.setText(response);
                                EditText text1 = (EditText) findViewById(R.id.tu_username);
                                text1.setText("");
                                text1 = (EditText) findViewById(R.id.editText2);
                                text1.setText("");
                            }

                            @Override
                            public void onError(ANError anError) {
                                // Handle error
                                Log.v(MainActivity.class.getSimpleName(), anError.getErrorDetail());
                                textView.setText("Unvalid Username/Password");
                            }
                        });


            }
        });
    }

}