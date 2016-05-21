package com.twilio.ipmessaging.util;

/**
 * Created by Karan on 5/14/2016.
 */
public interface ILoginListener {
    void onLoginStarted();

    void onLoginFinished();

    void onLoginError(String errorMessage);

    void onLogoutFinished();
}
