package org.mechaverse.simulation.common.cellautomaton.examples;

import java.awt.Color;
import java.io.InputStream;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

import com.google.common.base.Function;

/**
 * Four input boolean function implementation of {@link OpenClCellularAutomatonCLI}.
 */
public class OpenClCircuit extends OpenClCellularAutomatonCLI {

  public static void main(String[] args) throws Exception {
    OpenClCellularAutomatonCLI.main(args, new OpenClCircuit());
  }

  @Override
  protected InputStream getDescriptorInputStream() {
    return ClassLoader.getSystemResourceAsStream("circuit.xml");
  }

  @Override
  protected Function<Cell, Color> getCellColorProvider() {
    return SINGLE_BITPLANE_CELL_COLOR_PROVIDER;
  }
}