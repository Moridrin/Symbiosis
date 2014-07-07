package equa.controller;

import java.beans.PropertyChangeEvent;

public class EclipseProjectController extends ProjectController {

    private static final EclipseProjectController INSTANCE = new EclipseProjectController();

    public static EclipseProjectController getReference() {
        return INSTANCE;
    }

    private EclipseProjectController() {
        super();
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }
}
