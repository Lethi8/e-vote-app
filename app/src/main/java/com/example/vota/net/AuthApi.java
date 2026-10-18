package com.example.vota.net;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.PUT;
// This is for the supabase endpoints like account creation and signing in
public interface AuthApi {
//Creates new account
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body SignUpRequest body);
//Signing in with email and password
    // grant type is the password
    @POST("auth/v1/token")
    Call<AuthResponse> signIn(@Query("grant_type") String grantType, @Body SignInRequest body);
//User can change their password
    @PUT("auth/v1/user")
    Call<AuthResponse> updateUser(@Body Map<String, String> body);
//User can signout
    @POST("auth/v1/logout")
    Call<Void> logout(@Query("scope") String scope);

    class SignUpRequest {
        public String email, password;
        public Map<String, String> data;
        public SignUpRequest(String email, String password, Map<String, String> data) {
            this.email = email; this.password = password; this.data = data;
        }
    }

    class SignInRequest {
        public String email, password;
        public SignInRequest(String email, String password) { this.email = email; this.password = password; }
    }

    class AuthResponse {
        @SerializedName("access_token") public String accessToken;
        @SerializedName("refresh_token") public String refreshToken;
        public UserInfo user;
        public String message;
        public static class UserInfo { public String id, email; }
    }
}