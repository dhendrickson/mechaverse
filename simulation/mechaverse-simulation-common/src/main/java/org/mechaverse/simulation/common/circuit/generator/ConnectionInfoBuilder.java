package org.mechaverse.simulation.common.circuit.generator;

import org.mechaverse.simulation.common.circuit.generator.CircuitSimulationModel.ConnectionInfo;

/**
 * Builder for {@link ConnectionInfo}.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public interface ConnectionInfoBuilder {

  public ConnectionInfo build();
}
