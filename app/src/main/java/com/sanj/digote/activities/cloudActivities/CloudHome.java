package com.sanj.digote.activities.cloudActivities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sanj.EncryptionAlgorithm;
import com.sanj.digote.R;
import com.sanj.digote.adapters.NoteRecyclerViewAdapter;
import com.sanj.digote.data.URLs;
import com.sanj.digote.models.NoteModel;
import com.sanj.digote.wrappers.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudHome extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView txtNoNote;
    private String mUserId;
    private List<NoteModel> noteModelList;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mUserId = getIntent().getExtras().getString("userId");
        recyclerView = findViewById(R.id.recyclerView);
        txtNoNote = findViewById(R.id.txtNoNote);
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(CloudHome.this, CloudAddNote.class)));
        noteModelList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Helper.accountDeleted) {
            Helper.accountDeleted = false;
            finish();
        } else {
            loadNotes();
        }

    }

    private void loadNotes() {
        noteModelList = new ArrayList<>();
        AlertDialog waitingDialog = new Helper().waitingDialog("Intelligence Fetching Notes", this);
        Runnable fetchUserNotesThread = () -> {
            runOnUiThread(waitingDialog::show);

            HashMap<String, String> params = new HashMap<>();
            params.put("userId", mUserId);
            params.put("sort", sharedPreferences.getString("sort", "Date"));


            StringRequest request = new StringRequest(Request.Method.POST, URLs.fetchUserNotesUrl, response -> {
                try {
                    JSONObject responseObject = new JSONObject(response);
                    String responseCode = responseObject.getString("responseCode");


                    if (responseCode.equals("1")) {
                        JSONArray noteJSONArray = new JSONArray(responseObject.getString("responseData"));
                        for (int i = 0; i < noteJSONArray.length(); i++) {
                            JSONObject responseArrayObject = noteJSONArray.getJSONObject(i);
                            String plainText=new EncryptionAlgorithm().decrypt(responseArrayObject.getString("note"));
                            noteModelList.add(new NoteModel(responseArrayObject.getString("note_id"),
                                    responseArrayObject.getString("title"), plainText,
                                    responseArrayObject.getString("date"), responseArrayObject.getString("color")));
                        }
                        runOnUiThread(() -> {
                            recyclerView.setVisibility(View.VISIBLE);
                            txtNoNote.setVisibility(View.GONE);
                            loadNotesToRecyclerView();
                            waitingDialog.dismiss();
                        });
                    } else {
                        String responseMessage = responseObject.getString("responseMessage");
                        runOnUiThread(() -> {
                            recyclerView.setVisibility(View.GONE);
                            txtNoNote.setVisibility(View.VISIBLE);
                            waitingDialog.dismiss();
                            new Helper().errorToast(responseMessage, CloudHome.this);
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(waitingDialog::dismiss);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, error -> runOnUiThread(() -> {
                waitingDialog.dismiss();
                new Helper().errorToast("Oops! Sorry failed to connect to server please check your internet connection and try again later", CloudHome.this);
            })) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };
            Volley.newRequestQueue(getApplicationContext()).add(request);
        };
        new Thread(fetchUserNotesThread).start();

    }

    private void loadNotesToRecyclerView() {
        if (sharedPreferences.getString("arrangement", "").equals("grid")) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        NoteRecyclerViewAdapter adapter = new NoteRecyclerViewAdapter(noteModelList, this, true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cloud_home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menuArrange);
        if (sharedPreferences.getString("arrangement", "").equals("grid")) {
            menuItem.setTitle("ListView");
        } else {
            menuItem.setTitle("GridView");
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSignOut:
                signOut();
                break;
            case R.id.menuArrange:
                changeArrangement(item);
                break;
            case R.id.menuAccount:
                startActivity(new Intent(CloudHome.this, CloudAccountSetting.class));
                break;
        }
        return true;
    }

    private void signOut() {
        editor.putString("cloudId", "default");
        editor.apply();
        finish();
    }

    private void changeArrangement(MenuItem menuItem) {
        if (menuItem.getTitle().equals("ListView")) {
            editor.putString("arrangement", "list");
            menuItem.setTitle("GridView");
        } else {
            editor.putString("arrangement", "grid");
            menuItem.setTitle("ListView");
        }
        editor.apply();
        loadNotesToRecyclerView();

    }
}