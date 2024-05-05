package mainApp;

import java.io.IOException;
import java.lang.Runnable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class serverThread implements Runnable {

    private MainComponent component;
    private int port;
    private static final long tickrate = 7812500; // 128th of a second in ns
    private ServerSocket serverSocket;
    private Socket clientSocket;

    public serverThread(MainComponent component){
        this.component = component;
        this.port = Integer.parseInt(this.component.getPort());
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            this.clientSocket = serverSocket.accept();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        long time = 0;
        long deltaT = 10;
        while (true) {
            time  = 1*System.nanoTime();

            deltaT = 1*System.nanoTime() - time;
            try {
                TimeUnit.NANOSECONDS.sleep(this.tickrate - deltaT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // while (this.component)
    }

}