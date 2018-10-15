package com.dan.admin.opscpoe;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable{
    private String id;
    private String email;
    private String username;
    private String unit;
    private String mode;

    public Profile(){

    }

    public Profile(String id,String email,String username, String unit, String mode){
        this.id = id;
        this.email = email;
        this.username = username;
        this.unit = unit;
        this.mode = mode;
    }

    protected Profile(Parcel in) {
        email = in.readString();
        id = in.readString();
        username = in.readString();
    }
    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public static Parcelable.Creator<Profile> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return id;
    }

    public void setUser_id(String user_id) {
        this.id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "email='" + email + '\'' +
                ", user_id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(id);
        dest.writeString(username);
    }


}
