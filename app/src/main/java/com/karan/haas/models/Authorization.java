package com.karan.haas.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Karan on 4/28/2016.
 */
public class Authorization {
    @SerializedName("token")
    @Expose
    public String token;
}
