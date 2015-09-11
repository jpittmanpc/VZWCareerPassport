package vzw.vzwcareerpassport;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    public String getFbUID() {
        AuthData auth = this.fireBase.getAuth();
        String text="a";
        try {
            text = auth.getUid();
        }
        catch(NullPointerException err) {
            text = "a";
    }
        return text;
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        fireBase = new Firebase(fbUrl);
        loginFragment LoginFragment = new loginFragment();


        if (savedInstanceState == null) {

        }
        fireBase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    setContentView(R.layout.fragment);
                    ActionBar bar = getActionBar();
                    if (bar != null) { bar.setTitle("Your Achievements!"); }
                } else {
                    loginview();
                }
            }
        });
    }
    final void loginview() {

        setContentView(R.layout.activity_login);
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle("Log in");
        }
        final TextView textView = (TextView) findViewById(R.id.error);
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText pw = (EditText) findViewById(R.id.pw);
        Button send = (Button) findViewById(R.id.signButton);
        final View activityRootView = findViewById(R.id.linear);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                Log.v(TAG, "Height " + String.valueOf(heightDiff));
                if (heightDiff > 400) { // if more than 100 pixels, its probably a keyboard...
                    findViewById(R.id.imageView).setVisibility(View.GONE);
                } else {
                    if (findViewById(R.id.imageView) != null) {
                        findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
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
                //setContentView(R.layout.fragment);
                presence();
            }

            public void onAuthenticationError(FirebaseError firebaseError) {

                android.widget.TextView Error = (android.widget.TextView) findViewById(R.id.error);
                Error.setText(firebaseError.getMessage());
                Log.v(TAG, "Error from firebase: " + firebaseError);
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
        if (getFbUID() != "a") {
            Firebase presenceSys = fireBase.child("presence").child(fireBase.getAuth().getUid());
            presenceSys.onDisconnect().setValue(new ServerValue().TIMESTAMP);
            presenceSys.setValue("Online");
            View view = this.getCurrentFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            catch(NullPointerException err) {

            }
//            setContentView(R.layout.fragment);
            startListen();
        }
        else { loginview(); }
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
            ImageView change = (ImageView) getView(i);
            change.setImageResource(getResources().getIdentifier("badge"+i, "drawable", getPackageName()));
        }
    }
    public View getView(int id) {
        Resources res = getResources();
        Context mContext = getBaseContext();
        int idz = res.getIdentifier("ach" + id, "id", mContext.getPackageName());
        return findViewById(idz);
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "pause");
        super.onPause();
    }
    protected void onResume() {
        Log.d(TAG, "resume");
              super.onResume();

    }
    protected void onRestart() {
        Log.d(TAG, "restart");
        super.onRestart();
    }
    protected void onDestroy() {
        Log.d(TAG, "destroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);

        return true;
    }
    public void logout(MenuItem v) {
        fireBase.unauth();
        fireBase.goOffline();
        setContentView(R.layout.activity_login);
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
