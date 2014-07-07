/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.AbstractValue;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.CollectionType;
import equa.meta.objectmodel.CollectionTypeExpression;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.ParseResult;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.meta.objectmodel.Tuple;
import equa.meta.objectmodel.TupleItem;
import equa.meta.objectmodel.Type;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.objectmodel.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 *
 * @author FrankP
 */
public class FactNode extends ParentNode {

    private static final long serialVersionUID = 1L;

    /**
     * fact node of an unknown fact type
     *
     * @param model
     * @param parent
     * @param text
     * @param typeName
     */
    public FactNode(ExpressionTreeModel model, ParentNode parent, String text,
            String typeName) {
        super(model, parent, typeName);
        String textTrimmed = text.trim();
        if (textTrimmed.isEmpty()) {
            throw new RuntimeException("fact expression cannot be empty");
        }

        model.insertNodeInto(new TextNode(null, textTrimmed), this, 0);
    }

    /**
     * creation of factnode in behalf of parsable node with or without abstract
     * elements
     *
     * @param model
     * @param parent
     * @param expression
     * @param te.isParsable()
     * @throws MismatchException if one of the constants doesn't match
     */
    public FactNode(ExpressionTreeModel model, ParentNode parent, String expression,
            TypeExpression te) throws MismatchException, ChangeNotAllowedException {
        super(model, parent, te.getParent().getName());

        if (!te.isParsable()) {
            throw new RuntimeException("type expression is not parsable");
        }

        String trimmedExpression = expression.trim();
        FactType ft = te.getParent();

        ParseResult result = te.parse(trimmedExpression, true, model.getSource());
        // ToDo: otherParsingPossible
        List<Value> values = result.getValues();

        ExpressionNode[] nodes = new ExpressionNode[2 * ft.size() + 1];
        nodes[0] = new TextNode(null, te.constant(0));

        boolean ready = true;

        for (int i = 0; i < ft.size(); i++) {
            Role role = ft.getRole(te.getRoleNumber(i));
            ValueNode vn;
            if (role.getSubstitutionType() instanceof BaseType) {
                vn = new ValueLeaf(null, values.get(i).toString(),
                        role.getSubstitutionType().getName(), role.getRoleName(),
                        te.getRoleNumber(i));
            } else {
                ObjectRole objectRole = (ObjectRole) role;
                ObjectType objectType = objectRole.getSubstitutionType();
                if (values.get(i) instanceof AbstractValue) {
                    vn = new SuperTypeNode(model, null, objectType.getName(),
                            values.get(i).toString(), null, objectRole.getRoleName(), te.getRoleNumber(i));
                    ready = false;
                } else if (role.getSubstitutionType() instanceof CollectionType) {
                    vn = new CollectionNode(model, null, role.getRoleName(),
                            te.getRoleNumber(i), (Tuple) values.get(i), null, false);
                } else {
                    vn = new ObjectNode(model, null, values.get(i), role.getRoleName(), te.getRoleNumber(i), objectType.getOTE());
                }
            }
            nodes[2 * i + 1] = vn;
            nodes[2 * i + 2] = new TextNode(null, te.constant(i + 1));
        }

        for (int i = 0;
                i < nodes.length;
                i++) {
            model.insertNodeInto(nodes[i], this, i);
        }

        if (ready
                && !ft.hasAbstractRoles()) {
            model.setUnconditionallyReady(this);
        }
    }
    
   

    @Override
    int roleNumber(int childNumber) {
        FactType ft = getObjectModel().getFactType(getTypeName());
        if (ft == null) {
            return childNumber / 2;
        }

        TypeExpression fte = ft.getFTE();
        if (fte == null) {
            return childNumber / 2;
        } else {
            return fte.getRoleNumber(childNumber / 2);
        }

    }

    /**
     * initialization in behalf of parsable object node
     *
     * @param model
     * @param parent
     * @param sv
     * @param te
     */
    FactNode(ExpressionTreeModel model, ParentNode parent, Value sv,
            TypeExpression te) throws MismatchException, ChangeNotAllowedException {
        super(model, parent, sv.getType().getName());

        int sizeFactType = te.getParent().size();

        Iterator<String> itConstants = te.constants();

        model.insertNodeInto(new TextNode(null, itConstants.next()), this, 0);

        Tuple tuple = (Tuple) sv;
        int nodeNr = 1;
        while (nodeNr < sizeFactType * 2) {

            int roleNumber = te.getRoleNumber(nodeNr / 2);
            TupleItem item = tuple.getItem(roleNumber);
            if (item.getValue() instanceof AbstractValue) {
                String typeName = item.getValue().getType().getName();
                model.insertNodeInto(new SuperTypeNode(model, null, typeName, item.getValue().toString(),
                        null, "", roleNumber), this, nodeNr);

            } else if (item.getValue() instanceof Tuple) {
                TypeExpression ote;
                ote = ((ObjectType) item.getValue().getType()).getOTE();
                if (ote instanceof CollectionTypeExpression) {
                    CollectionType ct = (CollectionType) item.getRole().getSubstitutionType();
                    model.insertNodeInto(new CollectionNode(model, null, item.getRole().getRoleName(),
                            roleNumber, (Tuple) item.getValue(), ct.getElementRole().getRoleName(), false), this, nodeNr);
                } else {
                    model.insertNodeInto(new ObjectNode(model, null, item.getValue(),
                            item.getRole().getRoleName(), roleNumber,
                            ote), this, nodeNr);
                }
            } else {
                model.insertNodeInto(new ValueLeaf(null, item.getValue().toString(),
                        item.getValue().getType().getName(), item.getRole().getRoleName(), roleNumber), this, nodeNr);
            }
            nodeNr++;

            model.insertNodeInto(new TextNode(null, itConstants.next()), this, nodeNr);
            nodeNr++;
        }
        setReady(true);

    }

    public TextNode textNode(int nr) {
        if (0 <= nr && nr <= getChildCount() / 2) {
            return (TextNode) getChildAt(2 * nr);
        } else {
            return null;
        }
    }

    public ValueNode valueNode(int nr) {
        if (0 <= nr && nr < getChildCount() / 2) {
            return (ValueNode) getChildAt(2 * nr + 1);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return getText();
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getChildCount(); i++) {
            sb.append(this.getChildAt(i).toString());
        }

        return sb.toString();
    }

    protected int checkAndCalculateIndex(int nr) {
        int count = getChildCount();
        if (nr < 0 || nr >= count || nr % 2 == 1) {
            throw new RuntimeException("index " + nr + " is not allowed");
        }
        return nr / 2;
    }

    ValueLeaf addValueLeafAt(int nr, int from, int unto,
            String typeName, String roleName, int roleNumber) throws MismatchException,
            ChangeNotAllowedException, DuplicateException {

        checkAndCalculateIndex(nr);

        TextNode textNode = (TextNode) getChildAt(nr);
        String text = textNode.getText();
        // shrinking of object expression at the begin and end
        String textTrimmed = text.substring(from, unto).trim();
        int fromTrimmed = text.indexOf(textTrimmed, from);
        int untoTrimmed = fromTrimmed + textTrimmed.length();

        ExpressionTreeModel model = getExpressionTreeModel();

        ValueLeaf leaf = new ValueLeaf(null, textTrimmed, typeName, roleName, roleNumber);
        model.insertNodeInto(leaf, this, nr + 1);
        model.insertNodeInto(new TextNode(null, text.substring(untoTrimmed)), this, nr + 2);
        model.setText(textNode, text.substring(0, fromTrimmed));
        calculateReadyness();
        model.nodeStructureChanged(this);

        return leaf;
    }

    @Override
    ObjectNode addObjectNodeAt(int nr, int from, int unto, String typeName,
            String roleName, int roleNumber, boolean otherOptions) throws MismatchException,
            ChangeNotAllowedException, DuplicateException {

        checkAndCalculateIndex(nr);

        TextNode textNode = (TextNode) getChildAt(nr);
        String text = textNode.toString();
        // shrinking of object expression at the begin and end
        String textTrimmed = text.substring(from, unto).trim();
        int fromTrimmed = text.indexOf(textTrimmed, from);
        int untoTrimmed = fromTrimmed + textTrimmed.length();

        ExpressionTreeModel model = getExpressionTreeModel();
        ObjectModel om = model.getObjectModel();
        ObjectType ot = om.getObjectType(typeName);
        ObjectNode objectNode;
        if (ot != null && ot.getOTE().isParsable()) {
            if (ot.hasAbstractRoles()) {
                objectNode = new ObjectNode(model, null, textTrimmed, roleName, roleNumber, ot.getOTE());
            } else {
                Value sv = ot.parse(textTrimmed, otherOptions, model.getSource());
                objectNode = new ObjectNode(model, null, sv, roleName, roleNumber, ot.getOTE());
                model.setUnconditionallyReady(objectNode);
            }

        } else {
            objectNode = new ObjectNode(model, null, textTrimmed, typeName, roleName, roleNumber);
        }

        model.insertNodeInto(objectNode, this, nr + 1);
        model.insertNodeInto(new TextNode(null, text.substring(untoTrimmed)), this, nr + 2);
        model.setText(textNode, text.substring(0, fromTrimmed));
        model.nodeStructureChanged(this);

        return objectNode;
    }

    CollectionNode addCollectionNodeAt(int nr, int from, int unto, String collectionName,
            String roleName, int roleNumber, String begin, String separator, String end,
            String elementName, String elementRoleName, boolean sequence, boolean otherOptions)
            throws MismatchException, ChangeNotAllowedException, DuplicateException {

        checkAndCalculateIndex(nr);

        TextNode textNode = (TextNode) getChildAt(nr);
        String text = textNode.toString();
        // shrinking of collection expression at the begin and end
        String collectionTextTrimmed = text.substring(from, unto).trim();
        int fromTrimmed = text.indexOf(collectionTextTrimmed, from);
        int untoTrimmed = fromTrimmed + collectionTextTrimmed.length();

        ExpressionTreeModel model = getExpressionTreeModel();
        ObjectModel om = model.getObjectModel();
        CollectionType ct = (CollectionType) om.getObjectType(collectionName);
        CollectionNode collectionNode;
        ObjectType st = om.getObjectType(elementName);
        boolean abstract_element = st != null && st.isAbstract();
        boolean parsable = (ct != null) && ct.isParsable() && !abstract_element;

        if (ct == null) {
            if (abstract_element) {
                // new collection type with supertype nodes and value nodes
                collectionNode = new CollectionNode(model, null, collectionName, collectionTextTrimmed, roleName, roleNumber,
                        begin, separator, end, elementName, elementRoleName, sequence,
                        true);
            } else {
                // new collection type with value nodes
                collectionNode = new CollectionNode(model, null, collectionName, collectionTextTrimmed, roleName, roleNumber,
                        begin, separator, end, elementName, elementRoleName, sequence,
                        false);
            }
        } else {
            // known collection node 
            if (parsable) {
                // collection node with object nodes or value leafs  
                Tuple sv = (Tuple) ct.parse(collectionTextTrimmed, true, model.getSource());
                collectionNode = new CollectionNode(model, null, roleName, roleNumber, sv,
                        elementRoleName, otherOptions);
                model.setUnconditionallyReady(collectionNode);
            } else {   // OTE of collection node is only not parsable in case of abstract roles
                // collection node with supertype nodes and value nodes 
                collectionNode = new CollectionNode(model, null, collectionName, collectionTextTrimmed, roleName, roleNumber,
                        begin, separator, end, elementName, elementRoleName, sequence, true);

            }
        }

        model.insertNodeInto(collectionNode, this, nr + 1);
        model.insertNodeInto(new TextNode(null, text.substring(untoTrimmed)), this, nr + 2);
        model.setText(textNode, text.substring(0, fromTrimmed));
        model.nodeStructureChanged(this);
        return collectionNode;
    }

    SuperTypeNode addSuperTypeNodeAt(String superTypeName, int nr, int from, int unto,
            String roleName, int roleNumber, String typeName) throws MismatchException,
            ChangeNotAllowedException, DuplicateException {

        checkAndCalculateIndex(nr);

        TextNode textNode = (TextNode) getChildAt(nr);
        String text = textNode.toString();
        // shrinking of object expression at the begin and end
        String textTrimmed = text.substring(from, unto).trim();
        int fromTrimmed = text.indexOf(textTrimmed, from);
        int untoTrimmed = fromTrimmed + textTrimmed.length();

        ExpressionTreeModel model = getExpressionTreeModel();
        SuperTypeNode supertypeNode = new SuperTypeNode(model, null,
                superTypeName, textTrimmed, typeName, roleName, roleNumber);

        model.insertNodeInto(supertypeNode, this, nr + 1);
        model.insertNodeInto(new TextNode(null, text.substring(untoTrimmed)), this, nr + 2);
        model.setText(textNode, text.substring(0, fromTrimmed));

        model.nodeStructureChanged(this);

        return supertypeNode;
    }

    @Override
    public void registerAtObjectModel() throws MismatchException,
            ChangeNotAllowedException, DuplicateException {
        List<Integer> roleNumbers = getRoleNumbers();
        List<String> constants = getConstants();
        List<String> roleNames = getRoleNames(roleNumbers);

        ObjectModel om = getExpressionTreeModel().getObjectModel();
        FactType ft = om.getFactType(getTypeName());
        if (ft == null) {
            om.addFactType(getTypeName(), constants, getDefinedTypes(roleNumbers), roleNames, roleNumbers, getExpressionTreeModel().getSource());
        }

        om.addFact(this);
    }

    public List<Integer> getRoleNumbers() {
        int size = getChildCount();
        List<Integer> roleNumbers = new ArrayList<>(size);
        for (int i = 1; i < size; i += 2) {
            roleNumbers.add(((ISubstitution) getChildAt(i)).getRoleNumber());
        }
        return roleNumbers;
    }

    @Override
    public void deregisterAtObjectModel() throws ChangeNotAllowedException {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        om.removeFact(this);
    }

    /**
     *
     * @param roleNumbers
     * @return concrete valuenodes, order is according to order at the parent
     * facttype
     */
    public List<ValueNode> getConcreteNodes(List<Integer> roleNumbers) {
        int size = getChildCount();
        ValueNode[] concreteNodes = new ValueNode[size / 2];
        for (int i = 0; i < concreteNodes.length; i++) {
            ValueNode node = (ValueNode) getChildAt(i * 2 + 1);

            if (node instanceof SuperTypeNode) {
                concreteNodes[roleNumbers.get(i)] = ((SuperTypeNode) node).getConcreteNode();
            } else {
                concreteNodes[roleNumbers.get(i)] = node;
            }
        }
        return Arrays.asList(concreteNodes);
    }

    /**
     *
     * @param roleNumbers
     * @return types, ranking is according to ranking at the parent facttype
     */
    List<SubstitutionType> getDefinedTypes(List<Integer> roleNumbers) {
        SubstitutionType[] types = new SubstitutionType[roleNumbers.size()];

        for (int i = 0; i < types.length; i++) {
            ValueNode node = (ValueNode) getChildAt(2 * i + 1);
            types[roleNumbers.get(i)] = (SubstitutionType) node.getDefinedType();
        }
        return Arrays.asList(types);
    }

    List<String> getConstants() {
        ArrayList<String> constants = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i += 2) {
            constants.add(((ExpressionNode) getChildAt(i)).getText());
        }
        return constants;
    }

    @Override
    public Type getDefinedType() {
        return getExpressionTreeModel().getObjectModel().getFactType(getTypeName());
    }

    /**
     *
     * @param roleNumbers
     * @return roleNames, ranking is according to ranking at the parent facttype
     */
    List<String> getRoleNames(List<Integer> roleNumbers) {
        String[] roleNames = new String[roleNumbers.size()];

        for (int i = 0; i < roleNames.length; i++) {
            roleNames[roleNumbers.get(i)] = ((ISubstitution) getChildAt(2 * i + 1)).getRoleName();
        }
        return Arrays.asList(roleNames);
    }

    @Override
    public String getTypeDescription() {
        return getTypeName();
    }

    /**
     *
     * @param roleNumbers
     * @return values, ranking is according to ranking at the parent facttype
     */
    public List<String> getValues(int[] roleNumbers) {
        String[] values = new String[roleNumbers.length];

        for (int i = 0; i < values.length; i++) {
            values[roleNumbers[i]] = getChildAt(2 * i + 1).toString();
        }
        return Arrays.asList(values);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
