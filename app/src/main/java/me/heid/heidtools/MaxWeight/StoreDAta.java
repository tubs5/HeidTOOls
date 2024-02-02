package me.heid.heidtools.MaxWeight;

import android.content.Context;

import androidx.security.crypto.EncryptedFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import androidx.security.crypto.MasterKey;

public class StoreDAta {
    public void store(Context context, String directory, String data) throws GeneralSecurityException, IOException {
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.
        MasterKey mainKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // Create a file with this name, or replace an entire existing file
        // that has the same name. Note that you cannot append to an existing file,
        // and the file name cannot contain path separators.
        String fileToWrite = "my_sensitive_data.txt";
        EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                new File(directory, fileToWrite),
                mainKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

        byte[] fileContent = data.getBytes(StandardCharsets.UTF_8);
        OutputStream outputStream = encryptedFile.openFileOutput();
        outputStream.write(fileContent);
        outputStream.flush();
        outputStream.close();

    }
    public String load(Context context, String directory, String data) throws GeneralSecurityException, IOException {
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.
        MasterKey mainKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        String fileToRead = "my_sensitive_data.txt";
        EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                new File(directory, fileToRead),
                mainKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

        InputStream inputStream = encryptedFile.openFileInput();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int nextByte = inputStream.read();
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte);
            nextByte = inputStream.read();
        }

        byte[] plaintext = byteArrayOutputStream.toByteArray();
        return new String(plaintext);
    }
}
