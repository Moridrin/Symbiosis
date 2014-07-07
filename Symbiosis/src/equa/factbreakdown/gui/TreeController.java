/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown.gui;

import java.awt.Frame;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import equa.factbreakdown.CollectionNode;
import equa.factbreakdown.ExpressionNode;
import equa.factbreakdown.ExpressionTreeModel;
import equa.factbreakdown.FactNode;
import equa.factbreakdown.ObjectNode;
import equa.factbreakdown.ParentNode;
import equa.factbreakdown.SuperTypeNode;
import equa.factbreakdown.TextNode;
import equa.factbreakdown.ValueLeaf;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.CollectionType;
import equa.meta.objectmodel.CollectionTypeExpression;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.ParseResult;
import equa.meta.objectmodel.Role;
import equa.meta.objectmodel.SubstitutionType;
import equa.meta.objectmodel.TypeExpression;
import equa.meta.objectmodel.Value;
import equa.meta.traceability.ExternalInput;
import equa.project.ProjectRole;

/**
 *
 * @author FrankP
 */
public class TreeController {

    private final Frame frame;
    private final ExpressionTreeModel expressionTreeModel;
    private TextNode selectedTextNode;
    private FactNode root;

    public TreeController(Frame frame, ExpressionTreeModel model) {
        this.frame = frame;
        this.expressionTreeModel = model;
        selectedTextNode = null;
        root = null;
    }

    public Frame getFrame() {
        return frame;
    }

    public ObjectModel getObjectModel() {
        return expressionTreeModel.getObjectModel();
    }

    public ParentNode getRoot() {
        return (ParentNode) expressionTreeModel.getRoot();
    }

    public void executeRootDialog(Point screenPosition, String expression) {
        int from = 0;
        int unto = expression.length();

        // ask for typename of fact root 
        ExpressionTreeRootDialog typeNameDialog = new ExpressionTreeRootDialog(this, expression);

        typeNameDialog.setLocation(screenPosition.x + 200, screenPosition.y);
        typeNameDialog.setTitle("Please Enter Name of Fact Type");
        typeNameDialog.setVisible(true);

        String typeName = typeNameDialog.getTypeName();
        if (typeName.isEmpty()) {
            return;
        }

        FactType ft = getObjectModel().getFactType(typeName);
        root = null;

        try {
            if (typeNameDialog.isObjectType()) {
                if (ft != null && !ft.isObjectType()) {
                    // converting a factype into an objecttype
                    ft.duplicateTE();
                    MatchDialog dialog = openMatchDialog("Object type expression " + ft.getName(),
                            screenPosition, ft, ft.getObjectType().getOTE(), expression,
                            0, true, frame);
                    if (dialog != null && dialog.expressionChanged()) {
                        expression = dialog.getExpression();
                    }
                }
                root = expressionTreeModel.createObjectRoot(expression, typeName);
            } else {
                if (ft != null && ft.isObjectType() && ft.getFTE() == null) {
                    // adding fact at known objecttype without FTE
                    ft.duplicateTE();
                    MatchDialog dialog = openMatchDialog("Fact type expression " + ft.getName(),
                            screenPosition, ft, ft.getFTE(), expression,
                            0, false, frame);
                    if (dialog != null && dialog.expressionChanged()) {
                        expression = dialog.getExpression();
                    }
                }
                root = expressionTreeModel.createFactRoot(expression, typeName);
            }
            scanSuspiciousNodes(root);

        } catch (MismatchException exc) {
            if (ft != null && ft.isParsable()) {
                if (typeNameDialog.isObjectType()) {
                    executeMatchDialog("Object type expression " + ft.getName(),
                            ft, ft.getObjectType().getOTE(), expression, exc.getMismatchPosition(),
                            screenPosition, from, unto, typeName, null, null);
                } else {
                    executeMatchDialog("Fact type expression " + ft.getName(),
                            ft, ft.getFTE(), expression, exc.getMismatchPosition(),
                            screenPosition, from, unto, typeName, null, null);
                }
            } else {
                JOptionPane.showMessageDialog(frame, exc.getMessage());
            }
        } catch (ChangeNotAllowedException | DuplicateException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }
    }

    void scanSuspiciousNodes(ParentNode factNode) {
        for (int i = 1; i < factNode.getChildCount(); i += 2) {
            ExpressionNode node = (ExpressionNode) factNode.getChildAt(i);
            if (node instanceof ValueLeaf) {
                if (node.getText().indexOf(" ") >= 0) {
                    ValueLeaf obscureLeaf = (ValueLeaf) node;
                    ObscureValueLeafDialog confirmationDialog
                            = new ObscureValueLeafDialog(frame, true,
                                    obscureLeaf.getText(), obscureLeaf.getRoleName(), obscureLeaf.getTypeName());

                    if (!confirmationDialog.isConfirmed()) {
                        offerMatchDialog(obscureLeaf);
                        return;
                    }

                }
            } else if (node instanceof ParentNode) {
                scanSuspiciousNodes((ParentNode) node);
            }
        }
    }

    private void offerMatchDialog(ValueLeaf obscureLeaf) {
        ParentNode parentNode = obscureLeaf.getParent();
        FactType ft = expressionTreeModel.getObjectModel().getFactType(parentNode.getTypeName());
        if (ft != null) {
            TypeExpression te;
            boolean objectExpression;
            if (ft.isObjectType()) {
                te = ft.getObjectType().getOTE();
                objectExpression = true;
            } else {
                te = ft.getFTE();
                objectExpression = false;
            }
            ProjectRole currentUser = expressionTreeModel.getCurrentUser();
            new MatchDialog(ft, te, parentNode.getText(), 0, objectExpression, currentUser, frame);
        }
    }

    public void executeSubstitutionTypeDialog(Point screenPosition, String expression,
            TextNode selected, int from, int unto) {

        this.selectedTextNode = selected;

        SubstitutionTypeDialog typeDialog = new SubstitutionTypeDialog(this, expression);
        typeDialog.setLocation(screenPosition.x + 200, screenPosition.y);
        typeDialog.setTitle(expression);
        typeDialog.setVisible(true);

        if (typeDialog.getTypeName().isEmpty()) {
            return;
        }

        String typeName = typeDialog.getTypeName();
        ParentNode pn = selectedTextNode.getParent();
        if (pn != null) {
            ParentNode root = pn.getRoot();
            if (root.getTypeName().equalsIgnoreCase(typeName)) {
                JOptionPane.showMessageDialog(frame, "Warning: " + typeName + " equals the name of the root.");
            }
        }
        String roleName = typeDialog.getRoleName();

        if (pn != null) {
            if (pn.existingRoleName(roleName)) {
                JOptionPane.showMessageDialog(frame, "Attention: " + roleName + " is already used at "
                        + pn.getTypeName() + ".");
            }
        }
        String superTypeName = typeDialog.getSupertypeName();
        SubstitutionType st = expressionTreeModel.getObjectModel().getSubstitutionType(typeName);
        boolean isCollection = st != null && st instanceof CollectionType;

        if (typeDialog.isCollection() || isCollection) {
            String elementType = typeDialog.getElementType();
            addCollectionNode(typeName, expression, screenPosition, from, unto, roleName,
                    typeDialog.getBegin(), typeDialog.getSeparator(),
                    typeDialog.getEnd(), elementType,
                    typeDialog.getElementRole(),
                    typeDialog.isList());

        } else if (typeDialog.isBaseType()) {
            addValueLeaf(from, unto, typeName, roleName);
        } else if (typeDialog.getSupertypeName().isEmpty()) {
            if (selectedTextNode.getParent() instanceof SuperTypeNode) {
                // adding object node with existing inheritance
                addObjectNode(typeName, expression, screenPosition, from, unto,
                        roleName, selectedTextNode.getParent().getTypeName(), true);
            } else {
                // adding object node without inheritance
                addObjectNode(typeName, expression, screenPosition, from, unto, roleName, null, true);
            }
        } else {
            // adding object node with inheritance 
            addSuperTypeNode(typeName, expression, screenPosition, from, unto, roleName, superTypeName);
        }
    }

    private void addValueLeaf(int from, int unto, String typeName, String roleName) {
        if (getRoot() == null) {
            JOptionPane.showMessageDialog(frame, "BaseValue cannot be the root of "
                    + " expression tree.");
        } else {
            try {
                expressionTreeModel.addValueLeafAt((FactNode) selectedTextNode.getParent(),
                        selectedTextNode.getChildIndex(), from, unto, typeName, roleName);
            } catch (MismatchException | ChangeNotAllowedException | DuplicateException | ClassCastException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }
        //checkObscureValueLeafs(-1);
    }

    private void addObjectNode(String typeName, String expression, Point screenPosition,
            int from, int unto, String roleName, String superTypeName, boolean otherOptions) {
        FactType ft = getObjectModel().getFactType(typeName);

        if (ft != null && !ft.isObjectType()) {
            // facttype should be an object type
            ft.objectify();
        }

        try {
            if (getRoot() == null) {
                // expressionTreeModel.createObject(expression, typeName, roleName);
                throw new RuntimeException("root?");
            } else {
                int nr = selectedTextNode.getChildIndex();
                ObjectNode objectNode = expressionTreeModel.addObjectNodeAt(selectedTextNode.getParent(),
                        nr, from, unto, typeName, roleName, otherOptions);

                scanSuspiciousNodes(objectNode);
            }
        } catch (MismatchException ex) {
            TypeExpression ote = ft.getObjectType().getOTE();
            if (ft.getObjectType().isParsable()) {
                executeMatchDialog("Object type expression " + ft.getName(),
                        ft, ote,
                        expression, 0, screenPosition, from, unto, typeName, roleName, superTypeName);
            } else {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        } catch (ChangeNotAllowedException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        } catch (DuplicateException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }
        //checkObscureValueLeafs(-1);
    }

    private void addCollectionNode(String typeName, String expression, Point screenPosition,
            int from, int unto, String roleName, String begin, String separator,
            String end, String elementType, String elementRoleName, boolean sequence) {
        FactType ft = getObjectModel().getFactType(typeName);

        try {
            if (getRoot() == null) {
                // expressionTreeModel.createObject(expression, typeName, roleName);
                throw new RuntimeException("root?");
            } else {
                int nr = selectedTextNode.getChildIndex();
                CollectionNode cn = expressionTreeModel.addCollectionNodeAt((FactNode) selectedTextNode.getParent(),
                        nr, from, unto, typeName, roleName, begin, separator, end,
                        elementType, elementRoleName, sequence, true);
                scanSuspiciousNodes(cn);
            }
        } catch (MismatchException ex) {
            TypeExpression ote = ft.getObjectType().getOTE();
            if (ote.isParsable()) {
                CollectionElementDialog dialog = new CollectionElementDialog(this.getFrame(), true,
                        expression, begin, separator, end, elementType, elementRoleName);
                dialog.setVisible(true);
                if (!dialog.getSeparator().isEmpty() && !dialog.getElementType().isEmpty()) {
                    ObjectModel om = expressionTreeModel.getObjectModel();
                    CollectionType ct = (CollectionType) om.getObjectType(typeName);
                    CollectionTypeExpression cte = (CollectionTypeExpression) ct.getOTE();
                    ExternalInput input = new ExternalInput("modification caused by mismatch with other fact",
                            om.getProject().getCurrentUser());
                    try {
                        cte.setBeginEnd(dialog.getBegin(), dialog.getEnd(), input);
                        cte.setSeparator(dialog.getSeparator());

                    } catch (ChangeNotAllowedException ex1) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        } catch (ChangeNotAllowedException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        } catch (DuplicateException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }

    }

    private void addSuperTypeNode(String typeName, String expression, Point screenPosition,
            int from, int unto, String roleName, String supertype) {
        if (getRoot() == null) {
            JOptionPane.showMessageDialog(frame, "SuperType as root of "
                    + " expression tree is not yet implemented.");
            return;
        }

        FactType ft = getObjectModel().getFactType(typeName);

        if (ft != null && !ft.isObjectType()) {
            // facttype should be an object type
            ft.objectify();
        }

        // the rolename is related to the concrete subtype:
        if (roleName.equalsIgnoreCase(typeName)) {
            roleName = "";
        }

        try {
            SuperTypeNode supertypeNode = expressionTreeModel.addSuperTypeNodeAt((FactNode) selectedTextNode.getParent(),
                    selectedTextNode.getChildIndex(), supertype,
                    from, unto, roleName, typeName);
            scanSuspiciousNodes(supertypeNode.getConcreteNode());
        } catch (MismatchException ex) {
            // adding use of supertype at known facttype with a mismatch on type expression
            TypeExpression ote = ft.getObjectType().getOTE();
            if (ote.isParsable()) {
                executeMatchDialog("Object type expression " + ft.getName(),
                        ft, ote,
                        expression, 0, screenPosition, from, unto, typeName, roleName,
                        supertype);
            } else {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }

        } catch (ChangeNotAllowedException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        } catch (DuplicateException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        }
        //checkObscureValueLeafs(-1);
    }

    /**
     *
     * @param title
     * @param ft
     * @param te te.isParsable()
     * @param expression
     * @param mismatchPosition
     * @param p
     * @param from
     * @param unto
     * @param typeName
     * @param roleName
     * @param superTypeName
     */
    private void executeMatchDialog(String title, FactType ft, TypeExpression te,
            String expression, int mismatchPosition,
            Point p, int from, int unto, String typeName,
            String roleName, String superTypeName) {

        boolean objectExpression = roleName != null;
        MatchDialog dialog = openMatchDialog(title, p, ft, te, expression,
                mismatchPosition, objectExpression, frame);

        if (dialog != null) {
            String newExpression = dialog.getExpression();

            try {
                if (getRoot() == null) {
                    root = expressionTreeModel.createFactRoot(newExpression, typeName);
                    scanSuspiciousNodes(root);
                    //checkObscureValueLeafs(-1);
                } else {
                    // sub node
                    expressionTreeModel.replace(selectedTextNode, from, unto, newExpression);
                    FactNode parentNode = (FactNode) selectedTextNode.getParent();
                    int nr = selectedTextNode.getChildIndex();
                    if (superTypeName == null || superTypeName.isEmpty()) {
                        ObjectNode objectNode = expressionTreeModel.addObjectNodeAt(parentNode,
                                nr, from,
                                from + newExpression.length(), typeName, roleName, dialog.getExpressionParts(),
                                dialog.getSubstitutionSequence());
                        scanSuspiciousNodes(objectNode);
                    } else {
                        SuperTypeNode supertypeNode = expressionTreeModel.addSuperTypeNodeAt(parentNode,
                                nr, superTypeName, from,
                                from + newExpression.length(), roleName, typeName);
                        scanSuspiciousNodes(supertypeNode.getConcreteNode());
                    }
                }
            } catch (MismatchException | ChangeNotAllowedException | DuplicateException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }
    }

    private MatchDialog openMatchDialog(String title, Point p, FactType ft, TypeExpression te,
            String expression, int mismatchPosition, boolean objectExpression,
            Frame frame) {

        MatchDialog matchDialog = new MatchDialog(ft,
                te, expression, mismatchPosition, objectExpression, expressionTreeModel.getCurrentUser(), frame);
        matchDialog.setLocation(p.x, p.y);
        matchDialog.setSize(1000, 400);
        matchDialog.setTitle(title);
        matchDialog.setVisible(true);

        if (matchDialog.match()) {

            List<String> substitutionStrings = matchDialog.getSubstitutionStrings();
            int index = 0;
            while (index < ft.size()) {
                Role role = ft.getRole(te.getRoleNumber(index));
                if (role instanceof ObjectRole) {
                    ObjectType ot = (ObjectType) role.getSubstitutionType();
                    try {
                        ParseResult parseResult = ot.getOTE().parse(substitutionStrings.get(index),
                                true, expressionTreeModel.getSource());
                        if (parseResult.otherParsingPossible()) {
                            String message = makeMessage(ot.getOTE(), parseResult);
                            int option = JOptionPane.showConfirmDialog(frame, message,
                                    "Is the parsing correct?",
                                    JOptionPane.YES_NO_OPTION);
                            if (option == JOptionPane.NO_OPTION) {
                                return null;
                                // TODO: incorrect parsing
                                // TODO: don't forget: deleting of values of parsingResult in concerning populations
                            }
                        }
                    } catch (MismatchException ex) {
                        if (!ot.isParsable() /**
                                 * || ot.hasAbstractRoles()*
                                 */
                                ) {
                        } else {
                            MatchDialog submatchDialog = openMatchDialog("Object type expression " + ot.getName(),
                                    p, ot.getFactType(), ot.getOTE(), substitutionStrings.get(index), 0, true, frame);
                            if (submatchDialog != null) {
                                submatchDialog.setLocation(p.x, p.y);
                                submatchDialog.setSize(1000, 400);
                                submatchDialog.setVisible(true);
                                if (!submatchDialog.match()) {
                                    return null;
                                } else {
                                    substitutionStrings.set(index, submatchDialog.getExpression());
                                }
                            } else {
                                return null;
                            }
                        }
                    }
                }
                index++;
            }
            matchDialog.setSubstitutionStrings(substitutionStrings);
            return matchDialog;
        } else {
            return null;
        }
    }

    private String makeMessage(TypeExpression te, ParseResult parseResult) {
        Iterator<Value> itValues = parseResult.getValues().iterator();
        StringBuilder sb = new StringBuilder();
        Iterator<String> itConstants = te.constants();
        while (itValues.hasNext()) {
            sb.append(itConstants.next()).append("<");
            Value value = itValues.next();
            sb.append(value.toString()).append(" : ").append(value.getType().getName()).append(">");
        }
        sb.append(itConstants.next());
        return sb.toString();
    }
}
