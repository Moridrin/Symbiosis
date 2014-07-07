/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code;

import static equa.code.CodeNames.AUTO_INCR;
import static equa.code.CodeNames.SYSTEM_CLASS;
import static equa.code.CodeNames.TEMPLATE;
import static equa.code.ImportType.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import equa.code.operations.AccessModifier;
import equa.code.operations.ActualParam;
import equa.code.operations.BooleanCall;
import equa.code.operations.CT;
import equa.code.operations.Call;
import equa.code.operations.ChangeIdMethod;
import equa.code.operations.CollectionKind;
import equa.code.operations.Constructor;
import equa.code.operations.IFormalPredicate;
import equa.code.operations.IRelationalOperation;
import equa.code.operations.IndexOfMethod;
import equa.code.operations.IndexedProperty;
import equa.code.operations.IsRemovableMethod;
import equa.code.operations.MapType;
import equa.code.operations.Method;
import equa.code.operations.MoveMethod;
import equa.code.operations.Operation;
import equa.code.operations.OperationWithParams;
import equa.code.operations.Operator;
import equa.code.operations.Param;
import equa.code.operations.Property;
import equa.code.operations.RemoveMethod;
import equa.code.operations.STorCT;
import equa.code.operations.SubParam;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.Event;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;

/**
 * 
 * @author frankpeeters
 */
public class Java implements Language {

	private static final long serialVersionUID = 1L;
	private static Element imports;
	private static List<String> accessModifiers = Arrays.asList("public", "private", "protected");
	private static List<String> keywords = Arrays.asList("abstract", "synchronized", "final", "static", "native");

	static {
		try {
			imports = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(Language.class.getResourceAsStream("resources/imports/java.xml")).getDocumentElement();
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
			return "";
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
			result += "add(" + add + ")";
			break;
		case ITERATOR:
			throw new RuntimeException("undefined");
		case LIST:
			result += "add(" + add + ")";
			break;
		case MAP:
			result += "error";
		case SET:
			result += "add(" + add + ")";
			break;
		}
		return result;
	}

	private void addClass(String name, JarOutputStream jar, String dir, String loc) throws IOException {
		jar.putNextEntry(new JarEntry(dir + name + ".class"));
		InputStream is = new FileInputStream(new File(loc + "/gen/" + dir + name + ".class"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			out.write(ch);
		}
		jar.write(out.toByteArray());
		is.close();
		out.close();
		jar.closeEntry();
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
			assignment += "ArrayList<>()";
			break;
		case MAP:
			assignment += "HashMap<>()";
			break;
		case SET:
			STorCT t = ct;
			while (t instanceof CT) {
				t = ((CT) t).getType();
			}
			if (t instanceof ObjectType) {
				if (((ObjectType) t).isComparable()) {
					assignment += "TreeSet<>()";
				} else {
					assignment += "HashSet<>()";
				}
			} else {
				assignment += "TreeSet<>()";
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
	public String callMethod(String object, String name, List<? extends ActualParam> params) {
		StringBuilder result = new StringBuilder();
		if (!object.isEmpty()) {
			result.append(object).append(memberOperator());
		}
		result.append(name).append("(");
		for (int i = 0; i < params.size(); i++) {
			ActualParam p = params.get(i);
			if (p instanceof Call) {
				result.append(p.expressIn(this));
			} else if (p instanceof SubParam) {
				List<String> strings = new ArrayList<>();
				// strings.add(p.getName());
				while (p instanceof SubParam) {
					SubParam sb = (SubParam) p;
					strings.add(sb.getShortName());
					p = sb.getParent();
				}
				if (strings.size() >= 1) {
					result.append(strings.get(strings.size() - 1));
					for (int j = strings.size() - 2; j >= 0; j--) {
						result.append(memberOperator());
						result.append(getProperty(strings.get(j)));
					}
				}
			} else {
				result.append(p.expressIn(this));
			}
			if (i + 1 < params.size()) {
				result.append(", ");
			}
		}
		result.append(")");
		return result.toString();
	}

	@Override
	public String callMethod(String object, String name, String... params) {
		StringBuilder result = new StringBuilder();
		if (!object.isEmpty()) {
			result.append(object).append(memberOperator());
		}
		result.append(name).append("(");
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
		return name + " instanceof " + type(type);
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
		if (ot.getCodeClass().isFinal()) {
			result.append(" final");
		}
		result.append(" class ");
		result.append(ot.getName());
		if (template) {
			result.append(TEMPLATE);
			result.append(" extends ").append(ot.getName());
		} else if (ot.supertypes().hasNext()) {
			result.append(" extends ").append(ot.supertypes().next().getName());
		}
		result.append(" ").append(bodyStart());
		IndentedList list = new IndentedList();
		if (withOrm) {
			list.add("@Entity");
		}
		list.add(result.toString(), true);
		return list;
	}

	@Override
	public String clear(Relation r) {
		String result = r.fieldName() + memberOperator();
		switch (r.collectionType().getKind()) {
		case ARRAY:
			throw new RuntimeException("undefined");
		case COLL:
			result += "clear();";
			break;
		case ITERATOR:
			throw new RuntimeException("undefined");
		case LIST:
			result += "clear();";
			break;
		case MAP:
			result += "clear();";
			break;
		case SET:
			result += "clear();";
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
		list.add(operationHeader(c));
		list.add("super(");
		for (int i = 0; i < superParams.size(); i++) {
			list.addString(superParams.get(i));
			if (i + 1 < superParams.size()) {
				list.addString(", ");
			}
		}
		list.addString(");");
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

	private void deleteFolder(File file) {
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				deleteFolder(f);
			}
			f.delete();
		}
		file.delete();
	}

	@Override
	public String endLine() {
		return ";";
	}

	@Override
	public String equalsStatement(String string1, String string2) {
		return "Objects.equals(" + string1 + ", " + string2 + ")";
	}

	@Override
	public IndentedList field(Field f, boolean withOrm) {
		StringBuilder sb = new StringBuilder();
		sb.append(accessModifier(f.getAccessModifier()));
		sb.append((f.isImmutable() ? " final" : ""));
		sb.append((f.isClassField() ? " static " : " "));
		sb.append(type(f.getType()));
		sb.append(" ");
		sb.append(f.getName());
		sb.append(endLine());
		IndentedList list = new IndentedList();
		list.add(sb.toString());
		return list;
	}

	@Override
	public IndentedList forEachLoop(STorCT type, String name, String collection, IndentedList body) {
		IndentedList list = new IndentedList();
		list.add("for (" + type(type) + " " + name + " : " + collection + ") " + bodyStart(), true);
		list.add(body);
		list.add(bodyClosure());
		return list;
	}

	@Override
	public void generate(ObjectModel om, boolean lib, boolean orm, boolean mInh, String loc) throws Exception {
		om.getProject().setLastUsedLanguage(this);
		generateTemplate(om, loc);
		if (lib) {
			try {
				generateLib(om, orm, loc);
				return;
			} catch (Exception e) {
				// If there's an exception we try to generate the source code.
			}
		}
		for (FactType ft : om.types()) {
			if (ft.isClass()) {
				ObjectType ot = ft.getObjectType();
				CodeClass cc = ot.getCodeClass();
				File f = new File(loc + "/" + cc.getDirectory());
				f.mkdirs();
				File file = new File(loc + "/" + cc.getDirectory() + ot.getName() + ".java");
				PrintStream ps = new PrintStream(file);
				ps.append(cc.getCode(this, orm));
				ps.close();
			}
		}
		CodeClass cc = om.getCodeClass();
		File file = new File(loc + "/" + cc.getDirectory() + SYSTEM_CLASS + ".java");
		PrintStream ps = new PrintStream(file);
		ps.append(cc.getCode(this, orm));
		ps.close();
	}

	private void generateTemplate(ObjectModel om, String loc) throws FileNotFoundException {
		for (FactType ft : om.types()) {
			if (ft.getObjectType() != null) {
				CodeClass cc = ft.getObjectType().getCodeClass();
				if (cc.hasUnspecifiedOperation()) {
					File f = new File(loc + "/" + cc.getDirectory());
					f.mkdirs();
					File file = new File(loc + "/" + cc.getDirectory() + ft.getObjectType().getName() + TEMPLATE + ".java");
					PrintStream ps = new PrintStream(file);
					ps.append(cc.getCodeForSpecification(this));
					ps.close();
				}
			}
		}
	}

	private void generateLib(ObjectModel om, boolean orm, String loc) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
		List<JavaFileObject> list = getJavaFileContentsAsString(om, orm, loc);
		File file = new File(loc + "/gen");
		file.mkdir();
		CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector,
				Arrays.asList(new String[] { "-d", loc + "/gen" }), null, list);
		Boolean result = task.call();
		List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
		for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
			System.out.println(d.getLineNumber());
			System.out.println(d.getMessage(null));
		}
		if (result) {
			System.out.println("Compilation has succeeded");
		} else {
			System.out.println("Compilation fails.");
		}
		jarIt(om, loc);
	}

	private List<JavaFileObject> getJavaFileContentsAsString(ObjectModel om, boolean orm, String loc) throws Exception {
		List<JavaFileObject> list = new LinkedList<>();
		for (FactType ft : om.types()) {
			if (ft.isClass()) {
				ObjectType ot = ft.getObjectType();
				String code = ot.getCodeClass().getCode(this, orm);
				list.add(new JavaObjectFromString(loc + "/gen/" + ot.getName() + ".java", code));
			}
			String code = om.getCodeClass().getCode(this, orm);
			list.add(new JavaObjectFromString(loc + "/gen/" + SYSTEM_CLASS + ".java", code));
		}
		return list;
	}

	@Override
	public String getProperty(String name) {
		return "get" + Naming.withCapital(name) + "()";
	}

	@Override
	public String hashCodeStatement(String variable) {
		return "Objects.hashCode(" + variable + ")";
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

	@Override
	public List<ImportType> imports(Relation r) {
		List<ImportType> list = new LinkedList<>();
		if (r.isCollectionReturnType() || r.isMapRelation()) {
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

	private IndentedList imports(Set<ImportType> imports) {
		IndentedList list = new IndentedList();
		for (ImportType it : imports) {
			try {
				list.add("import " + Java.imports.getElementsByTagName(it.toString()).item(0).getTextContent() + ";");
			} catch (NullPointerException ex) {
				// there is no import for this language, no problem.
			}
		}
		return list;
	}

	@Override
	public String indexOf(Relation r, String param) {
		return r.fieldName() + memberOperator() + "indexOf(" + param + ")";
	}

	private void jarIt(ObjectModel om, String loc) throws IOException {
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(loc + "/" + om.getProject().getName() + ".jar"));
		for (FactType ft : om.types()) {
			if (ft.isClass()) {
				ObjectType ot = ft.getObjectType();
				addClass(ot.getName(), jar, ot.getCodeClass().getDirectory(), loc);
			}
		}
		jar.close();
		deleteFolder(new File(loc + "/gen"));
	}

	@Override
	public String memberOperator() {
		return ".";
	}

	@Override
	public IndentedList nameSpaceAndImports(NameSpace nameSpace, Set<ImportType> imports) {
		IndentedList list = new IndentedList();
		list.add(nameSpaceStart(nameSpace));
		list.add("");
		list.add(imports(imports));
		list.add("");
		return list;
	}

	@Override
	public IndentedList nameSpaceEnd() {
		return new IndentedList();
	}

	@Override
	public IndentedList nameSpaceStart(NameSpace nameSpace) {
		IndentedList list = new IndentedList();
		StringBuilder result = new StringBuilder("package ");
		do {
			result.append(nameSpace.getName().toLowerCase());
			if (nameSpace.hasSub()) {
				result.append(".");
			}
			nameSpace = nameSpace.getSubNameSpace();
		} while (nameSpace != null);
		result.append(";");
		list.add(result.toString());
		return list;
	}

	@Override
	public String negate(String statement) {
		return "!(" + statement + ")";
	}

	@Override
	public String newInstance(STorCT type, String... params) {
		StringBuilder sb = new StringBuilder();
		sb.append("new ");
		sb.append(type(type));
		sb.append("(");
		for (int i = 0; i < params.length; i++) {
			sb.append(params[i]);
			if (i + 1 < params.length) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String nonObjectKeyword() {
		return "object";
	}

	@Override
	public IndentedList operationHeader(Operation o) {
		StringBuilder result = new StringBuilder();
		IndentedList list = new IndentedList();
		// documentation
		String[] commentLines = o.getSpec().split("\n");
		if (commentLines.length != 0) {
			list.add(docStart());
			for (String s : commentLines) {
				list.add(docLine(s));
			}
			list.add(docEnd());
		}
		// access modifier
		result.append(accessModifier(o.getAccess()));
		// if static add static
		if (o.isClassMethod()) {
			result.append(" static");
		}
		if (o.isFinal()) {
			result.append(" final");
		}
		// if it is a method we have a return type
		if (o instanceof Method) {
			Method m = (Method) o;
			result.append(" ").append(type(m.getReturnType().getType()));
		}
		// add the name
		result.append(" ").append(o.getName());
		// add parameters
		// if (o instanceof CountProperty) {
		// result.append("()");
		// } else
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
		Iterator<equa.code.operations.Exception> exceptions = o.getExceptions();
		if (exceptions.hasNext()) {
			result.append(" throws");
		}
		while (exceptions.hasNext()) {
			equa.code.operations.Exception e = exceptions.next();
			result.append(" ").append(e.getName());
			if (exceptions.hasNext()) {
				result.append(",");
			}
		}
		if (o instanceof Method) {
			Method m = (Method) o;
			if (m.isOverrideMethod()) {
				list.add("@Override");
			}
		}
		list.add(result.append(" ").append(bodyStart()).toString(), true);
		if (o.getPreSpec() != null) {
			String condition = formalPredicateCondition(o, o.getPreSpec(), true);
			IndentedList ifTrue = new IndentedList();
			ifTrue.add(throwIllegalStateException("The PRE condition has been violated."));
			list.add(ifStatement(condition, ifTrue));
		}
		if (o.getEscape() != null) {
			String condition = formalPredicateCondition(o, o.getEscape().getCondition(), false);
			IndentedList ifTrue = new IndentedList();
			if (o instanceof Method) {
				Method m = (Method) o;
				if (m instanceof RemoveMethod) {
					ifTrue.add(returnStatement(callMethod(m.getParams().get(0).expressIn(this), IsRemovableMethod.NAME)));
				} else if (m instanceof ChangeIdMethod || m instanceof MoveMethod) {
					ifTrue.add(returnStatement("false"));
				} else if (m.getReturnType().getType() instanceof ObjectType) {
					ifTrue.add(returnStatement("null"));
				} else {
					ifTrue.add(returnStatement(""));
				}
			}
			list.add(ifStatement(condition, ifTrue));
		}
		return list;
	}

	private String throwIllegalStateException(String msg) {
		return "throw new IllegalStateException(" + stringSymbol() + msg + stringSymbol() + ");";
	}

	private String formalPredicateCondition(Operation o, IFormalPredicate predicate, boolean pre) {
		Iterator<BooleanCall> operands = predicate.operands();
		StringBuilder fullCondition = new StringBuilder();
		while (operands.hasNext()) {
			BooleanCall bc = operands.next();
			if (pre) {
				String condition = bc.expressIn(this);
				if (!bc.isNegated()) {
					condition = negate(condition);
				}
				fullCondition.append(condition);
			} else {
				fullCondition.append(bc.expressIn(this));
			}
			if (operands.hasNext()) {
				if (pre) {
					fullCondition.append(and());
				} else {
					fullCondition.append(or());
				}
			}
		}
		return fullCondition.toString();
	}

	@Override
	public String operator(Operator operator) {
		switch (operator) {
		case MINUS:
			return " - ";
		case PLUS:
			return " + ";
		case SMALLER:
			return " < ";
		case SMALLER_OR_EQUAL:
			return " <= ";
		case GREATER_OR_EQUAL:
			return " >= ";
		}
		throw new IllegalStateException("Operator unknown");
	}

	@Override
	public String or() {
		return " || ";
	}

	@Override
	public IndentedList property(Property p) {
		IndentedList list = new IndentedList();
		if (p.isGetter()) {
			String[] commentLines = p.getSpec().split("\n");
			if (commentLines.length != 0) {
				list.add(docStart());
				for (String s : commentLines) {
					list.add(docLine(s));
				}
				list.add(docEnd());
			}
			String prefix = " get";
			if (p.getReturnType().getType().equals(BaseType.BOOLEAN)) {
				prefix = " is";
			}
			list.add(accessModifier(p.getAccessGetter()) + " " + (p.isFinal() ? "final " : "") + type(p.getReturnType().getType()) + prefix
					+ Naming.withCapital(p.getName()) + "(", true);
			if (p instanceof IndexedProperty) {
				IndexedProperty ip = (IndexedProperty) p;
				List<Param> params = ip.getParams();
				for (int i = 0; i < params.size(); i++) {
					list.addString(type(params.get(i).getType()) + " ");
					list.addString(params.get(i).expressIn(this));
					if (i + 1 < params.size()) {
						list.addString(", ");
					}
				}
			}
			list.addString(") " + bodyStart());
			if (p.getReturnType().getType() instanceof CT) {
				list.add(returnStatement(unmodifiable(p.getRelation().collectionType(), p.getRelation().fieldName())));
			} else {
				Relation owner = p.getRelation().getOwner().getResponsibleRelation();
				if (owner != null && p.isDerivable() && owner.isSeqRelation()) {
					list.add(returnStatement(callMethod(owner.inverse().fieldName(),
							IndexOfMethod.NAME_PREFIX + Naming.withCapital(owner.name()), thisKeyword())));
				} else if (p instanceof IndexedProperty) {
					list.add(returnStatement(p.getRelation().fieldName() + memberOperator() + get(((IndexedProperty) p).getParams())));
				} else {
					list.add(returnStatement(p.getRelation().fieldName()));
				}
			}
			list.add(bodyClosure());
		}
		if (p.isSetter()) {
			if (p.isGetter()) {
				list.add("");
			}
			list.add(
					accessModifier(p.getAccessSetter()) + " " + (p.isFinal() ? "final " : "") + type(null) + " set"
							+ Naming.withCapital(p.getName()) + "(" + type(p.getReturnType().getType()) + " " + p.getName() + ") "
							+ bodyStart(), true);
			if (!p.getRelation().isMandatory() && p.getRelation().targetType().getUndefinedString() == null && !p.getRelation().targetType().equals(BaseType.BOOLEAN)) {
				list.add(assignment(p.getRelation().fieldName() + "Defined", "true"));
			}
			list.add(assignment(thisKeyword() + memberOperator() + p.getRelation().fieldName(), p.getName()));
			Relation inv = p.getRelation().inverse();
			if (inv != null && inv.isNavigable()) {
				list.add(thisKeyword() + memberOperator() + p.getRelation().fieldName() + memberOperator() + "set" + Naming.withCapital(inv.fieldName()) + "(" + thisKeyword() + ")" + endLine());
			}
			list.add(bodyClosure());
		}
		return list;
	}

	@Override
	public String remove(Relation r, String removable) {
		String result = r.fieldName() + memberOperator();
		switch (r.collectionType().getKind()) {
		case ARRAY:
			throw new RuntimeException("undefined");
		case COLL:
			result += "remove(" + removable + ")";
			break;
		case ITERATOR:
			throw new RuntimeException("undefined");
		case LIST:
			result += "remove(" + removable + ")";
			break;
		case MAP:
			result += "remove(" + removable + ")";
			break;
		case SET:
			result += "remove(" + removable + ")";
			break;
		}
		return result;
	}

	@Override
	public String removeAt(String name, String index) {
		return name + ".remove(" + index + ");";
	}

	@Override
	public String returnStatement(String statement) {
		if (statement.isEmpty()) {
			return "return;";
		} else {
			return "return " + statement + ";";
		}
	}

	@Override
	public String size(CollectionKind ck) {
		switch (ck) {
		case ARRAY:
			return "length";
		case COLL:
			return "size()";
		case ITERATOR:
			throw new RuntimeException("Iterator does not support size.");
		case LIST:
			return "size()";
		case MAP:
			return "size()";
		case SET:
			return "size()";
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
			result.append("subList(" + i + ", " + j + ")");
			break;
		case MAP:
			throw new RuntimeException("Undefined");
		case SET:
			throw new RuntimeException("Undefined");
		}
		return result.toString();
	}

	@Override
	public IndentedList systemClassHeader() {
		IndentedList list = new IndentedList();
		list.add(accessModifier(AccessModifier.PUBLIC) + " final class " + SYSTEM_CLASS + " " + bodyStart(), true);
		return list;
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
				result += "Map<";
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
					result += "Set<";
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
					result += "Collection<";
					result = type(ct.getType(), result, true);
					result += ">";
					break;
				}
			}
		} else if (type instanceof BaseType) {
			BaseType bt = (BaseType) type;
			switch (bt.getName()) {
			case "String":
				result += "String";
				break;
			case "Integer":
				result += isCT ? "Integer" : "int";
				break;
			case "Natural":
				result += isCT ? "Integer" : "int";
				break;
			case "Real":
				result += isCT ? "Double" : "double";
				break;
			case "Character":
				result += isCT ? "Character" : "char";
				break;
			case "Boolean":
				result += isCT ? "Boolean" : "boolean";
				break;
			case "Object":
				result += "Object";
				break;
			}
		}
		return result;
	}

	@Override
	public String unmodifiable(CT ct, String statement) {
		String result = "";
		switch (ct.getKind()) {
		case ARRAY:
			throw new RuntimeException("Undefined");
		case COLL:
			result = "Collections" + memberOperator() + "unmodifiable";
			result += "Collection(" + statement + ")";
		case ITERATOR:
			throw new RuntimeException("Undefined");
		case LIST:
			result = "Collections" + memberOperator() + "unmodifiable";
			result += "List(" + statement + ")";
			break;
		case MAP:
			result = "Collections" + memberOperator() + "unmodifiable";
			result += "Map(" + statement + ")";
		case SET:
			result += "new ArrayList<" + type(ct.getType(), "", true) + ">(" + statement + ")";
		}
		return result;
	}

	@Override
	public String throwUnsupportedOperationException(String msg) {
		return "throw new UnsupportedOperationException(" + stringSymbol() + msg + stringSymbol() + ");";
	}

	@Override
	public String setProperty(String object, String property, String parameter) {
		return new StringBuilder().append(object).append(memberOperator()).append("set").append(Naming.withCapital(property)).append("(")
				.append(parameter).append(");").toString();
	}

	@Override
	public String contains(Relation r, String name) {
		StringBuilder result = new StringBuilder();
		result.append(r.fieldName()).append(memberOperator());
		switch (r.collectionType().getKind()) {
		case ARRAY:
			throw new IllegalStateException();
		case COLL:
			result.append("contains");
			break;
		case ITERATOR:
			throw new IllegalStateException();
		case LIST:
			result.append("contains");
			break;
		case MAP:
			result.append("containsKey");
			break;
		case SET:
			result.append("contains");
			break;
		}
		result.append("(").append(name).append(")");
		return result.toString();
	}

	@Override
	public String docEnd() {
		return " */";
	}

	@Override
	public String docLine(String line) {
		return " * " + line;
	}

	@Override
	public String docStart() {
		return "/**";
	}

	@Override
	public Map<OperationHeader, IndentedList> getOperations(String s) {
		Map<OperationHeader, IndentedList> map = new HashMap<>();
		// We remove everything before the start of the class
		String body = s.substring(s.indexOf("{") + 3);
		// We remove the class closing
		body = body.substring(0, body.lastIndexOf("}"));
		while (body.contains("{")) {
			int lastIndex = body.indexOf("{") + 1;
			int end = body.indexOf("}") + 1;
			int totalCount = 1;
			int count = 1;
			while (body.substring(lastIndex).contains("{") && count > 0) {
				if (body.indexOf("{", lastIndex) > body.indexOf("}", lastIndex) || body.indexOf("{", lastIndex) == -1) {
					// a block ended.
					count--;
					lastIndex = body.indexOf("}", lastIndex) + 1;
				} else {
					// we have a block in our method
					lastIndex = body.indexOf("{", lastIndex) + 1;
					count++;
					totalCount++;
					end = Util.findNthOccurence(body, "}", totalCount) + 1;
				}
			}
			String method = body.substring(0, end);
			if (body.length() - 2 > end) {
				body = body.substring(end + 2);
			} else {
				body = body.substring(end);
			}
			map.put(getOH(method), IndentedList.fromString(method, -1));
		}
		return map;
	}

	private OperationHeader getOH(String method) {
		String[] parts = method.trim().split("\n")[0].trim().split("\\s+");
		int part = 0;
		String access = parts[part++];
		if (!accessModifiers.contains(access)) {
			access = "";
			part = 0;
		}
		String retrn = "";
		if (!parts[part].contains("(")) {
			boolean keywordFound = false;
			do {
				keywordFound = keywords.contains(parts[part]);
				if (keywordFound) {
					part++;
				}
			} while (keywordFound);
			retrn = parts[part];
			part++;
		}
		if (!parts[part].contains("(")) {
			throw new IllegalArgumentException("Method is not properly defined.");
		}
		String name = parts[part].split("\\(")[0];
		String stringParams = method.trim().split("\n")[0].substring(method.indexOf("(") + 1, method.indexOf(")"));
		List<String> params = Arrays.asList(stringParams.split(", "));
		do {
			part++;
		} while (!parts[part - 1].contains(")"));
		List<String> exceptions = new ArrayList<>();
		if (parts[part].equals("throws")) {
			part++;
			while (parts[part].contains(",")) {
				exceptions.add(parts[part].substring(0, parts[part].length() - 1));
				part++;
			}
			exceptions.add(parts[part]);
		}
		return new LanguageOH(access, retrn, name, params, exceptions, this);
	}

	@Override
	public String autoIncr(String autoincr) {
		return autoincr + AUTO_INCR + "++";
	}

	@Override
	public String adjustMap(Relation r, String key, String amount) {
		StringBuilder sb = new StringBuilder();
		sb.append(r.fieldName());
		sb.append(memberOperator());
		sb.append("put(");
		sb.append(key);
		sb.append(", ");
		sb.append(r.fieldName());
		sb.append(memberOperator());
		sb.append("get(");
		sb.append(key);
		sb.append(") + ");
		sb.append(amount);
		sb.append(");");
		return sb.toString();
	}

	@Override
	public String get(List<Param> params) {
		StringBuilder sb = new StringBuilder("get(");
		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).expressIn(this));
			if (i + 1 < params.size()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String propertyName(String name, STorCT returnType) {
		if (returnType.equals(BaseType.BOOLEAN)) {
			return "is" + Naming.withCapital(name);
		} else {
			return "get" + Naming.withCapital(name);
		}
	}

	@Override
	public IndentedList operationTemplate(OperationHeader oh) {
		IndentedList list = new IndentedList();
		list.add(oh.getAccessModifier(this) + " " + oh.getReturn(this) + " " + oh.getName(this) + "(", true);
		List<String> paramTypes = oh.getParamTypes(this);
		List<String> paramNames = oh.getParamNames(this);
		for (int i = 0; i < paramTypes.size(); i++) {
			list.addString(paramTypes.get(i) + " " + paramNames.get(i));
			if (i + 1 < paramTypes.size()) {
				list.addString(", ");
			}
		}
		list.addString(")");
		Iterator<String> exceptions = oh.getExceptions(this).iterator();
		if (exceptions.hasNext()) {
			list.addString(" throws ");
			do {
				list.addString(exceptions.next());
				if (exceptions.hasNext()) {
					list.addString(", ");
				}
			} while (exceptions.hasNext());
		}
		list.addString(" " + bodyStart());
		list.add(throwUnsupportedOperationException("Symbiosis: Please write code here"));
		list.add(bodyClosure());
		return list;
	}

	@Override
	public IndentedList constructorTemplate(Constructor c) {
		StringBuilder result = new StringBuilder();
		IndentedList list = new IndentedList();
		// access modifier
		result.append(accessModifier(c.getAccess()));
		// add the name
		result.append(" ").append(c.getName() + TEMPLATE);
		// add parameters
		Iterator<Param> params = c.getParams().iterator();
		result.append("(");
		while (params.hasNext()) {
			Param p = params.next();
			result.append(type(p.getType())).append(" ").append(p.getName());
			if (params.hasNext()) {
				result.append(", ");
			}
		}
		result.append(")");
		Iterator<equa.code.operations.Exception> exceptions = c.getExceptions();
		if (exceptions.hasNext()) {
			result.append(" throws");
		}
		while (exceptions.hasNext()) {
			equa.code.operations.Exception e = exceptions.next();
			result.append(" ").append(e.getName());
			if (exceptions.hasNext()) {
				result.append(",");
			}
		}
		list.add(result.append(" ").append(bodyStart()).toString(), true);
		result = new StringBuilder("super(");
		params = c.getParams().iterator();
		while (params.hasNext()) {
			Param p = params.next();
			result.append(p.getName());
			if (params.hasNext()) {
				result.append(", ");
			}
		}
		result.append(");");
		list.add(result.toString());
		list.add(bodyClosure());
		return list;
	}

	@Override
	public List<String> getImports(String s) {
		int importIndex = s.indexOf("\nimport ");
		if (importIndex == -1) {
			return java.util.Collections.emptyList();
		} else {
			List<String> imports = new ArrayList<>();
			do {
				imports.add(s.substring(importIndex + 8, s.indexOf(";", importIndex)));
				importIndex = s.indexOf("\nimport ", importIndex + 1);
			} while (importIndex != -1);
			return imports;
		}
	}

	@Override
	public String put(String name, String key, String value) {
		StringBuilder sb = new StringBuilder(name);
		sb.append(memberOperator());
		sb.append("put(");
		sb.append(key);
		sb.append(", ");
		sb.append(value);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public IndentedList bodyClosure(IRelationalOperation o) {
		IndentedList list = new IndentedList();
		List<Event> events = o.getRelation().getParent().getEvents();
		for (Event e : events) {
			//TODO Frank hier moet je zorgen dat je een if statement zet met de conditie enzo
			//Ik zie dat het nu nog niet in orde is.
			//Dan moet je zorgen dat de Methodes die een relational hebben deze methode aanroepen ipv de andere.
		}
		
		list.add("}", false);
		return list;
	}
}

class JavaObjectFromString extends SimpleJavaFileObject {

	private String contents = null;

	public JavaObjectFromString(String className, String contents) throws Exception {
		super(new File(className).toURI(), Kind.SOURCE);
		this.contents = contents;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return contents;
	}
}
