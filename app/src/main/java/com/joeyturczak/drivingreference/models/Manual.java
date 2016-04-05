package com.joeyturczak.drivingreference.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 *
 * Stores driver manual information in a Parcelable implementation
 */
public class Manual implements Parcelable {

    public static final int NOT_DOWNLOADED = 0;
    public static final int DOWNLOADED = 1;

    private long id;
    private String location;
    private String type;
    private String language;
    private String url;
    private String displayName;
    private int downloaded;
    private int lastPage;

    public Manual(long id, String location, String type, String language, String url, String displayName, int downloaded, int lastPage) {
        this.id = id;
        this.location = location;
        this.type = type;
        this.language = language;
        this.url = url;
        this.displayName = displayName;
        this.downloaded = downloaded;
        this.lastPage = lastPage;
    }

    protected Manual(Parcel in) {
        id = in.readLong();
        location = in.readString();
        type = in.readString();
        language = in.readString();
        url = in.readString();
        displayName = in.readString();
        downloaded = in.readInt();
        lastPage = in.readInt();
    }

    public static final Creator<Manual> CREATOR = new Creator<Manual>() {
        @Override
        public Manual createFromParcel(Parcel in) {
            return new Manual(in);
        }

        @Override
        public Manual[] newArray(int size) {
            return new Manual[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(location);
        dest.writeString(type);
        dest.writeString(language);
        dest.writeString(url);
        dest.writeString(displayName);
        dest.writeInt(downloaded);
        dest.writeInt(lastPage);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
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

    public int getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }
}
