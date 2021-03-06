package org.mechaverse.simulation.ant.core.module;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Food;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.ant.core.AntSimulationEnvironmentGenerator;
import org.mechaverse.simulation.ant.core.CellEnvironment;
import org.mechaverse.simulation.ant.core.EntityManager;
import org.mechaverse.simulation.common.cellautomaton.AbstractProbabilisticEnvironmentGenerator.EntityDistribution;
import org.mechaverse.simulation.common.cellautomaton.AbstractProbabilisticEnvironmentGenerator.ProbabilisticLocalGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

/**
 * Generates clusters of food to maintain a minimum quantity of food.
 */
public class FoodGenerationModule implements AntSimulationModule {

  /**
   * {@link ProbabilisticLocalGenerator} that generates a cluster of food.
   */
  public static class FoodLocalGenerator extends ProbabilisticLocalGenerator<EntityType> {

    private FoodLocalGenerator(double probability,
        Table<Integer, Integer, EntityDistribution<EntityType>> entityDistributions) {
      super(probability, entityDistributions);
    }

    public static FoodLocalGenerator newInstance(
        double probability, int radius, RandomGenerator random) {
      return new FoodLocalGenerator(probability, createDistributionMatrix(radius, random));
    }

    private static ImmutableTable<Integer, Integer, EntityDistribution<EntityType>>
        createDistributionMatrix(int radius, RandomGenerator random) {
      ImmutableTable.Builder<Integer, Integer, EntityDistribution<EntityType>> builder =
          ImmutableTable.builder();

      for (int row = 0; row < radius * 2; row++) {
        for (int col = 0; col < radius * 2; col++) {
          double x = col - radius;
          double y = row - radius;
          double p = (radius - Math.sqrt(x * x + y * y)) / radius;
          builder.put(row, col, EntityDistribution.of(EntityType.FOOD, p > 0 ? p : 0, random));
        }
      }
      return builder.build();
    }
  }

  private static Logger logger = LoggerFactory.getLogger(FoodGenerationModule.class);

  @Value("#{properties['foodMinCount']}") private int minFoodCount;
  @Value("#{properties['foodClusterRadius']}") private int foodClusterRadius;
  @Value("#{properties['foodInitialEnergy']}") private int foodInitialEnergy;

  private int foodCount = 0;

  @Override
  public void setState(AntSimulationState state, CellEnvironment env, EntityManager entityManager) {
  }

  @Override
  public void updateState(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager) {}

  @Override
  public void beforeUpdate(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {
    if (foodCount < minFoodCount) {
      int row = random.nextInt(env.getRowCount());
      int col = random.nextInt(env.getColumnCount());

      logger.debug("Generating food at ({}, {})", row, col);

      Function<EntityType, Entity> entityFactory = new Function<EntityType, Entity>() {
        @Override
        public Entity apply(EntityType entityType) {
          Entity entity = EntityUtil.newEntity(entityType);
          if (entityType == EntityType.FOOD) {
            entity.setEnergy(foodInitialEnergy);
            entity.setMaxEnergy(foodInitialEnergy);
          }
          return entity;
        }
      };
      new AntSimulationEnvironmentGenerator(entityFactory, entityManager, random).apply(
          FoodLocalGenerator.newInstance(1, foodClusterRadius, random), env, row, col, random);
    }
  }

  @Override
  public void beforePerformAction(AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void afterUpdate(final AntSimulationState state, CellEnvironment env,
      EntityManager entityManager, RandomGenerator random) {}

  @Override
  public void onAddEntity(Entity entity, AntSimulationState state) {
    if (entity instanceof Food) {
      foodCount++;
    }
  }

  @Override
  public void onRemoveEntity(Entity entity, AntSimulationState state) {
    if (entity instanceof Food) {
      foodCount--;
    }
  }
}
