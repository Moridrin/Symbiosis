/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.inspector;

/**
 *
 * @author FrankP
 */
public class DataNode implements SubNode {

    private SubNode[] data;

    public DataNode(SubNode[] data) {
        this.data = data;
    }

    @Override
    public Class<?> getType() {
        return null;
    }

    @Override
    public String getName() {
        return "data";
    }

    @Override
    public Object getValue() {
        return data;
    }
}
