package org.mechaverse.tools.circuit.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.circuit.model.Element;
import org.mechaverse.circuit.model.ElementType;
import org.mechaverse.circuit.model.LogicalUnit;
import org.mechaverse.circuit.model.Output;
import org.mechaverse.circuit.model.Param;
import org.mechaverse.circuit.model.Row;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.Input;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;
import org.mechaverse.tools.circuit.generator.ExternalElement.ExternalElementType;

/**
 * Builds a {@link CircuitSimulationModel} for a {@link Circuit}.
 *
 * @author thorntonv@mechaverse.org
 */
public class CircuitSimulationModelBuilder {

  public CircuitSimulationModel buildModel(Circuit circuit) {
    Map<String, ElementType> elementTypeMap = buildElementTypeMap(circuit);
    LogicalUnitInfo logicalUnitInfo =
        buildLogicalUnitInfo(circuit.getLogicalUnit(), elementTypeMap);
    return new CircuitSimulationModel(circuit, elementTypeMap, logicalUnitInfo);
  }

  private Map<String, ElementType> buildElementTypeMap(Circuit circuit) {
    Map<String, ElementType> map = new HashMap<>();
    map.put(ExternalElement.TYPE, ExternalElementType.INSTANCE);
    for (ElementType elementType : circuit.getElementTypes()) {
      map.put(elementType.getId(), elementType);
    }
    return map;
  }

  private LogicalUnitInfo buildLogicalUnitInfo(LogicalUnit unit,
      Map<String, ElementType> elementTypeMap) {
    // Build element matrix.
    Element[][] matrix = new Element[unit.getRows().size()][];
    int id = 1;
    for (int rowIdx = 0; rowIdx < matrix.length; rowIdx++) {
      Row row = unit.getRows().get(rowIdx);
      matrix[rowIdx] = new Element[row.getElements().size()];
      for (int colIdx = 0; colIdx < matrix[rowIdx].length; colIdx++) {
        Element element = row.getElements().get(colIdx);
        if (element.getId() == null) {
          // If the element does not have an id assign one.
          element.setId(String.valueOf(id));
          id++;
        }
        matrix[rowIdx][colIdx] = element;
      }
    }

    // Connect elements.
    // TODO(thorntonv): Make connection configurable.
    ElementInputMapBuilder inputMapBuilder =
        new ThreeNeighborElementInputMapBuilder(matrix, elementTypeMap);
    Map<Element, Input[]> inputMap = inputMapBuilder.build();

    List<ElementInfo> elements = new ArrayList<>();
    List<ElementInfo> externalElements = new ArrayList<>();
    for(Entry<Element, Input[]> entry : inputMap.entrySet()) {
      Element element = entry.getKey();
      ElementType elementType = elementTypeMap.get(element.getType());
      Input[] inputs = entry.getValue();
      if (!element.getType().equals(ExternalElement.TYPE)) {
        elements.add(buildElementInfo(element, elementType, inputs));
      } else {
        externalElements.add(buildElementInfo(element, elementType, inputs));
      }
    }
    return new LogicalUnitInfo(elements, externalElements);
  }

  protected ElementInfo buildElementInfo(Element element, ElementType elementType, Input[] inputs) {
    Map<String, String> outputVarNameMap = new HashMap<>();
    Map<String, String> paramVarNameMap = new HashMap<>();
    Map<String, Map<String, String>> outputParamIdMap = new HashMap<>();

    // Build a map that maps a param id (used to refer to the parameter in output expressions) to
    // its variable name.
    for (Param param : elementType.getParams()) {
      String varName = getElementParamVarName(element, param);
      if(!paramVarNameMap.containsKey(param.getId())) {
        paramVarNameMap.put(param.getId(), varName);
      } else {
        throw new IllegalStateException("Parameter " + param.getId()
            + " is defined more than once for element " + element.getId());
      }
    }
    for (Output output : elementType.getOutputs()) {
      outputVarNameMap.put(output.getId(), getElementOutputVarName(element, output));
      Map<String, String> paramIdMap = new HashMap<>();
      for (Param param : output.getParams()) {
        String id = getElementOutputParamId(output, param.getId());
        String varName = getElementOutputParamVarName(element, output, param);
        if(!paramVarNameMap.containsKey(id)) {
          paramIdMap.put(param.getId(), id);
          paramVarNameMap.put(id, varName);
        } else {
          throw new IllegalStateException("Parameter " + id
              + " is defined more than once for element " + element.getId());
        }
      }
      outputParamIdMap.put(output.getId(), paramIdMap);
    }
    return new ElementInfo(element, elementType, inputs, outputVarNameMap, paramVarNameMap,
        outputParamIdMap);
  }

  /**
   * @return the name of the variable which stores the current value of the given input
   */
  protected String getVarName(Input input) {
    return getElementOutputVarName(input.getElement(), input.getOutput());
  }

  /**
   * @return the name of the variable that stores the current output value of an element
   */
  protected String getElementOutputVarName(Element element, Output output) {
    if(element.getType().equals(ExternalElement.TYPE)) {
      return String.format("ex_%s", element.getId());
    }
    return String.format("e%s_out%s", element.getId(), output.getId());
  }

  /**
   * @return the name of the variable that stores the value of an element parameter
   */
  protected String getElementParamVarName(Element element, Param param) {
    return String.format("e%s_%s", element.getId(), param.getId());
  }

  /**
   * @return the id of an element output parameter that is used to refer to the parameter in
   *         output expressions
   */
  protected String getElementOutputParamId(Output output, String paramId) {
    return String.format("out%s_%s", output.getId(), paramId);
  }

  /**
   * @return the name of the variable that stores the value of an element output parameter
   */
  protected String getElementOutputParamVarName(Element element, Output output, Param param) {
    return String.format("e%s_out%s_%s", element.getId(), output.getId(), param.getId());
  }
}