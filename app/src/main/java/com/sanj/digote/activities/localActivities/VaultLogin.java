package com.sanj.digote.activities.localActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sanj.digote.R;
import com.sanj.digote.data.NoteDB;
import com.sanj.digote.wrappers.Helper;

import java.util.ArrayList;
import java.util.List;

import static com.sanj.digote.wrappers.Helper.vaultPassword;

public class VaultLogin extends AppCompatActivity {
    private TextView btnNext;
    private List<String> pinCharacters;
    private List<TextView> bulletLists;
    private List<View> dashList;
    private NoteDB noteDB;
    private boolean isVerificationOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setNavigationIcon(R.drawable.ic_back);
        noteDB = NoteDB.getInstance(this);
        isVerificationOnly = getIntent().getExtras() != null;
        initViews();
    }

    private void initViews() {
        pinCharacters = new ArrayList<>();
        bulletLists = new ArrayList<>();
        dashList = new ArrayList<>();

        TextView bullet1 = findViewById(R.id.bullet1);
        TextView bullet2 = findViewById(R.id.bullet2);
        TextView bullet3 = findViewById(R.id.bullet3);
        TextView bullet4 = findViewById(R.id.bullet4);
        btnNext = findViewById(R.id.btnNext);
        TextView numOne = findViewById(R.id.numOne);
        TextView numTwo = findViewById(R.id.numTwo);
        TextView numThree = findViewById(R.id.numThree);
        TextView numFour = findViewById(R.id.numFour);
        TextView numFive = findViewById(R.id.numFive);
        TextView numSix = findViewById(R.id.numSix);
        TextView numSeven = findViewById(R.id.numSeven);
        TextView numEight = findViewById(R.id.numEight);
        TextView numNine = findViewById(R.id.numNine);
        TextView numZero = findViewById(R.id.numZero);
        TextView numDelete = findViewById(R.id.delete);
        View dash1 = findViewById(R.id.dash1);
        View dash2 = findViewById(R.id.dash2);
        View dash3 = findViewById(R.id.dash3);
        View dash4 = findViewById(R.id.dash4);

        bulletLists.add(bullet1);
        bulletLists.add(bullet2);
        bulletLists.add(bullet3);
        bulletLists.add(bullet4);

        dashList.add(dash1);
        dashList.add(dash2);
        dashList.add(dash3);
        dashList.add(dash4);

        btnNext.setEnabled(false);
        dash1.setBackgroundResource(R.drawable.blue_dash);

        btnNext.setOnClickListener(v -> verification());
        numOne.setOnClickListener(v -> assignPinCharacter("1"));
        numTwo.setOnClickListener(v -> assignPinCharacter("2"));
        numThree.setOnClickListener(v -> assignPinCharacter("3"));
        numFour.setOnClickListener(v -> assignPinCharacter("4"));
        numFive.setOnClickListener(v -> assignPinCharacter("5"));
        numSix.setOnClickListener(v -> assignPinCharacter("6"));
        numSeven.setOnClickListener(v -> assignPinCharacter("7"));
        numEight.setOnClickListener(v -> assignPinCharacter("8"));
        numNine.setOnClickListener(v -> assignPinCharacter("9"));
        numZero.setOnClickListener(v -> assignPinCharacter("0"));
        numDelete.setOnClickListener(v -> deletePinCharacter());
    }

    private void verification() {
        StringBuilder pin = new StringBuilder();
        for (String pinChar : pinCharacters) {
            pin.append(pinChar);
        }
        if (isVerificationOnly) {
            vaultPassword = pin.toString();
            finish();
        } else {
            if (noteDB.vaultVerification(pin.toString())) {
                startActivity(new Intent(this, Vault.class));
                finish();
            } else {
                new Helper().errorToast("Incorrect PIN", this);
            }
        }
        pinCharacters.clear();
        btnNext.setEnabled(false);
        btnNext.setBackgroundResource(R.drawable.disabled_btn_backgroud);
        for (int i = 0; i < 4; i++) {
            bulletLists.get(i).setBackgroundResource(R.drawable.white_bullet);
            dashList.get(i).setBackgroundResource(R.drawable.white_dash);
        }
        dashList.get(0).setBackgroundResource(R.drawable.blue_dash);
    }

    @SuppressLint("ResourceAsColor")
    private void assignPinCharacter(String number) {
        if (pinCharacters.size() < 4) {
            pinCharacters.add(number);
            bulletLists.get(pinCharacters.size() - 1).setBackgroundResource(R.drawable.black_bullet);
            dashList.get(pinCharacters.size() - 1).setBackgroundResource(R.drawable.dark_dash);

            if (pinCharacters.size() == 4) {
                btnNext.setEnabled(true);
                btnNext.setBackgroundResource(R.drawable.enabled_btn_backgroud);
            } else {
                dashList.get(pinCharacters.size()).setBackgroundResource(R.drawable.blue_dash);
            }
        }

    }

    @SuppressLint("ResourceAsColor")
    private void deletePinCharacter() {
        if (!pinCharacters.isEmpty()) {
            int currentIndex = pinCharacters.size() - 1;
            pinCharacters.remove(currentIndex);
            bulletLists.get(currentIndex).setBackgroundResource(R.drawable.white_bullet);
            if (pinCharacters.size() < 4) {
                btnNext.setEnabled(false);
                btnNext.setBackgroundResource(R.drawable.disabled_btn_backgroud);
            }
            boolean isNextIndicated = false;
            currentIndex = pinCharacters.size() - 1;
            if (currentIndex >= 0) {
                for (int i = 0; i < 4; i++) {
                    if (i <= currentIndex) {
                        dashList.get(i).setBackgroundResource(R.drawable.dark_dash);
                    } else {
                        if (!isNextIndicated) {
                            dashList.get(i).setBackgroundResource(R.drawable.blue_dash);
                            isNextIndicated = true;
                        } else {
                            dashList.get(i).setBackgroundResource(R.drawable.white_dash);
                        }
                    }
                }
            } else {
                dashList.get(1).setBackgroundResource(R.drawable.white_dash);
                dashList.get(0).setBackgroundResource(R.drawable.blue_dash);
            }
        } else {
            dashList.get(0).setBackgroundResource(R.drawable.blue_dash);
        }

    }
}