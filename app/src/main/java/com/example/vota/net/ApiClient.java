package com.example.vota.net;

import com.example.vota.BuildConfig;
import com.example.vota.util.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static Retrofit retrofit;

    private ApiClient() {}

    public static synchronized Retrofit get(SessionManager session) {
        if (retrofit == null) {
            Interceptor auth = chain -> {
                Request.Builder b = chain.request().newBuilder()
                        .header("apikey", BuildConfig.SUPABASE_ANON_KEY);

                // Only send a user token once we actually have one.
                // (Content-Type is set by Retrofit from the @Body, so we don't add it.)
                String token = session.accessToken();
                if (token != null && !token.isEmpty()) {
                    b.header("Authorization", "Bearer " + token);
                }
                return chain.proceed(b.build());
            };

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);
            // Keep secrets out of Logcat even in debug builds.
            logging.redactHeader("apikey");
            logging.redactHeader("Authorization");

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(auth)
                    .addInterceptor(logging)
                    .build();

            // Strip any trailing slashes, then add exactly one.
            String baseUrl = BuildConfig.SUPABASE_URL.trim().replaceAll("/+$", "") + "/";

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}