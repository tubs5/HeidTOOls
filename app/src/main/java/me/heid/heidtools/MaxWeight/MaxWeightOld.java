package me.heid.heidtools.MaxWeight;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

import me.heid.heidtools.R;

public class MaxWeightOld extends AppCompatActivity {
    final int RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION = 21313;
    //TODO(UNSAFE AF):
    final String secret = "Lp9A7r7af-Ru0ceOuIZqit60";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_max_weight);
        getPerm(this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION == requestCode) {



                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    String idToken = account.getIdToken();
                    InputStream raw = getBaseContext().getResources().openRawResource(R.raw.client_secret);

                    GoogleClientSecrets clientSecrets =
                            GoogleClientSecrets.load(
                                    JacksonFactory.getDefaultInstance(), new InputStreamReader(raw));
                    Log.e("TAG", "onActivityResult: "+ idToken);
                    GoogleAuthorizationCodeTokenRequest tokenRequest = new GoogleAuthorizationCodeTokenRequest(
                            new NetHttpTransport(),
                            JacksonFactory.getDefaultInstance(),
                            "https://oauth2.googleapis.com/token",
                            clientSecrets.getDetails().getClientId(),
                            clientSecrets.getDetails().getClientSecret(),
                            idToken,
                            "");  // Specify the same redirect URI that you use with your web
                    // app. If you don't have a web version of your app, you can
                    // specify an empty string.
                    new GetDriveFiles().execute(tokenRequest);
                }catch (Exception e){
                    Log.e("FAILED TO LOAD", "onActivityResult: ", e);
                    e.fillInStackTrace();
                    Toast.makeText(this, "FAILED TO LOAD", Toast.LENGTH_SHORT).show();
                }


            }
        }
    }
    public void getPerm(Activity context){
        Scope drive = new Scope(SheetsScopes.DRIVE_FILE);
        Scope sheets = new Scope(SheetsScopes.SPREADSHEETS);
        if (!GoogleSignIn.hasPermissions( GoogleSignIn.getLastSignedInAccount(context), drive,sheets)){
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestScopes(drive,sheets)
                    .requestEmail()
                    .build();

            //GoogleSignIn.requestPermissions(context,RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION,GoogleSignIn.getLastSignedInAccount(context),gso);
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION);
            return;
        }


        loadSheet();
    }

    public void loadSheet(){


       //
    }
}
class GetDriveFiles extends AsyncTask<GoogleAuthorizationCodeTokenRequest,Void,String[]>{
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Weight Tracker";



    @Override
    protected String[] doInBackground(GoogleAuthorizationCodeTokenRequest... strings) {


        GoogleTokenResponse tokenResponse = null;
        try {
            tokenResponse = strings[0].execute();

        String accessToken = tokenResponse.getAccessToken();

// Use access token to call API
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        Drive drive =
                new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                        .setApplicationName("Auth Code Exchange Demo")
                        .build();
           File f =  new File();
           f.setName("Apa12398");
           f.setFileExtension(".efs");
            drive.files().create(f);
            // Print the names and IDs for up to 10 files.
            FileList result = drive.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
            System.out.println("ALL IS DONE");

        } catch (IOException e) {
            Log.e("FAILED TO Connect", "onActivityResult: ", e);
            e.fillInStackTrace();
        }
        return null;
    }
}