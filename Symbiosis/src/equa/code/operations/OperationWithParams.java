package equa.code.operations;

import static equa.code.CodeNames.SYSTEM_CLASS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import equa.code.CodeClass;
import equa.meta.DuplicateException;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.traceability.ModelElement;
import equa.util.Naming;

/**
 *
 * @author FrankP
 */
public abstract class OperationWithParams extends Operation {

    private static final long serialVersionUID = 1L;
    private List<Param> params;

    /**
     *
     * @param name
     * @param params
     */
    OperationWithParams(ObjectType parent, List<Param> params, ModelElement source) {
        super(parent, source);

        this.params = params;

    }

    OperationWithParams(ObjectModel parent, List<Param> params, ModelElement source) {
        super(parent, source);
        this.params = params;
    }

    @Override
    public Call call() {
        return call(params);
    }

    @Override
    public String callString() {
        if (this.isClassMethod()) {
            return ((ObjectType) getCodeClass().getParent()).getName() + "." + getName() + paramCallList();
        } else {
            return getName() + paramCallList();
        }
    }

    static String paramlist(List<Param> params) {
        StringBuilder sb = new StringBuilder();
        for (Param param : params) {
            sb.append(param.getName());
            sb.append(",");
        }
        if (sb.length() == 0) {
            return "";
        } else {
            return sb.substring(0, sb.length() - 1);
        }

    }

    @Override
    public String callString(List<? extends ActualParam> actualParams) {
        StringBuilder sb = new StringBuilder();
        if (isClassMethod()) {
            Object parent = getCodeClass().getParent();
            if (parent instanceof ObjectModel) {
                sb.append(SYSTEM_CLASS);
            } else {
                sb.append(((ObjectType) parent).getName());
            }
            sb.append(".");
        }
        sb.append(getName());
        sb.append("(");
        Iterator<? extends ActualParam> it = actualParams.iterator();
        if (it.hasNext()) {
            sb.append(it.next().callString());
        }
        while (it.hasNext()) {
            sb.append(",");

            sb.append(it.next().callString());
        }
        sb.append(")");
        return sb.toString();

    }

    @Override
    public Call call(List<? extends ActualParam> actualParams) {
        return new Call(this, actualParams);
    }

    public List<Param> getParams() {
    	if (params != null) {
    		return Collections.unmodifiableList(params);
    	} else {
    		return Collections.emptyList();
    	}
    }

    protected void setParams(List<Param> params) {
        this.params = params;
    }

    public List<String> getParamNameList() {
        List<String> names = new ArrayList<>();
        for (Param param : params) {
            names.add(param.getName());
        }

        return names;
    }

    /**
     * adding of params at the end of the actual list of parameters
     *
     * @param params
     * @throws DuplicateException if the adding of params would result in a
     * duplication of this behavioral feature or a name-conflict between the
     * params of this behavioral feature
     */
    public void addParams(List<Param> params) throws DuplicateException {
        if (!namesAreDifferent(params)) {
            throw new DuplicateException(("ALL PARAMETERS MUST HAVE A DIFFERENT NAME"));
        } else {

            ((CodeClass) getParent()).removeOperation(this);
            List<Param> backup = this.params;
            this.params = new ArrayList<>(backup);
            this.params.addAll(params);
            if (((CodeClass) getParent()).addOperation(this)) {
                this.params = backup;
                ((CodeClass) getParent()).addOperation(this);
                throw new DuplicateException("feature with the same signature already"
                        + " exists");
            }
        }
    }

    /**
     * removing of params out of this behavioral feature
     *
     * @param params
     * @throws DuplicateException if the removing of params would result in a
     * duplication of this behavioral feature
     */
    public void removeParams(List<Param> params) throws DuplicateException {
        // approach:
        // A) remove this feature as a whole B) change the removed feature
        // C) add the changed feature D) if something got wrong: rollback

        // A
        ((CodeClass) getParent()).removeOperation(this);

        // preparation with respect to D
        List<Param> backup = new ArrayList<>(this.params);
        // B
        this.params.removeAll(params);
        // C
        if (((CodeClass) getParent()).addOperation(this)) {
            // D
            this.params = new ArrayList<>(backup);
            ((CodeClass) getParent()).addOperation(this);
            throw new DuplicateException("behavioral feature with removed parameters"
                    + " already exists");
        }

    }

    /**
     * if name of param exists in the set of all names of parameters of this
     * behavior then the renaming will be rejected otherwise the renaming will
     * succeed
     *
     * @param param parameter of the parent of this feature
     * @param name is an identifier
     * @throws DuplicateException if name of param exists in the set of all
     * names of parameters of this behavioral feature
     */
    public void setParamName(Param param, String name)
            throws DuplicateException {
        if (!Naming.isIdentifier(name)) {
            throw new RuntimeException(("NAME OF ")
                    + ("PARAMETER ISN'T AN IDENTIFIER"));
        }

        if (!params.contains(param)) {
            throw new RuntimeException(("PARAMETER IS ")
                    + ("UNKNOWN AT THIS BEHAVIORAL FEATURE"));
        }

        for (Param p : params) {
            if (p.getName().equalsIgnoreCase(name)) {
                throw new DuplicateException(("THERE EXISTS ALREADY A PARAMETER ")
                        + ("WITH THE CHOOSEN (CASE-INSENSITIVE) NAME"));
            }
        }
        param.setName(name);
        //publisher.inform(this, null, ("NULL"), this);
    }

    /**
     * changing of the type of param
     *
     * @param param parameter of the parent of this feature
     * @param type
     * @throws DuplicateException if new signature of this feature exists in the
     * set of all signatures of the parent of this behavioral feature
     */
    public void setParamType(Param param, STorCT type)
            throws DuplicateException {
        // approach:
        // A) remove this feature as a whole B) change the removed feature
        // C) add the changed feature D) if something got wrong: rollback

        // A
        ((CodeClass) getParent()).removeOperation(this);

        // preparation with respect to D
        STorCT backup = param.getType();

        // B
        param.setType(type);
        // C
        if (((CodeClass) getParent()).addOperation(this)) {

            // D
            param.setType(backup);
            ((CodeClass) getParent()).addOperation(this);
            throw new DuplicateException("behavioral feaure with proposed parameterchange"
                    + " already exists.");
        }
    }

    /**
     * inserting param from position f just before the element on position t (in
     * original state); but if t=count of paramaters then the parameter is moved
     * to the back end.
     *
     * @param f 0 <= f < count of parameters of this behavioral feature @param t
     * 0 <= t <= count of parameters o f this behavioral feature @throws
     * DuplicateNameException if the mov ing would leads to a signature-
     * conflict with another behavioral feature of this class
     */
    public void moveParam(int f, int t) throws DuplicateException {
        if (f < 0 || f >= params.size()) {
            throw new RuntimeException(("PARAM WITH ")
                    + ("THIS NUMBER DOESN'T EXIST"));
        }
        if (t < 0 || t > params.size()) {
            throw new RuntimeException(("MOVING OF ")
                    + ("PARAM TOWARDS AN ILLEGAL POSITION"));
        }

        // approach:
        // A) remove this feature as a whole B) change the removed feature
        // C) add the changed feature D) if something got wrong: rollback
        if (f == t) {
            return;
        }

        // A
        ((CodeClass) getParent()).removeOperation(this);

        // preparation with respect to D
        List<Param> backup = new ArrayList<>(this.params);

        // B
        Param param = params.remove(f);
        if (f < t) {
            params.add(t - 1, param);
        } else { // f > t
            params.add(t, param);
        }
        // C
        if (((CodeClass) getParent()).addOperation(this)) {

            // D
            this.params = new ArrayList<>(backup);
            ((CodeClass) getParent()).addOperation(this);
            throw new DuplicateException("behavioral feature with proposed "
                    + "excange of parameters already exists");
        }
    }

    private boolean namesAreDifferent(List<Param> params) {
        TreeSet<String> set = new TreeSet<>();
        for (Param param : this.params) {
            set.add(param.getName());
        }
        for (Param param : params) {
            if (set.contains(param.getName())) {
                return false;
            } else {
                set.add(param.getName());
            }
        }
        return true;
    }

    @Override
    public int compareTo(Operation feature) {
        OperationWithParams feature_casted = (OperationWithParams) feature;
        if (this.params.size() != feature_casted.params.size()) {
            return this.params.size() - feature_casted.params.size();
        }

        Iterator<Param> it = this.params.iterator();
        Iterator<Param> itf = feature_casted.params.iterator();
        while (it.hasNext()) {
            Param p = it.next();
            Param pf = itf.next();
            if (p.getType() != pf.getType()) {
                return p.getType().compareTo(pf.getType());
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAccess().getAbbreviation());
        sb.append(" ");
        sb.append(getName());

        sb.append(paramList(true));

        Iterator<Exception> exceptions = getExceptions();
        if (exceptions.hasNext()) {
            sb.append(" throws ");
            while (exceptions.hasNext()) {
                sb.append(exceptions.next().getName());
                sb.append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }

    protected String paramList(boolean withNameAndType) {
        if (params == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Iterator<Param> it = getParams().iterator();
        if (it.hasNext()) {
            if (withNameAndType) {
                sb.append(it.next().toString());
            } else {
                sb.append(it.next().getType().getName());
            }
        }
        while (it.hasNext()) {
            sb.append(", ");
            if (withNameAndType) {
                sb.append(it.next().toString());
            } else {
                sb.append(it.next().getType().getName());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public String paramCallList() {
        if (params == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Iterator<Param> it = getParams().iterator();
        if (it.hasNext()) {

            sb.append(it.next().getName());
        }
        while (it.hasNext()) {
            sb.append(",");

            sb.append(it.next().getName());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public CodeClass getCodeClass() {
        if (getParent() instanceof ObjectModel) {
            return ((ObjectModel) getParent()).getCodeClass();
        }
        return ((ObjectType) getParent()).getCodeClass();
    }
    
    

}
