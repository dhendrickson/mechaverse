package org.mechaverse.gwt.client.environment;

import java.util.Iterator;

import org.mechaverse.api.model.simulation.ant.Environment;
import org.mechaverse.api.model.simulation.ant.SimulationState;
import org.mechaverse.api.simulation.util.SimulationUtil;
import org.mechaverse.gwt.client.util.UUID;
import org.mechaverse.gwt.shared.MechaverseGwtRpcServiceAsync;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A presenter for {@link SimulationEditorView}.
 * 
 * @author thorntonv@mechaverse.org
 */
public class SimulationEditorPresenter {

  protected class SimulationEditorViewObserver implements SimulationEditorView.Observer {

    @Override
    public void onNew() {
      createNewEnvironment();
    }

    @Override
    public void onSave() {
      save();
    }

    @Override
    public void onDelete() {
      deleteEnvironment();
    }

    @Override
    public void onEnvironmentSelected(String environmentId) {
      setEnvironment(environmentId);
    }
  }

  private final MechaverseGwtRpcServiceAsync service = 
      MechaverseGwtRpcServiceAsync.Util.getInstance();

  private String key;
  private String environmentId;
  private SimulationState state;
  private final EnvironmentEditorPresenter environmentEditorPresenter;
  private final SimulationEditorView view;

  public SimulationEditorPresenter(SimulationEditorView view) {
    this(SimulationPresenter.INITIAL_STATE_KEY, view);
  }

  public SimulationEditorPresenter(String key, SimulationEditorView view) {
    this.key = key;
    this.view = view;
    this.environmentEditorPresenter =
        new EnvironmentEditorPresenter(view.getEnvironmentEditorView());
    
    view.setObserver(new SimulationEditorViewObserver());
    loadState();
  }

  public void loadState() {
    service.loadState(key, new AsyncCallback<SimulationState>() {
      @Override
      public void onFailure(Throwable caught) {
        Window.alert(caught.getMessage());
        saveInitialState();
      }

      @Override
      public void onSuccess(final SimulationState state) {
        setState(state);
      }
    });
  }
  
  public void setState(SimulationState state) {
    this.state = state;
    setEnvironment(environmentId);
    view.setAvailableEnvironments(SimulationUtil.getEnvironments(state));
  }

  public void createNewEnvironment() {
    Preconditions.checkNotNull(state);
    
    Environment newEnvironment = new Environment();
    newEnvironment.setId(UUID.uuid().toString());
    newEnvironment.setWidth(25);
    newEnvironment.setHeight(25);
    state.getSubEnvironments().add(newEnvironment);
    
    save();
    environmentId = null;
    loadState();        
  }
  
  public void save() {
    Preconditions.checkNotNull(state);

    service.saveState(key, state, new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable cause) {
        Window.alert(cause.getMessage());
      }

      @Override
      public void onSuccess(Void arg0) {
        Window.alert("Save complete.");
      }
    });
  }

  public void deleteEnvironment() {
    Preconditions.checkNotNull(state);

    Iterator<Environment> environmentIt = state.getSubEnvironments().iterator();
    while (environmentIt.hasNext()) {
      Environment env = environmentIt.next();
      if (env.getId().equalsIgnoreCase(environmentId)) {
        environmentIt.remove();
      }
    }

    save();
    environmentId = null;
    loadState();
  }

  public void setEnvironment(final String environmentId) {
    Preconditions.checkNotNull(state);

    this.environmentId = environmentId;
    Environment env = state.getEnvironment();
    if (environmentId != null) {
      env = SimulationUtil.getEnvironment(state, environmentId);
      if (env == null) {
        env = state.getEnvironment();
      }
    }
    environmentEditorPresenter.setEnvironment(env);
  }

  public SimulationEditorView getView() {
    return view;
  }
  
  protected void saveInitialState() {
    service.getCurrentState(new AsyncCallback<SimulationState>() {
      @Override
      public void onFailure(Throwable arg0) {}

      @Override
      public void onSuccess(SimulationState state) {
        service.saveState(key, state, new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable cause) {
            Window.alert(cause.getMessage());
          }

          @Override
          public void onSuccess(Void arg0) {
            Window.alert("Created initial state.");
            loadState();
          }
        });
      }
    });
  }  
}