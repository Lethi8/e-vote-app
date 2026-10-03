package com.example.vota.model;

import com.google.gson.annotations.SerializedName;

public class EducationResource {
    public String id;
    public String eyebrow;
    public String title;
    public String body;
    @SerializedName("sort_order") public int sortOrder;

    public EducationResource() {}
}