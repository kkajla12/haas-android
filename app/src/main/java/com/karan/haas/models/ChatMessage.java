package com.karan.haas.models;

import com.twilio.ipmessaging.Constants;
import com.twilio.ipmessaging.Message;

import org.json.JSONObject;

/**
 * Created by Karan on 5/31/2016.
 */
public class ChatMessage implements Message {
    private JSONObject link;

    public ChatMessage(JSONObject link) {
        this.link = link;
    }

    @Override
    public String getSid() {
        return null;
    }

    @Override
    public String getAuthor() {
        return "custom";
    }

    @Override
    public String getTimeStamp() {
        return null;
    }

    @Override
    public String getMessageBody() {
        return link.toString();
    }

    @Override
    public void updateMessageBody(String s, Constants.StatusListener statusListener) {
        return;
    }

    @Override
    public String getChannelSid() {
        return null;
    }

    @Override
    public long getMessageIndex() {
        return 0;
    }
}
