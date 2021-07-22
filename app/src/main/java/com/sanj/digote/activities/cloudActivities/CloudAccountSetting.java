package com.sanj.digote.activities.cloudActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sanj.digote.wrappers.Helper.vaultPassword;

public class CloudAccountSetting extends AppCompatActivity {
    private String userName = "";
    private TextView txtInitial, txtWelcome;
    private NoteDB noteDB;
    private boolean isSecurity, isDelete;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_account_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        txtInitial = findViewById(R.id.txtInitial);
        txtWelcome = findViewById(R.id.txtWelcome);
        CardView cardSecurity = findViewById(R.id.cardSecurity);
        CardView cardDelete = findViewById(R.id.cardDelete);
        CardView cardAboutUs = findViewById(R.id.cardAboutUs);
        CardView cardSupport = findViewById(R.id.cardSupport);
        noteDB = NoteDB.getInstance(this);
        sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);

        TextView btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(CloudAccountSetting.this, CloudUpdateDetails.class);
            startActivity(intent);
        });
        vaultPassword = null;
        cardSecurity.setOnClickListener(v -> {
            isSecurity = true;
            isDelete = false;
            verifySecurityQuestion();
        });
        cardDelete.setOnClickListener(v -> {
            isDelete = true;
            isSecurity = false;
            verifySecurityQuestion();
        });
        cardAboutUs.setOnClickListener(v -> aboutUs());
        cardSupport.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.support_message_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle("Contact Support")
                .setView(view)
                .create();
        dialog.show();
        TextInputEditText edMessage = view.findViewById(R.id.edMessage);
        Button btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(edMessage.getText().toString().trim())) {
                dialog.dismiss();
                sendMessageThread(edMessage.getText().toString());
            } else {
                new Helper().errorToast("Failed! Sending empty message", CloudAccountSetting.this);
            }
        });
    }

    private void sendMessageThread(String message) {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Contacting Support", this);
        Runnable contactSupportThread = () -> {
            runOnUiThread(waitingDialog::show);

            HashMap<String, String> params = new HashMap<>();
            params.put("userId", sharedPreferences.getString("cloudId", "default"));
            params.put("userName", userName);
            params.put("message", message);

            StringRequest request = new StringRequest(Request.Method.POST, URLs.contactSupportUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");

                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            new Helper().successToast(responseMessage, CloudAccountSetting.this);
                            waitingDialog.dismiss();
                        });
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudAccountSetting.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        waitingDialog.dismiss();
                        new Helper().messageDialog(e.getMessage(), CloudAccountSetting.this);
                    });
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().messageDialog(error.getMessage(), CloudAccountSetting.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(contactSupportThread).start();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserData();
        if (vaultPassword != null) {
            if (noteDB.vaultVerification(vaultPassword)) {
                if (isSecurity) {
                    addQuestion();
                } else if (isDelete) {
                    deleteAccount();
                }

            } else {
                new Helper().errorToast("Incorrect PIN", this);
            }
        } else {
            theVault();
        }

    }

    private void deleteAccount() {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Deleting Account", this);
        Runnable deleteAccountThread = () -> {
            runOnUiThread(waitingDialog::show);

            HashMap<String, String> params = new HashMap<>();
            params.put("userId", sharedPreferences.getString("cloudId", "default"));

            StringRequest request = new StringRequest(Request.Method.POST, URLs.deleteUserAccountUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");

                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("cloudId", "default");
                            editor.apply();
                            Helper.accountDeleted = true;
                            new Helper().successToast(responseMessage, CloudAccountSetting.this);
                            waitingDialog.dismiss();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudAccountSetting.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        waitingDialog.dismiss();
                        new Helper().messageDialog(e.getMessage(), CloudAccountSetting.this);
                    });
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().messageDialog("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudAccountSetting.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(deleteAccountThread).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        vaultPassword = null;
    }

    private void theVault() {
        if (!noteDB.checkVaultExistence()) {
            addQuestion();
        }
    }

    private void addQuestion() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.vault_credentials_item, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle("Vault Credentials")
                .setView(view)
                .create();
        dialog.show();
        TextInputEditText edPin = view.findViewById(R.id.edPin);
        TextInputEditText edConfirmPin = view.findViewById(R.id.edConfirmPin);
        Button btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(edPin.getText().toString()) || TextUtils.isEmpty(edConfirmPin.getText().toString()))) {
                if (edPin.getText().toString().trim().equals(edConfirmPin.getText().toString().trim())) {
                    if (noteDB.addNewVaultCredentials(String.valueOf(new Date().getTime()), edPin.getText().toString().trim())) {
                        new Helper().successToast("Vault Credentials saved!!", CloudAccountSetting.this);
                        dialog.dismiss();
                    } else {
                        new Helper().errorToast("Failed to save credentials", CloudAccountSetting.this);
                    }
                } else {
                    new Helper().errorToast("Pins provided do not match", CloudAccountSetting.this);
                }
            } else {
                new Helper().errorToast("Failed! Submitting empty fields", CloudAccountSetting.this);
            }
        });
    }

    private void verifySecurityQuestion() {
        Intent intent = new Intent(this, VaultLogin.class);
        intent.putExtra("isVerificationOnly", true);
        startActivity(intent);
    }

    private void loadUserData() {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence fetching user details", this);

        Runnable fetchUserDataThread = () -> {
            runOnUiThread(waitingDialog::show);
            HashMap<String, String> params = new HashMap<>();
            params.put("userId", sharedPreferences.getString("cloudId", "default"));

            StringRequest request = new StringRequest(Request.Method.POST, URLs.fetchSingleUserDetailsUrl, response -> {
                try {
                    runOnUiThread(waitingDialog::dismiss);
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");

                    if (responseCode.equals("1")) {
                        JSONArray noteJSONArray = new JSONArray(responseObject.getString("responseData"));
                        for (int i = 0; i < noteJSONArray.length(); i++) {
                            JSONObject responseArrayObject = noteJSONArray.getJSONObject(i);
                            userName = responseArrayObject.getString("name");
                        }
                        runOnUiThread(() -> {
                            txtInitial.setText(String.valueOf(userName.charAt(0)));
                            txtWelcome.setText(String.format("Welcome, %s", userName));
                        });
                    } else {
                        String responseMessage = responseObject.getString("responseMessage");
                        runOnUiThread(() -> {
                            finish();
                            new Helper().messageDialog(responseMessage, CloudAccountSetting.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    finish();
                }
            }, error -> runOnUiThread(() -> {
                finish();
                waitingDialog.dismiss();
                new Helper().errorToast("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudAccountSetting.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(fetchUserDataThread).start();

    }

    private void aboutUs() {
        String about = "Digital Note 4.1.3\n" +
                "Build #AI-201.8743.12.41.7199119, built on July 17, 2021\n" +
                "Runtime version: 1.8.0_242-release-1644-b01 amd64\n" +
                "Android version 5 and above\n\n\n" +
                "Powered by <SANJ Inc./>";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About DIGITAL NOTE")
                .setMessage(about)
                .create()
                .show();
    }
}