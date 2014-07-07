package equa.code;

import java.io.Serializable;
import java.util.List;

public abstract class OperationHeader implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public abstract String getAccessModifier(Language l);

	public abstract String getName(Language l);
	
	public abstract List<String> getParamTypes(Language l);
	
	public abstract List<String> getParamNames(Language l);
	
	public abstract String getReturn(Language l);
	
	public abstract List<String> getExceptions(Language l);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName(Language.JAVA) == null) ? 0 : getName(Language.JAVA).hashCode());
		result += prime * result + ((getParamTypes(Language.JAVA) == null) ? 0 : getParamTypes(Language.JAVA).hashCode());
		return result;
	}
}
