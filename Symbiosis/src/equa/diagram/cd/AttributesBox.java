package equa.diagram.cd;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;

import equa.diagram.DiagramComponent;
import equa.diagram.DiagramContainer;
import equa.meta.classrelations.Relation;

public class AttributesBox extends DiagramContainer {

    // fields
    private static final long serialVersionUID = 1L;
    private ArrayList<Attribute> attributes;
    private Font component_font;

    // construction
    public AttributesBox(ClassBox container) {
        super(container);
        this.attributes = new ArrayList<>();
        setVertex(true);
        setValue("    attributes");
        setStyle("fontFamily=Verdana;align=right;fontSize=10;fontStyle=0;strokeColor=blue;verticalAlign=top;overflow=hidden");
        component_font = new Font("Verdana", Font.PLAIN, 12);
    }

    public Iterator<Attribute> getAttributes() {
        return attributes.iterator();
    }

    public void addAttribute(Relation relation) {
        attributes.add(new Attribute(relation, this));
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    protected DiagramComponent component(int i) {
        return attributes.get(i);
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
        for (Attribute attribute : attributes) {
            attribute.remove();
        }
        attributes.clear();
    }
}
