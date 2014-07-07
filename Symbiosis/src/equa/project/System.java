/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

/**
 *
 * @author frankpeeters
 */
public class System extends ProjectRole {

    private static final long serialVersionUID = 1L;
    public final static System SINGLETON = new System();

    private System() {
    }

    @Override
    public String getName() {
        return "System";
    }

    @Override
    public String getRole() {
        return "System role";
    }

    @Override
    public void setRole(String role) {
    }

    @Override
    public int compareTo(ProjectRole t) {
        return "system".compareTo(t.getName().toLowerCase());
    }
   
}
