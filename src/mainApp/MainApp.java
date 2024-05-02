package mainApp;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.Timer;


/**
 * Class: MainApp
 * @author Brendan Perez, Bryce Bejlovec, Owen Sapp
 * <br>Purpose: Top level class for CSSE220 Project containing main method 
 * <br>Restrictions: None
 */
public class MainApp {
	private static final int XDIM = 30;
	private static final int YDIM= 15;


	private static final int DELAY = 10;
	private MainComponent component;
	private JFrame frame;
	private long limit = 16666666;
	private void setFrameTitle(String title) {
		this.frame.setTitle(title);
	}
	private void levelSelect(int levelshift) {
		component.changeLevel(levelshift);
		levelshift = 0;
	}
	private void setUpViewer() {
		this.frame = new JFrame("JackBomb Level 0");
		this.frame.setSize(XDIM*60,YDIM*60+30);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
		this.component = new MainComponent(scanforLevel("levelGen.csv"), XDIM,YDIM);
		this.frame.add(this.component);
		this.component.interpolateLevel();
		
		this.frame.addKeyListener((KeyListener) new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==39) {
					component.updateHeroXVel(10); //R Arrow
				}else if(e.getKeyCode()==37) {
					component.updateHeroXVel(-10);;//L Arrow
				}
				
				if(e.getKeyCode()==40) {
					component.updateHeroYVel(10);
					component.phaseToggle(true);
					//D Arrow
				}else if(e.getKeyCode()==38) {
					component.jump(); //U Arrow
				}
				if(e.getKeyCode()==89) {
					levelSelect(-1);
					//Y
				}
				if(e.getKeyCode()==85) {
					levelSelect(1);
					//U
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode()==39) {
					component.updateHeroXVel(0); //R Arrow
				}else if(e.getKeyCode()==40) {
					component.phaseToggle(false); //D Arrow
				}else if(e.getKeyCode()==37) {
					component.updateHeroXVel(0);//L Arrow
				}
			}
		});
		
	} // setUpViewer
	
	private void runApp() {
		this.frame.setVisible(true);
	}
	
	public void repaint() {
		this.frame.repaint();
	}

	/**
	 * ensures: runs the application
	 * @param args unused
	 */
	public static void main(String[] args) {
		LevelGenerator.generateLevel(0, false, XDIM, YDIM);
		MainApp mainApp = new MainApp();
		mainApp.setUpViewer();
		mainApp.runApp();
		mainApp.setFrameTitle("Level " + mainApp.component.getLevel());
//		Timer t = new Timer(DELAY, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				
//			}
//		});
//		t.start();
		long time;
		long deltaT = 10;
		while(true) {
			
			time  = 1*System.nanoTime();
			mainApp.component.physics((float) (((float)deltaT)/10000000.00));
			mainApp.repaint();
			deltaT = 1*System.nanoTime() - time;
			// mainApp.levelSelect();
			if(mainApp.component.nextLevel())
				mainApp.setFrameTitle("Level " + mainApp.component.getLevel());
			// try{
			// 	TimeUnit.NANOSECONDS.sleep(mainApp.limit - deltaT);
			// } catch (Exception e) {
			// 	// TODO Auto-generated catch block
			// 	e.printStackTrace();
			// }
		}
	} // main
	
	
	public ArrayList<Integer[]> scanforLevel(String filename) {
		FileReader file = null;
		ArrayList<Integer[]> levelData = new ArrayList<Integer[]>();
		try {
			file = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("bruh");
		}
		Scanner scanner = new Scanner(file);
		while(scanner.hasNext()) {
			String[] sLevelData = scanner.next().split(",");
			Integer[] rowNum = new Integer[XDIM];
			for(int i = 0; i < sLevelData.length; i++) {
				rowNum[i] = Integer.parseInt(sLevelData[i]);
			}
			levelData.add(rowNum);
		}
		scanner.close();
		return levelData;
	}
}