package equa.code;

import static equa.code.ImportType.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import equa.code.operations.AccessModifier;
import equa.code.operations.ActualParam;
import equa.code.operations.CT;
import equa.code.operations.CollectionKind;
import equa.code.operations.Constructor;
import equa.code.operations.IRelationalOperation;
import equa.code.operations.MapType;
import equa.code.operations.Method;
import equa.code.operations.Operation;
import equa.code.operations.OperationWithParams;
import equa.code.operations.Operator;
import equa.code.operations.Param;
import equa.code.operations.Property;
import equa.code.operations.STorCT;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;

public class CSharp implements Language {

    private static final long serialVersionUID = 1L;
	private static Element imports;

    static {
        try {
            imports = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(Language.class.getResourceAsStream("resources/imports/csharp.xml")).getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String accessModifier(AccessModifier accessModifier) {
        switch (accessModifier) {
            case PUBLIC:
                return "public";
            case NAMESPACE:
                return "internal";
            case PROTECTED:
                return "protected";
            case PRIVATE:
                return "private";
        }
        throw new RuntimeException("There is an accessModifier missing");
    }

    @Override
    public String add(String name, CollectionKind kind, String add) {
        String result = name + memberOperator();
        switch (kind) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                result += "Add(" + add + ")";
                break;
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                result += "Add(" + add + ")";
                break;
            case MAP:
                throw new RuntimeException("undefined");
            case SET:
                result += "Add(" + add + ")";
                break;
        }
        return result;
    }

    @Override
    public String and() {
        return " && ";
    }

    @Override
    public String assignCollection(STorCT type) {
        String assignment = "new ";
        CT ct = (CT) type;
        switch (ct.getKind()) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                throw new RuntimeException("undefined");
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                assignment += "List<" + type(ct.getType()) + ">()";
                break;
            case MAP:
                assignment += "Dictionary<" + type(ct.getType()) + ">()";
                break;
            case SET:
                STorCT t = ct;
                while (t instanceof CT) {
                    t = ((CT) t).getType();
                }
                if (t instanceof ObjectType) {
                    if (((ObjectType) t).isComparable()) {
                        assignment += "SortedSet<" + type(ct.getType()) + ">()";
                    } else {
                        assignment += "HashSet<" + type(ct.getType()) + ">()";
                    }
                } else {
                    assignment += "SortedSet<" + type(ct.getType()) + ">()";
                }
                break;
        }
        return assignment;
    }

    @Override
    public String assignment(String variable, String expression) {
        return variable + " = " + expression + ";";
    }

    @Override
    public IndentedList bodyClosure() {
        IndentedList list = new IndentedList();
        list.add("}", false);
        return list;
    }

    @Override
    public String bodyStart() {
        return "{";
    }

    @Override
    public String callConstructor(String otName, String... params) {
    	StringBuilder result = new StringBuilder();
		result.append("new ").append(otName).append("(");
		for (int i = 0; i < params.length; i++) {
			result.append(params[i]);
			if (i + 1 < params.length) {
				result.append(", ");
			}
		}
		result.append(")");
		return result.toString();
    }

    @Override
    public String cast(STorCT type, String newName, String oldName) {
        return type(type) + " " + newName + " = " + "(" + type(type) + ") " + oldName + ";";
    }

    @Override
    public String checkType(String name, STorCT type) {
        return name + " is " + type(type);
    }

    @Override
    public String classClosure() {
        return "}";
    }

    @Override
    public IndentedList classHeader(AccessModifier accessModifier, ObjectType ot, boolean template, boolean withOrm) {
        StringBuilder result = new StringBuilder();
        result.append(accessModifier(accessModifier));
        if (ot.isAbstract()) {
            result.append(" abstract");
        }
        result.append(" class ").append(ot.getName());
        if (ot.supertypes().hasNext()) {
            result.append(" : ").append(ot.supertypes().next().getName());
        }
        result.append(" ").append(bodyStart());
        IndentedList list = new IndentedList();
        if (withOrm) {
            //TODO Csharp ORM
        }
        list.add(result.toString());
        return list;
    }

    @Override
    public String clear(Relation r) {
        String result = r.fieldName() + memberOperator();
        switch (r.collectionType().getKind()) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                result += "Clear();";
                break;
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                result += "Clear();";
                break;
            case MAP:
                result += "Clear();";
            case SET:
                result += "Clear();";
                break;
        }
        return result;
    }

    @Override
    public String concatenate(String string1, String string2) {
        return string1 + " + " + string2;
    }

    @Override
    public IndentedList constructorHeaderAndSuper(Constructor c, List<String> superParams) {
        IndentedList list = new IndentedList();
        list.add(operationHeader(c, true));
        list.addString(" : base(");
        for (int i = 0; i < superParams.size(); i++) {
            list.addString(superParams.get(i));
            if (i + 1 < superParams.size()) {
                list.addString(", ");
            }
        }
        list.addString(")" + bodyStart());
        return list;
    }

    @Override
    public String createInstance(STorCT type, String name, String otName, String... params) {
        StringBuilder result = new StringBuilder();
        result.append(type(type)).append(" ").append(name).append(" = ");
        result.append(callConstructor(otName, params));
        result.append(";");
        return result.toString();
    }

    @Override
    public String declarationAndAssignment(STorCT type, String variable, String expression) {
        return type(type) + " " + variable + " = " + expression + ";";
    }

    @Override
    public String endLine() {
        return ";";
    }

    @Override
    public String equalsStatement(String string1, String string2) {
        return "Object.Equals(" + string1 + ", " + string2 + ")";
    }

    @Override
    public IndentedList field(Field f, boolean withOrm) {
        StringBuilder sb = new StringBuilder();
        sb.append(accessModifier(f.getAccessModifier()));
        sb.append((f.isImmutable() ? " readonly" : ""));
        sb.append((f.isClassField() ? " static " : " "));
        sb.append(type(f.getType()));
        sb.append(f.getName());
        sb.append(endLine());
        IndentedList list = new IndentedList();
        if (withOrm) {
            // TODO orm
        }
        list.add(sb.toString());
        return list;
    }

    @Override
    public IndentedList forEachLoop(STorCT type, String name, String collection, IndentedList body) {
        IndentedList list = new IndentedList();
        list.add("foreach (" + type(type) + " " + name + " in " + collection + ") " + bodyStart(), true);
        list.add(body);
        list.add(bodyClosure());
        return list;
    }

    @Override
    public String getProperty(String name) {
        return Naming.withCapital(name);
    }

    @Override
    public IndentedList ifStatement(String condition, IndentedList trueStatement) {
        IndentedList list = new IndentedList();
        list.add("if (" + condition + ") " + bodyStart(), true);
        list.add(trueStatement);
        list.add(bodyClosure());
        return list;
    }

    @Override
    public IndentedList ifStatement(String condition, IndentedList trueStatement, IndentedList falseStatement) {
        IndentedList list = new IndentedList();
        list.add("if (" + condition + ") " + bodyStart(), true);
        list.add(trueStatement);
        list.add(bodyClosure() + " else " + bodyStart(), false, true);
        list.add(falseStatement);
        list.add(bodyClosure());
        return list;
    }

    private IndentedList imports(Set<ImportType> imports) {
        Set<String> set = new TreeSet<>();
        for (ImportType it : imports) {
            set.add("using " + CSharp.imports.getElementsByTagName(it.toString()).item(0).getTextContent() + ";");
        }
        IndentedList list = new IndentedList();
        for (String s : set) {
            list.add(s);
        }
        return list;
    }

    @Override
    public String memberOperator() {
        return ".";
    }

    @Override
    public IndentedList nameSpaceStart(NameSpace nameSpace) {
        IndentedList list = new IndentedList();
        StringBuilder result = new StringBuilder("namespace ");
        do {
            result.append(Naming.withCapital(nameSpace.getName()));
            if (nameSpace.hasSub()) {
                result.append(".");
            }
            nameSpace = nameSpace.getSubNameSpace();
        } while (nameSpace != null);
        list.add(result.toString() + " " + bodyStart(), true);
        return list;
    }

    @Override
    public String negate(String statement) {
        return "!(" + statement + ")";
    }

    @Override
    public IndentedList operationHeader(Operation o) {
        return operationHeader(o, false);
    }

    public IndentedList operationHeader(Operation o, boolean subC) {
        StringBuilder result = new StringBuilder();
        // access modifier
        result.append(accessModifier(o.getAccess()));
        // if static add static
        if (o.isClassMethod()) {
            result.append(" static");
        }
        // if override add override
        if (o instanceof Method) {
            Method m = (Method) o;
            if (m.isOverrideMethod()) {
                result.append(" override");
            }
        }
        // check for different types for return type
        if (o instanceof Property) {
            Property p = (Property) o;
            result.append(" ").append(type(p.getReturnType().getType()));
        } else if (o instanceof Method) {
            Method m = (Method) o;
            result.append(" ").append(type(m.getReturnType().getType()));
        }
        // add the name
        result.append(" ").append(Naming.withCapital(o.getName()));
        // add parameters
//		if (o instanceof CountProperty) {
//			result.append("()");
//		} else
        if (o instanceof OperationWithParams) {
            OperationWithParams owp = (OperationWithParams) o;
            Iterator<Param> params = owp.getParams().iterator();
            result.append("(");
            while (params.hasNext()) {
                Param p = params.next();
                result.append(type(p.getType())).append(" ").append(p.getName());
                if (params.hasNext()) {
                    result.append(", ");
                }
            }
            result.append(")");
        }
        IndentedList list = new IndentedList();
        if (subC) {
            list.add(result.toString(), true);
        } else {
            list.add(result.append(" ").append(bodyStart()).toString(), true);
        }
        return list;
    }

    @Override
    public IndentedList property(Property p) {
        IndentedList list = new IndentedList();
        if (!p.isGetter() && !p.isSetter()) {
            return list;
        }
        list.add(accessModifier(p.getAccessGetter()) + " " + type(p.getReturnType().getType()) + " " + Naming.withCapital(p.getName())
                + " " + bodyStart(), true);
        if (p.isGetter()) {
            String returnStatement = "";
//			if (p instanceof IsDefinedMethod)
//				returnStatement = returnStatement(p.getRelation().fieldName() + "Defined");
//			else
            if (p.getReturnType().getType() instanceof CT) {
                returnStatement = returnStatement(unmodifiable(p.getRelation().collectionType(), p.getRelation().fieldName()));
            } else {
                returnStatement = returnStatement(p.getRelation().fieldName());
            }
            list.add("get " + bodyStart() + " " + returnStatement + " " + bodyClosure());
        }
        if (p.isSetter()) {
            list.add("set " + bodyStart(), true);
            if (!p.getRelation().isMandatory() && p.getRelation().targetType().getUndefinedString() == null) {
                list.add(assignment(p.getRelation().fieldName() + "Defined", "true"));
            }
            list.add(assignment(thisKeyword() + memberOperator() + p.getRelation().fieldName(), "value"));
            list.add(bodyClosure());
        }
        list.add(bodyClosure());
        return list;
    }

    @Override
    public String remove(Relation r, String removable) {
        String result = r.fieldName() + memberOperator();
        switch (r.collectionType().getKind()) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                result += "Remove(" + removable + ")";
                break;
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                result += "Remove(" + removable + ")";
                break;
            case MAP:
                result += "Remove(" + removable + ")";
                break;
            case SET:
                result += "Remove(" + removable + ")";
                break;
        }
        return result;
    }

    @Override
    public String returnStatement(String statement) {
        return "return " + statement + ";";
    }

    @Override
    public String size(CollectionKind ck) {
        switch (ck) {
            case ARRAY:
                return "Length";
            case COLL:
                return "Count";
            case ITERATOR:
                throw new RuntimeException("Iterator does not support size.");
            case LIST:
                return "Count";
            case MAP:
                return "Count";
            case SET:
                return "Count";
        }
        throw new RuntimeException("There is a CollectionKind missing here.");
    }

    @Override
    public String stringSymbol() {
        return "\"";
    }

    @Override
    public String subseq(Relation r, String i, String j) {
        StringBuilder result = new StringBuilder();
        result.append(r.fieldName() + memberOperator());
        switch (r.collectionType().getKind()) {
            case ARRAY:
                throw new RuntimeException("Undefined");
            case COLL:
                throw new RuntimeException("Undefined");
            case ITERATOR:
                throw new RuntimeException("Undefined");
            case LIST:
                String count = j + " - " + i;
                result.append("GetRange(" + i + ", " + count + ")");
                break;
            case MAP:
                throw new RuntimeException("Undefined");
            case SET:
                throw new RuntimeException("Undefined");
        }
        return result.toString();
    }

    @Override
    public String thisKeyword() {
        return "this";
    }

    @Override
    public String type(STorCT type) {
        return type(type, "", false);
    }

    private String type(STorCT type, String result, boolean isCT) {
        if (type == null) {
            result += "void";
        } else if (type instanceof ObjectType) {
            result += type.getName();
        } else if (type instanceof CT) {
            if (type instanceof MapType) {
                MapType hmt = (MapType) type;
                result += "Dictonary<";
                result = type(hmt.getKeyType(), result, true);
                result += ", ";
                result = type(hmt.getValueType(), result, true);
                result += ">";
            } else {
                CT ct = (CT) type;
                switch (ct.getKind()) {
                    case LIST:
                        result += "List<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                    case SET:
                        result += "ISet<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                    case ARRAY:
                        result = type(ct.getType(), result, false);
                        result += "[]";
                        break;
                    case ITERATOR:
                        result += "Iterator<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                    case MAP:
                        throw new RuntimeException("This should be MapType, not CollectionType");
                    case COLL:
                        result += "ICollection<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                }
            }
        } else if (type instanceof BaseType) {
            BaseType bt = (BaseType) type;
            switch (bt.getName()) {
                case "String":
                    result += "string";
                    break;
                case "Integer":
                    result += "int";
                    break;
                case "Natural":
                    result += "int";
                    break;
                case "Real":
                    result += "double";
                    break;
                case "Character":
                    result += "char";
                    break;
                case "Boolean":
                    result += "bool";
                    break;
                case "Object":
                    result += "object";
                    break;
            }
        }
        return result;
    }

    @Override
    public String unmodifiable(CT ct, String statement) {
        String result = "new ReadOnlyCollection<" + type(ct.getType()) + ">(";
        switch (ct.getKind()) {
            case ARRAY:
                throw new RuntimeException("Undefined");
            case COLL:
                result += statement + ")";
            case ITERATOR:
                throw new RuntimeException("Undefined");
            case LIST:
                result += statement + ")";
                break;
            case MAP:
                throw new RuntimeException("Undefined");
            case SET:
                result += statement + memberOperator() + "ToList())" + memberOperator() + "ToList()";
        }
        return result;
    }
    @Override
    public IndentedList nameSpaceEnd() {
        IndentedList list = new IndentedList();
        list.add(bodyClosure());
        return list;
    }

    @Override
    public String removeAt(String name, String index) {
        return name + ".RemoveAt(" + index + ");";
    }

    @Override
    public String nonObjectKeyword() {
        return "obj";
    }

    @Override
    public IndentedList nameSpaceAndImports(NameSpace nameSpace, Set<ImportType> imports) {
        IndentedList list = new IndentedList();
        list.add(imports(imports));
        list.add("");
        list.add(nameSpaceStart(nameSpace));
        return list;
    }

    @Override
    public String indexOf(Relation r, String param) {
        return r.fieldName() + memberOperator() + "IndexOf(" + param + ")";
    }

    @Override
    public void generate(ObjectModel om, boolean lib, boolean orm, boolean mInh, String loc) throws Exception {
        for (FactType ft : om.types()) {
            if (ft.isClass()) {
                ObjectType ot = ft.getObjectType();
                CodeClass cc = ot.getCodeClass();
                File file = new File(loc + "/" + ot.getName() + ".cs");
                PrintStream ps = new PrintStream(file);
                ps.append(cc.getCode(this, orm));
                ps.close();
            }
        }
    }

    @Override
    public List<ImportType> imports(Relation r) {
        List<ImportType> list = new LinkedList<>();
        if (r.hasMultipleTarget()) {
            switch (r.collectionType().getKind()) {
                case COLL:
                    list.add(Collection);
                    break;
                case ITERATOR:
                    list.add(Iterator);
                    break;
                case LIST:
                    list.add(ImportType.List);
                    list.add(ArrayList);
                    break;
                case MAP:
                    list.add(Map);
                    list.add(HashMap);
                    break;
                case SET:
                    list.add(ImportType.Set);
                    if (r.targetType() instanceof ObjectType) {
                        if (((ObjectType) r.targetType()).isComparable()) {
                            list.add(SortedSet);
                        } else {
                            list.add(HashSet);
                        }
                    } else {
                        list.add(SortedSet);
                    }
                    break;
                case ARRAY:
                    break;
            }
        }
        return list;
    }

    @Override
    public String hashCodeStatement(String variable) {
        return variable + memberOperator() + "GetHashCode()";
    }

    @Override
    public String callMethod(String object, String name, String... params) {
        StringBuilder result = new StringBuilder();
        result.append(object).append(memberOperator()).append(name).append("(");
        for (int i = 0; i < params.length; i++) {
            result.append(params[i]);
            if (i + 1 < params.length) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public String newInstance(STorCT type, String... params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndentedList systemClassHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String operator(Operator operator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String callMethod(String object, String name, List<? extends ActualParam> params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String or() {
        return " || ";
    }

	@Override
	public String throwUnsupportedOperationException(String msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setProperty(String object, String property, String parameter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String contains(Relation r, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String docEnd() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String docLine(String line) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String docStart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OperationHeader, IndentedList> getOperations(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String autoIncr(String autoincr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String adjustMap(Relation r, String key, String amount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get(List<Param> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String propertyName(String name, STorCT returnType) {
		return Naming.withCapital(name);
	}

	@Override
	public IndentedList operationTemplate(OperationHeader oh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndentedList constructorTemplate(Constructor c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getImports(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String put(String name, String key, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndentedList bodyClosure(IRelationalOperation o) {
		// TODO Auto-generated method stub
		return null;
	}
}
