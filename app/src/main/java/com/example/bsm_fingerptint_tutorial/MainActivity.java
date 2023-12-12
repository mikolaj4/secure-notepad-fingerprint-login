package com.example.bsm_fingerptint_tutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity {

    Button btnLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogIn = findViewById(R.id.btnLogIn);

        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)){
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_LOGS", "App can authenticate using biometrics!");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_LOGS", "Device is not equipped with biometric hardware!");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_LOGS", "Biometry currently unavailable!");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("MY_LOGS", "No fingerprint assigned!");
                break;
        }

        BiometricPrompt.PromptInfo promptInfo;
        BiometricPrompt biometricPrompt;
        Executor executor;

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback(){
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString){
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);

                    Toast.makeText(getApplicationContext(),"Authentication succeeded!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), NotepadActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onAuthenticationFailed(){
                    super.onAuthenticationFailed();
                    Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for your notes")
                .setSubtitle("Log in using biometric credential")
                .setNegativeButtonText(" ")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        btnLogIn.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));

    }

}


















