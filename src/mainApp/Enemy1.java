package mainApp;

import java.awt.Color;
import java.awt.Graphics2D;

public class Enemy1 extends Creature{
	public Enemy1(int posx, int posy) {
		super(posx,posy);
		this.color = Color.PINK;
	}
	@Override
	public void moveTowardsHero(Creature hero) {
		if(hero.xPosition > this.xPosition) 
			this.setXVelocity(1);
		if(hero.xPosition < this.xPosition)
			this.setXVelocity(-1);
		if(hero.yPosition < this.yPosition)
			this.setYVelocity(-1);
		if(hero.yPosition > this.yPosition)
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
