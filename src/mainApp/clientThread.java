package mainApp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Runnable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.JFrame;

public class clientThread extends Thread {

    private MainComponent component;
    private String address;
    private int port;
    private Socket clientSocket;
    private DataOutputStream sendstream;
    private serverReceiver receiver;
    private ReadWriteLock lock;
    private DataInputStream ingest;
    private static final long tickrate = 7812500; // 128th of a second in ns

    public clientThread(MainComponent component){
        this.component = component;
        this.address = this.component.getAddress();
        this.port = Integer.parseInt(this.component.getPort());
        this.lock = this.component.getLock();
    }

    @Override
    public void run() {
        try {
            this.clientSocket = new Socket(this.address, this.port);
        } catch (NumberFormatException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.component.toggleHasClient();
        try {
            this.ingest = new DataInputStream(this.clientSocket.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            sendstream = new DataOutputStream(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        byte[] pac = {'u','e'};
        try {
            sendstream.write(pac);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            this.ingest.read(pac);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println((char)pac[1]);

        receiver = new serverReceiver(this.component, this.ingest);
        receiver.start();

        // receiver = new serverReceiver(this.component, this.clientSocket);
        // receiver.start();

        this.lock.readLock().lock();
        try {
            this.sendstream.write(this.component.getUserPack());
            this.sendstream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.lock.readLock().unlock();


        ArrayList<byte[]> packets;
        long time = 0;
        long deltaT = 10;
        while (this.component.hasClient()) {
            time  = 1*System.nanoTime();
            deltaT = 1*System.nanoTime() - time;
            packets = new ArrayList<>();
            this.lock.readLock().lock();
            for (int i = 0; i < 2; i++) {
                switch (i) {
                    case 0:
                        if (this.component.isNewName()){
                            this.component.toggleNewName();
                            packets.add(this.component.getUserPack());
                        }   
                        break;
                    case 1:
                        packets.addAll(this.component.getEntityPositions());
                        break;
                    default:
                        break;
                }
            }
            this.lock.readLock().unlock();
            for(byte[] packet : packets) {
                // try {
                //     sendstream.writeByte(packet[0] + 1);
                // } catch (IOException e) {
                //     // TODO Auto-generated catch block
                //     e.printStackTrace();
                // }
                try {
                    sendstream.write(packet);
                    sendstream.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    this.component.toggleHasClient();
                    try {
                        clientSocket.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
            try {
                TimeUnit.NANOSECONDS.sleep(this.tickrate - deltaT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
