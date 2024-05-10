package mainApp;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Runnable;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.JFrame;

public class serverReceiver extends Thread {

    private MainComponent component;
    // private int port;
    private static final long tickrate = 7812500; // 128th of a second in ns
    private Socket clientSocket;
    private DataInputStream ingest;
    private ReadWriteLock lock;

    public serverReceiver(MainComponent component, DataInputStream clientSocket){
        this.component = component;
        // this.port = Integer.parseInt(this.component.getPort());
        // this.clientSocket = clientSocket;
        this.lock = this.component.getLock();
        this.ingest = clientSocket;
        // try {
            // this.ingest = new DataInputStream(clientSocket.getInputStream());
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
    }

    private byte[] receivePacket(){
        byte[] length = {1};
        try {
            this.ingest.read(length);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return length;
        }
        // System.out.println(length[0]);
        byte[] packet = new byte[length[0]];

        try {
            this.ingest.read(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packet;

    }

    private void parsePacket(byte[] packet){
        this.lock.writeLock().lock();
        // System.out.println(packet[0]);
        // System.out.println(packet[1]);
        switch (packet[0]) {
            case 0:
                this.component.setOtherColor(packet);
                break;
            case 1:
                this.component.setLevel(packet);
                break;
            case 2:
                this.component.setEntityPos(packet);
                // System.out.println(packet[18]);
                break;
            default:
                break;
        }
        this.lock.writeLock().unlock();
    }

    @Override
    public void run() {

        while (this.component.hasClient() || this.component.isHost()) {
            byte[] packet = receivePacket();
            if (packet.length == 1) {
                break;
            }
            parsePacket(packet);
            
            // long time = 0;
            // long deltaT = 10;
            // while (this.component.hasClient()) {
            //     time  = 1*System.nanoTime();

            //     deltaT = 1*System.nanoTime() - time;
            //     try {
            //         TimeUnit.NANOSECONDS.sleep(this.tickrate - deltaT);
            //     } catch (InterruptedException e) {
            //         e.printStackTrace();
            //     }
            // }
        }
        return;
        // while (this.component)
    }

}