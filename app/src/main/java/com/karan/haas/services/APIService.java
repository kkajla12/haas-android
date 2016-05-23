package com.karan.haas.services;

import android.content.Context;

import com.karan.haas.BuildConfig;
import com.karan.haas.models.Authorization;
import com.karan.haas.models.User;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by Karan on 4/27/2016.
 */
public interface APIService {
    String BASE_URL = "https://haas.localtunnel.me/";

    // Login and Register
    @FormUrlEncoded
    @POST("login")
    Call<Authorization> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("register")
    Call<Authorization> register(@Field("username") String username, @Field("password") String password, @Field("firstname") String firstName, @Field("lastname") String lastName);

    // User
    @GET("user")
    Call<User> getUser();

    @FormUrlEncoded
    @PUT("user")
    Call<User> updateUser(@Body User user);

    // Twilio Token
    @FormUrlEncoded
    @POST("twilio/token")
    Call<Authorization> getTwilioToken(@Field("device") String device, @Field("identity") String identity);

    class Factory {
        public static APIService getInstance(Context context) {
            return getInstance(context, null);
        }

        public static APIService getInstance(Context context, final String token) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.readTimeout(15, TimeUnit.SECONDS);
            builder.connectTimeout(10, TimeUnit.SECONDS);
            builder.writeTimeout(10, TimeUnit.SECONDS);

            //builder.certificatePinner(new CertificatePinner.Builder().add("*.androidadvance.com", "sha256/RqzElicVPA6LkKm9HblOvNOUqWmD+4zNXcRb+WjcaAE=")
            //    .add("*.xxxxxx.com", "sha256/8Rw90Ej3Ttt8RRkrg+WYDS9n7IS03bk5bjP/UXPtaY8=")
            //    .add("*.xxxxxxx.com", "sha256/Ko8tivDrEjiY90yGasP6ZpBU4jwXvHqVvQI0GS3GNdA=")
            //    .add("*.xxxxxxx.com", "sha256/VjLZe/p3W/PJnd6lL8JVNBCGQBZynFLdZSTIqcO0SJ8=")
            //    .build());

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
                builder.addInterceptor(interceptor);
            }

            if(token != null) {
                builder.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request original = chain.request();

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Accept", "application/json")
                                .header("Authorization",
                                        "Bearer " + token)
                                .method(original.method(), original.body());

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                });
            }

            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(context.getCacheDir(), cacheSize);
            builder.cache(cache);

            Retrofit retrofit = new Retrofit.Builder().client(builder.build()).addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build();
            return retrofit.create(APIService.class);
        }
    }
}
