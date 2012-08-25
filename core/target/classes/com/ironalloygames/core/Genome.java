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
	
	public Genome(List<Genome> genomes)
	{
		float avgArms = 0;
		
		for(Genome g : genomes)
			avgArms += g.arms;
		
		avgArms /= genomes.size();
		
		avgArms += PetriDishEmpire.s.rand.nextGaussian();
		
		arms = Math.round(avgArms);
		
		float avgGenes = 0;
		
		for(Genome g : genomes)
			avgGenes += g.genes.size();
		
		avgGenes /= genomes.size();
		
		avgGenes += PetriDishEmpire.s.rand.nextGaussian();
		
		int genes = Math.round(avgGenes);
		
		for(int i=0;i<genes;++i)
		{
			int n = 1;
			Gene g = new Gene((float)PetriDishEmpire.s.rand.nextGaussian() + 2, (float)PetriDishEmpire.s.rand.nextGaussian() + 2, (float)PetriDishEmpire.s.rand.nextGaussian(), (float)PetriDishEmpire.s.rand.nextGaussian());
			
			for(int j=0;j<genomes.size();++j)
			{
				if(genomes.get(j).genes.size() > i && PetriDishEmpire.s.rand.nextInt(n++) == 1)
				{
					g = genomes.get(j).genes.get(i).mutate();
				}
			}
			
			this.genes.add(g);
		}
	}
}
