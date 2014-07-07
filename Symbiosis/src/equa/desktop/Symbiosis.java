/*
 * SymbiosisApp.java
 */
package equa.desktop;

import java.io.File;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class Symbiosis extends SingleFrameApplication {

    private Desktop desktop;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(desktop);

    }

    @Override
    protected void initialize(String[] args) {
        super.initialize(args);
        if (args.length > 0) {
            desktop = new Desktop(this, new File(args[args.length-1]));
        } else {
            desktop = new Desktop(this);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return the instance of SymbiosisApp
     */
    public static Symbiosis getApplication() {
        return Application.getInstance(Symbiosis.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(Symbiosis.class, args);
    }
}
