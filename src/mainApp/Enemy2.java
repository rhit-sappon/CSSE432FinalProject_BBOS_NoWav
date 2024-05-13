package mainApp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class Enemy2 extends Creature{
	private boolean near = false;
	private boolean fear;
	private int xdim;
	private int ydim;
	public Enemy2(int posx, int posy, byte value, int XDIM, int YDIM) {
		super(posx, posy, value);
		this.color = Color.RED;
		this.xdim = XDIM;
		this.ydim = YDIM;
		Random rand = new Random();
		fear = rand.nextBoolean();
		if(fear == false) {
			this.setXVelocity(5);
		}
		// TODO Auto-generated constructor stub
	}
	
	public void moveTowardsHero(Creature hero, Creature otherHero, boolean hasClient) {
		if(fear == true) {
			Creature target = hero;
			if(hasClient){
				float xDist = Math.abs(target.xPosition - this.xPosition);
				float yDist = Math.abs(target.yPosition - this.yPosition);
				float distance = xDist + yDist;
				float xDist2 = Math.abs(otherHero.xPosition - this.xPosition);
				float yDist2 = Math.abs(otherHero.yPosition - this.yPosition);
				float distance2 = xDist2 + yDist2;
				if(distance2 < distance) {
					target = otherHero;
				}
			}
			Rectangle2D.Double heroBox = new Rectangle2D.Double(target.getXPos() + target.getXVel(), target.getYPos(), 60, 60);
			if(Math.sqrt(Math.pow(heroBox.getCenterX() - (this.xPosition + 30),2) + Math.pow(heroBox.getCenterY() - (this.yPosition + 30),2)) > 80 && !near) {
				if(target.xPosition > this.xPosition) 
					this.setXVelocity(2);
				if(target.xPosition < this.xPosition)
					this.setXVelocity(-2);
				if(target.yPosition < this.yPosition)
					this.setYVelocity(-2);
				if(target.yPosition > this.yPosition)
					this.setYVelocity(2);
			} else {
				this.near = true;
				if(target.xPosition > this.xPosition) 
					this.setXVelocity(-5);
				if(target.xPosition < this.xPosition)
					this.setXVelocity(5);
				if(target.yPosition < this.yPosition)
					this.setYVelocity(5);
				if(target.yPosition > this.yPosition)
					this.setYVelocity(-5);
				if(Math.sqrt(Math.pow(heroBox.getCenterX() - (this.xPosition + 30),2) + Math.pow(heroBox.getCenterY() - (this.yPosition + 30),2)) > 450)
					this.near = false;
				
			}
		}
		else if(fear == false) {
			if(this.xPosition <= 60)
				this.setXVelocity(5);
			if(this.xPosition >= 60*(this.xdim-2))
				this.setXVelocity(-5);
			
		}
	}
	@Override
	public void collidesWithHero(Hero h, Scorecard scorecard) {
		if(this.rect.intersects(h.getRect())) {
			if(this.yPosition >= h.yPosition+50) 
				this.die();
			else{
				scorecard.loseLife();
				h.die();
			}
		}
		
	}
	@Override
	public void drawOn(Graphics2D g2d) {
		if(this.isAlive) {
			g2d = (Graphics2D) g2d.create();

			g2d.setColor(color);
			g2d.fill(rect);
			}else {
				g2d = (Graphics2D) g2d.create();

				g2d.setColor(color);
				g2d.fill(rect);
				setPosition(999,999);
			
	}

}
}