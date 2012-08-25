package com.ironalloygames.core;

import java.util.ArrayList;
import java.util.List;

public class Genome {
	public List<Gene> genes = new ArrayList<Gene>();
	public int arms;
	
	public Genome()
	{
		//while(PetriDishEmpire.s.rand.nextInt(5) != 0) genes.add(new Gene());
		
		int genes = (int)(PetriDishEmpire.s.rand.nextGaussian() * 3 + 5);
		
		genes = Math.max(genes, 1);
		
		for(int i=0;i<genes;++i) this.genes.add(new Gene());
	
		arms = Math.max((int)Math.round(PetriDishEmpire.s.rand.nextGaussian()*3 + 3), 2);
	}
	
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
		
		avgArms += PetriDishEmpire.s.rand.nextGaussian() * 0.15f;
		
		arms = Math.round(avgArms);
		
		float avgGenes = 0;
		
		for(Genome g : genomes)
			avgGenes += g.genes.size();
		
		avgGenes /= genomes.size();
		
		avgGenes += PetriDishEmpire.s.rand.nextGaussian() * 0.3f;
		
		int genes = Math.round(avgGenes);
		
		for(int i=0;i<genes;++i)
		{
			int n = 1;
			Gene g = new Gene();
			
			for(int j=0;j<genomes.size();++j)
			{
				if(genomes.get(j).genes.size() > i && PetriDishEmpire.s.rand.nextInt(n++) == 0)
				{
					g = genomes.get(j).genes.get(i).mutate();
				}
			}
			
			this.genes.add(g);
		}
	}
}
