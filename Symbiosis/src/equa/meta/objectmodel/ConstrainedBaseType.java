/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import equa.code.operations.BooleanCall;
import equa.code.operations.Constructor;
import equa.code.operations.IBooleanOperation;
import equa.code.operations.IFormalPredicate;
import equa.code.operations.IsCorrectValueMethod;
import equa.code.operations.Method;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ModelElement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * a constrained basetype is played by an objecttype; a constrained basetype
 * offers the opportunity to add value constraints on the underlying basetype
 *
 * @author frankpeeters
 */
public class ConstrainedBaseType extends ObjectType implements ListModel<Object> {

    private static final long serialVersionUID = 1L;
    private transient EventListenerList listenerList;
    private ValueConstraint vc;

    ConstrainedBaseType(FactType parent, RuleRequirement rule) {
        super(parent, ObjectType.CONSTRAINED_BASETYPE);
        vc = new ValueConstraint(getFactType(), rule);
        listenerList = new EventListenerList();
        super.setValueType(true);
    }

    @Override
    public void remove() {
        super.remove();
        if (vc != null) {
            try {
                vc.remove();
            } catch (ChangeNotAllowedException ex) {
                Logger.getLogger(ConstrainedBaseType.class.getName()).log(Level.SEVERE, null, ex);
            }
            vc = null;
        }
    }

    @Override
    public void remove(ModelElement member) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void deleteVc() {
        vc = null;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        listenerList = new EventListenerList();
    }

    @Override
    public boolean isSuitableAsIndex() {
        BaseType bt = getBaseType();
        if ((bt.equals(BaseType.NATURAL) || bt.equals(BaseType.INTEGER) || bt.equals(BaseType.CHARACTER)) && vc.isOneRange()) {
            return true;
        } else if ((bt.equals(BaseType.NATURAL) || bt.equals(BaseType.CHARACTER)) && vc.isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public int getSize() {
        return vc.getSize();
    }

    @Override
    public Object getElementAt(int i) {
        return vc.getElementAt(i);
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

    public ValueConstraint getValueConstraint() {
        return vc;
    }

    /**
     *
     * @return the underlying basetype
     */
    public BaseType getBaseType() {
        return (BaseType) getFactType().getRole(0).getSubstitutionType();
    }

    @Override
    public Value parse(String expression, boolean otherOptions, FactRequirement source) throws MismatchException {
        if (!vc.contains(expression)) {
            throw new MismatchException(null, expression + " doesn\'t belong to " + getName());
        }

        return getFactType().parse(expression.trim(), getOTE(), otherOptions, source);
    }

    @Override
    public void setValueType(boolean valueType) {
    }

    @Override
    public void addSuperType(ObjectType superType) {
    }

    @Override
    public void setAbstract(boolean _abstract) {
    }

    @Override
    void generateMethods() {
        List<Relation> relations = codeClass.getRelations();
        try {
            generateMethods(relations);
            if (!isLight()) {
                generatePropertiesMethod();
            }
            generateCorrectValueMethod(relations.get(0));
            generateToStringMethod();
            generateEqualsMethod();
            generateCompareToMethod();

        } catch (DuplicateException exc) {
            exc.printStackTrace();
        }
    }

    void generateConstructor(Relation relation) throws DuplicateException {
        List<Relation> relations = new ArrayList<>();
        relations.add(relation);
        Constructor constructor = new Constructor(relations, this, this.getFactType());
        IBooleanOperation isCorrectValue = (IBooleanOperation) getCodeClass().getOperation("isCorrectValue");
        IFormalPredicate precondition = new BooleanCall(isCorrectValue, false);
        constructor.setPreSpec(precondition);
        codeClass.addOperation(constructor);
    }

    void generateCorrectValueMethod(Relation relation) throws DuplicateException {
        Method isCorrectValueMethod = new IsCorrectValueMethod(relation, this);
        codeClass.addOperation(isCorrectValueMethod);
    }

    /**
     *
     * @return enumeration of values and ranges
     */
    public String valuesString() {
        return vc.valuesString();
    }

    void checkSyntaxis(String value) throws MismatchException {

        if (vc == null) {
            throw new MismatchException(null, "constrained base type is empty");
        } else {
            vc.checkSyntax(value);
        }
    }

    @Override
    public boolean isNumber() {
        return getBaseType().isNumber();
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
