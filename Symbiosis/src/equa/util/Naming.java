/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.util;

/**
 *
 * @author FrankP
 */
public class Naming {

    /**
     * @return true if str is an acceptable identifier, else false
     */
    public static boolean isIdentifier(String str) {
        String symbol = "[$_\\p{Alpha}]";
        String symbolOrDigit = "[$_\\d\\p{Alpha}]";
        return str.matches(symbol + symbolOrDigit + "*");
    }

    /**
     * definition of the name of a type in BNF: name ::= [namespace.]identifier
     * namespace ::= identifier {.identifier}
     */
    public static boolean isNameSpace(String str) {
        int index = str.indexOf('.');
        while (index >= 0) {
            if (!Naming.isIdentifier(str.substring(0, index))) {
                return false;
            }
            str = str.substring(index + 1);
            index = str.indexOf('.');
        }
        return Naming.isIdentifier(str);
    }

    /**
     * definition of the name of a type in BNF: name ::= [namespace.]identifier
     * namespace ::= identifier {.identifier> identifier ::= symbol {symbol
     * |digit } symbol ::= unicode_letter | $ | _
     */
    public static boolean isTypeName(String name) {

        int endIndex = name.lastIndexOf('.');
        if (endIndex >= 0 && !isNameSpace(name.substring(0, endIndex))) {
            return false;
        }
        return isIdentifier(name.substring(endIndex + 1));
    }

    /**
     * trimming of whitespaces and capitalizing of first letter after last dot;
     * in case of dotless typeName: first letter will be capitalized
     *
     * @param typeName
     * @return the resulting string with capitalized first letter
     */
    public static String restyleWithCapital(String typeName) {
        String str = typeName.trim();
        if (str.length() == 0) {
            return str;
        }

        int index = str.lastIndexOf(".");
        if (index == -1) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        } else if (index < str.length() - 1) {
            return str.substring(0, index)
                    + str.substring(index + 1, index + 2).toUpperCase()
                    + str.substring(index + 2);
        } else {
            return str;
        }
    }

    /**
     *
     * @param name
     * @return name, whereby the first letter is capitalized in case of a non
     * empty string
     */
    public static String withCapital(String name) {
        if (name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     *
     * @param name
     * @return name, whereby the first letter is decapitalized in case of a non
     * empty string
     */
    public static String withoutCapital(String name) {
        if (name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String singletonName(String singleton) {
        if (singleton.startsWith("_")) {
            return withoutCapital(singleton.substring(1));
        } else {
            return withoutCapital(singleton);
        }
    }
    
    public static String withoutVowels(String name) {
        String nameToLowerCase = name.toLowerCase();
        return nameToLowerCase.replaceAll("[aeiou]", "");
    }
    
    public static String plural(String name){
         if (name.endsWith("y")) {
            return name.substring(0, name.length() - 1) + "ies";
        } else if (name.endsWith("is")) {
            return name.substring(0,name.length() - 2) + "es";
        } else if (name.endsWith("man")) {
            return name.substring(0,name.length() - 2) + "en";
        } else {
            return name + "s";
        }
    }
}
