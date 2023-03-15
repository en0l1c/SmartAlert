package com.enolic.smartalert;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserActivity extends AppCompatActivity {

    Button signOutButton;
    FirebaseAuth mAuth;
    
    TextView fullnameTS;
    FirebaseUser user;
    FloatingActionButton submitNewAlertBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        

        signOutButton = findViewById(R.id.logoutBtn);
        fullnameTS = findViewById(R.id.fullnameTV);
        submitNewAlertBtn = findViewById(R.id.submitAlertBtn);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        if(user.getDisplayName() == null) {
            Toast.makeText(UserActivity.this, "there is no fullname to show", Toast.LENGTH_SHORT).show();

        }
        else {
            fullnameTS.setText(user.getDisplayName());
        }


        // SIGN OUT BUTTON
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Toast.makeText(UserActivity.this, "You signed out from UserActivity", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                finish();
            }
        });

        // SUBMIT NEW ALERT - BUTTON
        submitNewAlertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserActivity.this, SubmitAlertActivity.class));
                finish();
            }
        });


        // Initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.submitAlert);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        // Here you want to show the user a dialog box
        new AlertDialog.Builder(this)
                .setTitle(R.string.signingOut)
                .setMessage(R.string.areYouSure)
                .setPositiveButton("ΝΑΙ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // The user wants to leave - so dismiss the dialog and exit
                        dialog.dismiss();
                        mAuth.signOut();
                        Toast.makeText(UserActivity.this, "You signed out from UserActivity", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserActivity.this, LoginActivity.class));
                        finish();

                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // The user is not sure, so you can exit or just stay
                        dialog.dismiss();
                    }
                }).show();

    }


    // FOR OPTION MENU TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.usermenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.signOutOptions:
                mAuth.signOut();
                Toast.makeText(UserActivity.this, "You signed out from UserActivity", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}






























