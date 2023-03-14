package com.portfolio.proximityalerts;
public enum MType{
    KILL,
    INFO,
    TARGET,
    REQ;

    //public final byte type;

	/*MType(int t){
		type = (byte)t;
	}

	MType(byte b){
		type = b;
	}*/

    public byte toByte(){
        return (byte)this.ordinal();
    }

    public static MType fromByte(byte f){
        return MType.values()[f];
    }

    public static MType fromByte(int f){
        return MType.values()[f];
    }

    public static MType fromString(String s){
        return fromByte(Byte.parseByte(s));
    }
}