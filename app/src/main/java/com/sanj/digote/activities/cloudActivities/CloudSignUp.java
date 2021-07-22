package com.sanj.digote.activities.cloudActivities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.sanj.digote.R;
import com.sanj.digote.data.URLs;
import com.sanj.digote.wrappers.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CloudSignUp extends AppCompatActivity implements TextWatcher {
    private TextInputEditText firstName, secondName, email, password, confirmPassword;
    private Button btnSinUp;
    private TextView txtInvalidEmail,txtInvalidPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(R.drawable.ic_back);

        firstName = findViewById(R.id.firstName);
        secondName = findViewById(R.id.secondName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        txtInvalidEmail=findViewById(R.id.txtInvalidEmail);
        txtInvalidPassword=findViewById(R.id.txtInvalidPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        btnSinUp = findViewById(R.id.btnSignUp);
        TextView txtToSignIn = findViewById(R.id.txtToSignIn);
        txtToSignIn.setOnClickListener(v -> finish());

        firstName.addTextChangedListener(this);
        secondName.addTextChangedListener(this);
        email.addTextChangedListener(this);
        password.addTextChangedListener(this);
        confirmPassword.addTextChangedListener(this);

        btnSinUp.setVisibility(View.GONE);

        btnSinUp.setOnClickListener(v -> registerUser());

    }

    private void registerUser() {
        AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Registering user", this);
        Runnable registrationThread = () -> {
            runOnUiThread(waitingDialog::show);

            String userEmail, userPassword, userName;
            userEmail = email.getText().toString().trim();
            userPassword = password.getText().toString().trim();
            userName = firstName.getText().toString().trim() + " " + secondName.getText().toString().trim();

            HashMap<String, String> params = new HashMap<>();
            params.put("email", userEmail);
            params.put("password", userPassword);
            params.put("name", userName);

            StringRequest request = new StringRequest(Request.Method.POST, URLs.createAccountUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");

                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().successToast(responseMessage, CloudSignUp.this);
                        });
                        finish();
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudSignUp.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(waitingDialog::dismiss);
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().messageDialog("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudSignUp.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };

        if (Objects.requireNonNull(password.getText()).toString().equals(Objects.requireNonNull(confirmPassword.getText()).toString())) {
            new Thread(registrationThread).start();
        } else {
            new Helper().errorToast("Password do not match", this);
        }
    }

    private void appearNDisappearButton() {
        if (!email.getText().toString().endsWith("@gmail.com")){
            txtInvalidEmail.setVisibility(View.VISIBLE);
        }else{
            txtInvalidEmail.setVisibility(View.GONE);
        }

        if (password.getText().toString().length() >= 6){
            txtInvalidPassword.setVisibility(View.GONE);
        }else{
            txtInvalidPassword.setVisibility(View.VISIBLE);
        }

        boolean canUnleashButton = !Objects.requireNonNull(firstName.getText()).toString().isEmpty() &&
                !Objects.requireNonNull(secondName.getText()).toString().isEmpty() && !Objects.requireNonNull(email.getText()).toString().isEmpty()
                && !Objects.requireNonNull(password.getText()).toString().isEmpty() && !Objects.requireNonNull(confirmPassword.getText()).toString().isEmpty()
                && password.getText().toString().length() >= 6 && email.getText().toString().endsWith("@gmail.com");
        if (canUnleashButton) {
            btnSinUp.setVisibility(View.VISIBLE);
        } else {
            btnSinUp.setVisibility(View.GONE);
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
        appearNDisappearButton();

    }
}