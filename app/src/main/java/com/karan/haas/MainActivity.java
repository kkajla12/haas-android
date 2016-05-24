package com.karan.haas;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.karan.haas.models.Authorization;
import com.karan.haas.models.User;
import com.karan.haas.services.APIService;
import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.ChannelListener;
import com.twilio.ipmessaging.Channels;
import com.twilio.ipmessaging.Constants;
import com.twilio.ipmessaging.ErrorInfo;
import com.twilio.ipmessaging.IPMessagingClientListener;
import com.twilio.ipmessaging.Member;
import com.twilio.ipmessaging.Message;
import com.twilio.ipmessaging.Messages;
import com.twilio.ipmessaging.UserInfo;
import com.twilio.ipmessaging.application.TwilioApplication;
import com.twilio.ipmessaging.ui.MessageViewHolder;
import com.twilio.ipmessaging.util.BasicIPMessagingClient;
import com.twilio.ipmessaging.util.ILoginListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.ribot.easyadapter.EasyAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ChannelListener, ILoginListener, IPMessagingClientListener {

    // HAAS Auth Token
    private static final String mPreferences = "HAAS";
    private static final String mAuthPreferenceName = "authToken";
    private static final String mChannelPreferenceName = "channelId";
    private static final String mVoicePreferenceName = "voiceSetting";
    SharedPreferences mSharedPreferences;
    private String authToken;
    private int voicePreference;

    // Twilio Authentication
    private String capabilityToken = null;
    private BasicIPMessagingClient basicClient;
    private ProgressDialog progressDialog;

    // Twilio Chat
    private static final String TAG = "MainActivityTag";
    private List<Message> messages = new ArrayList<>();
    private EasyAdapter<Message> adapter;

    private ListView lvChat;
    private EditText etMessage;

    private Channel channel;
    private Channel[] channels;

    public static String local_author;

    private Context context;

    // Text-to-Speech
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSharedPreferences = getSharedPreferences(mPreferences, Context.MODE_PRIVATE);
        // Save auth token locally
        authToken = mSharedPreferences.getString(mAuthPreferenceName, mAuthPreferenceName);
        // Save voice preference locally
        voicePreference = mSharedPreferences.getInt(mVoicePreferenceName, 35);

        context = MainActivity.this;

        // Text-to-Speech engine
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    Object[] voices = tts.getVoices().toArray();
                    tts.setVoice((Voice)voices[(voicePreference == -1 ? 35 : voicePreference)]);
                }
            }
        });

        basicClient = TwilioApplication.get().getBasicClient();
        if(basicClient != null) {
            // Authentication
            authenticateUser();
        }


        // Message Text
        this.etMessage = (EditText) findViewById(R.id.etMessage);

        // Send Button
        Button btSend = (Button) findViewById(R.id.btSend);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etMessage.getText().toString();
                if (channel != null && input.length() > 0) {
                    Messages messagesObject = channel.getMessages();
                    final Message message = messagesObject.createMessage(input);
                    messagesObject.sendMessage(message, new Constants.StatusListener() {
                        @Override
                        public void onSuccess() {
                            Log.e(TAG, "Successful at sending message.");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messages.add(message);
                                    adapter.notifyDataSetChanged();
                                    etMessage.setText("");
                                    etMessage.requestFocus();
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "Error sending message.");
                        }
                    });
                }
            }
        });
    }

    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(intent);
        }

        if (id == R.id.logout) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(mAuthPreferenceName).apply();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void authenticateUser() {
        APIService apiService = APIService.Factory.getInstance(context, authToken);

        MainActivity.this.progressDialog = ProgressDialog.show(MainActivity.this, "",
                "Loading messages. Please wait...", true);

        apiService.getTwilioToken(Settings.Secure.ANDROID_ID, Settings.Secure.ANDROID_ID).enqueue(new Callback<Authorization>() {
            @Override
            public void onResponse(Call<Authorization> call, Response<Authorization> response) {
                if(response.isSuccessful()) {
                    Authorization authorization = response.body();
                    try {
                        MainActivity.local_author = "You";
                        basicClient.setCapabilityToken(authorization.token);
                        basicClient.doLogin(MainActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<Authorization> call, Throwable t) {

            }
        });

        apiService.getUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    User user = response.body();
                    try {
                        // set ChannelId in SharedPreferences
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString(mChannelPreferenceName, user.userEnv.twilioChannelId).apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // TODO: error handling
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // TODO: error handling
            }
        });
    }

    private class CustomMessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message lhs, Message rhs) {
            if (lhs == null) {
                return (rhs == null) ? 0 : -1;
            }
            if (rhs == null) {
                return 1;
            }
            return lhs.getTimeStamp().compareTo(rhs.getTimeStamp());
        }
    }

    private void setupListView() {
        final Messages messagesObject = channel.getMessages();

        if(messagesObject != null) {
            Message[] messagesArray = messagesObject.getMessages();
            if(messagesArray.length > 0 ) {
                messages = new ArrayList<>(Arrays.asList(messagesArray));
                Collections.sort(messages, new CustomMessageComparator());
            }
        }

        adapter = new EasyAdapter<>(this, MessageViewHolder.class, messages,
                new MessageViewHolder.OnMessageClickListener() {

                    @Override
                    public void onMessageClicked(Message message) {
                        // TODO: Implement options for deletion or edit
                    }
                });

        // List View
        lvChat = (ListView) findViewById(R.id.lvChat);
        lvChat.setAdapter(adapter);

        if (lvChat != null) {
            lvChat.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            lvChat.setStackFromBottom(true);
            adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    lvChat.setSelection(adapter.getCount() - 1);
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageAdd(final Message message) {
        if(message.getAuthor().equals("system")) {
            String toSpeak = "";
            try {
                JSONObject reader = new JSONObject(message.getMessageBody());
                toSpeak = reader.getString("voicemsg");
            } catch(Exception e) {
                // TODO: improve error handling
                e.printStackTrace();
            }
            adapter.addItem(message);
            // speak if not set to mute
            if(voicePreference != -1) {
                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }

    @Override
    public void onMessageChange(Message message) {

    }

    @Override
    public void onMessageDelete(Message message) {

    }

    @Override
    public void onMemberJoin(Member member) {

    }

    @Override
    public void onMemberChange(Member member) {

    }

    @Override
    public void onMemberDelete(Member member) {

    }

    @Override
    public void onAttributesChange(Map<String, String> map) {

    }

    @Override
    public void onTypingStarted(Member member) {

    }

    @Override
    public void onTypingEnded(Member member) {

    }

    @Override
    public void onChannelAdd(Channel channel) {

    }

    @Override
    public void onChannelChange(Channel channel) {

    }

    @Override
    public void onChannelDelete(Channel channel) {

    }

    @Override
    public void onError(ErrorInfo errorInfo) {

    }

    @Override
    public void onAttributesChange(String s) {

    }

    @Override
    public void onChannelHistoryLoaded(Channel channel) {

    }

    @Override
    public void onUserInfoChange(UserInfo userInfo) {

    }

    @Override
    public void onLoginStarted() {

    }

    @Override
    public void onLoginFinished() {
        basicClient.getIpMessagingClient().setListener(MainActivity.this);

        final String channelId = mSharedPreferences.getString(mChannelPreferenceName, "twilioChannelId");
        final Channels channelsLocal = basicClient.getIpMessagingClient().getChannels();

        Constants.StatusListener channelListener = new Constants.StatusListener() {
            @Override
            public void onSuccess() {
                channels = channelsLocal.getChannels();
                if(channels != null) {
                    for(Channel chan : channels) {
                        if(chan.getSid().equals(channelId)) {
                            channel = chan;
                            break;
                        }
                    }
                }

                if(channel != null) {
                    channel.setListener(MainActivity.this);

                    // join the channel
                    channel.join(new Constants.StatusListener(){
                        @Override
                        public void onSuccess() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setupListView();
                                    MainActivity.this.progressDialog.dismiss();
                                }
                            });
                            Log.d(TAG, "Successfully joined existing channel");
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "failed to join existing channel");
                        }
                    });
                    System.out.println("Joined Channel!");

                    Log.d(TAG, "Successfully found channel");
                }
            }

            @Override
            public void onError() {
                Log.e(TAG, "failed to get channel");
            }
        };

        channelsLocal.loadChannelsWithListener(channelListener);
    }

    @Override
    public void onLoginError(String errorMessage) {

    }

    @Override
    public void onLogoutFinished() {

    }
}
