package org.mechaverse.simulation.ant.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.ant.api.AntSimulationState;
import org.mechaverse.simulation.ant.api.SimulationModelUtil;
import org.mechaverse.simulation.ant.api.model.Direction;
import org.mechaverse.simulation.ant.api.model.Entity;
import org.mechaverse.simulation.ant.api.model.EntityType;
import org.mechaverse.simulation.ant.api.model.Environment;
import org.mechaverse.simulation.ant.api.model.SimulationModel;
import org.mechaverse.simulation.ant.api.util.EntityUtil;
import org.mechaverse.simulation.ant.core.entity.ant.AntOutput;

import com.google.common.io.ByteStreams;

/**
 * Common ant simulation test utility methods.
 */
public class AntSimulationTestUtil {

  public static Entity newEntity(EntityType entityType, String id, int x, int y,
      Direction direction, int energy, int maxEnergy) {
    Entity entity = EntityUtil.newEntity(entityType);
    entity.setId(id);
    entity.setX(x);
    entity.setY(y);
    entity.setDirection(direction);
    entity.setEnergy(energy);
    entity.setMaxEnergy(maxEnergy);
    return entity;
  }

  public static void assertStatesEqual(AntSimulationState state1, AntSimulationState state2)
      throws IOException {
    assertEquals(state1.getModel().getSeed(), state2.getModel().getSeed());
    assertEquals(state1.getModel().getEnvironment().getEntities().size(),
        state2.getModel().getEnvironment().getEntities().size());

    assertTrue(state1.keySet().toString(), state1.keySet().contains(AntSimulationState.MODEL_KEY));
    assertEquals(state1.keySet(), state2.keySet());
    for (String key : state1.keySet()) {
      byte[] data1 = state1.get(key);
      byte[] data2 = state2.get(key);
      if (key.equalsIgnoreCase(AntSimulationState.MODEL_KEY)) {
        data1 = decompress(data1);
        data2 = decompress(data2);
      }

      assertArrayEquals("Data for key " + key + " does not match.", data1, data2);
    }
  }

  public static void assertModelsEqual(SimulationModel expected, SimulationModel actual)
      throws IOException {
    assertEquals(new CellEnvironment(expected.getEnvironment()).toString(),
      new CellEnvironment(actual.getEnvironment()).toString());

    // Sort the entities so that order will not cause the comparison to fail.
    for (Environment env : SimulationModelUtil.getEnvironments(expected)) {
      Collections.sort(env.getEntities(), EntityUtil.ENTITY_ORDERING);
    }
    for (Environment env : SimulationModelUtil.getEnvironments(actual)) {
      Collections.sort(env.getEntities(), EntityUtil.ENTITY_ORDERING);
    }

    ByteArrayOutputStream model1ByteOut = new ByteArrayOutputStream(16 * 1024);
    ByteArrayOutputStream model2ByteOut = new ByteArrayOutputStream(16 * 1024);

    SimulationModelUtil.serialize(expected, model1ByteOut);
    SimulationModelUtil.serialize(actual, model2ByteOut);

    assertEquals(model1ByteOut.toString(), model2ByteOut.toString());
  }

  public static void assertEntitiesEqual(Entity expected, Entity actual) {
    assertEquals(expected.getClass(), actual.getClass());
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getX(), actual.getX());
    assertEquals(expected.getY(), actual.getY());
    assertEquals(expected.getEnergy(), actual.getEnergy());
    assertEquals(expected.getMaxEnergy(), actual.getMaxEnergy());
  }

  public static AntOutput randomAntOutput(RandomGenerator random) {
    int[] antOutputData = new int[AntOutput.DATA_SIZE];
    for (int idx = 0; idx < antOutputData.length; idx++) {
      antOutputData[idx] = random.nextInt(Short.MAX_VALUE);
    }
    return new AntOutput(antOutputData);
  }

  public static byte[] decompress(byte[] data) throws IOException {
    return ByteStreams.toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
  }
}
