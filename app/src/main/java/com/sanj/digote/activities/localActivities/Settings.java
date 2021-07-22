package com.sanj.digote.activities.localActivities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sanj.digote.R;

public class Settings extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private TextView txtFont, txtSort;
    private @SuppressLint("CommitPrefEdits")
    SharedPreferences.Editor editor;
    private int selectedFontSize, selectedSortCriteria;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(R.drawable.ic_back);

        sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        txtFont = findViewById(R.id.fontSizeValue);
        txtFont.setOnClickListener(v -> changeFontSize());

        txtSort = findViewById(R.id.sortValue);
        txtSort.setOnClickListener(v -> changeSortValue());

        TextView txtPrivacy = findViewById(R.id.privacyPolicy);
        txtPrivacy.setOnClickListener(v -> privacyPolicy());
        TextView txtAbout = findViewById(R.id.about);
        txtAbout.setOnClickListener(v -> aboutUs());

        initSelectedChoices();
    }

    private void initSelectedChoices() {
        String fontSize = sharedPreferences.getString("fontSize", "Medium");
        switch (fontSize) {
            case "Medium":
                selectedFontSize = 1;
                break;
            case "Small":
                selectedFontSize = 0;
                break;
            case "Large":
                selectedFontSize = 2;
                break;
        }

        String sortCriteria = sharedPreferences.getString("sort", "Date");
        switch (sortCriteria) {
            case "Date":
                selectedSortCriteria = 1;
                break;
            case "Title":
                selectedSortCriteria = 0;
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        txtSort.setText("by " + sharedPreferences.getString("sort", "Date"));
        txtFont.setText(sharedPreferences.getString("fontSize", "Medium"));
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

    private void privacyPolicy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy")
                .setMessage("We understand if you are storing really confidential notes with us. We " +
                        "assure you your data is safe and we agree not to disclose your data. " +
                        "All the notes on the cloud are stored under a heavy and customized dynamic" +
                        " American Encryption Standard in a very secure server whose location will remain" +
                        " undisclosed. In the cloud we secure and never forget every note")
                .create()
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void changeSortValue() {
        CharSequence[] sortValue = new CharSequence[]{"Title", "Date"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort by")
                .setSingleChoiceItems(sortValue, selectedSortCriteria, (dialog, which) -> {
                    selectedSortCriteria = which;
                    editor.putString("sort", (String) sortValue[which]);
                    editor.apply();
                    txtSort.setText("by " + sortValue[which]);
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void changeFontSize() {
        CharSequence[] fontSizes = new CharSequence[]{"Small", "Medium", "Large"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Font Size")
                .setSingleChoiceItems(fontSizes, selectedFontSize, (dialog, which) -> {
                    selectedFontSize = which;
                    editor.putString("fontSize", (String) fontSizes[which]);
                    editor.apply();
                    txtFont.setText(String.valueOf(fontSizes[which]));
                    dialog.dismiss();
                })
                .create()
                .show();
    }
}