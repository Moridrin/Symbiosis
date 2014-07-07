/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.diagram.ord.deprecated;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.w3c.dom.Document;

import com.mxgraph.examples.swing.editor.DefaultFileFilter;
import com.mxgraph.io.mxCodec;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

import equa.desktop.Desktop;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;

/**
 *
 * @author Maurice-Asus
 */
public class ObjectRoleDiagramPanel extends JPanel implements Dockable {

    private static final long serialVersionUID = 1L;
    private transient DockKey key = new DockKey("ORD");
    private transient ObjectRoleDiagram controller;
    private transient mxGraph graph;
    private transient mxGraphComponent graphComponent;
    private transient Desktop root;
    private transient ObjectModel om;

    /**
     * Construtor to create a new ORD.
     *
     * @param om
     * @param name
     * @param root
     */
    public ObjectRoleDiagramPanel(ObjectModel om, String name, Desktop root) {
        this(om, name, new ArrayList<FactType>());

        this.root = root;
        this.om = om;
        graphComponent.getHorizontalScrollBar().setUnitIncrement(50);

        this.initMouseListeners(this.graphComponent);
    }

    /**
     * Constructor in case if an existing ORD is loaded.
     *
     * @param om
     * @param contr
     * @param root
     */
    public ObjectRoleDiagramPanel(ObjectModel om, ObjectRoleDiagram contr, Desktop root) {
        this.root = root;
        key.setName(contr.getName());
        if (contr.getGraphComponent() != null) //If user opens a closed tab by the user in an open application
        {
            graph = contr.getGraphComponent().getGraph();
            graphComponent = contr.getGraphComponent();
        } // If the user has loaded the file and opens the tab.
        else {

            init();

            contr.setGraphComponent(graphComponent);
        }

        controller = contr;

        postInit();
    }

    public ObjectRoleDiagramPanel(ObjectModel om, String name, ArrayList<FactType> types) {
        key.setName(name);

        init();

        controller = new ObjectRoleDiagram(name, om, types, graphComponent, this);

        postInit();

        //Position the roleboxes using a layout manager.
        mxStackLayout layout = new mxStackLayout(graph, true, 20);
        //mxCircleLayout layout = new mxCircleLayout(graph);
        layout.execute(graph.getDefaultParent());
    }

    /**
     * Initializes the graph and graphComponent objects. It sets all the
     * settings needed to make the diagram behave and look the way it should.
     */
    private void init() {
        graph = new mxGraph() {
            @Override
            public boolean isCellMovable(Object cell) {
                if (((mxCell) getModel().getParent(cell)).getId().equals("rbParentCell")) {
                    return false;
                }

                if (cell instanceof OTBox) {
                    return !getModel().isVertex(getModel().getParent(((OTBox) cell).getDock()));
                }
                return !getModel().isVertex(getModel().getParent(cell));
            }

            @Override
            public boolean isCellSelectable(Object cell) {
                if (cell instanceof FTorOTBox || cell instanceof RoleBox || cell instanceof RoleConnector || cell instanceof InheritanceConnector || ((mxCell) cell).getChildCount() == 1 && ((mxCell) cell).getChildAt(0) instanceof FTorOTBox) {
                    return super.isCellSelectable(cell);
                }
                return false;
            }

            @Override
            public Object[] moveCells(Object[] cells, double dx, double dy, boolean clone, Object target, Point location) {
                if (cells[0] instanceof OTBox) {
                    cells[0] = ((OTBox) cells[0]).getParent();
                }
                return super.moveCells(cells, dx, dy, clone, target, location);
            }
        };
        mxCodec codec = new mxCodec();
        Document doc = mxUtils.loadDocument(ObjectRoleDiagramPanel.class.getResource(
                "default-style.xml").toString());
        codec.decode(doc.getDocumentElement(), graph.getStylesheet());

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

        graph.setCellsResizable(false);
        graph.setCellsCloneable(false);
        graph.setCellsLocked(true);
        graphComponent.setFoldingEnabled(false);
        //Disables the creation of new lines by the user
        graphComponent.setConnectable(false);
        //Disables drag and drop of cells.
        graph.setDropEnabled(false);
        graphComponent.setDragEnabled(false);

        Map<String, Object> style = graph.getStylesheet().getDefaultEdgeStyle();
        style.put(mxConstants.STYLE_EDGE, mxEdgeStyle.EntityRelation);
        style.put(mxConstants.STYLE_ROUNDED, Boolean.TRUE);
    }

    /**
     * Creates the graph, sets the layout and adds the mouseListeners. The
     * layout sets the default positions of the ORD boxes.
     *
     * @param loadedGraph
     */
    private void postInit() {
        graph.getModel().beginUpdate();
        try {
            createGraph();
            setRelations();
        } catch (Exception e) {
            Logger.getLogger(ObjectRoleDiagramPanel.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.getModel().endUpdate();
        }

        graphComponent.invalidate();
        setLayout(new BorderLayout());
        add(graphComponent);

        repaint();

        this.initMouseListeners(this.graphComponent);
    }

    @Override
    public DockKey getDockKey() {
        return key;
    }

    public ObjectRoleDiagram getController() {
        return controller;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private void newObjectType(OTBox model) {
        ArrayList<String> ucConstraints = new ArrayList<String>();
        mxCell dock = (mxCell) graph.addCell(model.getDock());
        mxCell parent = (mxCell) graph.addCell(model, dock);

        graph.addCell(model.getTypeExpressionCell(), model);

        int i = 0;
        double dockWidth = dock.getGeometry().getWidth();
        double maxLengthInPixels = model.getMaxLengthInPixels();
        double startPosition = (dockWidth / 2) - ((maxLengthInPixels + 40) / 2);
        double begin = 0;

        for (RoleBox roleBox : model.getRoleBoxes()) {
            roleBox.getRoleBoxParentCell().setGeometry(new mxGeometry(startPosition, 30 + i * 20, maxLengthInPixels + 20, 20));
            graph.addCell(roleBox.getRoleBoxParentCell(), parent);

            roleBox.addRoleBox(ucConstraints, begin, maxLengthInPixels);
            roleBox.getRoleBoxParentCell().setId("rbParentCell");
            i++;
            begin = 0;
        }
    }

    public mxCell newFactType(FTBox model) {
        ArrayList<String> ucConstraints = new ArrayList<String>();

        mxCell parent = (mxCell) graph.addCell(model);

        graph.addCell(model.getTypeExpressionCell(), model);

        int i = 0;
        double maxLengthInPixels = model.getMaxLengthInPixels();
        double begin = 0;

        for (RoleBox roleBox : model.getRoleBoxes()) {
            roleBox.getRoleBoxParentCell().setGeometry(new mxGeometry(0, 30 + i * 20, maxLengthInPixels + 20, 20));
            graph.addCell(roleBox.getRoleBoxParentCell(), parent);

            roleBox.addRoleBox(ucConstraints, begin, maxLengthInPixels);
            roleBox.getRoleBoxParentCell().setId("rbParentCell");
            i++;
            begin = 0;
        }

        return model;
    }

    public void newRelation(mxICell source, mxICell destination) {
        graph.insertEdge(graph.getDefaultParent(), null, "", source, destination, "startArrow=none;endArrow=none;strokeWidth=1;");
    }

    public void newInherritanceConnector(mxCell source, mxCell destination) {
        graph.insertEdge(graph.getDefaultParent(), null, "", source, destination, "elbow=horizontal;startArrow=none;endArrow=open;strokeWidth=1;endSize=15");
    }

    public void newMandatoryRelation(mxCell source, mxCell destination) {
        graph.insertEdge(graph.getDefaultParent(), null, "", source, destination, "startArrow=none;endArrow=oval;strokeWidth=1;endSize=15");
    }

    public void newCompositeRelation(mxCell source, mxCell destination) {
        graph.insertEdge(graph.getDefaultParent(), null, "", source, destination, "startArrow=none;endArrow=diamond;strokeWidth=1;endSize=20");
    }

    private void createGraph() {
        for (FTorOTBox model : controller.getTypes()) {
            if (model instanceof FTBox) {
                newFactType((FTBox) model);
            }
            if (model instanceof OTBox) {
                newObjectType((OTBox) model);
            }
        }
    }

    private void setRelations() {
        for (FTorOTBox model : controller.getTypes()) {
            for (RoleBox roleBox : model.getRoleBoxes()) {
                if (roleBox instanceof ObjectRoleBox) {
                    ObjectRoleBox objectRoleBox = (ObjectRoleBox) roleBox;

                    //mxCell source = (mxCell) ((mxGraphModel) graph.getModel()).getCell(model.getFactType().getName() + roleBox.getRole().getRoleNameOrNr());
                    //mxCell destination = (mxCell) ((mxGraphModel) graph.getModel()).getCell(objectRoleBox.getRoleConnector().getOtBox().getObjectType().getName());
                    if (objectRoleBox != null) {
                        if (objectRoleBox.getRoleConnector().getOtBox() != null) {
                            newRelation(roleBox, objectRoleBox.getRoleConnector().getOtBox().getParent());
                        }
                    }
                }
            }

            if (model instanceof OTBox) {
                OTBox ot = (OTBox) model;

                if (ot != null) {
                    for (InheritanceConnector connector : ot.getInheritanceConnector()) {
                        newInherritanceConnector(connector.getSource(), connector.getDestination());
                    }
                }
            }
        }
    }

    private void initMouseListeners(final mxGraphComponent component) {
        component.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Object cell = getController().getGraphComponent().getCellAt(e.getX(), e.getY());
                    if (cell != null) {
//                        if (cell instanceof OTBox) {
//                            root.getPropertyEditor().initPropertySheet(((OTBox) cell).getObjectType());
//                        } else if (cell instanceof BaseValueRoleBox) {
//                            BaseValueRoleBox baseValueRoleBox = (BaseValueRoleBox) cell;
//                            root.getPropertyEditor().initPropertySheet(baseValueRoleBox.getRole());
//                        } else if (cell instanceof ObjectRoleBox) {
//                            ObjectRoleBox objectRoleBox = (ObjectRoleBox) cell;
//                            root.getPropertyEditor().initPropertySheet(objectRoleBox.getRole());
//                        } else if (((mxCell) cell).getParent() instanceof FTorOTBox) {
//                            FTorOTBox fTorOTBox = (FTorOTBox) ((mxCell) cell).getParent();
//                            root.getPropertyEditor().initPropertySheet(fTorOTBox.getFactType().getTE());
//                        } else {
//                            //System.out.println("else ord mouseClicked:" + cell.getClass());
//                        }
                    }
                    if (cell == null) {
//                        root.getPropertyEditor().getPropertySheetMain().removeAll();
//                        root.getPropertyEditor().updateUI();
                    }
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Object cell = getController().getGraphComponent().getCellAt(e.getX(), e.getY());
                    if (cell != null && (cell instanceof BaseValueRoleBox || cell instanceof ObjectRoleBox)) {
                        //Show ORDPopupMenu
                        ORDPopupMenu ordMenu = new ORDPopupMenu(root.getFrame(), component.getGraph().getSelectionCells(), om);
                        ordMenu.getJPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) ///This sections takes care if a cell is found
                {
                    Object cell = getController().getGraphComponent().getCellAt(e.getX(), e.getY());
                    if (cell != null) {
                        //Print op welke cell je klikt (zat er al in)
                        //System.out.println("cell=" + graph.getLabel(cell));
                    } else {
                        //System.out.println("No cell found");
                    }
                }
            }
        });
    }

    /**
     * Initialises the event listener used when a label has been changed.
     */
//    private void initLabelChangeEventListener() {
//        graph.addListener(mxEvent.LABEL_CHANGED, new mxIEventListener() {
//            @Override
//			public void invoke(Object sender, mxEventObject evt) {
//                mxCell cell = (mxCell) evt.getProperty("cell");
//                String value = evt.getProperty("value").toString();
//            }
//        });
//    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().toLowerCase().equals("newtype")) {
            ((mxGraphModel) graph.getModel()).clear();
            graph.getModel().beginUpdate();

            try {
                createGraph();
                setRelations();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                graph.getModel().endUpdate();
            }

            Object parent = graph.getDefaultParent();
            setLayout(new BorderLayout());
            mxStackLayout layout = new mxStackLayout(graph, true, 20);
            layout.execute(parent);
            repaint();
        }
    }

    public void saveORDToImage() {
        if (controller != null) {
            DefaultFileFilter selectedFilter = null;
            DefaultFileFilter jpgFilter = new DefaultFileFilter(".jpg", "JPG " + " (.jpg)");

            String filename = null;

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
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(graphComponent, ex.toString(), mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(root.getFrame(), "The ORD could not be exported.", mxResources.get("error"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
