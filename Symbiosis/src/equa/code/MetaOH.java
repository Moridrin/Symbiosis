package equa.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import equa.code.operations.AccessModifier;
import equa.code.operations.Param;
import equa.code.operations.STorCT;

public class MetaOH extends OperationHeader  {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final boolean property;
    private final boolean classMethod;
    private final STorCT returnType;
    private final List<Param> params;

    public MetaOH(String name, boolean property, boolean classMethod, STorCT returnType, List<Param> params) {
        this.name = name;
        this.property = property;
        this.classMethod = classMethod;
        this.returnType = returnType;
        this.params = params;
    }

    public boolean isClassMethod() {
        return classMethod;
    }

    @Override
    public String getName(Language l) {
        if (property) {
            return l.propertyName(name, returnType);
        } else {
            return name;
        }
    }

    @Override
    public List<String> getParamTypes(Language l) {
        List<String> paramTypes = new ArrayList<>();
        for (Param p : params) {
            paramTypes.add(l.type(p.getType()));
        }
        return paramTypes;
    }

    @Override
    public List<String> getParamNames(Language l) {
        List<String> paramNames = new ArrayList<>();
        for (Param p : params) {
            paramNames.add(p.getName());
        }
        return paramNames;
    }

    @Override
    public String getReturn(Language l) {
        return l.type(returnType);
    }

    @Override
    public String getAccessModifier(Language l) {
        return l.accessModifier(AccessModifier.PUBLIC);
    }

    @Override
    public List<String> getExceptions(Language l) {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        OperationHeader other = (OperationHeader) obj;
        Language l;
        if (other instanceof LanguageOH) {
            l = ((LanguageOH) other).getLanguage();
        } else {
            l = Language.JAVA;
        }
        if (getName(l) == null) {
            if (other.getName(l) != null) {
                return false;
            }
        } else if (!getName(l).equals(other.getName(l))) {
            return false;
        }
        if (getParamTypes(l) == null) {
            if (other.getParamTypes(l) != null) {
                return false;
            }
        } else if (!getParamTypes(l).equals(other.getParamTypes(l))) {
            return false;
        }
        return true;
    }

}
