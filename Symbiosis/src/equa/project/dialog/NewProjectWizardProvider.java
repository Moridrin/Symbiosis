/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project.dialog;

import java.util.Map;

import javax.swing.JComponent;

import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPanelProvider;

import equa.project.Project;

/**
 *
 * @author Keethanjan
 */
public class NewProjectWizardProvider extends WizardPanelProvider {

    private static final String title = "New project";

    public NewProjectWizardProvider() {
        super(title, "setProjectInfo", "Create a new project");
    }

    @Override
    protected JComponent createPanel(
            final WizardController wizardController,
            String str, final Map map) {
        wizardController.setProblem("Name must be entered");
        Map<String, Object> mappie = (Map<String, Object>) map;
        NewProjectPage page = new NewProjectPage(wizardController, mappie);
        return page;
    }

    @Override
    protected Object finish(Map settings) throws WizardException {
        return new Project(settings.get("name").toString(), settings.get("creator").toString(), settings.get("role").toString(), (Boolean) settings.get("projectmember"));
    }
}
