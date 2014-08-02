package org.mechaverse.simulation.ant.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Nest;
import org.mechaverse.simulation.common.genetic.CrossoverGeneticData;
import org.mechaverse.simulation.common.genetic.CutAndSplitCrossoverGeneticRecombinator;

import com.google.common.collect.Sets;

/**
 * An environment simulation module that maintains a target ant population size.
 */
public class AntReproductionModule implements EnvironmentSimulationModule {

  /**
   * Calculates the fitness of a set of ants.
   */
  public static interface AntFitnessCalculator {

    EnumeratedDistribution<Ant> getAntFitnessDistribution(Set<Ant> ants, RandomGenerator random);
  }

  /**
   * An ant fitness calculator that calculates ant fitness based on age and energy. Age is given a
   * weight of 70% and energy is weighted 30%. The following formula is used:
   * <p>
   * <code>.7 * age/ageSum + .3 * energy/energySum</code>
   */
  public static class SimpleAntFitnessCalculator implements AntFitnessCalculator {

    @Override
    public EnumeratedDistribution<Ant> getAntFitnessDistribution(
        Set<Ant> ants, RandomGenerator random) {
      double ageSum = 0;
      double energySum = 0;
      for (Ant ant : ants) {
        ageSum += ant.getAge();
        energySum += ant.getEnergy();
      }

      List<Pair<Ant, Double>> pmf = new ArrayList<>();
      for (Ant ant : ants) {
        double fitness = .7 * ant.getAge() / ageSum + .3 * ant.getEnergy() / energySum;
        pmf.add(new Pair<Ant, Double>(ant, fitness));
      }

      return new EnumeratedDistribution<Ant>(random, pmf);
    }
  }

  // TODO(thorntonv): Support multiple nests and nests of different types.

  // TODO(thorntonv): Make these values configurable.
  private final int targetAntCount = 500;
  private final int initialAntEnergy = 1000;

  private final Set<Ant> ants = Sets.newIdentityHashSet();
  private Nest nest;
  private final CutAndSplitCrossoverGeneticRecombinator geneticRecombinator =
      new CutAndSplitCrossoverGeneticRecombinator();
  private final AntFitnessCalculator fitnessCalculator = new SimpleAntFitnessCalculator();

  @Override
  public void update(CellEnvironment env, EntityManager entityManager, RandomGenerator random) {
    if (ants.size() < targetAntCount && nest != null) {
      Cell cell = env.getCell(nest);
      if (cell.getEntity(EntityType.ANT) == null) {
        Ant ant = generateRandomAnt(random);
        cell.setEntity(ant, EntityType.ANT);
        entityManager.addEntity(ant);
      }
    }
  }

  public Ant generateRandomAnt(RandomGenerator random) {
    Ant ant = new Ant();
    ant.setDirection(AntSimulationUtil.randomDirection(random));
    ant.setMaxEnergy(initialAntEnergy);
    ant.setEnergy(ant.getMaxEnergy());
    return ant;
  }

  public Ant generateAnt(RandomGenerator random) {
    Ant ant = new Ant();
    ant.setDirection(AntSimulationUtil.randomDirection(random));
    ant.setMaxEnergy(initialAntEnergy);
    ant.setEnergy(ant.getMaxEnergy());

    EnumeratedDistribution<Ant> antFitnessDistribution =
        fitnessCalculator.getAntFitnessDistribution(ants, random);

    Ant parent1 = antFitnessDistribution.sample();
    Ant parent2 = antFitnessDistribution.sample();

    while (ants.size() > 1 && parent2 == parent1) {
      parent2 = antFitnessDistribution.sample();
    }

    CrossoverGeneticData parent1GeneticData = new CrossoverGeneticData(parent1.getData(), null);
    CrossoverGeneticData parent2GeneticData = new CrossoverGeneticData(parent2.getData(), null);

    ant.setBitMutationProbability(
        (parent1.getBitMutationProbability() + parent2.getBitMutationProbability()) / 2);
    CrossoverGeneticData antGeneticData =
        geneticRecombinator.recombine(parent1GeneticData, parent2GeneticData, random);

    ant.setData(antGeneticData.getData());
    return ant;
  }

  @Override
  public void onAddEntity(Entity entity) {
    if (entity instanceof Ant) {
      ants.add((Ant) entity);
    } else if (entity instanceof Nest) {
      nest = (Nest) entity;
    }
  }

  @Override
  public void onRemoveEntity(Entity entity) {
    if (entity instanceof Ant) {
      ants.remove(entity);
    } else if (entity instanceof Nest) {
      if (entity == nest) {
        nest = null;
      }
    }
  }
}