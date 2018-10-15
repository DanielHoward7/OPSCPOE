package com.dan.admin.opscpoe;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable{
    private String id;
    private String email;
    private String username;
    private String mode;

    public Profile(){

    }

    public Profile(String id,String email,String username, String mode){
        this.id = id;
        this.email = email;
        this.username = username;
        this.mode = mode;
    }

    protected Profile(Parcel in) {
        email = in.readString();
        id = in.readString();
        username = in.readString();
        mode = in.readString();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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
        dest.writeString(mode);
    }


}
