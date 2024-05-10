package mainApp;

import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.locks.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.geom.Rectangle2D.Float;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Random;
import java.awt.Toolkit;
import java.nio.ByteBuffer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;

public class MainComponent extends JComponent {
	
	private Hero hero;
	private Hero otherHero;
	ArrayList<Integer[]> levelData = new ArrayList<>();
	ArrayList<Rectangle2D.Double> levelWalls;
	Rectangle2D.Double levelBackdrop;
	Rectangle2D.Double gamePause;
	ArrayList<Rectangle2D.Double> levelPlatforms;
	ArrayList<Rectangle2D.Double> levelBombs;
	ArrayList<Creature> objects = new ArrayList<>();
	ArrayList<Creature> toDie = new ArrayList<>();

	// ArrayList<Rectangle2D.Double> mainMenu = new ArrayList<>();
	// ArrayList<Rectangle2D.Double> pause =  new ArrayList<>();
	// ArrayList<Rectangle2D.Double> settings = new ArrayList<>();

	ArrayList<HashMap<String,Rectangle2D.Double>> buttons = new ArrayList<>();

	// HashMap<String,Rectangle2D.Double> mainMenu = new HashMap<>();
	// HashMap<String,Rectangle2D.Double> pause = new HashMap<>();
	// HashMap<String,Rectangle2D.Double> settings = new HashMap<>();

	private static final byte EPACKET = 2;
	

	private boolean isUnix = System.getProperty("os.name").startsWith("Linux");
	private float fpsTimer = 100;
	private long level = 0;
	private int bombs = 0;
	protected static final float GRAVITY = (float) 1.0;
	private Scorecard scorecard;
	private float step = 1;
	private float avgstep = 10;
	private int jumptime = 0;
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadWriteLock levelLock = new ReentrantReadWriteLock();
	private boolean nextlevel = false;
	private int xdim;
	private int ydim;
	private int xpix;
	private int ypix;
	private int xmid;
	private int ymid;
	private boolean pausecontrol = false;
	private boolean inmenu = true;
	private boolean showSettings = false;
	private int inText = -1;
	private Color pauseColor = new Color(0.1f,0.1f,0.1f,0.5f);
	private ArrayList<StringBuffer> settingStrings = new ArrayList<>();
	private HashMap<String,Integer> inTextToCode = new HashMap<String,Integer>();
	private String clientName;

	private boolean isHost = true;
	private boolean isServer = false;
	private boolean hasClient = false;
	private boolean isNewName = false;
	private boolean isNewLevel = false;

	private int prevObj = 0;
	
	public MainComponent(ArrayList<Integer[]> levelData, int XDIM, int YDIM) {
		super();
		this.hero = new Hero(10, 10);
		this.otherHero = new Hero(10, 10);
		this.scorecard = new Scorecard(XDIM,YDIM);
		this.levelData=levelData;
		this.xdim = XDIM;
		this.ydim = YDIM;
		this.xpix = XDIM*60;
		this.ypix = YDIM*60;
		this.xmid = this.xpix/2;
		this.ymid = this.ypix/2;

		this.levelBackdrop = new Rectangle2D.Double(0,0,this.xpix,this.ypix);
		this.gamePause = new Rectangle2D.Double(0,0,this.xpix,this.ypix);

		this.buttons.add(new HashMap<String,Rectangle2D.Double>());
		this.buttons.add(new HashMap<String,Rectangle2D.Double>());
		this.buttons.add(new HashMap<String,Rectangle2D.Double>());

		this.buttons.get(0).put("Play Singleplayer",new Rectangle2D.Double(this.xmid - 150 ,this.ymid - 150,300,100)); //Singleplayer
		this.buttons.get(0).put("Connect to Server",new Rectangle2D.Double(this.xmid - 150 ,this.ymid,300,100)); //Join server
		this.buttons.get(0).put("Host a Server",new Rectangle2D.Double(this.xmid - 150 ,this.ymid + 150,300,100)); //Host Server
		this.buttons.get(0).put("Settings",new Rectangle2D.Double(this.xmid - 150 ,this.ymid + 300,300,100)); //Settings

		this.buttons.get(1).put("Return to Menu",new Rectangle2D.Double(this.xmid - 150 ,this.ymid - 150,300,100)); //back to menu
		this.buttons.get(1).put("Settings",new Rectangle2D.Double(this.xmid - 150 ,this.ymid,300,100)); //Settings

		this.buttons.get(2).put("Go Back",new Rectangle2D.Double(this.xmid - 150 ,this.ymid - 300,300,100));//Back to previous screen
		this.buttons.get(2).put("Server IP",new Rectangle2D.Double(this.xmid - 150 ,this.ymid - 150,300,100));
		this.buttons.get(2).put("Color",new Rectangle2D.Double(this.xmid - 150 ,this.ymid,300,100));
		this.buttons.get(2).put("Name",new Rectangle2D.Double(this.xmid - 150 ,this.ymid + 150,300,100));
		this.buttons.get(2).put("Port",new Rectangle2D.Double(this.xmid - 150 ,this.ymid + 300,300,100));

		this.inTextToCode.put("Color",0);
		this.inTextToCode.put("Name",1);
		this.inTextToCode.put("Server IP",2);
		this.inTextToCode.put("Port",3);

		this.settingStrings.add(new StringBuffer(6));
		this.settingStrings.add(new StringBuffer(20));
		this.settingStrings.add(new StringBuffer(32));
		this.settingStrings.add(new StringBuffer(5));

		this.settingStrings.get(0).append("18A1F1");
		this.settingStrings.get(1).append("Name");
		this.settingStrings.get(2).append("127.0.0.1");
		this.settingStrings.get(3).append("25565");
		setHeroColor();
		
		// this.settingStrings.add(new StringBuilder());

		makeMumsic();
	} 
	
	protected void physics(float step) {
		this.fpsTimer += step;
		this.avgstep = (avgstep + step)/2;
		if(fpsTimer >= 100) {
			fpsTimer -= 100;
			scorecard.framerate(avgstep);
		}
		if (this.pausecontrol || this.inmenu) {
			return;
		}
		lock.readLock().lock();
		if(this.jumptime > 0) {
			this.updateHeroYVel(-20);
			this.hero.setOnGround(false);
			this.hero.setOnPlat(false);
			this.jumptime--;
		}
		lock.readLock().unlock();
		this.step = step;
		if((this.hero.getYVel() < 20 || this.hero.getOnPlat() && this.hero.getPhase()) && !this.hero.getOnGround())
			this.updateHeroYVel(this.hero.getYVel() + GRAVITY*(this.step));
		
		if(this.hero.isDead()) {
			if(this.scorecard.getLives() > 0) {
			this.hero.isAlive = true;
			this.hero.setPosition(240,780);
			}
		}
		if(this.handleCeilingCollision(this.hero))
			this.hero.updateY(this.step);
		if(this.handleWallCollisionX(this.hero))
			this.hero.updateX(this.step);
		this.hero.updateRect();
		loop(this.hero);
		
		if(this.hero.getYVel() == 0.0 || !this.hero.getOnGround() || !this.hero.getOnPlat()) {
			this.hero.setOnPlat(this.handleOnPlat(this.hero));
			this.hero.setOnGround(this.handleOnGround(this.hero));
			}
		ArrayList<Rectangle2D.Double> tempBomb = new ArrayList<Rectangle2D.Double>();
		
		if(levelBombs.size() > 0) {
			for(Rectangle2D.Double bomb : levelBombs) {
				if(bomb.intersects(this.hero.getRect())){
					this.scorecard.addScore(10);
					tempBomb.add(bomb);
					this.bombs --;
				}
			}
		}
		
		
		if(objects.size() > 0)
			for(Creature enemy : objects) {
				enemy.collidesWithHero(this.hero,this.scorecard);
				enemy.moveTowardsHero(this.hero);
				if (this.isHost) {
					if(this.handleCeilingCollision(enemy))
						enemy.updateY(this.step);
					if(this.handleWallCollisionX(enemy))
						enemy.updateX(this.step);
					enemy.updateRect();
				}
			}
		
		if(this.hero.getOnGround() || this.hero.getOnPlat())
			if((this.hero.getYVel() > 0 || this.hero.getOnGround() && !this.hero.getPhase()) && this.jumptime == 0)
				this.updateHeroYVel(0);
		this.levelLock.writeLock().lock();
		for(Rectangle2D.Double bomb : tempBomb) {
			levelBombs.remove(bomb);
		}
		this.levelLock.writeLock().unlock();
		if(this.bombs == 0) {
			SoundiBoi.stopTrack();
			makeMumsic();
			changeLevel(1);
		}
		
	}

	protected void unixSync() {
		if (this.isUnix) {
			Toolkit.getDefaultToolkit().sync();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		unixSync();
		Graphics2D g2d = (Graphics2D) g;
		
		this.levelLock.readLock().lock();
		if(this.inmenu){
			drawMenu(g2d);
			this.levelLock.readLock().unlock();
			return;
		}
		if (this.hero.isAlive) {
			drawLevel(g2d);
			
			this.hero.drawOn(g2d);
			if (this.hasClient || !this.isHost) {
				this.otherHero.drawOn(g2d);
			}
			if(objects.size() > 0)
				for(Creature enemy : objects) {
					enemy.drawOn(g2d);
				}
		}

		this.scorecard.drawOn(g2d);

		if (this.pausecontrol){
			drawMenu(g2d);
		}
		this.levelLock.readLock().unlock();
	} // paintComponent
	
	public Creature getHero() {
		return this.hero;
	}
	
	public boolean nextLevel() {
		if(this.nextlevel) {
			this.nextlevel = false;
			return true;
		}
		return false;
	}

	public void changeLevel(int dir) {
		this.levelLock.writeLock().lock();
		this.scorecard.addScore(100);
		this.scorecard.setLife(3);
		this.level = this.level + dir;
		LevelGenerator.generateLevel(this.level, false,this.xdim,this.ydim);
		scanforLevel("levelGen.csv");
		interpolateLevel();
		this.levelLock.writeLock().unlock();
		this.nextlevel = true;
		this.isNewLevel = true;
		
	}

	public void restart(){
		this.level = 0;
		changeLevel(0);
		this.scorecard.reset();
		this.pausecontrol = false;
		this.hasClient = false;
		this.isHost = true;
		this.isServer = false;
	}

	public long getLevel() {
		return this.level;
	}
	
	public void updateHeroXVel(float velocity) {
		this.hero.setXVelocity(velocity);
	}
	public void updateHeroYVel(float velocity) {
		this.hero.setYVelocity(velocity);
	}
	public void updateLevelData(ArrayList<Integer[]> data) {
		this.levelData = data;
	}
	
	public void jump() {
		if((this.hero.getOnGround() || this.hero.getOnPlat())) {
			lock.writeLock().lock();
			this.jumptime = 1;
			lock.writeLock().unlock();
		}
	}
	
	
	public boolean handleWallCollisionX(Creature creature) {
		Rectangle2D.Double creatureCollide;
		float heroVel = creature.getXVel() * this.step;
		if(heroVel < 0)
			creatureCollide = new Rectangle2D.Double(creature.getXPos() + heroVel, creature.getYPos(), 60 - heroVel, 60);
		else
			creatureCollide = new Rectangle2D.Double(creature.getXPos() , creature.getYPos(), 60 + heroVel, 60);
		for(Rectangle2D.Double rect: levelWalls) {
			if(rect.intersects(creatureCollide) ) {
				double testX = ((creature.getXPos() + 30 - rect.getCenterX())%rect.getCenterX());
				if(testX >= 0) {
					creature.setPosition((float)rect.getMaxX(), creature.getYPos());
					return false;
				}
				else if(testX <= -60) {
					creature.setPosition((float)rect.getX() - 60, creature.getYPos());
					return false;
				} 
			}
		}
		return true;
	}
	public boolean handleCeilingCollision(Creature creature) {
		if(creature.getYVel() < 0) {
			Rectangle2D.Double creatureCollide = new Rectangle2D.Double(creature.getXPos(), creature.getYPos() + creature.getYVel()*this.step, 60, 60);
			for(Double rect: levelWalls) {
				if(rect.intersects(creatureCollide)) {
					creature.setPosition(creature.getXPos(), (int)rect.getMaxY());
					return false;
				}
			}
		}
		return true;
	}
//	int heroVelX = creature.getXVel();
//	int heroVelY = creature.getYVel();
//	int XPos,XDim,YPos,YDim;
//	if(heroVelX < 0) {
//		XPos = creature.getXPos() + heroVelX;
//		XDim = 60 - heroVelX;
//	}else {
//		XPos = creature.getXPos();
//		XDim = 60 + heroVelX;
//	}
//	
//	if(heroVelY < 0) {
//		YPos = creature.getYPos() + heroVelX;
//		YDim = 60 - heroVelY;
//	}else {
//		YPos = creature.getYPos();
//		YDim = 60 + heroVelY;
//	}
//	System.out.println(creature.getXPos() % 598);
//	Rectangle2D.Double creatureCollide = new Rectangle2D.Double(XPos, YPos, XDim, YDim);
	public void loop(Creature creature) {
		if(creature.getXPos() < -58) {
			creature.setPosition(this.xpix - 2, creature.getYPos());
		} else if(creature.getXPos() > this.xpix - 2) {
			creature.setPosition(-58, creature.getYPos());
		}if(creature.getYPos() < -58) {
			creature.setPosition(creature.getXPos(), this.ypix - 2);
			creature.setYVelocity(-20);
		} else if(creature.getYPos() > this.ypix-2) {
			creature.setPosition(creature.getXPos(),-58);
		}
	}

	public boolean handleOnGround(Creature creature) {
		float heroVel = creature.getYVel()*this.step;
		if(heroVel >= 0) {
			Rectangle2D.Double creatureCollide = new Rectangle2D.Double(creature.getXPos(), creature.getYPos() + 1, 60, 60+heroVel); 
			for(Rectangle2D.Double rect: levelWalls) {
				if(rect.intersects(creatureCollide)) {
					if(rect.getY() >= creature.getYPos()) 
						creature.setPosition(creature.getXPos(), (int)rect.getMinY() - 60);
					return true;
				}	
			}
		}
		return false;
	}
	
	public boolean handleOnPlat(Creature creature) {
		float heroVel = creature.getYVel()*this.step;
		if(heroVel >= 0) {
			Rectangle2D.Double creatureCollide = new Rectangle2D.Double(creature.getXPos(), creature.getYPos() + 1, 60, 60 + heroVel);
			for(Rectangle2D.Double rect: levelPlatforms) {
				if(rect.intersects(creatureCollide) && rect.getY()-50 > creature.getYPos()) {
					if(creature.getPhase()) {
						if((creature.getYPos()+60) % rect.getY() <=1 )
							creature.setPosition(creature.getXPos(), (int)rect.getMinY() - 30);
						return false;
					}else if(rect.getY() >= creature.getYPos()) 
						creature.setPosition(creature.getXPos(), (int)rect.getMinY() - 60);
					return true;
				} 
			}
		}
		return false;
	}

	// public boolean hitStartButton(int x, int y){
	// 	return this.mainMenu.get(0).contains(x,y);
	// }
	// public boolean hitConnectButton(int x, int y){
	// 	return this.mainMenu.get(1).contains(x,y);
	// }
	
	// public boolean hitHostButton(int x, int y){
	// 	return this.mainMenu.get(2).contains(x,y);
	// }

	// public boolean hitSettingsButton(int x, int y){
	// 	if (this.inmenu) {
	// 		return this.mainMenu.get(3).contains(x,y);
	// 	} else if (this.pausecontrol) {
	// 		return this.pause.get(1).contains(x,y);
	// 	}

	public boolean hitButton(int buttset, String buttname, int x, int y) {
		return this.buttons.get(buttset).get(buttname).contains(x, y);
	}
	

	// 	return false;
	// }

	// public boolean hitLeaveSettings(int x, int y){
	// 	return this.settings.get(0).contains(x,y);
	// }

	// public boolean hitToMenu(int x, int y){
	// 	return this.pause.get(0).contains(x, y);
	// }

	public void phaseToggle(boolean toggle) {
		this.hero.setPhase(toggle);
	}
	
	public void interpolateLevel() {
		this.bombs = 0;
		this.hero.setPosition(240,780);
		this.updateHeroYVel(0);
		this.updateHeroXVel(0);
		this.hero.setOnGround(true);
		this.levelWalls = new ArrayList<Rectangle2D.Double>();
		this.levelPlatforms = new ArrayList<Rectangle2D.Double>();
		this.levelBombs = new ArrayList<Rectangle2D.Double>();
		this.objects = new ArrayList<Creature>();
		byte entityValue = 1;
		int spacingx = 0;
		int spacingy = 0;
		for(int r = 0; r<this.levelData.size();r++) {
			Integer[] row = this.levelData.get(r);
			for(int c = 0; c<row.length;c++) {
				if(row[c]==1) {
					levelWalls.add(new Rectangle2D.Double(spacingx,spacingy,60,60));
				}
				else if(row[c]==2) {
					if (this.isHost) {
						this.hero.setPosition(spacingx, spacingy);
					} else {
						this.otherHero.setPosition(spacingx, spacingy);
					}
				}
				else if(row[c]==3) {
					levelPlatforms.add(new Rectangle2D.Double(spacingx,spacingy,60,20));
				}else if(row[c]==4) {
					this.bombs += 1;
					levelBombs.add(new Rectangle2D.Double(spacingx+5,spacingy+5,50,50));
				}else if(row[c]==5) {
					Enemy1 enemy = new Enemy1(spacingx,spacingy, entityValue);
					entityValue++;
					objects.add(enemy);
				}else if(row[c]==6) {
					Enemy2 enemy = new Enemy2(spacingx,spacingy, entityValue, this.xdim, this.ydim);
					entityValue++;
					objects.add(enemy);
				} else if (row[c]==7){
					if (this.isHost) {
						if (this.hasClient)
							this.otherHero.setPosition(spacingx, spacingy);
					} else {
						this.hero.setPosition(spacingx, spacingy);
					}
				}
				spacingx+=60;
			}
			spacingx=0;
			spacingy+=60;
		}
		this.prevObj = this.objects.size();
	}
	
	public void timer() {
	}
		
	
	
	private void drawLevelWalls(Graphics2D g2d) {
		for(Rectangle2D.Double rect: levelWalls) {
			g2d.setColor(Color.GREEN);
			g2d.fill(rect);
		}
	}
	private void drawBackground(Graphics2D g2d, Color backColor) {
		g2d.setColor(backColor);
		g2d.fill(levelBackdrop);
	}
	private void drawLevelPlatforms(Graphics2D g2d) {
		for(Rectangle2D.Double rect: levelPlatforms) {
			g2d.setColor(Color.CYAN);
			g2d.fill(rect);
		}
	}
	private void drawLevelBombs(Graphics2D g2d) {
		for(Rectangle2D.Double rect: levelBombs) {
			g2d.setColor(Color.BLACK);
			g2d.fill(rect);
		}
	}

	public void togglePause(){
		if(!this.inmenu)
			this.pausecontrol = !this.pausecontrol;
	}
	
	public void startGame(){
		this.inmenu = !this.inmenu;
	}

	public void inputText(int field){
		this.inText = field;
	}
	
	public int getInput(){
		return this.inText;
	}

	public void toggleSettings(){
		this.showSettings = !this.showSettings;
	}

	public boolean isInMenu(){
		return this.inmenu;
	}

	public boolean isPaused(){
		return this.pausecontrol;
	}

	public boolean isSettings(){
		return this.showSettings;
	}

	public boolean isHost(){
		return this.isHost;
	}

	public void toggleisHost(){
		this.isHost = !this.isHost;
	}


	public boolean hasClient(){
		return this.hasClient;
	}

	public void toggleHasClient(){
		this.hasClient = !this.hasClient;
	}

	public boolean isServer(){
		return this.isServer;
	}

	public void toggleServer(){
		this.isServer = !this.isServer;
	}
	
	public boolean isNewName(){
		return this.isNewName;
	}

	public void toggleNewName(){
		this.isNewName = !this.isNewName;
	}

	public boolean isNewLevel(){
		return this.isNewLevel;
	}

	public void toggleNewLevel(){
		this.isNewLevel = !this.isNewLevel;
	}


	public void readInput(char letter){
		if (this.inText > -1) {
			if(this.settingStrings.get(this.inText).length() < this.settingStrings.get(this.inText).capacity()){
				this.settingStrings.get(this.inText).append(letter);
				if(this.inText == 1){
					this.isNewName = true;
				}
			}
		}
	}

	public void backspace() {
		if (this.inText > -1) {
			int tLen = this.settingStrings.get(this.inText).length();
			if(tLen > 0) {
				this.settingStrings.get(this.inText).setLength(tLen-1);
				if(this.inText == 1){
					this.isNewName = true;
				}
			}
		}
	}

	// private void drawPause(Graphics2D g2d){
	// 	g2d.setColor(this.pauseColor);
	// 	g2d.fill(this.gamePause);
	// 	if (this.showSettings) {
	// 		for(Rectangle2D.Double button : this.settings){
	// 			g2d.setColor(this.pauseColor);
	// 			g2d.fill(button);
	// 			g2d.setColor(Color.WHITE);
	// 			g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 25)); 
	// 			g2d.drawString("Placeholder", (int)(button.x+button.width/2 - 70) , (int)(button.y + button.height/2));
	// 		}
			
	// 	} else {
	// 		for(Rectangle2D.Double button : this.pause){
	// 			g2d.setColor(this.pauseColor);
	// 			g2d.fill(button);
	// 			g2d.setColor(Color.WHITE);
	// 			g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 25)); 
	// 			g2d.drawString("Placeholder", (int)(button.x+button.width/2 - 70) , (int)(button.y + button.height/2));
	// 		}
	// 	}
	// }

	private void drawMenu(Graphics2D g2d){
		int selector = 0;
		if (this.inmenu) {
			drawBackground(g2d, Color.WHITE);
		} else {
			selector = 1;
			drawBackground(g2d, this.pauseColor);
		}

		if (this.showSettings){
			selector = 2;
		}

		for(String label : this.buttons.get(selector).keySet()){
			Rectangle2D.Double button = this.buttons.get(selector).get(label);
			g2d.setColor(this.pauseColor);
			g2d.fill(button);
		
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 25)); 
			g2d.drawString(label, (int)(button.x+button.width/2 - label.length()*6.25) , (int)(button.y + button.height/2));
		}
		if (this.showSettings) {
			drawSetStrings(g2d);
		}
	}

	private void drawSetStrings(Graphics2D g2d){
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 25));
		for(String label : this.buttons.get(2).keySet()){
			if(label.contains("Back"))
				continue;
			Rectangle2D.Double button = this.buttons.get(2).get(label);
			g2d.drawString(this.settingStrings.get(this.inTextToCode.get(label)).toString(), (int)(button.x+button.width/2 - this.settingStrings.get(this.inTextToCode.get(label)).length()*6.25) , (int)(button.y + button.height/2 + 30));
		}
	}
	
	private void drawLevel(Graphics2D g2d) {
		drawBackground(g2d,Color.WHITE);
		drawLevelWalls(g2d);
		drawLevelPlatforms(g2d);
		drawLevelBombs(g2d);
	}

	public void makeMumsic() {
		Random rand = new Random();
		int n = rand.nextInt(11);
		if(n < 2) {
			n = 2;
		}
		try {
			SoundiBoi.audioPlayer(n);
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SoundiBoi.playTrack();
	}
	
	public void scanforLevel(String filename) {
		FileReader file = null;
		try {
			file = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("bruh");
		}
		levelData.clear();
		Scanner scanner = new Scanner(file);
		while(scanner.hasNext()) {
			String[] sLevelData = scanner.next().split(",");
			Integer[] rowNum = new Integer[this.xdim];
			for(int i = 0; i < sLevelData.length; i++) {
				rowNum[i] = Integer.parseInt(sLevelData[i]);
			}
			this.levelData.add(rowNum);
		}
		scanner.close();
	}

	private byte[] getEntityPosHelper(byte entityValue, float xPos, float yPos, byte lives) {
		ByteBuffer entityArray = ByteBuffer.allocate(20);
		entityArray.put((byte)19);
		entityArray.put(EPACKET);
		entityArray.put(this.hero.getEntityValue());
		entityArray.putFloat(xPos);
		entityArray.putFloat(yPos);
		entityArray.put(lives);
		return entityArray.array();
	}

	public byte[] getUserPack() {
		byte packlen = (byte)(4+this.settingStrings.get(1).length()+1);
		byte r = (byte)Integer.parseInt(this.settingStrings.get(0).substring(0,2),16);
		byte g = (byte)Integer.parseInt(this.settingStrings.get(0).substring(2,4),16);
		byte b = (byte)Integer.parseInt(this.settingStrings.get(0).substring(4,6),16);

		ByteBuffer entityArray = ByteBuffer.allocate(packlen+1);
		entityArray.put(packlen);
		entityArray.put((byte)0);
		entityArray.put(r);
		entityArray.put(g);
		entityArray.put(b);

		for (char letter : this.settingStrings.get(1).toString().toCharArray()){
			entityArray.putChar(letter);
		}
		entityArray.put((byte) 0);

		return entityArray.array();
	}

	public byte[] getServerPack() {
		ByteBuffer entityArray = ByteBuffer.allocate(11);
		entityArray.put((byte)10);
		entityArray.put((byte)1);
		entityArray.put((byte)0);
		entityArray.putLong(this.level);

		return entityArray.array();
	}

	public ArrayList<byte[]> getEntityPositions(){
		ArrayList<byte[]> sendArray = new ArrayList<>();
		byte [] entityArray;
		entityArray = getEntityPosHelper(this.hero.getEntityValue(), this.hero.getXPos(), this.hero.getYPos(), (byte) this.scorecard.getLives());
		sendArray.add(entityArray);
		if (this.isHost) {
			byte deadValue = 0;
			if (this.prevObj > this.objects.size()){
				deadValue = 3;
				this.prevObj = this.objects.size();
			}
			for (byte i = 0; i < this.objects.size(); i++ ){
				Creature entity = this.objects.get(i);
				if (deadValue == 3){
					deadValue = (byte)(entity.getEntityValue()%2 + 1);
				}
				sendArray.add(getEntityPosHelper(entity.getEntityValue(), entity.getXPos(), entity.getYPos(), (byte)1));
				entity.setEntityValue((byte)1);
			}
			if (deadValue != 0) {
				sendArray.add(getEntityPosHelper(deadValue, 0.0f, 0.0f, (byte)0));

			}
		}
		
		return sendArray;

	}

	public void setEntityPos(byte[] entityPos){
		if (entityPos[1] == 0) {
			this.otherHero.setPosition(ByteBuffer.wrap(entityPos).getFloat(2), ByteBuffer.wrap(entityPos).getFloat(10));
			if (this.scorecard.getLives() > entityPos[18]) {
				this.scorecard.loseLife();
			}
		} else {
			switch (entityPos[1]) {
				case 1:
					if (entityPos[18] == 0) {
						this.objects.get(0).die(); 
					} else {
						this.objects.get(0).setPosition(ByteBuffer.wrap(entityPos).getFloat(2), ByteBuffer.wrap(entityPos).getFloat(10));
					}
					break;
				case 2:
					if (entityPos[18] == 0) {
						this.objects.get(1).die(); 
					} else {
						int index2 = this.objects.size() - 1;
						this.objects.get(index2).setPosition(ByteBuffer.wrap(entityPos).getFloat(2), ByteBuffer.wrap(entityPos).getFloat(10));
					}
					break;
				default:
					for (Creature entity : this.objects) {
						entity.die();
					}
					break;
			}
		}

	}

	public void setHeroColor() {
		int r = Integer.parseInt(this.settingStrings.get(0).substring(0,2),16);
		int g = Integer.parseInt(this.settingStrings.get(0).substring(2,4),16);
		int b = Integer.parseInt(this.settingStrings.get(0).substring(4,6),16);
		Color newColor = new Color(r, g, b);
		this.hero.setColor(newColor);
	}

	public void setOtherColor(byte[] packet) {
		Color newColor = new Color(packet[1], packet[2], packet[3]);
		this.otherHero.setColor(newColor);
		StringBuffer name = new StringBuffer();
		int i = 4;
		char letter = (char) packet[i];
		while (letter != 0) {
			name.append(letter);
			i++;
			letter = (char) packet[i];
		}
		this.clientName = name.toString();
	}

	public void setLevel(byte[] packet) {
		ByteBuffer levelpack = ByteBuffer.wrap(packet);
		this.level = levelpack.getLong(2);
		this.changeLevel(0);
	}

	public int getXMid(){
		return this.xmid;
	}

	public int getYMid(){
		return this.ymid;
	}

	public String getAddress(){
		return this.settingStrings.get(2).toString();
	}

	public String getPort(){
		return this.settingStrings.get(3).toString();
	}

	public ReadWriteLock getLock(){
		return this.levelLock;
	}

	public ArrayList<byte[]> retrieveEntityPacket(){
		return new ArrayList<byte[]>();
	}

	private ArrayList<byte[]> serverEntityPacketHelper(){
		return new ArrayList<byte[]>();
	}

	private ArrayList<byte[]> clientEntityPacketHelper(){
		return new ArrayList<byte[]>();
	}
}
