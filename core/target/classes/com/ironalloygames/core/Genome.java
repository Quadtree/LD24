package com.ironalloygames.core;

import java.util.ArrayList;
import java.util.List;

public class Genome {
	public List<Gene> genes = new ArrayList<Gene>();
	public int arms;
	
	public Genome(List<Gene> genes, int arms) {
		super();
		this.genes = genes;
		this.arms = arms;
	}
}
