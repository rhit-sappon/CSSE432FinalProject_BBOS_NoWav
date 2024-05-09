package mainApp;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Runnable;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class serverReceiver implements Runnable {

    private MainComponent component;
    // private int port;
    private static final long tickrate = 7812500; // 128th of a second in ns
    private Socket clientSocket;
    private DataInputStream ingest;

    public serverReceiver(MainComponent component, Socket clientSocket){
        this.component = component;
        // this.port = Integer.parseInt(this.component.getPort());
        this.clientSocket = clientSocket;
        try {
            this.ingest = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private byte[] receivePacket(){
        byte[] length = {1};
        try {
            this.ingest.read(length,0, length[0]);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] packet = new byte[length[0]];

        try {
            this.ingest.read(packet,0,length[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packet;

    }

    private void parsePacket(byte[] packet){
        switch (packet[0]) {
            case 0:
                this.component.setOtherColor(packet);
                break;
        
            default:
                break;
        }
    }

    @Override
    public void run() {

        while (this.component.hasClient()) {
            byte[] packet = receivePacket();
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
        
        // while (this.component)
    }

}