package com.sanj.digote.activities.localActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanj.digote.R;
import com.sanj.digote.adapters.NoteRecyclerViewAdapter;
import com.sanj.digote.data.NoteDB;
import com.sanj.digote.models.NoteModel;

import java.util.List;

public class Vault extends AppCompatActivity {
    private TextView txtNoNote;
    private EditText edSearch;
    private RecyclerView recyclerView;
    private NoteDB noteDB;
    private SharedPreferences sharedPreferences;
    private boolean isSecured;
    private @SuppressLint("CommitPrefEdits")
    SharedPreferences.Editor editor;
    private Menu mMenu;


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(R.drawable.ic_back);
        isSecured = true;
        edSearch = findViewById(R.id.edSearch);
        recyclerView = findViewById(R.id.recyclerView);
        txtNoNote = findViewById(R.id.txtNoNote);

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
                    List<NoteModel> noteModelList = noteDB.getSearchSecuredNotes(edSearch.getText().toString());
                    if (noteModelList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        txtNoNote.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        txtNoNote.setVisibility(View.GONE);
                        if (sharedPreferences.getString("arrangement", "").equals("grid")) {
                            recyclerView.setLayoutManager(new GridLayoutManager(Vault.this, 2));
                        } else {
                            recyclerView.setLayoutManager(new LinearLayoutManager(Vault.this));
                        }
                        NoteRecyclerViewAdapter adapter = new NoteRecyclerViewAdapter(noteModelList, Vault.this, false);
                        recyclerView.setAdapter(adapter);
                    }

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        mMenu = menu;
        MenuItem menuItem = menu.findItem(R.id.menuArrange);
        if (sharedPreferences.getString("arrangement", "").equals("grid")) {
            menuItem.setTitle("ListView");
        } else {
            menuItem.setTitle("GridView");
        }
        menuItem = menu.findItem(R.id.menuVault);
        menuItem.setVisible(false);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetting:
                isSecured = false;
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.menuArrange:
                changeArrangement(item);
                break;
        }
        return true;
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
        if (isSecured) {
            loadNotes();
            if (mMenu != null) {
                MenuItem menuItem = mMenu.findItem(R.id.menuArrange);
                if (sharedPreferences.getString("arrangement", "").equals("grid")) {
                    menuItem.setTitle("ListView");
                } else {
                    menuItem.setTitle("GridView");
                }
                menuItem = mMenu.findItem(R.id.menuVault);
                menuItem.setVisible(false);
            }
        } else {
            finish();
        }

    }

    private void loadNotes() {
        List<NoteModel> noteModelList = noteDB.getAllSecuredNotes();
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
