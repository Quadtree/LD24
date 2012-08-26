package com.ironalloygames.core;

import java.util.HashMap;

import playn.core.PlayN;
import playn.core.Sound;

public class AudioSystem {
	public static void play(String name)
	{
		if(!sounds.containsKey(name))
			sounds.put(name, PlayN.assets().getSound("sound/" + name));
		
		sounds.get(name).play();
	}
	
	public static void preload(String name)
	{
		if(!sounds.containsKey(name))
			sounds.put(name, PlayN.assets().getSound("sound/" + name));
	}
	
	static HashMap<String, Sound> sounds = new HashMap<String, Sound>();
}
