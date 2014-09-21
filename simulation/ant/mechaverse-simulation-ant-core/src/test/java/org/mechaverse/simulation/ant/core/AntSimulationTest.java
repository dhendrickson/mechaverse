package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.model.Ant;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.common.SimulationDataStore;
import org.mechaverse.simulation.common.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

/**
 * Unit test for {@link AntSimulation}.
 */
public class AntSimulationTest {

  private static final int TEST_ITERATION_COUNT = 2500;

  private static class RandomActiveEntityProvider extends SimpleActiveEntityProvider {

    @Override
    public Optional<ActiveEntity> getActiveEntity(Entity entity) {
      if (entity instanceof Ant) {
        Ant ant = (Ant) entity;
        return Optional.<ActiveEntity>of(new ActiveAnt(ant, new RandomAntBehavior()));
      }
      return super.getActiveEntity(entity);
    }
  }

  private static class EntityTypeCounter {

    int[] entityTypeCounts = new int[EntityUtil.ENTITY_TYPES.length];

    public void addEntity(Entity entity) {
      entityTypeCounts[EntityUtil.getType(entity).ordinal()]++;
    }

    public void removeEntity(Entity entity) {
      entityTypeCounts[EntityUtil.getType(entity).ordinal()]--;
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounts[type.ordinal()];
    }
  }

  private static class EntityTypeCountObserver implements EntityManager.Observer {

    private EntityTypeCounter entityTypeCounter = new EntityTypeCounter();

    @Override
    public void onAddEntity(Entity entity) {
      entityTypeCounter.addEntity(entity);
    }

    @Override
    public void onRemoveEntity(Entity entity) {
      entityTypeCounter.removeEntity(entity);
    }

    public int getEntityCount(EntityType type) {
      return entityTypeCounter.getEntityCount(type);
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(AntSimulationTest.class);

  private RandomGenerator random;

  @Before
  public void setUp() {
    this.random = RandomUtil.newGenerator(AntSimulationTest.class.getName().hashCode());
  }

  @Test
  public void simulate() {
    AntSimulation simulation = new AntSimulation();

    EntityTypeCountObserver entityCountObserver = new EntityTypeCountObserver();
    simulation.addObserver(entityCountObserver);

    verifyEntityTypeCounts(simulation.getState().getModel(), entityCountObserver);

    for (int cnt = 0; cnt < TEST_ITERATION_COUNT; cnt++) {
      simulation.step();

      verifyEntityTypeCounts(simulation.getState().getModel(), entityCountObserver);
    }

    assertTrue(entityCountObserver.getEntityCount(EntityType.FOOD) >= simulation.getState()
        .getConfig().getMinFoodCount());
  }

  @Test
  public void randomGeneratorIsDeterministic() {
    assertEquals(2280233513574350514L, random.nextLong());
  }

  @Test
  public void simulate_verifyDeterministic() throws IOException {
    byte[] initialState = AntSimulation.randomState(
        new AntSimulationEnvironmentGenerator(), random).serialize();
    AntSimulation simulation1 = new AntSimulation();
    AntSimulation simulation2 = new AntSimulation();
    simulation1.setState(AntSimulationState.deserialize(initialState));
    simulation2.setState(AntSimulationState.deserialize(initialState));

    for (int cnt = 0; cnt < 50; cnt++) {
      simulation1.step();
      simulation2.step();

      verifyStatesEqual(simulation1.getState(), simulation2.getState());
    }

    byte[] state = simulation1.getState().serialize();
    simulation1 = new AntSimulation();
    simulation1.setState(AntSimulationState.deserialize(state));
    simulation2 = new AntSimulation();
    simulation2.setState(AntSimulationState.deserialize(state));

    for (int cnt = 0; cnt < 100; cnt++) {
      simulation1.step();
      simulation2.step();

      verifyStatesEqual(simulation1.getState(), simulation2.getState());
    }
  }

  @Test
  public void simulate_random() {
    AntSimulation simulation = new AntSimulation(new RandomActiveEntityProvider(), random);
    for (int cnt = 0; cnt < TEST_ITERATION_COUNT; cnt++) {
      simulation.step();
    }
  }

  @Test
  public void getAndSetEntityState() {
    ActiveEntityProvider provider = new SimpleActiveEntityProvider() {
      @Override
      public Optional<ActiveEntity> getActiveEntity(final Entity entity) {
        if (entity instanceof Ant) {
          final Ant ant = (Ant) entity;
          return Optional.<ActiveEntity>of(new ActiveAnt(ant, new SimpleAntBehavior() {
            int age = 0;
            @Override
            public AntOutput getOutput(RandomGenerator random) {
              age++;
              if(age > 10) {
               entity.setEnergy(0);
              }
              return super.getOutput(random);
            }

            @Override
            public void setState(SimulationDataStore state) {
              age = Integer.parseInt(new String(state.get("age")));
              assertEquals(entity.getId(), new String(state.get("id")));
              assertEquals(((Ant) entity).getAge(), age);
            }

            @Override
            public SimulationDataStore getState() {
              SimulationDataStore dataStore = new SimulationDataStore();
              dataStore.put("id", entity.getId().getBytes());
              dataStore.put("age", String.valueOf(age).getBytes());
              return dataStore;
            }
          }));
        }
        return super.getActiveEntity(entity);
      }
    };

    AntSimulation simulation = new AntSimulation(provider, random);

    for (int cnt = 1; cnt <= 100; cnt++) {
      simulation.step();

      AntSimulationState state = simulation.getState();
      int antCount = 0;
      for (Entity entity : state.getModel().getEnvironment().getEntities()) {
        if (entity instanceof Ant) {
          SimulationDataStore entityDataStore = state.getEntityValues(entity);
          assertEquals(ImmutableSet.of("id", "age"), entityDataStore.keySet());
          assertEquals(entity.getId(), new String(entityDataStore.get("id")));
          assertEquals(((Ant) entity).getAge(),
              Integer.parseInt(new String(entityDataStore.get("age"))));
          antCount++;
        }
      }
      assertEquals("Key set was: " + state.keySet().toString(),
          antCount*2 + 2, state.keySet().size());
    }

    AntSimulationState state = simulation.getState();
    simulation = new AntSimulation(provider, random);
    simulation.setState(state);
    simulation.step();

    state = simulation.getState();
    for (Entity entity : state.getModel().getEnvironment().getEntities()) {
      if (entity instanceof Ant) {
        SimulationDataStore entityDataStore = state.getEntityValues(entity);
        assertEquals(ImmutableSet.of("id", "age"), entityDataStore.keySet());
        assertEquals(entity.getId(), new String(entityDataStore.get("id")));
        assertEquals(((Ant) entity).getAge(),
            Integer.parseInt(new String(entityDataStore.get("age"))));
      }
    }
  }

  @Test
  public void antBehaviorOnRemoveEntity() {
    final Set<Integer> availableIds = new HashSet<Integer>();
    ActiveEntityProvider provider = new SimpleActiveEntityProvider() {
      @Override
      public Optional<ActiveEntity> getActiveEntity(final Entity entity) {
        if (entity instanceof Ant) {
          final Ant ant = (Ant) entity;
          return Optional.<ActiveEntity>of(new ActiveAnt(ant, new SimpleAntBehavior() {
            private Integer id;

            @Override
            public AntOutput getOutput(RandomGenerator random) {
              if(id == null) {
                id = Iterables.getFirst(availableIds, null);
                availableIds.remove(id);
              }
              assertTrue(id != null);
              return super.getOutput(random);
            }

            @Override
            public void onRemoveEntity() {
              availableIds.add(id);
              id = null;
              super.onRemoveEntity();
            }
          }));
        }
        return super.getActiveEntity(entity);
      }
    };

    AntSimulation simulation = new AntSimulation(provider, random);
    int targetAntCount = simulation.getState().getConfig().getTargetAntCount();
    for(int cnt = 1; cnt <= targetAntCount; cnt++) {
      availableIds.add(cnt);
    }

    for (int cnt = 1; cnt <= TEST_ITERATION_COUNT; cnt++) {
      simulation.step();

      int expectedAvailableIdCount = targetAntCount;
      for(Entity entity : simulation.getState().getModel().getEnvironment().getEntities()) {
        if(entity instanceof Ant) {
          expectedAvailableIdCount--;
        }
      }

      logger.debug("{} available ids", availableIds.size());
      assertEquals(expectedAvailableIdCount, availableIds.size());
    }
  }

  private void verifyEntityTypeCounts(SimulationModel model, EntityTypeCountObserver observer) {
    EntityTypeCounter counter = new EntityTypeCounter();

    for (Entity entity : model.getEnvironment().getEntities()) {
      counter.addEntity(entity);
      if (entity instanceof Ant) {
        Entity carriedEntity = ((Ant) entity).getCarriedEntity();
        if (carriedEntity != null) {
          counter.addEntity(carriedEntity);
        }
      }
    }

    for (EntityType entityType : EntityType.values()) {
      int actualCount = counter.getEntityCount(entityType);
      logger.debug("{} count = {}", entityType.name(), actualCount);
      assertEquals(actualCount, observer.getEntityCount(entityType));
    }
  }

  private void verifyStatesEqual(AntSimulationState state1, AntSimulationState state2)
      throws IOException {
    assertEquals(state1.getModel().getSeed(), state2.getModel().getSeed());
    assertEquals(state1.getModel().getEnvironment().getEntities().size(),
        state2.getModel().getEnvironment().getEntities().size());

    assertTrue(state1.keySet().toString(), state1.keySet().contains(AntSimulationState.MODEL_KEY));
    assertTrue(state1.keySet().contains(AntSimulationState.CONFIG_KEY));
    assertEquals(state1.keySet(), state2.keySet());
    for (String key : state1.keySet()) {
      byte[] data1 = state1.getData(key);
      byte[] data2 = state2.getData(key);
      if (key.equalsIgnoreCase(AntSimulationState.MODEL_KEY)) {
        data1 = decompress(data1);
        data2 = decompress(data2);
      }
      assertArrayEquals(data1, data2);
    }
  }

  private byte[] decompress(byte[] data) throws IOException {
    return ByteStreams.toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
  }
}
