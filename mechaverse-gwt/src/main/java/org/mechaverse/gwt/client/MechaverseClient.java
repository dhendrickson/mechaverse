package org.mechaverse.gwt.client;

import org.mechaverse.gwt.client.environment.SimulationEditorPresenter;
import org.mechaverse.gwt.client.environment.SimulationEditorView;
import org.mechaverse.gwt.client.environment.SimulationPresenter;
import org.mechaverse.gwt.client.environment.SimulationView;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The main entry point.
 * 
 * @author thorntonv@mechaverse.org
 */
public class MechaverseClient implements EntryPoint {

  public void onModuleLoad() {
    ensureCssInjected();

    String editParam = Window.Location.getParameter("edit");
    if (editParam != null && editParam.equalsIgnoreCase("true")) {
      SimulationEditorPresenter presenter =
          new SimulationEditorPresenter(new SimulationEditorView());
      RootLayoutPanel.get().add(presenter.getView());
    } else {
      SimulationPresenter presenter = new SimulationPresenter(new SimulationView());
      RootPanel.get().add(presenter.getView());
    }
  }

  protected static void ensureCssInjected() {
    MechaverseResourceBundle.INSTANCE.css().ensureInjected();
  }
}