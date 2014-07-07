package equa.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageOH extends OperationHeader {

	private static final long serialVersionUID = 1L;
	private final String retrn, name, access;
	private final List<String> paramTypes = new ArrayList<>();
	private final List<String> paramNames = new ArrayList<>();
	private final List<String> exceptions;
	private final Language l;

	public LanguageOH(String access, String retrn, String name, List<String> params, List<String> exceptions, Language l) {
		this.access = access;
		this.retrn = retrn;
		this.name = name;
		for (String s : params) {
			if (s.isEmpty()) {
				continue;
			}
			String[] split = s.split(" ");
			paramTypes.add(split[0]);
			paramNames.add(split[1]);
		}
		this.exceptions = exceptions;
		this.l = l;
	}

	@Override
	public String getName(Language l) {
		return name;
	}

	@Override
	public List<String> getParamTypes(Language l) {
		return Collections.unmodifiableList(paramTypes);
	}

	@Override
	public List<String> getParamNames(Language l) {
		return Collections.unmodifiableList(paramNames);
	}

	@Override
	public String getReturn(Language l) {
		return retrn;
	}

	@Override
	public String getAccessModifier(Language l) {
		return access;
	}

	@Override
	public List<String> getExceptions(Language l) {
		return Collections.unmodifiableList(exceptions);
	}
	
	public Language getLanguage() {
		return l;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		OperationHeader other = (OperationHeader) obj;
		if (getName(l) == null) {
			if (other.getName(l) != null)
				return false;
		} else if (!getName(l).equals(other.getName(l)))
			return false;
		if (getParamTypes(l) == null) {
			if (other.getParamTypes(l) != null)
				return false;
		} else if (!getParamTypes(l).equals(other.getParamTypes(l)))
			return false;
		return true;
	}
}
