package equa.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.swing.JOptionPane;

import equa.diagram.cd.ControllerBasedClassDiagramPanel;
import equa.meta.DuplicateException;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.requirements.RequirementModel;
import equa.project.Project;
import equa.project.ProjectRole;
import fontys.observer.PropertyListener;

public abstract class ProjectController extends AbstractController implements PropertyListener {

    private Project project;
    private EntityManager em;
    private boolean useJPA = false;

    protected ProjectController() {
        if (useJPA) {
            em = PersistanceManager.getEntityManager();
        }
    }

    public Project getProject() {
        if (useJPA) {
            if (project == null) {
                return project;
            }
            return em.find(Project.class, project.getName());
        } else {
            return project;
        }
    }

    void setProject(Project project) {
        this.project = project;
        initListeners();
    }

    public List<?> getAllProjects() {
        EntityTransaction userTransaction = em.getTransaction();

        userTransaction.begin();
        Query query = em.createQuery("SELECT p FROM Project p");
        List<?> resultList = query.getResultList();
        userTransaction.commit();
        return resultList;
    }

    public void openProject(Project p) {
        if (useJPA) {
            Project pro = null;
            if (p != null) {
                pro = em.find(Project.class, p.getName());
            }
            if (pro != null) {
                setProject(pro);
            }
        } else {
        }
    }

    public void createNewProject(Project project) {
        if (useJPA) {
            EntityTransaction userTransaction = em.getTransaction();

            /**
             * Push project to the database
             */
            userTransaction.begin();
            em.persist(project);
            userTransaction.commit();

            setProject(em.find(Project.class, project.getName()));
        } else {
            setProject(project);
        }
    }

    public void openProject(File projectSave) throws ClassNotFoundException, IOException {
        setProject(Project.getProject(projectSave));
    }

    private void initListeners() {
        ObjectModel om = project.getObjectModel();
        om.addListener(this, "newType");
        om.addListener(this, "removedType");
        RequirementModel rm = project.getRequirementModel();
        rm.addListener(this, "newReq");
        rm.addListener(this, "remReq");
    }

    public void saveProject() throws FileNotFoundException {
        if (project.save(project.getFile())) {
            System.out.println("Save succesfull");
        } else {
            System.out.println("Save Failed");
        }

    }

    public void refreshViews() {
        //em.refresh(em.find(Project.class, project.getName()));
        if (useJPA) {
            em.getEntityManagerFactory().getCache().evictAll();
            em.refresh(getProject());
        }
        synchronized (registeredViews) {
            for (IView v : registeredViews) {
                v.refresh();
            }
        }
    }

    /**
     * Overridden by either Swing or EclipseController
     *
     * @param obj
     */
    public void showView(Object obj) {
    }

    public void createClassDiagram(String classDiagramName) {
        // TODO: Verplaatsen naar Views: String name = showInputDialog("Please enter a name of the CD");
        if (classDiagramName != null && !classDiagramName.isEmpty()) {
            ControllerBasedClassDiagramPanel cd = new ControllerBasedClassDiagramPanel(classDiagramName, this, true);
            try {
                project.addClassDiagram(cd.getClassDiagram());
                refreshViews();
                showView(cd);
            } catch (DuplicateException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }
    }

    public ProjectRole getCurrentUser() {
        if (project != null) {
            return project.getCurrentUser();
        } else {
            return null;
        }
    }

    public void setCurrentUser(ProjectRole currentUser) {
        project.setCurrentUserAndInform();
    }

    public boolean isUseJPA() {
        return useJPA;
    }

    public void setUseJPA(boolean useJPA) {
        this.useJPA = useJPA;
        em = PersistanceManager.getEntityManager();
        System.out.println("EntityManager: " + em);
    }
}
