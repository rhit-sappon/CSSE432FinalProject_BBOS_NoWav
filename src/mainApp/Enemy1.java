package mainApp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.math.*;

public class Enemy1 extends Creature{
	public Enemy1(int posx, int posy, byte value) {
		super(posx,posy,value);
		this.color = Color.PINK;
	}
	@Override
	public void moveTowardsHero(Creature hero, Creature otherHero, boolean hasClient) {
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
		
		if(target.xPosition > this.xPosition) 
			this.setXVelocity(1);
		else if(target.xPosition < this.xPosition)
			this.setXVelocity(-1);
		if(target.yPosition < this.yPosition)
			this.setYVelocity(-1);
		else if(target.yPosition > this.yPosition)
			this.setYVelocity(1);
		
	}
	
	@Override 
	public void collidesWithHero(Hero h, Scorecard scorecard) {
		if(this.rect.intersects(h.getRect())) {
				this.die();
				scorecard.loseLife();
				scorecard.loseLife();
				h.die();
				h.die();

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
