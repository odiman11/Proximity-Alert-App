package com.portfolio.proximityalerts;


import java.io.*;
import java.net.*;
import java.util.*;



class Message {
    MType type;
    Position position;
    String MMSI;
    String name;
    int cog;
    float sog;

    public Message(MType typ, Position pos, String mmsi, String nam, int co, double so){
        type = typ;
        position = pos;
        MMSI = mmsi;
        name = nam;
        cog = co;
        sog = (float)so;
    }

    public Message(DatagramPacket p) throws Exception{
        this(p.getData(), p.getLength());
    }


    public Message(byte[] bmsg, int length) throws Exception{
        String message = new String(bmsg, 0, length);
        String [] msg = message.split(";");

        type = MType.fromString(msg[0]);

        switch(type){
            case KILL:
                break;
            case INFO:
                break;
            case TARGET:
                position = new Position(msg[1]);
                MMSI = msg[2];
                name = msg[3];
                try{cog = Byte.parseByte(msg[4]); }catch (Exception x){cog = 0;}
                try{sog = Float.parseFloat(msg[5]); }catch (Exception x){sog = 0;}
                break;
            case REQ:
                position = new Position(msg[1]);
                MMSI = "";
                name = "";
                break;
            default:
                throw new Exception("Bad Message " + message);
        }
    }

    public String toString(){
        return type.toByte() + ";" +
                position + ";" +
                MMSI + ";" +
                name + ";" +
                cog + ";" +
                sog + ";";
    }

    public byte[] getBytes(){
        return this.toString().getBytes();
    }



    public static void main(String args[]) throws Exception
    {
        String msg = "2; 45,47;428000;vasya;120;45.916";
        System.out.println("Hello, world1!\n"+ new Message(msg.getBytes(), msg.length()));

        MType java1 = MType.fromByte(3);
        System.out.println(java1.toByte());
        System.out.println(java1);
    }
}


