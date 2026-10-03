package com.example.vota.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Election implements Serializable {
    public String id;
    public String title;
    public String type;
    public String description;
    public String region;
    public String status;
    @SerializedName("opens_at") public String opensAt;
    @SerializedName("closes_at") public String closesAt;
    @SerializedName("results_published") public boolean resultsPublished;

    public Election() {}
}