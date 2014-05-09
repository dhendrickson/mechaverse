package org.mechaverse.tools.circuit.generator;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.mechaverse.circuit.model.Circuit;
import org.mechaverse.tools.circuit.CircuitTestUtil;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.ElementInfo;
import org.mechaverse.tools.circuit.generator.CircuitSimulationModel.LogicalUnitInfo;

public class AbstractCircuitSimulationGeneratorTest {

  private static class TestCircuitSimulationGenerator extends AbstractCircuitSimulationGenerator {

    public TestCircuitSimulationGenerator(CircuitSimulationModel model) {
      super(model);
    }

    @Override
    public void generate(PrintWriter out) {}
  }

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testGetVarMappedExpression() {
    CircuitSimulationModelBuilder modelBuilder = new CircuitSimulationModelBuilder();
    Circuit testCircuit1 = CircuitTestUtil.createTestCircuit1();
    CircuitSimulationModel model = modelBuilder.buildModel(testCircuit1);
    TestCircuitSimulationGenerator generator = new TestCircuitSimulationGenerator(model);

    LogicalUnitInfo logicalUnitInfo = model.getLogicalUnitInfo();
    ElementInfo element1 = logicalUnitInfo.getElementInfo("1");
    ElementInfo element2 = logicalUnitInfo.getElementInfo("2");

    // Test element1 output expressions.
    assertEquals("(ex_in2 & e1_out1_input2Mask) | (e2_out1 & e1_out1_input3Mask)",
        generator.getVarMappedExpression(element1, element1.getOutputs().get(0)));
    assertEquals("(ex_in1 & e1_out2_input1Mask) | (e2_out1 & e1_out2_input3Mask)",
        generator.getVarMappedExpression(element1, element1.getOutputs().get(1)));
    assertEquals("(ex_in1 & e1_out3_input1Mask) | (ex_in2 & e1_out3_input2Mask)",
        generator.getVarMappedExpression(element1, element1.getOutputs().get(2)));

    // Test element2 output expressions.
    assertEquals("(ex_in3 & e2_out1_input2Mask) | (ex_in4 & e2_out1_input3Mask)",
        generator.getVarMappedExpression(element2, element2.getOutputs().get(0)));
    assertEquals("(e1_out3 & e2_out2_input1Mask) | (ex_in4 & e2_out2_input3Mask)",
        generator.getVarMappedExpression(element2, element2.getOutputs().get(1)));
    assertEquals("(e1_out3 & e2_out3_input1Mask) | (ex_in3 & e2_out3_input2Mask)",
        generator.getVarMappedExpression(element2, element2.getOutputs().get(2)));
  }
}