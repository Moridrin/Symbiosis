package equa.code.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import equa.code.CodeClass;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.ChangeNotAllowedException;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.traceability.ModelElement;
import equa.util.Naming;

/**
 *
 * @author FrankP
 */
public abstract class Operation extends ModelElement implements IOperation, Comparable<Operation> {

    private static final long serialVersionUID = 1L;
    private final Collection<Exception> exceptions;
    private IFormalPredicate preSpec;
    private IPredicate postSpec;
    private Escape escape;
    private AccessModifier access;
    private boolean unspecified;

    /**
     *
     * @param source
     */
    protected Operation(ObjectType parent, ModelElement source) {
        super(parent, source);
        exceptions = new ArrayList<>();
        preSpec = null;
        postSpec = null;
        escape = null;
        access = AccessModifier.PUBLIC;
    }

    /**
     * creation of a system class method
     *
     * @param objectModel
     * @param source
     */
    Operation(ObjectModel objectModel, ModelElement source) {
        super(objectModel, source);
        exceptions = new ArrayList<>();
        preSpec = null;
        postSpec = null;
        escape = null;
        access = AccessModifier.PUBLIC;
    }

    ObjectModel getObjectModel() {
        return (ObjectModel) getParent().getParent().getParent();
    }

    public abstract CodeClass getCodeClass();

    /**
     * init of pre, post and escape (optional) specification
     */
    public abstract void initSpec();

    public String self() {
        ObjectType ot = (ObjectType) getParent();
        return Naming.withoutCapital(ot.getName());
    }

    static String detectUniqueName(String roleName, List<String> names) {
        String name = roleName;
        if (names.contains(name)) {
            int nr = 1;
            while (names.contains(name + "_" + nr)) {
                nr++;
            }
            name = name + "_" + nr;
        }
        names.add(name);
        return name;
    }

    static String collectionCondition(Relation relation, boolean remove, boolean set) {
        if (remove) {
            int lower = relation.multiplicityLower();
            String normalResult = normalSetResult(relation, true, set);
            if (lower == 0) {
                return normalResult;
            } else {
                return "if (self@Pre.count" + Naming.withCapital(relation.name() + "s") + "() = " + lower + " then (self = self@Pre))"
                        + " else " + normalResult + " endif";
            }
        } else {
            int upper = relation.multiplicityUpper();
            String normalResult = normalSetResult(relation, false, set);
            if (upper == -1) {
                return normalResult;
            } else {
                return "if (self@Pre.count" + Naming.withCapital(relation.name()) + "() = " + upper + " then (self = self@Pre))" + " else "
                        + normalResult + " endif";
            }
        }
    }

    static String normalSetResult(Relation relation, boolean remove, boolean set) {
        StringBuilder result = new StringBuilder();
        String property = relation.roleName() + "s";
        if (set) {
            result.append("self.collect(").append(property).append(") = self@Pre.collect(").append(property).append(")->");
        } else {
            result.append("self->collect(").append(property).append("(index)) = " + "self@Pre->collect(").append(property)
                    .append("(index)" + ").");
        }
        if (remove) {
            result.append("ex");
        } else {
            result.append("in");
        }
        if (set) {
            result.append("cluding(").append(relation.roleName()).append(")");
        } else {
            result.append("cluding(" + "self@Pre->collect(").append(relation.roleName()).append("(index)" + ").asSequence().at(index+1)");
        }

        return result.toString();
    }

    public boolean isClassMethod() {
        return false;
    }

    /**
     *
     * @return the precondition with respect to the state of the corresponding
     * object and given parameters before execution of this behavior
     */
    public IFormalPredicate getPreSpec() {
        return this.preSpec;
    }

    /**
     * changing of the precondition
     *
     * @param preSpec
     */
    public void setPreSpec(IFormalPredicate preSpec) {
        this.preSpec = preSpec;
    }

    public void setEscape(IFormalPredicate condition, IPredicate result) {
        escape = new Escape(condition, result);
    }

    /**
     *
     * @return the postcondition after an abnormal execution (including
     * condition when escape raises)
     */
    public Escape getEscape() {
        return escape;
    }

    /**
     *
     * @return the postcondition concerning the state of the corresponding
     * object after a normal execution
     */
    public IPredicate getPostSpec() {
        return this.postSpec;
    }

    /**
     * changing the (postcondition after a normal execution)
     *
     * @param postSpec
     */
    public void setPostSpec(IPredicate postSpec) {
        this.postSpec = postSpec;
        // publisher.inform(this, null, ("NULL"), this);
    }

    /**
     *
     * @return the specification of this behavioral feature
     */
    public String getSpec() {
        StringBuilder sb = new StringBuilder();

        sb.append("Pre:\t");
        if (preSpec != null) {
            sb.append(getPreSpec().returnValue());
        }
        sb.append(System.lineSeparator());

        sb.append("Post:\t");
        if (escape == null) {
            if (postSpec != null) {
                sb.append(getPostSpec().returnValue());
            }
        } else {
            IFormalPredicate condition = escape.getCondition();
            sb.append("IF ");
            if (condition.isNegated()) {
                String withoutNegation = ((BooleanCall) condition).withoutNegationString();
                sb.append(withoutNegation);
                sb.append(System.lineSeparator());
                sb.append("\tTHEN ");
                if (postSpec != null) {
                    sb.append(getPostSpec().returnValue());
                }
                sb.append(System.lineSeparator());
                sb.append("\tELSE ");
                sb.append(escape.getResult().returnValue());

            } else {
                sb.append(condition.returnValue());
                sb.append(System.lineSeparator());
                sb.append("\tTHEN ");
                sb.append(escape.getResult().returnValue());
                sb.append(System.lineSeparator());
                sb.append("\tELSE ");
                if (postSpec != null) {
                    sb.append(getPostSpec().returnValue());
                }
            }
        }
        sb.append(System.lineSeparator());

        List<String> excStrings = exceptionStrings();
        for (String exc : excStrings) {
            sb.append(exc);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     *
     * @return an iterator over the declared exceptions of this behavioral
     * feature
     */
    public Iterator<Exception> getExceptions() {
        return exceptions.iterator();
    }

    /**
     *
     * @return a list with strings; every string specifies one thrown execption
     */
    public List<String> exceptionStrings() {
        List<String> excStrings = new ArrayList<String>();
        Iterator<Exception> itExc = getExceptions();
        while (itExc.hasNext()) {
            Exception exc = itExc.next();
            excStrings.add("Throws " + exc.getName() + " if " + exc.getTriggerSpec().toString());
        }
        return excStrings;
    }

    /**
     * exc is added to the set of declared exceptions of this behavioral feature
     *
     * @param exc
     */
    public void addException(Exception exc) {
        if (exceptions.contains(exc)) {
            return;
        }
        exceptions.add(exc);
        // publisher.inform(this, null, ("NULL"), this);
    }

    /**
     * exc is removed from the the set of decalred exceptions of this behavioral
     * feature
     *
     * @param exc
     */
    public void removeException(Exception exc) {
        exceptions.remove(exc);
        // publisher.inform(this, null, ("NULL"), this);
    }

    /**
     *
     * @return the name of this behavioral feature
     */
    @Override
    public abstract String getName();

    public abstract String getNameParamTypesAndReturnType();

    public abstract void rename(String newName);

    public abstract List<ImportType> getImports();

    /**
     *
     * @return the order of the different kinds of behavioral features; used
     * during the calculation of compareTo
     */
    public abstract int order();

    /**
     *
     * @return the acces modifier of this feature
     */
    public AccessModifier getAccess() {
        return access;
    }

    /**
     * changing the acces modifier of this feature to access
     *
     * @param access
     */
    public void setAccessModifier(AccessModifier access) {
        this.access = access;
    }

    @Override
    public void remove() throws ChangeNotAllowedException {
//        super.remove();
//        ((ObjectType) getParent()).getCodeClass().removeOperation(this);
    }


    /**
     *
     * @param l
     * @return the complete source code of this operation; it includes signature
     * and api-specification and it is expressed in language
     */
    public abstract IndentedList getCode(Language l);

    public boolean isUnspecified() {
        return unspecified;
    }

    public void setUnspecified(boolean unspecified) {
        this.unspecified = unspecified;
    }

    public boolean isFinal() {
        return getCodeClass().hasUnspecifiedOperation() && !isUnspecified() && !(this instanceof Constructor);
    }

    public abstract boolean adaptName(CodeClass codeClass);

    public void seal(IndentedList body) {
        //Default is nothing. Some operations support this, not all
    }

    @Override
    public boolean isManuallyCreated() {
        return false;
    }
}
