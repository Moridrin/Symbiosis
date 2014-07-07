/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import equa.meta.DuplicateException;
import equa.meta.IncorrectNameException;

/**
 *
 * @author frankpeeters
 */
@Entity
public class ProjectRoles implements ComboBoxModel<ProjectRole>, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<ProjectRole> projectRoles;
    @OneToOne
    private Project project;
    @Transient
    private transient EventListenerList listenerList;

    public ProjectRoles() {
    }

    public ProjectRoles(Project project) {
        this.project = project;
        projectRoles = new ArrayList<>();
        listenerList = new EventListenerList();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        listenerList = new EventListenerList();
    }

    public Iterator<ProjectRole> projectRoles() {
        return projectRoles.iterator();
    }

    public ProjectRole getProjectRole(String name, String role) {
        for (ProjectRole projectrole : projectRoles) {
            if (projectrole.getName().equalsIgnoreCase(name)
                    && projectrole.getRole().equalsIgnoreCase(role)) {
                return projectrole;
            }
        }
        if (name.equalsIgnoreCase("System")) {
            return System.SINGLETON;
        }
        return null;
    }

    public void changeId(ProjectRole projectrole, String name, String role) throws equa.meta.DuplicateException, IncorrectNameException {
        if (getProjectRole(name, role) != null) {
            throw new DuplicateException("Name already in use at project");
        }
        if (name.isEmpty()) {
            throw new IncorrectNameException("Name of paraticipant can not be empty");
        }
        projectrole.setName(name);
        projectrole.setRole(role);
        fireListChanged();
    }

    public StakeholderRole addStakeholder(String name, String role) {
        if (getProjectRole(name, role) == null) {
            if (name.isEmpty()) {
                return null;
            }
            StakeholderRole sh = new StakeholderRole(name, role);
            projectRoles.add(sh);
            project.checkDefaultCategory(sh);
            fireListChanged();
            return sh;
        }
        return null;
    }

    public ProjectMemberRole addProjectMember(String name, String role) {
        if (getProjectRole(name, role) == null) {
            if (name.isEmpty()) {
                return null;
            }
            ProjectMemberRole pm = new ProjectMemberRole(name, role);
            projectRoles.add(pm);
            fireListChanged();
            return pm;
        }
        return null;
    }

    public void removeProjectRole(ProjectRole participant) {
        projectRoles.remove(participant);
        fireListChanged();
    }

    public Project getProject() {
        return project;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof ProjectRole) {
            project.setCurrentUserAndInform((ProjectRole) anItem);
        } else if (anItem == null) {
            if (project != null) {
                project.setCurrentUserAndInform(null);
            }
        }
    }

    @Override
    public Object getSelectedItem() {
        return project.getCurrentUser();
    }

    @Override
    public int getSize() {
        return projectRoles.size();
    }

    @Override
    public ProjectRole getElementAt(int index) {
        if (0 <= index && index < projectRoles.size()) {
            return projectRoles.get(index);
        } else {
            return null;
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    public void fireListChanged() {
        EventListener[] listeners = listenerList.getListeners(ListDataListener.class);
        for (EventListener l : listeners) {
            ((ListDataListener) l).contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
        }
    }

    boolean isProjectMember(ProjectRole currentUser) {
        for (ProjectRole projectRole : projectRoles) {
            if (projectRole.getName().equalsIgnoreCase(currentUser.getName())) {
                if (projectRole instanceof ProjectMemberRole) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isStakeholder(ProjectRole currentUser) {
        for (ProjectRole projectRole : projectRoles) {
            if (projectRole.getName().equalsIgnoreCase(currentUser.getName())) {
                if (projectRole instanceof StakeholderRole) {
                    return true;
                }
            }
        }
        return false;
    }
}
