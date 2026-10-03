package com.example.vota.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Party implements Serializable {
    public String id;
    @SerializedName("election_id") public String electionId;
    public String name;
    public String abbreviation;
    public String description;
    public String color;
    @SerializedName("policy_summary") public String policySummary;
    @SerializedName("key_positions") public String keyPositions;

    public Party() {}
}