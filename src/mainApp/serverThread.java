package mainApp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Runnable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.JFrame;

public class serverThread implements Runnable {

    private MainComponent component;
    private int port;
    private static final long tickrate = 7812500; // 128th of a second in ns
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private serverReceiver receiver;
    private int sendState;
    private DataOutputStream sendstream;
    private ReadWriteLock lock;

    public serverThread(MainComponent component){
        this.component = component;
        this.port = Integer.parseInt(this.component.getPort());
        this.lock = this.component.getLock();
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (this.component.isServer()) {
            try {
                this.clientSocket = serverSocket.accept();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.component.toggleHasClient();

            receiver = new serverReceiver(this.component, this.clientSocket);
            receiver.run();

            try {
                sendstream = new DataOutputStream(this.clientSocket.getOutputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                this.component.toggleHasClient();
            }
            
            
            
            long time = 0;
            long deltaT = 10;
            ArrayList<byte[]> packets;
            // this.component.toggleNewName();
            // this.component.toggleNewLevel();
            while (this.component.hasClient()) {
                time  = 1*System.nanoTime();
                deltaT = 1*System.nanoTime() - time;
                packets = new ArrayList<>();
                this.lock.readLock().lock();
                for (int i = 0; i < 3; i++) {
                    switch (i) {
                        case 0:
                            if (this.component.isNewName()){
                                this.component.toggleNewName();
                                packets.add(this.component.getUserPack());
                            }   
                            break;
                        case 1:
                            if (this.component.isNewLevel()){
                                this.component.toggleNewLevel();
                                packets.add(this.component.getServerPack());
                            }
                            break;
                        case 2:
                            packets.addAll(this.component.getEntityPositions());
                            break;
                        default:
                            break;
                    }
                }
                this.lock.readLock().unlock();
                for(byte[] packet : packets) {
                    try {
                        sendstream.writeByte(packet[0] + 1);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        sendstream.write(packet);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        this.component.toggleHasClient();
                    }
                }
                try {
                    TimeUnit.NANOSECONDS.sleep(this.tickrate - deltaT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        
        // while (this.component)
    }

}