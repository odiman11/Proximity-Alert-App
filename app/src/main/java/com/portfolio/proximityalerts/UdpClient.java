package com.portfolio.proximityalerts;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UdpClient {

    //CHANGE HOST AND PORT IF NEEDED
    private static final String HOST = GlobalSettings.getServerDomain();
    private static final int PORT = GlobalSettings.getPort();

    public static final String DEVICE_NAME = "phone";
    public static final String DEVICE_MMSI = "0014";

    //VARIABLES
    public static final String TAG = "UDPClient";
    DatagramSocket udpSocket;
    InetAddress serverAddr;
    DatagramPacket packet;
    static boolean isAlive = true;
    byte[] buf;
    private static Thread Thread1;
    private static UdpClient clientInstance = null;
    static MutableLiveData<Message> clientLiveData = null;
    static long currentTimeStamp;
    static long timeStamp;
    static Location newLocation;

    RadarManager radarManager;
    Context mainFragmentContext;

    //CONSTRUCTOR
    public UdpClient(Context context) {
        clientLiveData = new MutableLiveData<Message>();
        timeStamp = System.currentTimeMillis()/1000;
        mainFragmentContext = context;
    }//CONSTRUCTOR END

    //client server actions must be preformed in their own thread
    class Thread1 implements Runnable {
        @Override
        public void run() {
            //loop until socket is created
            if(udpSocket == null || udpSocket.isClosed()) {
                while (true) {
                    try {
                        // create new socket and connect to the server
                        udpSocket = new DatagramSocket(PORT);
                        serverAddr = InetAddress.getByName(HOST);
                        Log.e(TAG, "socket created");
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "failed to create socket" + e);
                        e.printStackTrace();
                    }
                }//END SOCKET LOOP
            }
            //Listen loop
            while (isAlive) {
                //newLocation = GpsManager.currentLocation;
                currentTimeStamp = System.currentTimeMillis();
                if(currentTimeStamp - timeStamp >= 5000 && newLocation != null) {
                    timeStamp = currentTimeStamp;
                    //send data
                    sendMessage(newLocation);
                    //Log.e(TAG, "msg sent");

                    //recv data
                    while (true) {
                        try {
                            byte[] message = new byte[80];
                            packet = new DatagramPacket(message, message.length);
                            udpSocket.setSoTimeout(1000);
                            udpSocket.receive(packet);
                            Message recv = new Message(packet.getData(), packet.getLength());
                            //Log.e(TAG, recv.toString() + " received");
                            //clientLiveData.postValue(recv);
                            EncounterView view = new EncounterView(mainFragmentContext, recv);
                            RadarManager.setEncounter(view);


                        } catch (SocketTimeoutException e) {
                            //Log.e("Timeout Exception","UDP Connection:",e);
                            System.out.println("timed out");
                            break;
                        } catch (IOException e) {
                            Log.e(" UDP client", "error: ", e);
                            udpSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }//END LISTEN LOOP
        }
    }//THREAD END

    public void sendMessage(Location location){
        Position p = new Position((float)location.getLatitude(),(float)location.getLongitude());
        Message send = new Message(MType.REQ, p, DEVICE_MMSI, DEVICE_NAME, (byte) 0, 0);
        buf = send.toString().getBytes();
        packet = new DatagramPacket(buf, buf.length, serverAddr, PORT);
        //Log.e(TAG, "trying to send location" + location);
        try {
            udpSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UdpClient getInstance(Context context) {
        if (clientInstance == null) {
            clientInstance = new UdpClient(context);
        }
        return clientInstance;
    }

    public static MutableLiveData<Message> getNewMessage() {
        if (clientLiveData == null) {
            clientLiveData = new MutableLiveData<Message>();
        }
        return clientLiveData;
    }

    public void startClient(){
        setIsAlive(true);
        Thread1 = new Thread(new Thread1());
        Thread1.start();
    }

    public void stopClient(){
        setIsAlive(false);
    }

    public static void setCurrentLocation(Location l){
        newLocation = l;
    }

    private static void setIsAlive(boolean state){
        isAlive = state;
    }
}
