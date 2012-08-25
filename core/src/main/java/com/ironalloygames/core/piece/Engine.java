package com.ironalloygames.core.piece;

import playn.core.Color;

public class Engine extends Piece {
	protected int getColor(){ return Color.rgb(255, 255, 0); }

	@Override
	public float getEnginePower() {
		return 0.004f * getMass();
	}
}
