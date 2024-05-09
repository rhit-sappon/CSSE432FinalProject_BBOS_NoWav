package mainApp;

import java.awt.Color;

public class Hero extends Creature{
	public Hero(int posx, int posy) {
		super(posx,posy,(byte)0);
		this.color = Color.BLUE;
	}
}
