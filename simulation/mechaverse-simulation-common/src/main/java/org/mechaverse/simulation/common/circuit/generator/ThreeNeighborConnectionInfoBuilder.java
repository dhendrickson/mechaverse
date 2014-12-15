package org.mechaverse.simulation.common.circuit.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ConnectionInfo;
import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.Input;

import com.google.common.collect.ImmutableList;

/**
 * Connects the given matrix of elements so that every element is connected to 3 of its neighbors.
 * Each element will be connected to its left and right neighbors and alternating elements will be
 * connected to the neighbors above and below. External inputs are created for the missing neighbors
 * on the boundary.
 */
public class ThreeNeighborConnectionInfoBuilder extends AbstractConnectionInfoBuilder {

  private final ConnectionInfo connectionInfo;

  public ThreeNeighborConnectionInfoBuilder(Element[][] elements,
      Map<String, ElementType> elementTypeMap) {
    super(elements, elementTypeMap);
    this.connectionInfo = buildConnectionInfo();
  }

  @Override
  public ConnectionInfo build() {
    return connectionInfo;
  }

  protected ConnectionInfo buildConnectionInfo() {
    Map<Element, Input[]> inputMap = new LinkedHashMap<>();
    List<ExternalElement> externalElements = new ArrayList<>();

    int rowCount = elements.length;
    for (int row = 0; row < rowCount; row++) {
      int colCount = elements[row].length;
      for (int col = 0; col < colCount; col++) {
        Element element = elements[row][col];
        Input[] inputs = new Input[3];

        // Connect to left element.
        if (col > 0) {
          inputs[0] = createElementInput(elements[row][col - 1], 2);
        } else {
          inputs[0] = createExternalInput(0, -1, elements[row][colCount - 1], 2, externalElements);
        }
        // Alternately connect to the row above and below.
        if(col % 2 == row % 2) {
          // Connect to the row above.
          if (row > 0) {
            inputs[1] = createElementInput(elements[row - 1][col], 1);
          } else {
            inputs[1] = createExternalInput(-1, 0, elements[rowCount-1][col], 1, externalElements);
          }
        } else {
          // Connect to the row below.
          if (row < elements.length - 1) {
            inputs[1] = createElementInput(elements[row + 1][col], 1);
          } else {
            inputs[1] = createExternalInput(1, 0, elements[0][col], 1, externalElements);
          }
        }

        // Connect to the right element.
        if (col < elements[row].length - 1) {
          inputs[2] = createElementInput(elements[row][col + 1], 0);
        } else {
          inputs[2] = createExternalInput(0, 1, elements[row][0], 0, externalElements);
        }
        inputMap.put(element, inputs);
      }
    }

    return new ConnectionInfo(inputMap, ImmutableList.copyOf(externalElements));
  }
}
