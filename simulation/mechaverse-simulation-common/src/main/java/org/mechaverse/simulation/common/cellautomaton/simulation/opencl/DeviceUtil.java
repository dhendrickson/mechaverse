package org.mechaverse.simulation.common.cellautomaton.simulation.opencl;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;

/**
 * Utility methods for working with OpenCL devices.
 *
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public final class DeviceUtil {

  private DeviceUtil() {}

  /**
   * Returns a string containing device information.
   */
  public static String getDeviceInfo() {
    StringBuilder out = new StringBuilder();

    for (CLPlatform platform : CLPlatform.listCLPlatforms()) {
      for (CLDevice device : platform.listCLDevices()) {
        out.append(String.format("%s - %s%n", device.getName(), device.getVendor()));
      }
    }
    return out.toString();
  }
}
