package com.example.bsm_fingerptint_tutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NAME = "nazwaKlucza";
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

                    String plaintext = "Tekst do zaszyforwania";
                    byte[] encryptedInfo;

                    try {
                        encryptedInfo = result.getCryptoObject().getCipher().doFinal(
                                plaintext.getBytes(Charset.defaultCharset()));
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }

                    Log.d("MY_LOGS", "Success! Encrypted txt: " + Arrays.toString(encryptedInfo));

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
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using biometric credential")
                .setNegativeButtonText("xd")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();


        try {
            generateSecretKey(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .build()
                    );
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }


        btnLogIn.setOnClickListener(view -> {
            try {
                Cipher cipher = getCipher();
                SecretKey secretKey = getSecretKey();
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        });

    }



    private void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    private SecretKey getSecretKey() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");

        keyStore.load(null);
        return ((SecretKey)keyStore.getKey(KEY_NAME, null));
    }

    private Cipher getCipher() throws GeneralSecurityException{
        return Cipher.getInstance("AES/CBC/PKCS7Padding");
        //KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7
    }


}


















