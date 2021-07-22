package com.sanj.digote.activities.cloudActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.sanj.digote.R;
import com.sanj.digote.activities.localActivities.VaultLogin;
import com.sanj.digote.data.NoteDB;
import com.sanj.digote.data.URLs;
import com.sanj.digote.wrappers.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.sanj.digote.wrappers.Helper.vaultPassword;

public class CloudUpdateDetails extends AppCompatActivity implements TextWatcher {
    private TextInputEditText firstName, secondName, email;
    private Button btnUpdate;
    private NoteDB noteDB;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_update_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(R.drawable.ic_back);
        noteDB = NoteDB.getInstance(this);

        firstName = findViewById(R.id.firstName);
        secondName = findViewById(R.id.secondName);
        email = findViewById(R.id.email);
        btnUpdate = findViewById(R.id.btnUpdate);
        firstName.addTextChangedListener(this);
        secondName.addTextChangedListener(this);
        email.addTextChangedListener(this);

        sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);

        btnUpdate.setVisibility(View.GONE);

        btnUpdate.setOnClickListener(v -> update());
    }

    private void update() {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Updating account details", this);
        Runnable updateUserDetailsThread = () -> {
            runOnUiThread(waitingDialog::show);

            String userEmail, userName;
            userEmail = email.getText().toString().trim();
            userName = firstName.getText().toString().trim() + " " + secondName.getText().toString().trim();

            HashMap<String, String> params = new HashMap<>();
            params.put("email", userEmail);
            params.put("name", userName);
            params.put("userId", sharedPreferences.getString("cloudId", "default"));

            StringRequest request = new StringRequest(Request.Method.POST, URLs.updateUserDetailsUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");


                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().successToast(responseMessage, CloudUpdateDetails.this);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudUpdateDetails.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(waitingDialog::dismiss);
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().messageDialog("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudUpdateDetails.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(updateUserDetailsThread).start();

    }


    private void appearNDisappearButton() {
        boolean canUnleashButton = !Objects.requireNonNull(firstName.getText()).toString().isEmpty() &&
                !Objects.requireNonNull(secondName.getText()).toString().isEmpty() && !Objects.requireNonNull(email.getText()).toString().isEmpty() && email.getText().toString().endsWith("@gmail.com");
        if (canUnleashButton) {
            btnUpdate.setVisibility(View.VISIBLE);
        } else {
            btnUpdate.setVisibility(View.GONE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cloud_update_details_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuSecurity) {
            updateSecurity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSecurity() {
        Intent intent = new Intent(this, VaultLogin.class);
        intent.putExtra("isVerificationOnly", true);
        startActivity(intent);
    }

    private void updateSecurityThread(String password) {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Updating account details", this);
        Runnable updateUserSecurityThread = () -> {
            runOnUiThread(waitingDialog::show);

            HashMap<String, String> params = new HashMap<>();
            params.put("password", password);
            params.put("userId", sharedPreferences.getString("cloudId", "default"));

            StringRequest request = new StringRequest(Request.Method.POST, URLs.updateUserSecurityUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");


                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().successToast(responseMessage, CloudUpdateDetails.this);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudUpdateDetails.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(waitingDialog::dismiss);
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().messageDialog("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudUpdateDetails.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(updateUserSecurityThread).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        vaultPassword = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserData();
        if (vaultPassword != null) {
            if (noteDB.vaultVerification(vaultPassword)) {
                updateSecurityDialog();
            } else {
                new Helper().errorToast("Incorrect PIN", this);
            }
        }

    }

    private void updateSecurityDialog() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.cloud_security_update_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle("Update Cloud Security")
                .setView(view)
                .create();
        dialog.show();
        TextInputEditText edPassword = view.findViewById(R.id.edPassword);
        TextInputEditText edConfirmPassword = view.findViewById(R.id.edConfirmPassword);
        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(edPassword.getText().toString()) || TextUtils.isEmpty(edConfirmPassword.getText().toString()))) {
                if (edPassword.getText().toString().trim().equals(edConfirmPassword.getText().toString().trim())) {
                    updateSecurityThread(edPassword.getText().toString().trim());
                    dialog.dismiss();
                } else {
                    new Helper().errorToast("Passwords provided do not match", CloudUpdateDetails.this);
                }
            } else {
                new Helper().errorToast("Submitting empty fields", CloudUpdateDetails.this);
            }
        });
    }

    private void loadUserData() {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence fetching user details", this);

        Runnable fetchSingleUserDetailsThread = () -> {
            runOnUiThread(waitingDialog::show);
            HashMap<String, String> params = new HashMap<>();
            params.put("userId", sharedPreferences.getString("cloudId", "default"));

            StringRequest request = new StringRequest(Request.Method.POST, URLs.fetchSingleUserDetailsUrl, response -> {
                try {
                    runOnUiThread(waitingDialog::dismiss);
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");

                    if (responseCode.equals("1")) {
                        String userEmail = "", userName = "";
                        JSONArray noteJSONArray = new JSONArray(responseObject.getString("responseData"));
                        for (int i = 0; i < noteJSONArray.length(); i++) {
                            JSONObject responseArrayObject = noteJSONArray.getJSONObject(i);
                            userName = responseArrayObject.getString("name");
                            userEmail = responseArrayObject.getString("email");
                        }
                        String finalUserName = userName;
                        String finalUserEmail = userEmail;
                        runOnUiThread(() -> {
                            String[] names = finalUserName.split(" ");
                            firstName.setText(names[0]);
                            secondName.setText(names[1]);
                            email.setText(finalUserEmail);
                        });
                    } else {
                        String responseMessage = responseObject.getString("responseMessage");
                        runOnUiThread(() -> new Helper().messageDialog(responseMessage, CloudUpdateDetails.this));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    finish();
                }
            }, error -> runOnUiThread(() -> {
                finish();
                waitingDialog.dismiss();
                new Helper().errorToast("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudUpdateDetails.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(fetchSingleUserDetailsThread).start();

    }
}