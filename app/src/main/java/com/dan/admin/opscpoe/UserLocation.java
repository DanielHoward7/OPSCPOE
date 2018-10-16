package com.dan.admin.opscpoe;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class UserLocation {

    private GeoPoint geoPoint;
    private Profile profile;

    public UserLocation(GeoPoint geoPoint, Profile profile) {
        this.geoPoint = geoPoint;
        this.profile = profile;
    }

    public UserLocation() {

    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }


    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
