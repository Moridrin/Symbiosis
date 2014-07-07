package equa.controller;

import java.beans.PropertyChangeEvent;

import equa.desktop.Desktop;

public class SwingProjectController extends ProjectController {

    private static final SwingProjectController INSTANCE = new SwingProjectController();

    private Desktop desktop;

    public void setDesktop(Desktop desktop) {
        this.desktop = desktop;
    }

    public Desktop getDesktop() {
        return desktop;
    }

    public static SwingProjectController getReference() {
        return INSTANCE;
    }

    private SwingProjectController() {
        super();
    }

    public void initDesktop() {
        desktop.refresh();
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }
}
