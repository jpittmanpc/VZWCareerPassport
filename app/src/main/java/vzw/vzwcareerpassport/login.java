package vzw.vzwcareerpassport;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

public class login extends AppCompatActivity {
    static String fbUrl = "https://vzw.firebaseio.com";
    static String TAG = "login class";
    public Firebase fireBase;
    public ValueEventListener mConnectListener;
    public ChildEventListener childEventListener;
    public AuthData fbUID;

    public AuthData getFbUID() {
        return fbUID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            /* Immediately set content view if Firebase is Auth */
            loginFragment LoginFragment = new loginFragment();
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
                startListen();
                presence();
            }

            public void onAuthenticationError(FirebaseError firebaseError) {

                android.widget.TextView Error = (android.widget.TextView) findViewById(R.id.error);
                Error.setText(firebaseError.getMessage());
                Log.v(TAG, "Error from firebase: " + firebaseError);
            }
        });
    }
    public void logout(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireBase.unauth();
                fireBase.goOffline();
                setContentView(R.layout.activity_login);
            }
        });
    }
    public void onStart() {
        super.onStart();
        mConnectListener = fireBase.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Log.v(TAG, "Connected");
                    presence();
                } else {
                    Log.v(TAG, "No longer connected");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //This shouldn't happen
            }
        });
    }

    private void presence() {
        if (getFbUID() != null) {
            Firebase presenceSys = fireBase.child("presence").child(fireBase.getAuth().getUid());
            presenceSys.onDisconnect().setValue(new ServerValue().TIMESTAMP);
            presenceSys.setValue("Online");
            loginFragment LoginFragment = new loginFragment();
            setContentView(R.layout.fragment);
        }
    }

    public void startListen() {
            childEventListener = fireBase.getRoot().child("users").child(fireBase.getAuth().getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String var) {
                    if (dataSnapshot.getKey() == "name") {
                        TextView textView = (TextView) findViewById(R.id.name);
                        Log.v(TAG, "Snapshot" + dataSnapshot);
                        textView.setText(dataSnapshot.getValue().toString());
                    }
                    if (dataSnapshot.getKey() == "department") {
                        TextView textView = (TextView) findViewById(R.id.dept);
                        textView.setText(dataSnapshot.getValue().toString());
                    }
                    if (dataSnapshot.getKey() == "achievements") {
                        update(dataSnapshot);
                        Log.v(TAG, "Ach " + dataSnapshot.getChildrenCount());
                    }

                    Log.v(TAG, "child added " + " Previous: " + var + " this " + dataSnapshot.getKey() + " value: " + dataSnapshot.getValue());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    update(dataSnapshot);
                    Log.v(TAG, "child changed" + s + dataSnapshot.getValue());

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.v(TAG, "removed " + dataSnapshot.getValue());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.v(TAG, "moved" + s + dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.v(TAG, "error" + firebaseError);

                }
            });
        }
    public void update(DataSnapshot dataSnapshot) {
        Log.v(TAG, "Update " + dataSnapshot.getKey());
            Log.v(TAG, "Which it does..");
            TextView percentage = (TextView) findViewById(R.id.pct);
            TextView textView = (TextView) findViewById(R.id.achievementNumber);
            int Num = (int) dataSnapshot.getChildrenCount();
            Log.v(TAG, "n " + Num);
            String text = String.valueOf(Num);
            percentage.setText(text + "0%");
            textView.setText(text);
            for (int i = Num; i > 0; i--) {
                Log.v(TAG, "Number " + i);
                Button change = (Button) getView(i);
                change.setBackground(null);
                change.setBackgroundColor(0xff99cc00);
        }
    }
    public View getView(int id) {
        String namez = "ach" + id;
        Resources res = getResources();
        Context mContext = getBaseContext();
        int idz = res.getIdentifier("ach" + id, "id", mContext.getPackageName());
        return findViewById(idz);
    }
    }


/*
    @Override
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
}
*/