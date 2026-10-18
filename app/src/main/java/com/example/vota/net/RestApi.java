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

// This is all the database and RPC endpoint the app calls
public interface RestApi {
//All of the elections with the newest opening first
    @GET("rest/v1/elections?select=*&order=opens_at.desc")
    Call<List<Election>> elections();
//specific election by id
    @GET("rest/v1/elections?select=*")
    Call<List<Election>> electionById(@Query("id") String idFilter);
//every party across the election
    @GET("rest/v1/parties?select=*")
    Call<List<Party>> partiesForElection(@Query("election_id") String electionIdFilter);
//parties that belong to one election
    @GET("rest/v1/parties?select=*")
    Call<List<Party>> partiesAll();
//education articles
    @GET("rest/v1/education_resources?select=*&order=sort_order.asc")
    Call<List<EducationResource>> educationResources();
// casts a ballot by calling the cast_ballot() data function
    @POST("rest/v1/rpc/cast_ballot")
    Call<String> castBallot(@Body Map<String, String> body);
//calls the verify_demo() function
    @POST("rest/v1/rpc/verify_demo")
    Call<Void> verifyDemo(@Body Map<String, String> emptyBody);
//counts per party for one election and never exposes a single ballot tow, it'll only show the totals
    @GET("rest/v1/rpc/get_results")
    Call<List<ResultRow>> results(@Query("p_election_id") String electionId);
    @GET("rest/v1/profiles?select=*")
    Call<List<Profile>> myProfile(@Query("id") String idFilter);
//If they voted on that specific election before then itll just show them a receipt hash and just reflects their receipt
    @GET("rest/v1/voter_participation?select=receipt_hash")
    Call<List<ReceiptRow>> myReceipt(@Query("election_id") String electionIdFilter);
// Badges the person has earned
    @GET("rest/v1/badges?select=code,earned_at")
    Call<List<BadgeRow>> myBadges();

    @POST("rest/v1/rpc/award_badge")
    Call<Void> awardBadge(@Body Map<String, String> body);
// Updates the users own number should they want too
    @POST("rest/v1/rpc/update_my_phone")
    Call<Void> updateMyPhone(@Body Map<String, String> body);
// They can delete their account from the database
    @POST("rest/v1/rpc/delete_own_account")
    Call<Void> deleteOwnAccount(@Body Map<String, String> emptyBody);

    @GET("rest/v1/voter_participation?select=election_id,participated_at,receipt_hash,elections(title)&order=participated_at.desc")
    Call<List<ParticipationRow>> myParticipationHistory();
//The callers own notifications, newest first
    @GET("rest/v1/notifications?select=*&order=created_at.desc")
    Call<List<NotificationRow>> myNotifications();

    class NotificationRow {
        public String title;
        public String body;
    }

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