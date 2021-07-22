package com.sanj.digote.activities.localActivities;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sanj.digote.R;
import com.sanj.digote.activities.cloudActivities.CloudHome;
import com.sanj.digote.activities.cloudActivities.CloudSinIn;
import com.sanj.digote.adapters.NoteRecyclerViewAdapter;
import com.sanj.digote.data.NoteDB;
import com.sanj.digote.models.NoteModel;
import com.sanj.digote.wrappers.Helper;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private TextView txtNoNote;
    private EditText edSearch;
    private RecyclerView recyclerView;
    private NoteDB noteDB;
    private SharedPreferences sharedPreferences;
    private @SuppressLint("CommitPrefEdits")
    SharedPreferences.Editor editor;
    private Menu mMenu;


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        edSearch = findViewById(R.id.edSearch);
        recyclerView = findViewById(R.id.recyclerView);
        txtNoNote=findViewById(R.id.txtNoNote);
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        ImageView imgCloud = findViewById(R.id.imgCloud);
        imgCloud.setOnClickListener(v -> {
            String cloudId = sharedPreferences.getString("cloudId", "default");
            if (cloudId.equals("default")) {
                startActivity(new Intent(MainActivity.this, CloudSinIn.class));
            } else {
                Intent intent = new Intent(MainActivity.this, CloudHome.class);
                intent.putExtra("userId", cloudId);
                startActivity(intent);
            }
        });
        btnAdd.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddNote.class)));

        noteDB = NoteDB.getInstance(this);
        sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edSearch.getText().toString().isEmpty()) {
                    loadNotes();
                } else {
                    List<NoteModel> noteModelList = noteDB.getSearchUnSecuredNotes(edSearch.getText().toString());
                    if (noteModelList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        txtNoNote.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        txtNoNote.setVisibility(View.GONE);
                        if (sharedPreferences.getString("arrangement", "").equals("grid")) {
                            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                        } else {
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        }
                        NoteRecyclerViewAdapter adapter = new NoteRecyclerViewAdapter(noteModelList, MainActivity.this, false);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }
        });

        initSettings();

    }

    private void initSettings() {

        if (!(sharedPreferences.getBoolean("available", false))) {
            editor.putBoolean("available", true);
            editor.putString("arrangement", "grid");
            editor.putString("fontSize", "Medium");
            editor.putString("sort", "Date");
            editor.putString("cloudId", "default");
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        mMenu = menu;
        MenuItem menuItem = mMenu.findItem(R.id.menuArrange);
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
            case R.id.menuSetting:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.menuArrange:
                changeArrangement(item);
                break;
            case R.id.menuVault:
                theVault();
        }
        return true;
    }

    private void theVault() {
        if (noteDB.checkVaultExistence()) {
            startActivity(new Intent(this, VaultLogin.class));
        } else {
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
                if (!(TextUtils.isEmpty(Objects.requireNonNull(edPin.getText()).toString()) || TextUtils.isEmpty(Objects.requireNonNull(edConfirmPin.getText()).toString()))) {
                    if (edPin.getText().toString().trim().equals(edConfirmPin.getText().toString().trim())) {
                        if (noteDB.addNewVaultCredentials(String.valueOf(new Date().getTime()), edPin.getText().toString().trim())) {
                            new Helper().successToast("Vault Credentials saved!!", MainActivity.this);
                            startActivity(new Intent(MainActivity.this, VaultLogin.class));
                            dialog.dismiss();
                        } else {
                            new Helper().errorToast("Failed to save credentials", MainActivity.this);
                        }
                    } else {
                        new Helper().errorToast("Pins provided do not match", MainActivity.this);
                    }
                } else {
                    new Helper().errorToast("Submitting empty fields", MainActivity.this);
                }
            });
        }
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
        loadNotes();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadNotes();
        if (mMenu != null) {
            MenuItem menuItem = mMenu.findItem(R.id.menuArrange);
            if (sharedPreferences.getString("arrangement", "").equals("grid")) {
                menuItem.setTitle("ListView");
            } else {
                menuItem.setTitle("GridView");
            }
        }
    }

    private void loadNotes() {
        List<NoteModel> noteModelList = noteDB.getAllUnSecuredNotes();
        if (noteModelList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            txtNoNote.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            txtNoNote.setVisibility(View.GONE);
            if (sharedPreferences.getString("arrangement", "").equals("grid")) {
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
            NoteRecyclerViewAdapter adapter = new NoteRecyclerViewAdapter(noteModelList, this, false);
            recyclerView.setAdapter(adapter);
        }
    }
}