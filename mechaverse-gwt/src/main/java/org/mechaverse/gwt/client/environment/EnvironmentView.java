package org.mechaverse.gwt.client.environment;

import java.util.Set;

import org.mechaverse.api.model.simulation.ant.Direction;
import org.mechaverse.api.model.simulation.ant.Entity;
import org.mechaverse.api.model.simulation.ant.EntityType;
import org.mechaverse.api.model.simulation.ant.Environment;
import org.mechaverse.gwt.client.MechaverseResourceBundle;
import org.mechaverse.gwt.client.util.Coordinate;
import org.mechaverse.gwt.client.util.EntityUtil;

import com.google.common.collect.Sets;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A view which displays an environment.
 * 
 * @author thorntonv@mechaverse.org
 */
public class EnvironmentView extends SimplePanel {

  public static interface Observer {
    
    void onCellClick(int row, int column);
    void onCellAltClick(int row, int column);
  }

  private int cellWidth = MechaverseResourceBundle.INSTANCE.dirt().getWidth();
  private int cellHeight = MechaverseResourceBundle.INSTANCE.dirt().getHeight();

  private Canvas canvas;
  private Environment environment;
  private Set<Coordinate> dirtyCells = Sets.newHashSet();
  private Set<Observer> observers = Sets.newHashSet();

  public EnvironmentView() {
    addStyleName(MechaverseResourceBundle.INSTANCE.css().environmentPanel());
    this.canvas = Canvas.createIfSupported();

    add(canvas);
    canvas.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        int row = event.getY() / cellHeight;
        int column = event.getX() / cellWidth;

        for (Observer observer : observers) {
          if(event.getNativeButton() == NativeEvent.BUTTON_LEFT) { 
            observer.onCellClick(row, column);
          } else {
            observer.onCellAltClick(row, column);            
          }
        }
      }
    });
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
    canvas.setCoordinateSpaceWidth(cellWidth * environment.getWidth());
    canvas.setCoordinateSpaceHeight(cellHeight * environment.getHeight());

    dirtyCells.clear();
    for (int row = 0; row < environment.getHeight(); row++) {
      for (int column = 0; column < environment.getWidth(); column++) {
        dirtyCells.add(Coordinate.create(row, column));
      }
    }

    update();
  }
  
  public HandlerRegistration addObserver(final Observer observer) {
    observers.add(observer);
    return new HandlerRegistration() {
      public void removeHandler() {
        observers.remove(observer);
      }
    };
  }

  public void update() {
    Context2d context = canvas.getContext2d();

    for (Coordinate coordinate : dirtyCells) {
      drawImage(MechaverseResourceBundle.DIRT_IMAGE_ELEMENT, context, coordinate.getRow(),
          coordinate.getColumn());
    }
    dirtyCells.clear();

    for (Entity entity : environment.getEntities()) {
      EntityType entityType = EntityUtil.getType(entity);
      ImageElement image = MechaverseResourceBundle.ENTITY_IMAGE_ELEMENTS.get(entityType);
      if (entityType == EntityType.ANT) {
        Direction direction = entity.getDirection();
        direction = direction != null ? direction : Direction.EAST;
        image = MechaverseResourceBundle.BLACK_ANT_IMAGE_ELEMENTS.get(direction);
      }
      drawEntity(entity, image, context);
    }
  }

  protected void drawEntity(Entity entity, ImageElement image, Context2d context) {
    Coordinate coord = Coordinate.create(entity.getY(), entity.getX());
    drawImage(image, context, coord.getRow(), coord.getColumn());
    dirtyCells.add(coord);
  }

  protected void drawImage(ImageElement image, Context2d context, int row, int column) {
    int xOffset = (cellWidth - image.getWidth()) / 2;
    int yOffset = (cellHeight - image.getHeight()) / 2;
    context.drawImage(image, column * cellWidth + xOffset, row * cellHeight + yOffset);
  }
}