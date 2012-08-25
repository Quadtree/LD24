package com.ironalloygames.core;

import org.jbox2d.common.Vec2;

import playn.core.Surface;

public class Camera {
	public Surface surf;
	
	public void drawLine(Vec2 start, Vec2 end, int color)
	{
		surf.setFillColor(color);
		surf.drawLine(start.x, start.y, end.x, end.y, 1);
	}
}
