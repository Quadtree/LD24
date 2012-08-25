package com.ironalloygames.core.piece;

import playn.core.Color;

public class Weapon extends Piece {
	private static final int CHARGE_TIME = 60;
	private static final float DAMAGE_MOD = 0.5f;

	protected int getColor(){ return Color.rgb(255, 0, 0); }
	
	float damage = 0;
	
	public void update()
	{
		damage = Math.min((damage + getLength() / CHARGE_TIME) * DAMAGE_MOD, getLength() * DAMAGE_MOD);
		
		for(Piece p : contactList)
		{
			if(owner.playerOwned == false || p.owner.playerOwned == false)
			{
				p.takeDamage(damage);
				damage = 0;
				break;
			}
		}
	}
}
