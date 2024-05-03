package mainApp;

import java.lang.Runnable;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class serverThread implements Runnable {

    private MainComponent component;
    private int port;
    private static final long tickrate = 7812500; // 128th of a second in ns

    public serverThread(String port, MainComponent component){
        this.component = component;
        this.port = Integer.parseInt(port);
    }

    @Override
    public void run() {
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