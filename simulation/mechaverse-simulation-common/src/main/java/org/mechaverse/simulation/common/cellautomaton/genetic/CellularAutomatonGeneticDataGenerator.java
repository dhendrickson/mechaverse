package org.mechaverse.simulation.common.cellautomaton.genetic;

import java.math.RoundingMode;

import org.apache.commons.math3.random.RandomGenerator;
import org.mechaverse.simulation.common.cellautomaton.genetic.CellularAutomatonGeneticData.CellGeneticData;
import org.mechaverse.simulation.common.cellautomaton.simulation.generator.CellularAutomatonSimulationModel;
import org.mechaverse.simulation.common.genetic.GeneticData;
import org.mechaverse.simulation.common.genetic.GeneticDataStore;
import org.mechaverse.simulation.common.util.ArrayUtil;

import com.google.common.math.IntMath;

/**
 * Generates cellular automaton genetic data.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonGeneticDataGenerator {

  public static final String CELLULAR_AUTOMATON_STATE_KEY = "cellularAutomatonState";
  public static final String OUTPUT_MAP_KEY = "outputMap";

  public void generateGeneticData(GeneticDataStore dataStore,
      CellularAutomatonSimulationModel model, int outputSize, RandomGenerator random) {
    dataStore.put(CELLULAR_AUTOMATON_STATE_KEY, generateStateGeneticData(model, random));
    dataStore.put(OUTPUT_MAP_KEY, generateOutputMapGeneticData(model, outputSize, random));
  }

  public CellularAutomatonGeneticData generateStateGeneticData(
      CellularAutomatonSimulationModel model, RandomGenerator random) {
    int rowCount = model.getHeight() * model.getLogicalUnitInfo().getHeight();
    int colCount = model.getWidth() * model.getLogicalUnitInfo().getWidth();

    CellGeneticData[][] cellData = new CellGeneticData[rowCount][colCount];
    int[][] cellGroups = new int[rowCount][colCount];

    int groupWidth =
        IntMath.sqrt(IntMath.sqrt(rowCount * colCount, RoundingMode.HALF_DOWN), RoundingMode.DOWN);
    int groupHeight = groupWidth;

    for (int row = 0; row < rowCount; row++) {
      for (int col = 0; col < colCount; col++) {
        int[] values = new int[model.getCell(row, col).getStateSize()];
        for (int idx = 0; idx < values.length; idx++) {
          values[idx] = random.nextInt();
        }
        cellData[row][col] = new CellGeneticData(values);

        int groupRow = row / groupHeight;
        int groupCol = col / groupWidth;
        cellGroups[row][col] = groupRow * (colCount / groupWidth) + groupCol;
      }
    }

    return new CellularAutomatonGeneticData(cellData, cellGroups);
  }

  public GeneticData generateOutputMapGeneticData(CellularAutomatonSimulationModel model,
      int outputSize, RandomGenerator random) {
    return generateOutputMapGeneticData(
        outputSize, model.getCellOutputStateSize(), random);
  }

  public GeneticData generateOutputMapGeneticData(int outputSize, int stateSize,
      RandomGenerator random) {
    GeneticData.Builder geneticDataBuilder = GeneticData.newBuilder();
    for (int idx = 0; idx < outputSize; idx++) {
      geneticDataBuilder.writeInt(random.nextInt(stateSize), idx);
      geneticDataBuilder.markSplitPoint();
    }
    return geneticDataBuilder.build();
  }

  public int[] getCellularAutomatonState(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(CELLULAR_AUTOMATON_STATE_KEY).getData());
  }

  public int[] getOutputMap(GeneticDataStore dataStore) {
    return ArrayUtil.toIntArray(dataStore.get(OUTPUT_MAP_KEY).getData());
  }
}
