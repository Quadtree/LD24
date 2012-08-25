package com.ironalloygames.core.piece;

import playn.core.Color;

public class WeakPoint extends Piece {
	protected int getColor(){ return Color.rgb(160, 160, 160); }

	@Override
	public float getHPMod() {
		return 0.1f;
	}
	
	public float getCostMod(){ return 0.15f; }
}
