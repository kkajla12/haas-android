package com.karan.haas.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Created by Karan on 4/28/2016.
 */
public class User {
    @SerializedName("_id")
    @Expose
    public String Id;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("__v")
    @Expose
    public int V;
    @SerializedName("name")
    @Expose
    public Name name;
    @SerializedName("userEnv")
    @Expose
    public UserEnv userEnv;

    public class UserEnv {
        @SerializedName("_id")
        @Expose
        public String Id;
        @SerializedName("user")
        @Expose
        public String user;
        @SerializedName("twilioChannelId")
        @Expose
        public String twilioChannelId;
        @SerializedName("__v")
        @Expose
        public int V;
        @SerializedName("googleVoicePreference")
        @Expose
        public int googleVoicePreference;
        @SerializedName("travelSearchPreference")
        @Expose
        public TravelSearchPreference travelSearchPreference;
    }

    public class Name {
        @SerializedName("last")
        @Expose
        public String last;
        @SerializedName("first")
        @Expose
        public String first;
    }

    public class TravelSearchPreference {
        @SerializedName("expedia")
        @Expose
        public boolean expedia;
        @SerializedName("kayak")
        @Expose
        public boolean kayak;
    }
}
