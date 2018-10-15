package com.dan.admin.opscpoe;

import android.app.Application;
import com.dan.admin.opscpoe.Profile;

public class UserProfile extends Application {

   private Profile profile = null;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
