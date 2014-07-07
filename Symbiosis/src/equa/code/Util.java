package equa.code;

import java.io.File;
import java.nio.file.Files;

import equa.meta.objectmodel.ObjectType;

public class Util {

    public static ObjectType getRootSuperType(ObjectType ot) {
        while (ot.supertypes().hasNext()) {
            ot = ot.supertypes().next();
        }
        return ot;
    }

	public static String getString(File f) {
		try {
			return new String(Files.readAllBytes(f.toPath()));
		} catch (Exception e) {
			return null;
		}
	}

	public static int findNthOccurence(String body, String str, int count) {
		int result = -1;
		for (int i = count; i > 0; i--) {
			result = body.indexOf(str, result + 1);
			if (result == -1) {
				return result;
			}
		}
		return result;
	}
}
