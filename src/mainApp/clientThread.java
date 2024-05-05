package mainApp;

import java.io.IOException;
import java.lang.Runnable;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class clientThread implements Runnable {

    private MainComponent component;
    private String address;
    private String port;
    private Socket clientSocket;
    private static final long tickrate = 7812500; // 128th of a second in ns

    public clientThread(MainComponent component){
        this.component = component;
        this.address = this.component.getAddress();
        this.port = this.component.getPort();
    }

    @Override
    public void run() {
        try {
            this.clientSocket = new Socket(this.address, Integer.parseInt(this.port));
        } catch (NumberFormatException | IOException e) {
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
    }

}
