package mainApp;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Scorecard {
	private int currentScore;
	private int finalscore;
	private int lives;
	private boolean gameover = false;
	private float fps;
	private int xdim;
	private int ydim;
	private Color endColor = new Color(0.4f,0.4f,0.4f,0.5f);
	BufferedImage image;
	
	public Scorecard(int XDIM, int YDIM) {
		this.currentScore = 0;
		this.lives = 3;
		this.finalscore = 0;
		this.fps = 0;
		this.gameover = false;
		this.xdim = XDIM;
		this.ydim = YDIM;
		try {
			this.image = ImageIO.read(new File("gameoverlol.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setLife(int lives) {
		this.lives = lives;
		if(this.lives > 0)
			this.gameover = false;
	}
	
	public int getLives() {
		return this.lives;
	}
	public void loseLife() {
		this.lives--;
		if(this.lives == 0) {
			gameover = true;
			SoundiBoi.stopTrack();
			try {
				SoundiBoi.audioPlayer(1);
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
	}
	public void addScore(int score) {
		this.currentScore += score;
		if(gameover == false)
			this.finalscore += score;
	}

	public void framerate(float timestep) {
		this.fps = 100/timestep;
	}

	public int getScore() {
		return this.currentScore;
	}
	
	public void drawOn(Graphics2D g) {
		Graphics2D g2d = (Graphics2D) g.create();
		if(!gameover) {
			g2d.setColor(Color.BLACK);
			g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 25)); 
			g2d.drawString("Score: " + this.currentScore, 0, 25);
			g2d.drawString("Lives: " + this.lives, 0, 45);
			g2d.drawString("Framerate: " + this.fps, 0, 65);
			
		}
		else {

		g2d.setColor(this.endColor);
		g2d.fillRect(0, 0, this.xdim * 60, this.ydim*60);
		g2d.setColor(Color.black);
		g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 25)); 
		g2d.drawString("GAME OVER!", this.xdim*30-60, this.ydim * 15);
		g2d.drawString("Score: " + finalscore, this.xdim*30-50, this.ydim*40);
		BufferedImage imageToDraw = this.image;
		g2d.drawImage(imageToDraw, this.xdim*30 - 100, this.ydim*20, 200, 200, null);
		}
	}
	

}

