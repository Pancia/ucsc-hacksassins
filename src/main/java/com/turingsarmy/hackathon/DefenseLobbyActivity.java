package com.turingsarmy.hackathon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;

public class DefenseLobbyActivity extends Activity {

    private static final String TAG = DefenseLobbyActivity.class.getSimpleName();
    private TextView numDefense, prompt;
    private Button exit;
    private boolean ping = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.defense_lobby);

        numDefense = (TextView) findViewById(R.id.defenselobby_textview_numdefense);
        prompt = (TextView) findViewById(R.id.defenselobby_textview_prompt);
        exit = (Button) findViewById(R.id.defenselobby_button_exit);

        tryToFindNumDef();

        exit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                tryToLeaveLobby();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        pingServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void tryToFindNumDef() {
        HashMap<String, String> map = new HashMap<String, String>();
        GPSTracker track = new GPSTracker(this);
            map.put("COLLEGE".toLowerCase(), track.getCurrentCollege());
        AsyncJsonRequestManager man = new AsyncJsonRequestManager(DefenseLobbyActivity.this);
                man.setAction(AsyncJsonRequestManager.Actions.GETCOLLEGEINFO);
                man.setRequestBody(map);
                man.setCallback(new MyFutureTask() {
                    @Override
                    public void onRequestCompleted(JSONObject json) {
                        String defender_count = json.optString("defender_count");
                        updateTextView(numDefense, defender_count);
                    }
                }).execute();
    }

    private void updateTextView(final TextView tv, final String string) {
        DefenseLobbyActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(string);

            }
        });
    }

    private void tryToLeaveLobby() {
        ping = false;
        HashMap<String, String> map = new HashMap<String, String>();
        GPSTracker track = new GPSTracker(this);
        map.put("COLLEGE".toLowerCase(), track.getCurrentCollege());
        map.put("USERNAME".toLowerCase(), MyShrdPrfs.myShrdPrfs.getString("USERNAME", ""));
        AsyncJsonRequestManager man = new AsyncJsonRequestManager(DefenseLobbyActivity.this);
        man.setAction(AsyncJsonRequestManager.Actions.UPDATECOLLEGEINFO);
        man.setRequestBody(map);
        man.setCallback(new MyFutureTask() {
            @Override
            public void onRequestCompleted(JSONObject json) {
                Intent myIntent = new Intent(DefenseLobbyActivity.this, PlayActivity.class);
                DefenseLobbyActivity.this.startActivity(myIntent);
            }

        }).execute();
    }

    private void pingServer() {
        Log.w(TAG, "defenselobby pingServer()");
        HashMap<String, String> map = new HashMap<String, String>();
        GPSTracker track = new GPSTracker(this);
        map.put("GAMEMODE".toLowerCase(), "defender");
        map.put("USERNAME".toLowerCase(), MyShrdPrfs.myShrdPrfs.getString("USERNAME", ""));
        AsyncJsonRequestManager man = new AsyncJsonRequestManager(DefenseLobbyActivity.this);
        man.setAction(AsyncJsonRequestManager.Actions.JOINGAME);
        man.setRequestBody(map);
        man.setCallback(new MyFutureTask() {
            @Override
            public void onRequestCompleted(JSONObject json) {
                String response = json.optString("response");
                String p2_username = json.optString("p2_username");
                if(response.equals("try again")){
                    if (ping) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pingServer();
                    }
                }
                else if (!p2_username.equals("")){
                    Intent myIntent = new Intent(DefenseLobbyActivity.this, PlayGameActivityRPS.class);
                    myIntent.putExtra("p1name", MyShrdPrfs.myShrdPrfs.getString("USERNAME", ""));
                    myIntent.putExtra("p2name", p2_username);
                    myIntent.putExtra("gamemode", "defender");
                    DefenseLobbyActivity.this.startActivity(myIntent);
                }
            }

        }).execute();
    }
}
