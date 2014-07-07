/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.CollectionType;
import equa.meta.objectmodel.CollectionTypeExpression;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.SubstitutionType;
import equa.meta.objectmodel.Tuple;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.objectmodel.Value;
import equa.meta.requirements.RequirementModel;

/**
 *
 * @author frankpeeters
 */
public class CollectionNode extends ParentNode implements ISubstitution {

    private static final long serialVersionUID = 1L;
    private String roleName;
    private int roleNumber; //makes any sense?
    private String elementTypeName;
    private final String elementRoleName;
    private String begin;
    private String separator;
    private String end;
    private boolean sequence;

    public CollectionNode(ExpressionTreeModel model, ParentNode parent,
            String typeName, String text,
            String roleName, int roleNumber,
            String begin, String separator, String end, String elementTypeName,
            String elementRoleName, boolean sequence, boolean abstract_nodes)
            throws MismatchException, ChangeNotAllowedException, DuplicateException {
        super(model, parent, typeName);
        this.roleName = roleName;
        this.roleNumber = roleNumber;
        this.elementTypeName = elementTypeName;
        this.elementRoleName = elementRoleName;
        this.begin = begin;
        this.end = end;
        this.separator = separator;
        if (elementTypeName.isEmpty()) {
            throw new MismatchException(null,"Empty substitution is impossible.");
        }
         this.sequence = sequence;
        createSubnodes(text, abstract_nodes);     
    }

    public CollectionNode(ExpressionTreeModel model, ParentNode parent,
            String roleName, int roleNumber, Tuple tuple, String elementRoleName,
            boolean otherOptions) throws MismatchException, ChangeNotAllowedException {
        super(model, parent, tuple.getType().getName());

        this.roleName = roleName;
        this.roleNumber = roleNumber; //makes any sense?
        this.elementTypeName = tuple.getType().getName();
        this.elementRoleName = elementRoleName;
        createSubnodes(tuple, otherOptions);
    }

    public String getElementTypeName() {
        return elementTypeName;
    }

    public String getElementRoleName() {
        return elementRoleName;
    }

    private void createSubnodes(String text, boolean abstract_nodes)
            throws MismatchException, ChangeNotAllowedException {
        String textToLowerCase = text.toLowerCase();
        if (!textToLowerCase.startsWith(begin.toLowerCase())) {
            throw new MismatchException(null,"Text \"" + text + "\"" + " doesnt start with begin text: \""
                    + begin + "\"");
        }
        TextNode tn = new TextNode(null, begin);
        ExpressionTreeModel model = this.getExpressionTreeModel();

        int nodeCount = 0;
        model.insertNodeInto(tn, this, nodeCount);
        nodeCount++;

        int indexFrom = begin.length();
        SubstitutionType st = detectSubstitutionType(model);
        int indexSeparator = text.indexOf(separator, indexFrom);
        String elementText;

        List<String> elements = new ArrayList<>();

        while (indexSeparator != -1) {
            elementText = text.substring(indexFrom, indexSeparator).trim();
            if (elementText.isEmpty()) {
                throw new MismatchException(null, "Collection \"" + text + "\"" + " seems to "
                        + "contain an empty element");
            }
            elements.add(elementText);

            indexFrom = indexSeparator + separator.length();
            indexSeparator = text.indexOf(separator, indexFrom);
        }

        if (end.isEmpty()) {
            elementText = text.substring(indexFrom).trim();
        } else {
            indexSeparator = text.indexOf(end, indexFrom);
            if (indexSeparator == -1 || indexSeparator + end.length() != text.length()) {
                throw new MismatchException(null, "Text doesnt end with end text.");
            }
            elementText = text.substring(indexFrom, indexSeparator).trim();
        }

        if (!elementText.isEmpty()) {
            elements.add(elementText);
        }

        if (elements.size() > 0) {
            if (isSetNode()) {
                Collections.sort(elements);
            }
            nodeCount = insertElement(abstract_nodes, elements.get(0), st, model, nodeCount);
            for (int i = 1; i < elements.size(); i++) {
                tn = new TextNode(null, separator);
                model.insertNodeInto(tn, this, nodeCount);
                nodeCount++;
                String element = elements.get(i);
                nodeCount = insertElement(abstract_nodes, element, st, model, nodeCount);
            }
        }

        tn = new TextNode(null, end);
        model.insertNodeInto(tn, this, nodeCount);
    }

    private boolean isSetNode() { 
        return sequence = false;
    }

    private int insertElement(boolean abstract_nodes, String element, SubstitutionType st, ExpressionTreeModel model, int nodeCount) throws MismatchException, ChangeNotAllowedException {
        if (abstract_nodes) {
            insertSuperTypeNode(element, st, model, nodeCount);
        } else {
            insertValueNode(element, st, model, nodeCount, true);
        }
        nodeCount++;
        return nodeCount;
    }

    private void createSubnodes(Tuple tuple, boolean otherOptions) throws MismatchException, ChangeNotAllowedException {

        CollectionType ct = (CollectionType) tuple.getType();
        List<Value> values = ct.elementsOf(tuple.getItem(0).getValue().toString());
        SubstitutionType st = ct.getElementType();
        CollectionTypeExpression cte = ((CollectionTypeExpression) ct.getOTE());

        this.elementTypeName = st.getName();
        this.begin = cte.getBegin();
        this.separator = cte.getSeparator();
        this.end = cte.getEnd();
        this.sequence = ct.isSequence();

        TextNode tn = new TextNode(null, begin);
        ExpressionTreeModel model = this.getExpressionTreeModel();

        int nodeCount = 0;
        model.insertNodeInto(tn, this, nodeCount);
        nodeCount++;

        for (int i = 0; i < values.size() - 1; i++) {
            insertValueNode(values.get(i).toString(), st, model, nodeCount,
                    otherOptions);
            nodeCount++;

            tn = new TextNode(null, separator);
            model.insertNodeInto(tn, this, nodeCount);
            nodeCount++;
        }
        insertValueNode(values.get(values.size() - 1).toString(), st, model,
                nodeCount, otherOptions);
        nodeCount++;
        tn = new TextNode(null, end);
        model.insertNodeInto(tn, this, nodeCount);
    }

    private void insertValueNode(String elementText, SubstitutionType st,
            ExpressionTreeModel model, int nodeCount, boolean otherOptions)
            throws MismatchException, ChangeNotAllowedException {
        ValueNode vn;
        if (st == null) {
            vn = new ObjectNode(model, null, elementText, elementTypeName, elementRoleName, nodeCount / 2);
        } else if (st instanceof BaseType) {
            vn = new ValueLeaf(null, elementText, elementTypeName, elementRoleName, nodeCount / 2);
        } else { // st is an object type which is present within the object model
            Value value = st.parse(elementText, otherOptions, model.getSource());
            TypeExpression te = ((ObjectType) st).getOTE();
            vn = new ObjectNode(model, null, value, elementRoleName, nodeCount / 2, te);
        }
        model.insertNodeInto(vn, this, nodeCount);
    }

    private void insertSuperTypeNode(String elementText, SubstitutionType st,
            ExpressionTreeModel model, int nodeCount)
            throws MismatchException, ChangeNotAllowedException {
        SuperTypeNode sn;
        sn = new SuperTypeNode(model, null, st.getName(), elementText, null, elementRoleName, nodeCount / 2);
        model.insertNodeInto(sn, this, nodeCount);
    }

    public List<ValueNode> getConcreteValueNodes() {
        List<ValueNode> valueNodes = new ArrayList<>();
        for (int i = 1; i < this.getChildCount(); i += 2) {
            ValueNode vn = (ValueNode) getChildAt(i);
            if (vn instanceof SuperTypeNode) {
                vn = ((SuperTypeNode) vn).getConcreteNode();
            }
            valueNodes.add(vn);
        }
        return valueNodes;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public String getSeparator() {
        return separator;
    }

    @Override
    public void registerAtObjectModel() throws MismatchException, ChangeNotAllowedException, DuplicateException {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        ObjectType ot = om.getObjectType(getTypeName());
        SubstitutionType st = null;
        if (ot == null) {
            st = BaseType.getBaseType(elementTypeName);
            if (st == null) {
                st = om.getObjectType(elementTypeName);
            }
            if (st == null) {
                throw new ChangeNotAllowedException("element type is still not registered");
            }
            ot = om.addCollectionType(this, sequence, getExpressionTreeModel().getSource());
        }
        RequirementModel rm = getExpressionTreeModel().getSource().getModel();
        ot.getFactType().addCollectionFact(this, sequence, st, rm);
    }

    void registerOtherElements()
            throws MismatchException, ChangeNotAllowedException, DuplicateException {
        ExpressionTreeModel model = getExpressionTreeModel();
        for (int i = 1; i < getChildCount(); i += 2) {
            ValueNode valueNode = (ValueNode) getChildAt(i);
            if (!valueNode.isReady()) {
                // replace valueNode with parsed version, if necessary
                ParentNode parsedNode;
                if (valueNode instanceof SuperTypeNode) {
                    model.setUnconditionallyReady((SuperTypeNode) valueNode);
                } else {
                    SubstitutionType st = model.getObjectModel().getSubstitutionType(valueNode.getTypeName());
                    Value value = st.parse(valueNode.getText(), false, model.getSource());
                    if (st instanceof BaseType) {
                    } else {
                        parsedNode = new ObjectNode(model, null, value, ((ISubstitution) valueNode).getRoleName(),
                                0, ((ObjectType) st).getOTE());
                        this.remove(i);
                        model.insertNodeInto(parsedNode, this, i);
                        model.setUnconditionallyReady(parsedNode);
                    }
                }
            }
        }
        model.nodeStructureChanged(this);
    }

    @Override
    public void deregisterAtObjectModel() throws ChangeNotAllowedException {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        om.removeCollection(this);
    }

    @Override
    ObjectNode addObjectNodeAt(int nr, int from, int unto, String typeName,
            String roleName, int roleNumber, boolean otherOptions) throws MismatchException, ChangeNotAllowedException, DuplicateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CollectionType getDefinedType() {
        return (CollectionType) getExpressionTreeModel().getObjectModel().getObjectType(getTypeName());
    }

    @Override
    public String getTypeDescription() {
        return getRoleName() + " : " + getTypeName();
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getChildCount(); i++) {
            sb.append(((ExpressionNode) getChildAt(i)).toString());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getText();
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public int getRoleNumber() {
        return roleNumber;
    }

    private SubstitutionType detectSubstitutionType(ExpressionTreeModel model) {
        ObjectModel om = model.getObjectModel();
        SubstitutionType st = BaseType.getBaseType(elementTypeName);
        if (st == null) {
            st = om.getObjectType(elementTypeName);
        }
        return st;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    int roleNumber(int childNr) {
        return childNr / 2;
    }

    @Override
    public void setRoleNumber(int nr) {
        roleNumber = nr;
    }
}
