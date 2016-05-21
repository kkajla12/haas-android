package com.twilio.ipmessaging.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.karan.haas.models.Authorization;
import com.karan.haas.services.APIService;
import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.Constants.InitListener;
import com.twilio.ipmessaging.Constants.StatusListener;
import com.twilio.ipmessaging.ErrorInfo;
import com.twilio.ipmessaging.IPMessagingClientListener;
import com.twilio.ipmessaging.TwilioIPMessagingClient;
import com.twilio.ipmessaging.TwilioIPMessagingSDK;
import com.twilio.ipmessaging.UserInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Karan on 5/14/2016.
 */
public class BasicIPMessagingClient implements IPMessagingClientListener, TwilioAccessManagerListener {
    private static final String TAG = "BasicIPMessagingClient";
    private TwilioIPMessagingClient ipMessagingClient;
    private Context context;
    private static String capabilityToken;
    private TwilioAccessManager accessMgr;
    private Handler loginListenerHandler;
    private String urlString;

    public BasicIPMessagingClient(Context context) {
        super();
        this.context = context;
    }

    public void setCapabilityToken(String capabilityToken) {
        this.capabilityToken = capabilityToken;
    }

    public static String getCapabilityToken() {
        return capabilityToken;
    }

    public TwilioIPMessagingClient getIpMessagingClient() {
        return ipMessagingClient;
    }

    private Handler setupListenerHandler() {
        Looper looper;
        Handler handler;
        if((looper = Looper.myLooper()) != null) {
            handler = new Handler(looper);
        } else if((looper = Looper.getMainLooper()) != null) {
            handler = new Handler(looper);
        } else {
            throw new IllegalArgumentException("Channel Listener must have a Looper.");
        }
        return handler;
    }

    public void doLogin(final ILoginListener listener) {
        this.loginListenerHandler = setupListenerHandler();
        TwilioIPMessagingSDK.setLogLevel(android.util.Log.DEBUG);
        if(!TwilioIPMessagingSDK.isInitialized()) {
            TwilioIPMessagingSDK.initializeSDK(this.context, new InitListener() {
                @Override
                public void onInitialized() {
                    createClientWithAccessManager(listener);
                }

                @Override
                public void onError(Exception error) {
                    Log.d(TAG, error.getMessage());
                }
            });
        } else {
            createClientWithToken(listener);
        }
    }

    private void createClientWithAccessManager(final ILoginListener listener) {
        this.accessMgr = TwilioAccessManagerFactory.createAccessManager(this.capabilityToken, new TwilioAccessManagerListener() {
            @Override
            public void onTokenExpired(TwilioAccessManager twilioAccessManager) {
                Log.d(TAG, "token expired.");

                // Should be utilized as a singleton that is injected into each activity upon construction.
                // The problem is that there may be thread-safety issues here.
                APIService apiService = APIService.Factory.getInstance(context);

                apiService.getTwilioToken(Settings.Secure.ANDROID_ID, Settings.Secure.ANDROID_ID).enqueue(new Callback<Authorization>() {
                    @Override
                    public void onResponse(Call<Authorization> call, Response<Authorization> response) {
                        if(response.isSuccessful()) {
                            Authorization authorization = response.body();
                            capabilityToken = authorization.token;
                            setCapabilityToken(capabilityToken);

                            ipMessagingClient.updateToken(BasicIPMessagingClient.getCapabilityToken(), new StatusListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Updated Token was successfull");
                                }

                                @Override
                                public void onError() {
                                    Log.e(TAG, "Updated Token failed");
                                }});
                            accessMgr.updateToken(null);
                        } else {
                            // TODO
                        }
                    }

                    @Override
                    public void onFailure(Call<Authorization> call, Throwable t) {
                        // TODO
                    }
                });
            }

            @Override
            public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {
                Log.d(TAG, "token updated.");
            }

            @Override
            public void onError(TwilioAccessManager twilioAccessManager, String s) {
                Log.d(TAG, "token error: " + s);
            }
        });

        ipMessagingClient = TwilioIPMessagingSDK.createIPMessagingClientWithAccessManager(BasicIPMessagingClient.this.accessMgr, BasicIPMessagingClient.this);
        if(ipMessagingClient != null) {
            ipMessagingClient.setListener(BasicIPMessagingClient.this);
            BasicIPMessagingClient.this.loginListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(listener != null) {
                        System.out.println("Listener: " + listener);
                        listener.onLoginFinished();
                    }
                }
            });
        } else {
            listener.onLoginError("ipMessagingClientWithAccessManager is null");
        }
    }

    private void createClientWithToken(ILoginListener listener) {
        ipMessagingClient = TwilioIPMessagingSDK.createIPMessagingClientWithToken(this.capabilityToken, BasicIPMessagingClient.this);
        if(ipMessagingClient != null) {
            if(listener != null) {
                System.out.println("Listener: " + listener);
                listener.onLoginFinished();
            }
        } else {
            listener.onLoginError("ipMessagingClient is null");
        }
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
    public void onTokenExpired(TwilioAccessManager twilioAccessManager) {

    }

    @Override
    public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {

    }

    @Override
    public void onError(TwilioAccessManager twilioAccessManager, String s) {

    }
}
