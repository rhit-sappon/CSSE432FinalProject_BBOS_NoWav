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
	BufferedImage image;
	
	public Scorecard() {
		this.currentScore = 0;
		this.lives = 3;
		this.finalscore = 0;
		this.gameover = false;
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
			
		}
		else {

		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, 619, 939);
		g2d.setColor(Color.black);
		g2d.setFont(new Font("Comic Sans MS", Font.PLAIN, 25)); 
		g2d.drawString("GAME OVER!", 250, 200);
		g2d.drawString("Score: " + finalscore, 250, 240);
		BufferedImage imageToDraw = this.image;
		g2d.drawImage(imageToDraw, 175, 400, 200, 200, null);
		}
	}
	

}

