package com.dan.admin.opscpoe;

public class Profile {
    private String id;
    private String unit;
    private String mode;

    public Profile(){

    }

    public Profile(String id, String unit, String mode){
        this.id = id;
        this.unit = unit;
        this.mode = mode;
    }

    public String getId(){
        return id;
    }

    public String getUnit(){
        return unit;
    }

    public String getMode(){
        return mode;
    }


}
