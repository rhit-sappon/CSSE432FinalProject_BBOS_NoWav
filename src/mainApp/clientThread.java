package mainApp;

import java.lang.Runnable;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class clientThread implements Runnable {

    private MainComponent component;
    private String address;
    private static final long tickrate = 7812500; // 128th of a second in ns

    public clientThread(String address, MainComponent component){
        this.component = component;
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
    }

}
