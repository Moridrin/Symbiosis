package equa.diagram.cd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.w3c.dom.Document;

import com.mxgraph.examples.swing.editor.DefaultFileFilter;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

import equa.controller.ProjectController;
import equa.diagram.DiagramComponent;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectType;

public class ControllerBasedClassDiagramPanel extends JPanel implements Dockable {

    private static final long serialVersionUID = 1L;
    //public static final int MIN_WIDTH = 60;
    private DockKey key;
    private ClassDiagram classDiagram;
    private mxGraph graph;
    private mxGraphComponent graphComponent;

    public ControllerBasedClassDiagramPanel(String name, ProjectController projectController, boolean newPanel) {
        key = new DockKey(name);
        this.key.setCloseEnabled(false);
        init();

        this.classDiagram = new ClassDiagram(name, projectController.getProject().getObjectModel());

        postInit(newPanel);
    }

    public ControllerBasedClassDiagramPanel(String name, ProjectController projectController, Collection<FactType> selection, boolean newPanel) {
        key = new DockKey(name);

        init();

        Collection<ObjectType> objectTypes = new ArrayList<>();
        for (FactType ft : selection) {
            if (ft.isObjectType()) {
                objectTypes.add(ft.getObjectType());
            }
        }
        this.classDiagram = new ClassDiagram(name, objectTypes);

        postInit(newPanel);
    }

    public ControllerBasedClassDiagramPanel(ProjectController projectController, ClassDiagram classDiagram) {
        key = new DockKey(classDiagram.getName());

        init();
        this.classDiagram = classDiagram;

        postInit(false);
    }

    public ControllerBasedClassDiagramPanel(ProjectController projectController, ClassDiagram classDiagram, Boolean postInit) {
        key = new DockKey(classDiagram.getName());

        init();
        this.classDiagram = classDiagram;

        postInit(postInit);
    }

    public ClassDiagram getClassDiagram() {
        return classDiagram;
    }

    public void setClassDiagram(ClassDiagram classDiagram) {
        this.classDiagram = classDiagram;
    }

    public mxGraph getGraph() {
        return graph;
    }

    /**
     * Common code for both of the constructors. This part might not be needed
     * if the user closed the application and loads the file.
     */
    private void init() {
        graph = new mxGraph() {
            //Disallows the child to be moved outside of the parent
            @Override
            public boolean isCellMovable(Object cell) {
                return cell instanceof ClassBox;
            }

            @Override
            public boolean isCellFoldable(Object cell, boolean collapse) {
                if (cell instanceof OperationsBox || cell instanceof AttributesBox) {
                    return true;
                }
                return false;
            }
//            @Override
//            public void updateAlternateBounds(Object cell, mxGeometry geo,
//                    boolean willCollapse) {
//                if (cell != null && geo != null) {
//                    if (geo.getAlternateBounds() == null) {
//                        mxRectangle bounds = null;
//
//                        if (isCollapseToPreferredSize()) {
//                            bounds = getPreferredSizeForCell(cell);
//
//                            if (isSwimlane(cell)) {
//                                mxRectangle size = getStartSize(cell);
//
//                                bounds.setHeight(Math.max(bounds.getHeight(),
//                                        size.getHeight()));
//                                bounds.setWidth(Math.max(bounds.getWidth(),
//                                        size.getWidth()));
//                            }
//                        }
//
//                        if (bounds == null) {
//                            bounds = geo;
//                        }
//
//                        mxRectangle heightSize = getPreferredSizeForCell(cell);
//
//                        geo.setAlternateBounds(new mxRectangle(geo.getX(), geo.getY(),
//                                bounds.getWidth(), heightSize.getHeight()));
//
//                        mxCell parent = (mxCell) cell;
//
//
//                        while (!(parent instanceof ClassBox)) {
//                            heightSize = getPreferredSizeForCell(parent);
//                            graph.resizeCell(parent, new mxRectangle(parent.getGeometry().getX(),
//                                    parent.getGeometry().getY(), parent.getGeometry().getWidth(), heightSize.getHeight()));
//
//                            parent = (mxCell) parent.getParent();
//                        }
//
//                        graph.updateCellSize(parent, false);
//
//                        heightSize = getPreferredSizeForCell(parent);
//                        graph.resizeCell(parent, new mxRectangle(parent.getGeometry().getX(),
//                                parent.getGeometry().getY(), parent.getGeometry().getWidth(), heightSize.getHeight()));
//                    } else {
//                        geo.getAlternateBounds().setX(geo.getX());
//                        geo.getAlternateBounds().setY(geo.getY());
//                    }
//                }
//            }
        };

        ((mxGraphModel) graph.getModel()).setCreateIds(true);

        graphComponent = new mxGraphComponent(graph) {
            private static final long serialVersionUID = 1L;

            @Override
            public mxGraphHandler createGraphHandler() {
                return new mxGraphHandler(this) {
                    @Override
                    public boolean isRemoveCellsFromParent() {
                        return false;
                    }
                };
            }
        };

        // Disable the editing
        graph.setCellsLocked(true);

        // Disallows the user to resize
        graph.setCellsResizable(true);

        // Disallows the user to clone
        graph.setCellsCloneable(false);

        // Ensures that cells properly resize while collapsing
        graph.setCollapseToPreferredSize(false);

        graph.setCellsSelectable(true);

        // Disables the creation of new lines by the user
        graphComponent.setConnectable(false);

        // Disables drag and drop of cells.
        graph.setDropEnabled(false);
        graphComponent.setDragEnabled(false);

        graphComponent.getHorizontalScrollBar().setUnitIncrement(50);

        // Loads the style sheet.
        Document doc = mxUtils.loadDocument(ClassDiagram.class.getResource(
                "default-style.xml").toString());
        mxCodec codec = new mxCodec();
        codec.decode(doc.getDocumentElement(), graph.getStylesheet());
    }

    /**
     * Common code for both of the constructors. This is the second part and is
     * always executed.
     */
    final void postInit(boolean newPanel) {

        // Adds cells to the model in a single step. This creates our diagram.
        graph.getModel().beginUpdate();
        try {
            if (newPanel) {
                defaultPositionClassBoxes();
            }
            drawClassBoxes();

            setLayout(new BorderLayout());
            add(graphComponent);

            drawConnectors();
        } finally {
            graph.getModel().endUpdate();
        }

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Object cell;
                    cell = graphComponent.getCellAt(e.getX(), e.getY());
                    if (cell != null) {
                        if (cell instanceof OperationsBoxItem) {
//                            OperationsBoxItem operation = (OperationsBoxItem) cell;
                            //desktop.getPropertyEditor().initPropertySheet(operation);
                        } else if (cell instanceof Attribute) {
//                            Attribute attribute = (Attribute) cell;
                            //desktop.getPropertyEditor().initPropertySheet(attribute);
                        }
                    } else {
                        //desktop.getPropertyEditor().getPropertySheetMain().removeAll();
                        //desktop.getPropertyEditor().updateUI();
                    }
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    //CDPopup cdMenu = new CDPopup(ControllerBasedClassDiagramPanel.this, e.getX(), e.getY());
                    //cdMenu.getJPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public void unfold(Attribute attribute) {
        ObjectType ot = (ObjectType) attribute.getRelation().targetType();
        eraseCells();
        graph.clearSelection();
        graph.getModel().beginUpdate();
        try {
            mxGeometry geoClassBox = attribute.getContainer().getContainer().getGeometry();
            ClassBox classBox = classDiagram.unfold(ot);
            classBox.setGeometry(new mxGeometry(geoClassBox.getX() + geoClassBox.getWidth() + 100, geoClassBox.getY(), 0, 0));
            drawClassBoxes();
            drawConnectors();
        } finally {
            graph.getModel().endUpdate();
        }
    }

    public void fold(ClassBox classBox) {
        ObjectType ot = classBox.getObjectType();
        graph.clearSelection();

        graph.getModel().beginUpdate();
        try {
            eraseCells();
            classDiagram.fold(ot);
            drawClassBoxes();
            drawConnectors();
        } finally {
            graph.getModel().endUpdate();
            graph.getView().validate();
        }
    }

    private void eraseCells() {
        eraseConnectors();
        eraseClassBoxes();
    }

    int getCells() {
        Iterator<ClassBox> it = classDiagram.classBoxes();
        int c = 0;
        while (it.hasNext()) {
            c += it.next().getChilds();
        }
        return c;
    }

    /**
     * This method loops through all the classboxes and creates the graph.
     */
    private void drawClassBoxes() {
        Iterator<ClassBox> itClassBoxes = classDiagram.classBoxes();
        while (itClassBoxes.hasNext()) {
            ClassBox classBox = itClassBoxes.next();
            drawClassBox(classBox);
        }
    }

    private void eraseClassBoxes() {
        Iterator<ClassBox> itClassBoxes = classDiagram.classBoxes();
        while (itClassBoxes.hasNext()) {
            ClassBox classBox = itClassBoxes.next();
            eraseClassBox(classBox);
        }
    }

    private void defaultPositionClassBoxes() {
        Iterator<ClassBox> itClassBoxes = classDiagram.classBoxes();
        int i = 0;
        while (itClassBoxes.hasNext()) {
            ClassBox classBox = itClassBoxes.next();
            classBox.setGeometry(new mxGeometry(150 * ((i * 100) / 800), (i * 100) % 800, 0, 0));
            i++;
        }
    }

    private void drawConnectors() {
        // drawing of connectors after the sizing and positioning of the classboxes
        Iterator<ClassBox> itClassBoxes = classDiagram.classBoxes();
        while (itClassBoxes.hasNext()) {
            ClassBox classBox = itClassBoxes.next();
            drawConnectors(classBox);
        }
    }

    private void eraseConnectors() {
        Iterator<ClassBox> itClassBoxes = classDiagram.classBoxes();
        while (itClassBoxes.hasNext()) {
            ClassBox classBox = itClassBoxes.next();
            eraseConnectors(classBox);
        }
    }

    private void drawClassBox(ClassBox classBox) {
        // work around to force a refresh of the class box:
        classBox.setGeometry(classBox.getGeometry());

        graph.addCell(classBox);

        // AttributesBox
        graph.addCell(classBox.getAttributesBox(), classBox);

        // OperationsBox
        graph.addCell(classBox.getOperationsBox(), classBox);

        // inspect and add attributes
        Iterator<Attribute> itAttributes = classBox.getAttributesBox().getAttributes();
        while (itAttributes.hasNext()) {
            Attribute attribute = itAttributes.next();
            graph.addCell(attribute, classBox.getAttributesBox());
        }

        // inspect and add operations
        Iterator<OperationsBoxItem> itOperations = classBox.getOperationsBox().getItems();
        while (itOperations.hasNext()) {
            OperationsBoxItem item = itOperations.next();
            graph.addCell(item, classBox.getOperationsBox());
        }
    }

    private void eraseClassBox(ClassBox classBox) {
        for (int i = classBox.getOperationsBox().getChildCount() - 1; i >= 0; i--) {
            classBox.getOperationsBox().remove(i);
        }
        for (int i = classBox.getAttributesBox().getChildCount() - 1; i >= 0; i--) {
            classBox.getAttributesBox().remove(i);

        }
        classBox.remove(classBox.getAttributesBox());
        classBox.remove(classBox.getOperationsBox());
        classBox.removeFromParent();
    }

    private void drawConnectors(ClassBox classBox) {
        Iterator<GeneralizationConnector> itGeneralizationConnectors = classBox.getGeneralizationConnectors();
        while (itGeneralizationConnectors.hasNext()) {
            GeneralizationConnector inheritanceConnector = itGeneralizationConnectors.next();
            drawGeneralizationConnector(inheritanceConnector);
        }

        Iterator<AssociationConnector> itAssociationConnectors = classBox.getAssociationConnectors();
        while (itAssociationConnectors.hasNext()) {
            AssociationConnector associationConnector = itAssociationConnectors.next();
            drawAssociationConnector(associationConnector);
        }
    }

    private void eraseConnectors(ClassBox classBox) {
        for (int i = classBox.getEdgeCount() - 1; i >= 0; i--) {
            classBox.getEdgeAt(i).removeFromParent();
        }
    }

    /**
     * Draws an association connector to the diagram given an
     * AssociationConnector.
     *
     * @param connector
     */
    public void drawAssociationConnector(AssociationConnector connector) {
        DiagramComponent start = connector.getStart();
        DiagramComponent destination = connector.getDestination();

        mxCell edge = (mxCell) graph.createEdge(graph.getDefaultParent(), null, connector.getRoleText(), start,
                destination, connector.getStyle());

        int distance;
        if (connector.getRank() == 0) {
            distance = 20;
        } else {
            distance = -20;
        }
        edge.setGeometry(new mxGeometry(0.8, distance, 0, 0));
        edge.getGeometry().setRelative(true);

        graph.addEdge(edge, graph.getDefaultParent(), start, destination, null);
    }

    public void drawGeneralizationConnector(GeneralizationConnector connector) {
        ClassBox subClass = connector.getSubClass();
        ClassBox superClass = connector.getSuperClass();

        graph.insertEdge(graph.getDefaultParent(), null, "", subClass,
                superClass, connector.getStyle());
    }

    public void saveCDToImage() {

        DefaultFileFilter selectedFilter;
        DefaultFileFilter jpgFilter = new DefaultFileFilter(".jpg", "JPG " + " (.jpg)");

        String filename;

        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

        javax.swing.filechooser.FileFilter defaultFilter = jpgFilter;

        fc.addChoosableFileFilter(defaultFilter);

        fc.setFileFilter(defaultFilter);

        fc.showDialog(null, "Save");

        filename = fc.getSelectedFile().getAbsolutePath();

        selectedFilter = (DefaultFileFilter) fc.getFileFilter();

        if (selectedFilter instanceof DefaultFileFilter) {
            String ext = (selectedFilter).getExtension();

            if (!filename.toLowerCase().endsWith(ext)) {
                filename += ext;
            }
        }

        try {
            Color bg = graphComponent.getBackground();

            BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, bg,
                    graphComponent.isAntiAlias(), null,
                    graphComponent.getCanvas());

            if (image != null) {
                ImageIO.write(image, "jpg", new File(filename));
            } else {
                JOptionPane.showMessageDialog(graphComponent,
                        mxResources.get("noImageData"));
            }
        } catch (IOException | HeadlessException ex) {
            JOptionPane.showMessageDialog(graphComponent, ex.toString(), mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        graphComponent.refresh();
    }

    @Override
    public DockKey getDockKey() {
        return key;
    }

    @Override
    public Component getComponent() {
        return this;
    }
}
