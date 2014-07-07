/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.utils;

/**
 *
 * @author S. Roubtsov
 */
public class StringUtils {

    public static String charInStringToUpperCase(String str, int index) {
        char cupper = Character.toUpperCase(str.charAt(index));
        return str.substring(0, index) + cupper + str.substring(index + 1);
    }

    public static String charInStringToLowerCase(String str, int index) {
        char clower = Character.toLowerCase(str.charAt(index));
        return str.substring(0, index) + clower + str.substring(index + 1);
    }

    public static String RemoveEmptyLinesFromStr(String str) {
        return str.replaceAll("(?m)^[ \t]*\r?\n", "");
    }
}
