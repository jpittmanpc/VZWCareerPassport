package vzw.vzwcareerpassport;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class login extends AppCompatActivity {
    static String fbUrl = "https://vzw.firebaseio.com";
    static String TAG = "login class";
    public Firebase fireBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            /* Immediately set content view if Firebase is Auth */
            setContentView(R.layout.fragment);
        }
        Firebase.setAndroidContext(this);
        fireBase = new Firebase(fbUrl);
        setContentView(R.layout.activity_login);
        final TextView textView = (TextView) findViewById(R.id.error);
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText pw = (EditText) findViewById(R.id.pw);
        Button send = (Button) findViewById(R.id.signButton);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("authenticating...");
                String sende = email.getText().toString();
                String pass = pw.getText().toString();
                Log.v(TAG, sende + pass);
                if (sende != null && pass != null) {
                    auth(sende, pass);
                }
            }
        });
    }
    final void auth(String email, String pass) {
        Log.v(TAG, email + " " + pass);
        fireBase.authWithPassword(email, pass, new com.firebase.client.Firebase.AuthResultHandler() {


            public void onAuthenticated(AuthData authData) {
                Log.v(TAG, "Auth" + authData.getUid());
                // Set Content view to next layout (Fragments)
                android.widget.TextView Error = (android.widget.TextView) findViewById(R.id.error);
                Error.setText("Loading..");
                loginFragment LoginFragment = new loginFragment();
                setContentView(R.layout.fragment);
            }

            public void onAuthenticationError(FirebaseError firebaseError) {

                android.widget.TextView Error = (android.widget.TextView) findViewById(R.id.error);
                Error.setText(firebaseError.getMessage());
                Log.v(TAG, "Error from firebase: " + firebaseError);
            }
        });
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}