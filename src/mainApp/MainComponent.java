package mainApp;

import java.awt.Color;
import java.util.concurrent.locks.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.geom.Rectangle2D.Float;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;

public class MainComponent extends JComponent {
	
	private Hero hero;
	ArrayList<Integer[]> levelData = new ArrayList<>();
	ArrayList<Rectangle2D.Double> levelWalls;
	Rectangle2D.Double levelBackdrop = new Rectangle2D.Double(0,0,600,900);
	ArrayList<Rectangle2D.Double> levelPlatforms;
	ArrayList<Rectangle2D.Double> levelBombs;
	ArrayList<Creature> objects = new ArrayList<>();
	ArrayList<Creature> toDie = new ArrayList<>();
	
	private long level = 0;
	private int bombs = 0;
	protected static final float GRAVITY = (float) 1.0;
	private Scorecard scorecard;
	private float step = 1;
	private int jumptime = 0;
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private ReadWriteLock levelLock = new ReentrantReadWriteLock();
	private boolean nextlevel = false;
	
	public MainComponent(ArrayList<Integer[]> levelData) {
		super();
		this.hero = new Hero(10, 10);
		this.scorecard = new Scorecard();
		this.levelData=levelData;
		makeMumsic();
	} 
	
	protected void physics(float step) {
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
				if(this.handleCeilingCollision(enemy))
					enemy.updateY(this.step);
				if(this.handleWallCollisionX(enemy))
					enemy.updateX(this.step);
				enemy.updateRect();
			
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
			changeLevel(true);
		}
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		this.levelLock.readLock().lock();
		drawLevel(g2d);
		
		this.hero.drawOn(g2d);
		if(objects.size() > 0)
			for(Creature enemy : objects) {
				enemy.drawOn(g2d);
			}

		this.scorecard.drawOn(g2d);
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

	public void changeLevel(boolean dir) {
		this.levelLock.writeLock().lock();
		this.scorecard.addScore(100);
		this.scorecard.setLife(3);
		if(dir) {
			this.level += 1;
		}else {
			this.level -= 1;
		}
		updateLevelData(scanforLevel("levelGen.csv"));
		LevelGenerator.generateLevel(this.level, false);
		interpolateLevel();
		this.levelLock.writeLock().unlock();
		this.nextlevel = true;
		
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
			creature.setPosition(598, creature.getYPos());
		} else if(creature.getXPos() > 598) {
			creature.setPosition(-58, creature.getYPos());
		}if(creature.getYPos() < -58) {
			creature.setPosition(creature.getXPos(),898);
			creature.setYVelocity(-20);
		} else if(creature.getYPos() > 898) {
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
		int spacingx = 0;
		int spacingy = 0;
		for(int r = 0; r<this.levelData.size();r++) {
			Integer[] row = this.levelData.get(r);
			for(int c = 0; c<row.length;c++) {
				if(row[c]==1) {
					levelWalls.add(new Rectangle2D.Double(spacingx,spacingy,60,60));
				}
				else if(row[c]==2) {
					this.hero.setPosition(spacingx,spacingy);
				}
				else if(row[c]==3) {
					levelPlatforms.add(new Rectangle2D.Double(spacingx,spacingy,60,20));
				}else if(row[c]==4) {
					this.bombs += 1;
					levelBombs.add(new Rectangle2D.Double(spacingx+5,spacingy+5,50,50));
				}else if(row[c]==5) {
					Enemy1 enemy = new Enemy1(spacingx,spacingy);
					objects.add(enemy);
				}else if(row[c]==6) {
					Enemy2 enemy = new Enemy2(spacingx,spacingy);
					objects.add(enemy);
				}
				spacingx+=60;
			}
			spacingx=0;
			spacingy+=60;
		}
	}
	
	public void timer() {
	}
		
	
	
	private void drawLevelWalls(Graphics2D g2d) {
		for(Rectangle2D.Double rect: levelWalls) {
			g2d.setColor(Color.GREEN);
			g2d.fill(rect);
		}
	}
	private void drawLevelBackdrop(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
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
	
	
	private void drawLevel(Graphics2D g2d) {
		drawLevelBackdrop(g2d);
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
			Integer[] rowNum = new Integer[10];
			for(int i = 0; i < sLevelData.length; i++) {
				rowNum[i] = Integer.parseInt(sLevelData[i]);
			}
			levelData.add(rowNum);
		}
		scanner.close();
		return levelData;
	}
}
