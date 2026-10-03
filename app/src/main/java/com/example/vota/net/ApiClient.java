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

    public static Retrofit get(SessionManager session) {
        if (retrofit == null) {
            Interceptor auth = chain -> {
                Request original = chain.request();
                Request.Builder b = original.newBuilder()
                        .header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                        .header("Content-Type", "application/json");
                String token = session.accessToken();
                b.header("Authorization", "Bearer " + (token.isEmpty() ? BuildConfig.SUPABASE_ANON_KEY : token));
                return chain.proceed(b.build());
            };

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(auth)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}