package com.ironalloygames.core.piece;

import playn.core.Color;

public class Armor extends Piece {
	protected int getColor(){ return Color.rgb(0, 128, 255); }

	@Override
	public float getHPMod() {
		return 10;
	}

	@Override
	public int getHitSparkColor() {
		return Color.rgb(0, 128, 255);
	}
	
	public float getCostMod(){ return 1.7f; }
}
