package com.example.vota.net;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.PUT;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface AuthApi {

    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body SignUpRequest body);

    @POST("auth/v1/token")
    Call<AuthResponse> signIn(
            @Query("grant_type") String grantType,
            @Body SignInRequest body
    );
    @GET("auth/v1/user")
    Call<AuthResponse.UserInfo> getCurrentUser();
    @PUT("auth/v1/user")
    Call<AuthResponse> updateUser(@Body Map<String, String> body);

    @POST("auth/v1/logout")
    Call<Void> logout(@Query("scope") String scope);

    @Headers({
            "Prefer: return=minimal"
    })
    @POST("rest/v1/users")
    Call<Void> insertUser(@Body UserRequest body);


    class SignUpRequest {
        public String email;
        public String password;
        public Map<String, String> data;

        public SignUpRequest(
                String email,
                String password,
                Map<String, String> data
        ) {
            this.email = email;
            this.password = password;
            this.data = data;
        }
    }


    class SignInRequest {
        public String email;
        public String password;

        public SignInRequest(
                String email,
                String password
        ) {
            this.email = email;
            this.password = password;
        }
    }


    class UserRequest {
        public String user_id;
        public String fullname;
        public String date_of_birth;
        public String home_address;
        public String email;
        public String phone_num;
        public String id_number;

        public UserRequest(
                String user_id,
                String fullname,
                String date_of_birth,
                String home_address,
                String email,
                String phone_num,
                String id_number
        ) {
            this.user_id = user_id;
            this.fullname = fullname;
            this.date_of_birth = date_of_birth;
            this.home_address = home_address;
            this.email = email;
            this.phone_num = phone_num;
            this.id_number = id_number;
        }
    }


    class AuthResponse {

        @SerializedName("access_token")
        public String accessToken;

        @SerializedName("refresh_token")
        public String refreshToken;

        public UserInfo user;

        public String message;


        public static class UserInfo {
            public String id;
            public String email;

            @SerializedName("email_confirmed_at")
            public String emailConfirmedAt;

            @SerializedName("confirmed_at")
            public String confirmedAt;
        }
    }

}
