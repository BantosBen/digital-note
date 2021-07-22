package com.sanj.digote.activities.cloudActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sanj.EncryptionAlgorithm;
import com.sanj.digote.R;
import com.sanj.digote.data.NoteDB;
import com.sanj.digote.data.URLs;
import com.sanj.digote.models.NoteModel;
import com.sanj.digote.wrappers.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CloudAddNote extends AppCompatActivity {

    boolean isUpdate, isUpdateButtonClicked, isTextEditedNotSaved, isColorEditedNotSaved;
    private String YEAR, MONTH, DAY, noteId, cardColor;
    private EditText edTitle, edNote;
    private TextView txtNoteView;
    private ScrollView txtNoteViewScroll;
    private NoteModel noteModel;
    private NoteDB noteDB;
    private SharedPreferences sharedPreferences;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_add_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(R.drawable.ic_back);

        edTitle = findViewById(R.id.edTitle);
        edNote = findViewById(R.id.edNote);
        txtNoteView = findViewById(R.id.txtNoteView);
        txtNoteViewScroll = findViewById(R.id.txtNoteViewScroll);
        String[] months_of_year = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar calendar = Calendar.getInstance();
        YEAR = String.valueOf(calendar.get(Calendar.YEAR));
        MONTH = months_of_year[calendar.get(Calendar.MONTH)];
        DAY = String.valueOf(calendar.get(Calendar.DATE));
        noteDB = NoteDB.getInstance(this);

        isTextEditedNotSaved = false;
        isColorEditedNotSaved = false;
        isUpdateButtonClicked = false;

        isUpdate = getIntent().getExtras() != null;
        if (isUpdate) {
            noteId = getIntent().getExtras().getString("id");
            edNote.setVisibility(View.GONE);
            edTitle.setEnabled(false);
            txtNoteViewScroll.setVisibility(View.VISIBLE);
        } else {
            edTitle.setEnabled(true);
            edNote.setVisibility(View.VISIBLE);
            txtNoteViewScroll.setVisibility(View.GONE);
        }
        cardColor = "White";
        sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);
        initNote(sharedPreferences.getString("fontSize", "Medium"));
    }


    private void initNote(String string) {
        switch (string) {
            case "Medium":
                edTitle.setTextSize(15L);
                edNote.setTextSize(15L);
                txtNoteView.setTextSize(15L);
                break;
            case "Small":
                edTitle.setTextSize(12L);
                edNote.setTextSize(12L);
                txtNoteView.setTextSize(12L);
                break;
            case "Large":
                edTitle.setTextSize(18L);
                edNote.setTextSize(18L);
                txtNoteView.setTextSize(18L);
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cloud_add_note_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menuAction);
        if (!isUpdate) {
            menuItem.setVisible(false);
            menuItem = menu.findItem(R.id.menuDownload);
            menuItem.setVisible(false);
            menuItem = menu.findItem(R.id.menuDelete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDownload:
                downloadNote();
                break;
            case R.id.menuDelete:
                if (isUpdate) {
                    deleteNote();
                } else {
                    new Helper().errorToast("No selected note", this);
                }
                break;
            case R.id.menuColor:
                pickColor();
                break;
            case R.id.menuAction:
                setAction(item);
                break;
            case R.id.menuUpload:
                if (sharedPreferences.getString("cloudId", "default").equals("default")) {
                    new Helper().errorToast("Login your account first to upload you note...", CloudAddNote.this);
                    Intent intent = new Intent(CloudAddNote.this, CloudSinIn.class);
                    intent.putExtra("onlyLogin", true);
                    startActivity(intent);
                } else {
                    if (!TextUtils.isEmpty(edTitle.getText().toString().trim())) {
                        uploadNoteToCloud();
                    } else {
                        new Helper().errorToast("Provide title for the note!!", this);
                    }
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadNote() {
        if (noteDB.getSingleNote(noteModel.getId()) != null) {
            if (noteDB.updateNote(noteModel)) {
                new Helper().successToast("Note downloaded!!", this);
                finish();
            } else {
                new Helper().errorToast("Failed to downloaded!!", this);
            }
        } else {
            if (noteDB.addNewNote(noteModel)) {
                new Helper().successToast("Note downloaded!!", this);
                finish();
            } else {
                new Helper().errorToast("Failed to downloaded!!", this);
            }
        }
    }


    private void uploadNoteToCloud() {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Uploading note", this);
        Runnable addNewNoteThread = () -> {
            runOnUiThread(waitingDialog::show);

            HashMap<String, String> params = new HashMap<>();
            if (isUpdate) {
                params.put("noteId", noteModel.getId());
            } else {
                params.put("noteId", String.valueOf(new Date().getTime()));
            }
            params.put("userId", sharedPreferences.getString("cloudId", "default"));

            String encryptedNote= "";
            try {
                encryptedNote = new EncryptionAlgorithm().encrypt(edNote.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            params.put("title", edTitle.getText().toString());
            params.put("text", encryptedNote);
            params.put("color", cardColor);

            String date = MONTH + " " + DAY + " " + YEAR;
            params.put("date", date);

            params.put("timestamp", String.valueOf(new Date().getTime()));
            params.put("security", "0");

            StringRequest request = new StringRequest(Request.Method.POST, URLs.addNewNoteUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");


                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            isTextEditedNotSaved = false;
                            isColorEditedNotSaved = false;
                            isUpdateButtonClicked = true;
                            waitingDialog.dismiss();
                            new Helper().successToast(responseMessage, CloudAddNote.this);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudAddNote.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(waitingDialog::dismiss);
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().messageDialog("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudAddNote.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(addNewNoteThread).start();

    }

    private void setAction(MenuItem item) {
        if (item.getTitle().equals("Edit")) {
            edTitle.setEnabled(true);
            edNote.setVisibility(View.VISIBLE);
            txtNoteViewScroll.setVisibility(View.GONE);
            item.setTitle("View");
            item.setIcon(R.drawable.ic_view);
        } else {
            edTitle.setEnabled(false);
            edNote.setVisibility(View.GONE);
            txtNoteView.setText(edNote.getText().toString());
            txtNoteViewScroll.setVisibility(View.VISIBLE);
            item.setTitle("Edit");
            item.setIcon(R.drawable.ic_edit);
        }
    }


    private void pickColor() {
        CharSequence[] colorChoice = new CharSequence[]{"White", "Blue", "Red", "Green", "Orange"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Color")
                .setSingleChoiceItems(colorChoice, 0, (dialog, which) -> {
                    cardColor = String.valueOf(colorChoice[which]);
                    isColorEditedNotSaved = true;
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void deleteNote() {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Deleting Note", this);
        Runnable deleteUserNoteThread = () -> {
            runOnUiThread(waitingDialog::show);

            HashMap<String, String> params = new HashMap<>();
            params.put("userId", sharedPreferences.getString("cloudId", "default"));
            params.put("noteId", noteId);


            StringRequest request = new StringRequest(Request.Method.POST, URLs.deleteUserNoteUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");
                    String responseMessage = responseObject.getString("responseMessage");

                    if (responseCode.equals("1")) {
                        runOnUiThread(() -> {
                            new Helper().successToast(responseMessage, CloudAddNote.this);
                            waitingDialog.dismiss();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            waitingDialog.dismiss();
                            new Helper().messageDialog(responseMessage, CloudAddNote.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    finish();
                    runOnUiThread(waitingDialog::dismiss);
                }
            }, error -> runOnUiThread(() -> {
                finish();
                waitingDialog.dismiss();
                new Helper().messageDialog("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudAddNote.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(deleteUserNoteThread).start();

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (isUpdate) {
            loadNote();
        }
    }

    private void loadNote() {
        android.app.AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Fetching Note", this);
        Runnable fetchSingleNoteThread = () -> {
            runOnUiThread(waitingDialog::show);

            HashMap<String, String> params = new HashMap<>();
            params.put("userId", sharedPreferences.getString("cloudId", "default"));
            params.put("noteId", noteId);


            StringRequest request = new StringRequest(Request.Method.POST, URLs.fetchSingleUserNoteUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");


                    if (responseCode.equals("1")) {
                        JSONArray noteJSONArray = new JSONArray(responseObject.getString("responseData"));
                        for (int i = 0; i < noteJSONArray.length(); i++) {
                            JSONObject responseArrayObject = noteJSONArray.getJSONObject(i);
                            String plainText=new EncryptionAlgorithm().decrypt(responseArrayObject.getString("note"));
                            noteModel = new NoteModel(responseArrayObject.getString("note_id"),
                                    responseArrayObject.getString("title"), plainText,
                                    responseArrayObject.getString("date"), responseArrayObject.getString("color"));
                        }
                        runOnUiThread(() -> {
                            edTitle.setText(noteModel.getTitle());
                            edNote.setText(noteModel.getNote());
                            txtNoteView.setText(noteModel.getNote());
                            cardColor = noteModel.getColor();
                            waitingDialog.dismiss();
                        });
                    } else {
                        String responseMessage = responseObject.getString("responseMessage");
                        runOnUiThread(() -> {
                            finish();
                            waitingDialog.dismiss();
                            new Helper().errorToast(responseMessage, CloudAddNote.this);
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                    runOnUiThread(waitingDialog::dismiss);
                }
            }, error -> runOnUiThread(() -> {
                finish();
                waitingDialog.dismiss();
                new Helper().errorToast("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudAddNote.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(fetchSingleNoteThread).start();

    }

    @Override
    public void onBackPressed() {
        if (isUpdate) {
            isTextEditedNotSaved = !noteModel.getTitle().equals(edTitle.getText().toString()) || !noteModel.getNote().equals(edNote.getText().toString());
        }
        if (isTextEditedNotSaved || isColorEditedNotSaved && !TextUtils.isEmpty(edTitle.getText().toString().trim()) && !isUpdateButtonClicked) {
            new AlertDialog.Builder(this)
                    .setTitle("Digital Note")
                    .setMessage("Do you wan to save changes?")
                    .setPositiveButton("Upload", (dialog, which) -> uploadNoteToCloud())
                    .setNegativeButton("Discard", (dialog, which) -> {
                        dialog.dismiss();
                        super.onBackPressed();
                    })
                    .create()
                    .show();
        } else {
            super.onBackPressed();
        }
    }

}