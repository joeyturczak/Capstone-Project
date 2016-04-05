package com.joeyturczak.drivingreference.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import com.joeyturczak.myjavalibrary.DrivingValues.Location;
import com.joeyturczak.myjavalibrary.DrivingValues.Type;
import com.joeyturczak.myjavalibrary.DrivingValues.Language;

/**
 * Copyright (C) 2016 Joey Turczak
 */

@Entity
public class DrivingManual {

    @Id private Long id;
    private Location location;
    private Type type;
    private Language language;
    private String url;
    private String displayName;
    @Index
    private long lastUpdated;

    public DrivingManual() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
