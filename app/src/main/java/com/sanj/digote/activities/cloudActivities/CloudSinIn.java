package com.sanj.digote.activities.cloudActivities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sanj.digote.R;
import com.sanj.digote.data.URLs;
import com.sanj.digote.wrappers.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CloudSinIn extends AppCompatActivity implements TextWatcher {
    private EditText edEmail, edPassword;
    private Button btnSignIn;
    private SharedPreferences.Editor editor;
    private boolean isOnlyLogin;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_sin_in);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(R.drawable.ic_back);

        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        TextView txtToSignUp = findViewById(R.id.txtToSignUp);
        isOnlyLogin = getIntent().getExtras() != null;

        SharedPreferences sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        txtToSignUp.setOnClickListener(v -> startActivity(new Intent(CloudSinIn.this, CloudSignUp.class)));
        btnSignIn.setVisibility(View.GONE);

        edEmail.addTextChangedListener(this);
        edPassword.addTextChangedListener(this);

        btnSignIn.setOnClickListener(v -> authentication());


    }

    private void authentication() {
        AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Authenticating user", this);
        Runnable authenticationThread = () -> {
            runOnUiThread(waitingDialog::show);
            String userEmail, userPassword;
            userEmail = edEmail.getText().toString().trim();
            userPassword = edPassword.getText().toString().trim();

            HashMap<String, String> params = new HashMap<>();
            params.put("email", userEmail);
            params.put("password", userPassword);

            StringRequest request = new StringRequest(Request.Method.POST, URLs.verifyUserUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");


                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().successToast(responseMessage, CloudSinIn.this);
                        });
                        String mUserId = responseObject.getString("responseData");
                        editor.putString("cloudId", mUserId);
                        editor.apply();
                        if (!isOnlyLogin) {
                            Intent intent = new Intent(CloudSinIn.this, CloudHome.class);
                            intent.putExtra("userId", mUserId);
                            startActivity(intent);
                        }
                        finish();
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudSinIn.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(waitingDialog::dismiss);
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().messageDialog("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudSinIn.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(authenticationThread).start();

    }

    private void appearNDisappearButtonSinIn() {
        if (!edPassword.getText().toString().isEmpty() && !edEmail.getText().toString().isEmpty() &&
                edEmail.getText().toString().endsWith("@gmail.com") && edPassword.getText().toString().length() >= 6) {
            btnSignIn.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.GONE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        appearNDisappearButtonSinIn();
    }
}