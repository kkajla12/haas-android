package com.karan.haas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup radioVoicePreferences;
    private RadioButton radioMute;
    private RadioButton radioUsMale;
    private RadioButton radioUsFemale;
    private RadioButton radioUkMale;
    private RadioButton radioUkFemale;
    private Button buttonSaveSettings;

    private HashMap<String, Integer> voiceToInt;
    private HashMap<Integer, String> intToVoice;
    private String currentVoiceName;
    private int currentVoiceInt;


    private static final String mPreferences = "HAAS";
    private static final String mVoicePreferenceName = "voiceSetting";
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = getSharedPreferences(mPreferences, Context.MODE_PRIVATE);

        radioVoicePreferences = (RadioGroup) findViewById(R.id.radio_voice_preferences);
        radioMute = (RadioButton) findViewById(R.id.radio_mute);
        radioUsMale = (RadioButton) findViewById(R.id.radio_us_male);
        radioUsFemale = (RadioButton) findViewById(R.id.radio_us_female);
        radioUkMale = (RadioButton) findViewById(R.id.radio_uk_male);
        radioUkFemale = (RadioButton) findViewById(R.id.radio_uk_female);

        voiceToInt = new HashMap<>();
        voiceToInt.put("radio_mute", -1);
        voiceToInt.put("radio_us_male", 35);
        voiceToInt.put("radio_us_female", 25);
        voiceToInt.put("radio_uk_male", 43);
        voiceToInt.put("radio_uk_female", 9);

        intToVoice = new HashMap<>();
        intToVoice.put(-1, "radio_mute");
        intToVoice.put(35, "radio_us_male");
        intToVoice.put(25, "radio_us_female");
        intToVoice.put(43, "radio_uk_male");
        intToVoice.put(9, "radio_uk_female");

        currentVoiceInt = mSharedPreferences.getInt(mVoicePreferenceName, 35);
        currentVoiceName = intToVoice.get(currentVoiceInt);
        RadioButton toSet = (RadioButton) radioVoicePreferences.findViewWithTag(currentVoiceName);
        if(!toSet.isChecked()) {
            toSet.toggle();
        }

        buttonSaveSettings = (Button) findViewById(R.id.button_save_settings);
        buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if(radioMute.isChecked()) {
                    System.out.println("Mute: " + radioMute.getTag());
                    currentVoiceInt = voiceToInt.get(radioMute.getTag());
                } else if(radioUsMale.isChecked()) {
                    System.out.println("US Male: " + radioUsMale.getTag());
                    currentVoiceInt = voiceToInt.get(radioUsMale.getTag());
                } else if(radioUsFemale.isChecked()) {
                    System.out.println("US Female: " + radioUsFemale.getTag());
                    currentVoiceInt = voiceToInt.get(radioUsFemale.getTag());
                } else if(radioUkMale.isChecked()) {
                    System.out.println("UK Male: " + radioUkMale.getTag());
                    currentVoiceInt = voiceToInt.get(radioUkMale.getTag());
                } else {
                    System.out.println("UK Female: " + radioUkFemale.getTag());
                    currentVoiceInt = voiceToInt.get(radioUkFemale.getTag());
                }
                editor.putInt(mVoicePreferenceName, currentVoiceInt).apply();

                Toast toast = Toast.makeText(getApplicationContext(), "Settings Updated", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}
