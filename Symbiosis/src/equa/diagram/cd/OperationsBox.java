package equa.diagram.cd;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;

import equa.code.operations.Operation;
import equa.diagram.DiagramComponent;
import equa.diagram.DiagramContainer;

public class OperationsBox extends DiagramContainer {

    // fields
    private static final long serialVersionUID = 1L;
    private ArrayList<OperationsBoxItem> items;
    private Font component_font;

    // construction
    public OperationsBox(ClassBox container) {
        super(container);
        this.items = new ArrayList<>();
        setVertex(true);
        setValue("    operations");
        setStyle("fontFamily=Verdana;align=right;fontSize=10;fontStyle=0;strokeColor=blue;verticalAlign=top;overflow=hidden");
        component_font = new Font("Verdana", Font.PLAIN, 12);
    }

    // properties
    public Iterator<OperationsBoxItem> getItems() {
        return items.iterator();
    }

    public void addOperation(Operation feature) {
        items.add(new OperationsBoxItem(feature, this));
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    protected DiagramComponent component(int i) {
        return items.get(i);
    }

    @Override
    public Font getComponentFont() {
        return component_font;
    }

    @Override
    public Font getFont() {
        return container.getComponentFont();
    }

    void clear() {
        for (OperationsBoxItem item : items) {
            item.remove();
        }
        items.clear();
    }
}
