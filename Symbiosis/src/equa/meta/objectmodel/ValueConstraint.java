/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import equa.meta.ChangeNotAllowedException;
import equa.meta.MismatchException;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.ExternalInput;
import equa.project.ProjectRole;

/**
 *
 * @author frankpeeters
 */
public class ValueConstraint extends StaticConstraint {

    private static final long serialVersionUID = 1L;
    private final Set<BaseValue> values;
    private final List<Range> ranges;

    public ValueConstraint(FactType parent, RuleRequirement rule) {
        super(parent, rule);
        values = new TreeSet<>();
        ranges = new ArrayList<>(1);
    }

    ConstrainedBaseType cbt() {
        return (ConstrainedBaseType) ((FactType) getParent()).getObjectType();
    }

    public int getSize() {
        return values.size() + ranges.size();
    }

    public int countAcceptedElements() {
        int count = 0;
        for (Range range : ranges) {
            int c = range.countAcceptedElements();
            if (c == -1) {
                return -1;
            } else {
                count += c;
            }
        }
        count += values.size();
        return count;
    }

    public Object getElementAt(int i) {
        if (i < values.size()) {
            Iterator<BaseValue> it = values.iterator();
            for (int j = 0; j < i; j++) {
                it.next();
            }
            return it.next();
        } else {
            return ranges.get(i - values.size());
        }

    }

    /**
     *
     * @param stringvalue
     * @return true if stringvalue satisfies one of the value constraints,
     * otherwise false
     */
    public boolean contains(String stringvalue) {

        BaseValue value;
        try {
            value = new BaseValue(stringvalue, cbt().getBaseType());
        } catch (MismatchException ex) {
            return false;
        }
        if (values.contains(value)) {
            return true;
        }
        for (Range range : ranges) {
            if (range.contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return an iterator over all individual values; values within the ranges
     * are not included
     */
    public Iterator<BaseValue> values() {
        return values.iterator();
    }

    /**
     *
     * @return an iterator over all ranges of this constrained basetype
     */
    public Iterator<Range> ranges() {
        return ranges.iterator();
    }

    public void addValue(String stringvalue) throws ChangeNotAllowedException {
        BaseValue value;
        try {
            value = new BaseValue(stringvalue, cbt().getBaseType());
        } catch (MismatchException ex) {
            throw new ChangeNotAllowedException("value doesn't belong to the "
                    + "concerning basetype");
        }
        if (value.getType() != cbt().getBaseType()) {
            throw new ChangeNotAllowedException(
                    value.toString() + " is not an element of " + getName());
        }
        for (Range range : ranges) {
            if (range.contains(value)) {
                throw new ChangeNotAllowedException(
                        value.toString() + " is already an element of " + range.toString());
            }
        }

        values.add(value);
        notifyRuleRequirement();

        cbt().fireListChanged();
    }

    private void notifyRuleRequirement() {
        //assumption: this constraint possesses only one source, especially a rule requirement
        RuleRequirement rule = (RuleRequirement) creationSource();
        FactType ft = (FactType) getParent();
        ObjectModel om = (ObjectModel) ft.getParent();
        ProjectRole projectRole = om.getProject().getCurrentUser();
        ExternalInput source = new ExternalInput("", projectRole);
        try {
            rule.setText(source, "Value constraint in behalf of "
                    + ft.getName() + ": "
                    + valuesString());
        } catch (ChangeNotAllowedException exc) {
            throw new RuntimeException("Change Not Allowed: " + exc.getMessage());
        }
    }

    /**
     * adding of range if possible, otherwise an exception will be raised
     *
     * @param range
     * @throws ChangeNotAllowedException
     */
    public void addRange(Range range) throws ChangeNotAllowedException {
        if (range.getLower().getType() != cbt().getBaseType()) {
            throw new ChangeNotAllowedException(
                    range.toString() + " is not subset of " + getName());
        }
        for (Range r : ranges) {
            if (r.hasOverlapWith(range)) {
                throw new ChangeNotAllowedException(
                        r.toString() + " has overlap with " + range.toString());
            }
        }
        for (BaseValue value : values) {
            if (range.contains(value)) {
                throw new ChangeNotAllowedException(
                        range.toString() + " contains " + value.toString());
            }
        }
        ranges.add(range);
        notifyRuleRequirement();

        cbt().fireListChanged();
    }

    public void removeValue(String stringvalue) {
        BaseValue value;
        int rangeDots = stringvalue.indexOf("..");
        if (rangeDots >= 0) {
            Range r;
            try {
                r = new Range(new BaseValue(stringvalue.substring(0, rangeDots), cbt().getBaseType()),
                        new BaseValue(stringvalue.substring(rangeDots + 2), cbt().getBaseType()));
                removeRange(r);

            } catch (MismatchException ex) {
                Logger.getLogger(ValueConstraint.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            try {
                value = new BaseValue(stringvalue, cbt().getBaseType());
            } catch (MismatchException ex) {
                return;
            }
            values.remove(value);
            notifyRuleRequirement();
            cbt().fireListChanged();
        }
    }

    public void removeRange(Range range) {
        ranges.remove(range);
        notifyRuleRequirement();
        cbt().fireListChanged();
    }

    /**
     *
     * @return enumeration of values and ranges
     */
    public String valuesString() {
        StringBuilder sb = new StringBuilder();
        for (BaseValue value : values) {
            sb.append(value);
            sb.append(",");
        }

        for (Range range : ranges) {
            sb.append(range.toString());
            sb.append(",");
        }
        if (sb.length() == 0) {
            return "none";
        } else {
            return sb.substring(0, sb.length() - 1);
        }
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
        cbt().deleteVc();
        super.remove();
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public String getAbbreviationCode() {
        return "v";
    }

    public boolean isEnum() {
        return ranges.isEmpty();
    }

    void checkSyntax(String value) throws MismatchException {
        if (values.isEmpty() && ranges.isEmpty()) {
            return;
        }

        cbt().getBaseType().checkSyntaxis(value);
        BaseValue bv = new BaseValue(value, cbt().getBaseType());
        for (BaseValue baseValue : values) {
            if (baseValue.equals(bv)) {
                return;
            }
        }
        for (Range range : ranges) {
            if (range.contains(bv)) {
                return;
            }
        }
        throw new MismatchException(null, "value doesn't fulfill value constraint");
    }

    boolean isOneRange() {
        return values.isEmpty() && ranges.size() == 1;
    }

    boolean isEmpty() {
        return values.isEmpty() && ranges.isEmpty();
    }

    @Override
    public FactType getFactType() {
        return (FactType) getParent();
    }

    @Override
    public String getRequirementText() {
        return cbt().getBaseType() + "-values of <"
                + getFactType().getName()
                + "> are restricted to: "
                + valuesString()
                + ".";
    }

    @Override
    public boolean isRealized() {
        return true;
    }
}
