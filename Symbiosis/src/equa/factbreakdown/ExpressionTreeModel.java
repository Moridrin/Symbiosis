/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Value;
import equa.meta.requirements.FactRequirement;
import equa.meta.traceability.ExternalInput;
import equa.project.ProjectRole;
import equa.util.Naming;

/**
 *
 * @author FrankP
 */
public class ExpressionTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;
    private final ProjectRole currentUser;
    private FactRequirement source;
    private final ObjectModel om;

    public ExpressionTreeModel(ObjectModel objectModel, ProjectRole currentUser,
            FactRequirement source) {
        super(null);
        this.currentUser = currentUser;
        this.om = objectModel;
        this.source = source;
    }

    public ProjectRole getCurrentUser() {
        return currentUser;
    }

    public void setRoot(ParentNode parentNode, FactRequirement source) {
        root = parentNode;
        this.source = source;
    }

    private TreeModelEvent createEvent(ValueNode vn) {
        LinkedList<Object> pathFromNode = new LinkedList<>();
        ExpressionNode child = vn;
        pathFromNode.add(child);
        ExpressionNode node = child;
        if (root != null) {
            while (node != root) {
                node = node.getParent();
                pathFromNode.add(node);
            }
        }
        int n = pathFromNode.size();
        Object[] pathFromRoot = new Object[n];
        for (int i = 0; i < pathFromRoot.length; i++) {
            pathFromRoot[i] = pathFromNode.get(n - i - 1);
        }
        TreeModelEvent event = new TreeModelEvent(this, pathFromRoot);

        return event;
    }

    private void fireTreeStructureChanged(TreeModelEvent event) {
        EventListener[] listeners = listenerList.getListeners(TreeModelListener.class);
        for (EventListener l : listeners) {
            ((TreeModelListener) l).treeStructureChanged(event);
        }
    }

//    private int roleNumber(ParentNode parent, int substitutionNumber, boolean objectExpression) {
//        FactType ft = om.getFactType(parent.getTypeName());
//        if (ft == null) {
//            return substitutionNumber;
//        }
//        if (objectExpression) {
//            if (ft.isObjectType()) {
//                if (ft.size() == 0) {
//                    return 0;
//                } else {
//                    return ft.getObjectType().getOTE().getRoleNumber(substitutionNumber);
//                }
//            } else {
//                return substitutionNumber;
//            }
//        } else {
//            TypeExpression fte = ft.getFTE();
//            if (fte == null) {
//                return substitutionNumber;
//            } else {
//                return fte.getRoleNumber(substitutionNumber);
//            }
//        }
//    }
    public FactNode createFactRoot(String expression, String typeName)
            throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        FactType ft = om.getFactType(typeName);
        FactNode factNode;
// TODO: second and third conjunct necessary?
        if (ft != null && ft.getFTE().isParsable()/** && !ft.hasAbstractRoles()**/) {
            factNode = new FactNode(this, null, expression, ft.getFTE());
        } else {
            factNode = new FactNode(this, null, expression, typeName);
        }

        setRoot(factNode);
        fireTreeStructureChanged(createEvent(factNode));
        //nodeStructureChanged(root);
        return factNode;
    }

    public ObjectNode createObjectRoot(String expression, String typeName)
            throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        FactType ft = om.getFactType(typeName);
        ObjectNode objectNode;
        // TODO: second and third conjunct necessary?
        if (ft != null && ft.isParsable() /**&& !ft.hasAbstractRoles()**/) {
            objectNode = new ObjectNode(this, null, expression,
                    Naming.withoutCapital(typeName), 0, ft.getObjectType().getOTE());
        } else {
            objectNode = new ObjectNode(this, expression, typeName);
        }

        setRoot(objectNode);
        fireTreeStructureChanged(createEvent(objectNode));
        //nodeStructureChanged(root);
        return objectNode;
    }

    public ValueLeaf addValueLeafAt(FactNode parent, int nr, int from, int unto,
            String typeName, String roleName) throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        ValueLeaf leaf = parent.addValueLeafAt(nr, from, unto, typeName,
                roleName, -1);
        fireTreeStructureChanged(createEvent(parent));
        //nodeStructureChanged(parent);
        //roleNumber(parent, nr / 2, roleName != null)
        return leaf;
    }

    public ObjectNode addObjectNodeAt(ParentNode parent, int nr, int from, int unto, String typeName,
            String roleName, boolean otherOptions) throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        ObjectNode object = parent.addObjectNodeAt(nr, from, unto, typeName, roleName,
                -1, otherOptions);
        fireTreeStructureChanged(createEvent(parent));
        //nodeStructureChanged(parent);
        return object;
    }

    public ObjectNode addObjectNodeAt(ParentNode parent, int nr, int from, int unto, String typeName,
            String roleName, List<String> values, List<Integer> numbers) throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        ObjectType ot = om.getObjectType(typeName);
        Value value = ot.parse(values, source);
        TextNode textNode = (TextNode) parent.getChildAt(nr);
        String text = textNode.getText();
        String textTrimmed = text.substring(from, unto).trim();
        int fromTrimmed = text.indexOf(textTrimmed, from);
        int untoTrimmed = fromTrimmed + textTrimmed.length();
        ObjectNode object = new ObjectNode(this, null, value, roleName,
                -1, ot.getOTE());
        insertNodeInto(object, parent, nr + 1);
        insertNodeInto(new TextNode(null, text.substring(untoTrimmed)), parent, nr + 2);
        setText(textNode, text.substring(0, fromTrimmed));
        nodeStructureChanged(parent);
        fireTreeStructureChanged(createEvent(parent));
        return object;
    }

    public CollectionNode addCollectionNodeAt(FactNode parent, int nr, int from, int unto, String collectionName,
            String roleName, String begin, String separator, String end, String elementName,
            String elementRoleName, boolean sequence, boolean otherOptions)
            throws MismatchException, ChangeNotAllowedException, DuplicateException {
        CollectionNode cn;
        cn = parent.addCollectionNodeAt(nr, from, unto, collectionName, roleName,
                -1, begin, separator,
                end, elementName, elementRoleName, sequence, otherOptions);
        fireTreeStructureChanged(createEvent(parent));
        return cn;
    }

//    public CollectionNode addCollectionNodeWithSuperTypeNodesAt(FactNode parent, int nr, int from, int unto, String collectionName,
//            String roleName, String begin, String separator, String end, String elementName,
//            String elementRoleName, boolean sequence)
//            throws MismatchException, ChangeNotAllowedException, DuplicateException {
//        CollectionNode cn;
//        cn = parent.addCollectionNodeAt(nr, from, unto, collectionName, roleName,
//                roleNumber(parent, nr / 2, roleName != null), begin, separator,
//                end, elementName, elementRoleName, sequence);
//        fireTreeStructureChanged(createEvent(parent));
//        return cn;
//    }
    public SuperTypeNode addSuperTypeNodeAt(FactNode parent, int nr,
            String superTypeName, int from, int unto,
            String roleName, String typeName) throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        SuperTypeNode supertypeNode;
        supertypeNode
                = parent.addSuperTypeNodeAt(superTypeName, nr, from, unto, roleName,
                        -1, typeName);
        fireTreeStructureChanged(createEvent(supertypeNode));
        //nodeStructureChanged(parent);
        return supertypeNode;
    }

    public void removeValueNodeAt(ParentNode parent, int nr)
            throws ChangeNotAllowedException, MismatchException,
            DuplicateException {

        if (parent.isReady()) {
            setReady(parent, false);
        }

        if (nr == -1) {
            removeFinishedNode(parent);
        } else {
            ValueNode vn = (ValueNode) parent.getChildAt(nr);
            if (vn instanceof ParentNode) {
                removeFinishedNode((ParentNode) vn);
            }
            parent.removeValueNodeAt(nr);
        }
        fireTreeStructureChanged(createEvent(parent));
        //nodeStructureChanged(parent);
    }

    void removeChildrenValueNodes(ParentNode vn) throws ChangeNotAllowedException, MismatchException, DuplicateException {
        int childCount = vn.getChildCount();

        for (int i = childCount - 1; i >= 0; i--) {
            if (vn.getChildAt(i) instanceof ValueNode) {
                removeValueNodeAt(vn, i);
            }
        }
    }

    private void removeFinishedNode(ParentNode node) throws ChangeNotAllowedException {
        System.out.println("Remove node: " + node.toString() + "; " + node.getClass() + "; " + ((ValueNode) node).getTypeName());
        node.deregisterAtObjectModel();
    }

    private void addFinishedNode(ParentNode node) throws MismatchException, ChangeNotAllowedException, DuplicateException {
        System.out.println("finished node: " + node.toString() + "; " + node.getClass() + "; " + ((ValueNode) node).getTypeName());
        node.registerAtObjectModel();
    }

    void updateObjectModel(ParentNode node) throws MismatchException, DuplicateException, ChangeNotAllowedException {
        if (node.isReady()) {
            addFinishedNode(node);
        } else {
            System.out.println("update not possible, node not ready " + node.getTypeName());
        }
    }

    public FactRequirement getSource() {
        return source;
    }

    public ObjectModel getObjectModel() {
        return om;
    }

    public void setReady(ExpressionNode node, boolean makeReady) {
//        if (makeReady) {
//            if (!node.allValueSubNodesAreReady()) {
//                return;
//            }
//        }
        if (node.setReady(makeReady)) {
            nodeChanged(node);
        } else {
        }

    }

    public void setUnconditionallyReady(ParentNode parentNode) {
        parentNode.setUnconditionallyReady();
        nodeChanged(parentNode);
    }

    void refreshSource() throws ChangeNotAllowedException {
        ExternalInput modification = new ExternalInput("text has been changed during decomposition", currentUser);
        source.setText(modification, root.toString());
    }

    public void replace(TextNode textNode, int from, int unto, String expression) {
        String backup = textNode.getText();
        try {
            textNode.replace(from, unto, expression);
            nodeChanged(textNode);
            refreshSource();
        } catch (ChangeNotAllowedException ex) {
            textNode.setText(backup);
        }
    }

    public void setText(TextNode textNode, String text) {
        String backup = textNode.getText();
        textNode.setText(text);
        try {
            nodeChanged(textNode);
            refreshSource();
        } catch (ChangeNotAllowedException ex) {
            textNode.setText(backup);
        }
    }

    public void setRoleName(ISubstitution substitution, String roleName) {
        substitution.setRoleName(roleName);
        nodeChanged((ValueNode) substitution);
    }

    public void setTypeName(ValueNode valueNode, String typeName) {
        valueNode.setTypeName(typeName);
        nodeChanged(valueNode);
    }
}
