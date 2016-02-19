package org.mechaverse.simulation.common.cellautomaton.ui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton;
import org.mechaverse.simulation.common.cellautomaton.simulation.CellularAutomaton.Cell;

import com.google.common.base.Function;

/**
 * A {@link CellularAutomaton} visualizer.
 * 
 * @author Vance Thornton (thorntonv@mechaverse.org)
 */
public class CellularAutomatonVisualizer extends JFrame {

  private static final long serialVersionUID = 1L;
  private static final String TITLE = "Cellular Automaton Simulator";

  private final CellularAutomaton cellularAutomaton;
  private final CellularAutomatonRenderer renderer;
  private final BufferedImageView imageView;
  private final int framesPerSecond;
  private final int frameCount;

  public CellularAutomatonVisualizer(CellularAutomaton cellularAutomaton,
      Function<Cell, Color> cellColorProvider, int width, int height, int framesPerSecond) {
    this(cellularAutomaton, cellColorProvider, width, height, framesPerSecond, -1);
  }

  public CellularAutomatonVisualizer(CellularAutomaton cellularAutomaton,
      Function<Cell, Color> cellColorProvider, int width, int height, int framesPerSecond, int frameCount) {
    this.cellularAutomaton = cellularAutomaton;
    this.renderer =
        new CellularAutomatonRenderer(cellularAutomaton, cellColorProvider, width, height);
    this.imageView = new BufferedImageView();
    this.framesPerSecond = framesPerSecond;
    this.frameCount = frameCount;

    initUI();
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setVisible(true);
      }
    });
  }
  
  public void start() {
    try {
      int cnt = 1;
      while (frameCount == -1 || cnt <= frameCount) {
        update();
        Thread.sleep(1000 / framesPerSecond);
        cnt++;
      }
    } catch (InterruptedException ignored) {}
  }
  
  public void update() {
    imageView.setImage(renderer.draw());
    imageView.repaint();
    cellularAutomaton.update();
  }
  
  private void initUI() {
    setTitle(TITLE);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    add(imageView);
    setResizable(false);
    imageView.setPreferredSize(renderer.getPreferredSize());
    pack();
    setLocationRelativeTo(null);
  }
}
