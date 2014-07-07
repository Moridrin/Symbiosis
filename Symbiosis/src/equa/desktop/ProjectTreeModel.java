/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.desktop;

import java.util.EventListener;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import equa.controller.PersistanceManager;
import equa.controller.SwingProjectController;
import equa.diagram.cd.ClassDiagram;
import equa.diagram.ord.deprecated.ObjectRoleDiagram;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementModel;
import equa.project.ITerm;
import equa.project.Project;
import equa.project.ProjectRole;
import equa.project.ProjectRoles;
import equa.project.Vocabulary;

/**
 *
 * @author Keethanjan
 */
public class ProjectTreeModel implements TreeModel {

    private Project project;
    private DefaultTreeModel innerModel;
    private DefaultMutableTreeNode projectNode;
    private DefaultMutableTreeNode objectModelNode;
    private DefaultMutableTreeNode ordNode;
    private DefaultMutableTreeNode cdNode;
    private DefaultMutableTreeNode requirementsModelNode;
    private DefaultMutableTreeNode useCaseModelNode;
    private DefaultMutableTreeNode participantsNode;
    private DefaultMutableTreeNode vocabularyNode;
    //private DefaultMutableTreeNode testModelNode;
    private EventListenerList listenerList;

    public ProjectTreeModel(Project project) {
        this.project = project;
        listenerList = new EventListenerList();
        projectNode = new DefaultMutableTreeNode(project.getName());
        participantsNode = new DefaultMutableTreeNode("Participants");
        vocabularyNode = new DefaultMutableTreeNode("Vocabulary");
        objectModelNode = new DefaultMutableTreeNode("ObjectModel");
        requirementsModelNode = new DefaultMutableTreeNode("RequirementsModel");
        useCaseModelNode = new DefaultMutableTreeNode("UseCaseModel");
        //testModelNode = new DefaultMutableTreeNode("TestModel");
        ordNode = new DefaultMutableTreeNode("ORDs");
        cdNode = new DefaultMutableTreeNode("CDs");

        innerModel = new DefaultTreeModel(projectNode);

        innerModel.insertNodeInto(participantsNode, projectNode, 0);
        innerModel.insertNodeInto(vocabularyNode, projectNode, 1);
        innerModel.insertNodeInto(requirementsModelNode, projectNode, 2);
        innerModel.insertNodeInto(useCaseModelNode, projectNode, 3);
        innerModel.insertNodeInto(objectModelNode, projectNode, 4);
        innerModel.insertNodeInto(ordNode, projectNode, 5);
        innerModel.insertNodeInto(cdNode, projectNode, 6);
        //innerModel.insertNodeInto(testModelNode, projectNode, 3);

        initialize();
    }

    // <editor-fold defaultstate="collapsed" desc="TreeModel methods">
    @Override
    public Object getRoot() {
        return innerModel.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        return innerModel.getChild(parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
        return innerModel.getChildCount(parent);
    }

    @Override
    public boolean isLeaf(Object node) {
        return innerModel.isLeaf(node);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        innerModel.valueForPathChanged(path, newValue);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return innerModel.getIndexOfChild(parent, child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    protected void fireTreeStructureChanged(Object oldRoot) {
        TreeModelEvent event = new TreeModelEvent(this, new Object[]{oldRoot});
        EventListener[] listeners = listenerList.getListeners(TreeModelListener.class);
        for (EventListener l : listeners) {
            ((TreeModelListener) l).treeStructureChanged(event);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialize methods">
    private void initialize() {
        int i = 0;
        ProjectRoles participants = project.getParticipants();
        Iterator<ProjectRole> itparticipants = participants.projectRoles();
        while (itparticipants.hasNext()) {
            ProjectRole participant = itparticipants.next();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(participant);
            innerModel.insertNodeInto(node, participantsNode, i);
            i++;
        }

        Vocabulary vocabulary = project.getVocabulary();
        Iterator<ITerm> itvocabulary = vocabulary.terms();
        i = 0;
        while (itvocabulary.hasNext()) {
            ITerm term = itvocabulary.next();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(term.getName());
            innerModel.insertNodeInto(node, vocabularyNode, i);
            i++;
        }

        ObjectModel om = project.getObjectModel();
        i = 0;
        for (Iterator<FactType> it = om.typesIterator(); it.hasNext();) {
            FactType ft = it.next();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(ft);
            innerModel.insertNodeInto(node, objectModelNode, i);
            i++;
        }

        RequirementModel rm;
        SwingProjectController controller = SwingProjectController.getReference();
        if (controller.isUseJPA()) {
            EntityManager em = PersistanceManager.getEntityManager();
            rm = em.find(Project.class, project.getName()).getRequirementModel();
        } else {
            rm = project.getRequirementModel();
        }

        i = 0;
        for (Iterator<Requirement> it = rm.requirements(); it.hasNext();) {
            Requirement req = it.next();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(req);
            innerModel.insertNodeInto(node, requirementsModelNode, i);
            i++;
        }

        Iterator<ClassDiagram> itCd = project.getClassDiagrams();
        while (itCd.hasNext()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(itCd.next());
            innerModel.insertNodeInto(node, cdNode, cdNode.getChildCount());
        }

        Iterator<ObjectRoleDiagram> itOrd = project.getObjectRoleDiagrams();
        while (itOrd.hasNext()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(itOrd.next());
            innerModel.insertNodeInto(node, ordNode, ordNode.getChildCount());
        }
    }

    public void addObjectRoleDiagram(ObjectRoleDiagram ord) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(ord);
        innerModel.insertNodeInto(node, ordNode, ordNode.getChildCount());
    }

    public void addClassDiagram(ClassDiagram cd) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(cd);
        innerModel.insertNodeInto(node, cdNode, cdNode.getChildCount());
    }
    // </editor-fold>
}
