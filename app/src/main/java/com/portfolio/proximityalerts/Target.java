package com.portfolio.proximityalerts;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;


class Target {
    Position position;
    String name;
    float cog;
    float sog;
    String MMSI;

    LocalDateTime lastime;
    byte profile;

    public Target(Position ipos, String iname, float icog, float isog, byte iprofile){
        position = ipos;
        name = iname;
        cog = icog;
        sog = isog;
        lastime = LocalDateTime.now();
        profile = iprofile;
    }

    public Target(Message m){
        MMSI = m.MMSI;
        position = m.position;
        lastime = LocalDateTime.now();
        name = m.name;
        cog = m.cog;
        sog = m.sog;
        profile = 0;
    }

    public String toString()
    {
        return MMSI + "-" + name + " at " + position;
    }

    public void update(Message m){

        if(m.position.valid()) position = m.position;
        lastime = LocalDateTime.now();
        if((name == null) || (name.length() < m.name.length())) name = m.name;
        if(m.cog != Float.NaN) cog = m.cog;
        if(m.sog != Float.NaN) sog = m.sog;
    }


}