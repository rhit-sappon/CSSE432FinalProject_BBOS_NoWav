package mainApp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;


public abstract class Creature {
	protected Rectangle2D.Float rect;
	protected float xPosition;
	protected float yPosition;
	protected float xVelocity = 0;
	protected float yVelocity = 0;
	protected boolean isAlive;
	protected boolean phase;
	protected boolean onGround;
	protected boolean onPlat;
	protected Color color;
	protected byte entityValue;
	public Creature(int posX, int posY, byte value) {
		this.xPosition = posX;
		this.yPosition = posY;
		this.entityValue = value;
		this.isAlive = true;
		this.rect = new Rectangle2D.Float(xPosition, yPosition, 60, 60);
		this.onGround = true;
		this.onPlat = true;
		this.phase = false;
	}
	public void setXVelocity(float velocity) {
		if(this.isAlive) {
		this.xVelocity = velocity;
		}
	}
	
	public void setYVelocity(float velocity) {
		if(this.isAlive) {
		this.yVelocity = velocity;
		}
	}
	public void setPosition(float x, float f) {
		this.xPosition = x;
		this.yPosition = f;
	}
	public void die() {
		this.isAlive = false;
	}
	public void updateX(float step) {
		this.xPosition += xVelocity*step;
	}
	public void updateY(float step) {
		this.yPosition += yVelocity*step;
	}
	
	public void updateRect() {
		this.rect.setRect(xPosition, yPosition, 60, 60);
	}
	
	public float getXPos() {
		return this.xPosition;
	}
	public float getYPos() {
		return this.yPosition;
	} 
	
	public float getXVel() {
		return this.xVelocity;
	}
	public float getYVel() {
		return this.yVelocity;
	}
	public Rectangle2D.Float getRect(){
		return this.rect;
	}
	
	public boolean isDead() {
		return !isAlive;
	}

	public boolean getIsAlive(){
		return isAlive;
	}
	
	public boolean getPhase() {
		return phase;
	}
	public void setPhase(boolean toggle) {
		phase = toggle;
	}
	
	public boolean getOnGround() {
		return onGround;
	}
	public void setOnGround(boolean toggle) {
		onGround = toggle;
	}
	
	public boolean getOnPlat() {
		return onPlat;
	}
	public void setOnPlat(boolean toggle) {
		onPlat = toggle;
	}
	
	public void drawOn(Graphics2D g2d) {
		if(this.isAlive) {
		g2d = (Graphics2D) g2d.create();

		g2d.setColor(color);
		g2d.fill(rect);
		}
	}
	public void moveTowardsHero(Creature hero) {
		// TODO Auto-generated method stub
		
	}
	public byte getEntityValue(){
		return this.entityValue;
	}
	public void setEntityValue(byte value){
		this.entityValue = value;
	}
	
	public void collideWithCreature(Creature c) {
	} 
	
	public void collidesWithHero(Hero h, Scorecard scorecard) {
		
	}
	
	public void onRemove() {
		
	}

	public void setColor(Color newColor) {
		this.color = newColor;
	}
}
