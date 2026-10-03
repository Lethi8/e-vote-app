package com.example.vota.net;

import com.example.vota.model.Election;
import com.example.vota.model.EducationResource;
import com.example.vota.model.Party;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestApi {

    @GET("rest/v1/elections?select=*&order=opens_at.desc")
    Call<List<Election>> elections();

    @GET("rest/v1/elections?select=*")
    Call<List<Election>> electionById(@Query("id") String idFilter);

    @GET("rest/v1/parties?select=*")
    Call<List<Party>> partiesForElection(@Query("election_id") String electionIdFilter);

    @GET("rest/v1/parties?select=*")
    Call<List<Party>> partiesAll();

    @GET("rest/v1/education_resources?select=*&order=sort_order.asc")
    Call<List<EducationResource>> educationResources();

    @POST("rest/v1/rpc/cast_ballot")
    Call<String> castBallot(@Body Map<String, String> body);

    @POST("rest/v1/rpc/verify_demo")
    Call<Void> verifyDemo(@Body Map<String, String> emptyBody);

    @GET("rest/v1/rpc/get_results")
    Call<List<ResultRow>> results(@Query("p_election_id") String electionId);

    @GET("rest/v1/profiles?select=*")
    Call<List<Profile>> myProfile(@Query("id") String idFilter);

    @GET("rest/v1/voter_participation?select=receipt_hash")
    Call<List<ReceiptRow>> myReceipt(@Query("election_id") String electionIdFilter);

    @GET("rest/v1/badges?select=code,earned_at")
    Call<List<BadgeRow>> myBadges();

    @POST("rest/v1/rpc/award_badge")
    Call<Void> awardBadge(@Body Map<String, String> body);

    @POST("rest/v1/rpc/update_my_phone")
    Call<Void> updateMyPhone(@Body Map<String, String> body);

    @POST("rest/v1/rpc/delete_own_account")
    Call<Void> deleteOwnAccount(@Body Map<String, String> emptyBody);

    @GET("rest/v1/voter_participation?select=election_id,participated_at,receipt_hash,elections(title)&order=participated_at.desc")
    Call<List<ParticipationRow>> myParticipationHistory();

    class BadgeRow {
        public String code;
        public String earned_at;
    }

    class ResultRow {
        public String party_id;
        public String party_name;
        public long vote_count;
    }

    class Profile {
        public String id;
        public String full_name;
        public String phone;
        public String sa_id_number;
        public String role;
        public boolean verified;
        public String created_at;
    }

    class ParticipationRow {
        public String election_id;
        public String participated_at;
        public String receipt_hash;
        public ElectionRef elections;
        public static class ElectionRef { public String title; }
    }

    class ReceiptRow {
        public String receipt_hash;
    }
}