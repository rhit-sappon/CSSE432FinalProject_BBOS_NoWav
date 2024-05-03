package mainApp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.lang.Thread;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Class: MainApp
 * @author Brendan Perez, Bryce Bejlovec, Owen Sapp
 * <br>Purpose: Top level class for CSSE220 Project containing main method 
 * <br>Restrictions: None
 */
public class MainApp {
	private static final int XDIM = 20;
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
		this.frame = new JFrame("Level 0");
		this.frame.setSize(XDIM*60,YDIM*60+30);
		this.frame.setLayout(new BorderLayout());
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
		this.component = new MainComponent(scanforLevel("levelGen.csv"), XDIM,YDIM);
		this.frame.add(this.component,BorderLayout.CENTER);
		this.component.interpolateLevel();
		this.component.addMouseListener((MouseListener) new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println(e.getPoint());
				// int xmidclick = e.getX() - component.getXMid();
				// int ymidclick = -45 + e.getY() - component.getYMid();
				// System.out.println(xmidclick);
				// System.out.println(ymidclick);
				// if( -150 < xmidclick && xmidclick < 150) {
				// 	if( -150 < ymidclick && ymidclick < -50) {
				// 		component.startGame();
				// 	}
				// }
				if (component.isSettings()){
					if (component.hitLeaveSettings(e.getX(), e.getY())) {
						component.toggleSettings();
					}
				} else if (component.isInMenu()) {
					if (component.hitStartButton(e.getX(),e.getY())){
						component.startGame();
					} else if(component.hitConnectButton(e.getX(),e.getY())){
						component.startGame();
					} else if(component.hitHostButton(e.getX(),e.getY())){
						component.startGame();
					} else if(component.hitSettingsButton(e.getX(),e.getY())){
						component.toggleSettings();
					}
				} else if (component.isPaused()) {
					if (component.hitToMenu(e.getX(),e.getY())){
						component.startGame();
					} else if(component.hitSettingsButton(e.getX(),e.getY())){
						component.toggleSettings();
					}
				}
				// throw new UnsupportedOperationException("Unimplemented method 'mouseClicked'");
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				// throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				// throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				// throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				// throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
			}
			
		});
		
		this.frame.addKeyListener((KeyListener) new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {;
				if(e.getKeyCode()==27) {
					if (component.isPaused()) {
						if(component.isSettings()) {
							component.toggleSettings();
						}
					}
					component.togglePause();
				}
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