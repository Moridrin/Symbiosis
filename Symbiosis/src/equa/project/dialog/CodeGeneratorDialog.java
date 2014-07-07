package equa.project.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import equa.code.Languages;
import equa.controller.ProjectController;

@SuppressWarnings("serial")
public class CodeGeneratorDialog extends JDialog {

    private ProjectController projectController;
    private JCheckBox chkBoxLibrary, chkBoxORM, chkBoxMInh;

    /**
     * Create the dialog.
     */
    public CodeGeneratorDialog(JFrame parent, ProjectController controller) {
        super(parent, true);
        setTitle("Generate Code");
        projectController = controller;

        String rootNS = controller.getProject().getRootNameSpace().getName();
        String currentLocation = System.getProperty("user.dir");

        // Top Panel
        JPanel topPanel = new JPanel();
        getContentPane().add(topPanel, BorderLayout.PAGE_START);
        JLabel lblTop = new JLabel("Generate code from your Object Model");
        topPanel.add(lblTop);

        // Center panel with options
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(5, 5, 5, 5);
        JLabel lblLanguage = new JLabel("Language:");
        panel.add(lblLanguage, c);
        c.gridx = 1;
        final JComboBox<Languages> cmbBoxLanguages = new JComboBox<>(Languages.values());
        panel.add(cmbBoxLanguages, c);
        c.gridx = 0;
        c.gridy = 1;
        JLabel lblLocation = new JLabel("Location:");
        panel.add(lblLocation, c);
        c.gridx = 1;
        final JTextField txtLocation = new JTextField(20);
        txtLocation.setText(currentLocation);
        panel.add(txtLocation, c);
        c.gridx = 2;
        JButton btnBrowse = new JButton("Browse");
        panel.add(btnBrowse, c);
        c.gridx = 0;
        c.gridy = 2;
        JLabel lblRootNS = new JLabel("Root namespace:");
        panel.add(lblRootNS, c);
        c.gridx = 1;
        final JTextField txtRootNS = new JTextField(20);
        txtRootNS.setText(rootNS);
        panel.add(txtRootNS, c);
        c.gridx = 0;
        c.gridy = 3;
        JLabel lblLibrary = new JLabel("Generate as library:");
        panel.add(lblLibrary, c);
        c.gridx = 1;
        chkBoxLibrary = new JCheckBox();
        panel.add(chkBoxLibrary, c);
        c.gridx = 0;
        c.gridy = 4;
        JLabel lblORM = new JLabel("Generate ORM:");
        panel.add(lblORM, c);
        c.gridx = 1;
        chkBoxORM = new JCheckBox();
        panel.add(chkBoxORM, c);
        c.gridx = 0;
        c.gridy = 5;
        JLabel lblMInh = new JLabel("Multiple Inheritance:");
        panel.add(lblMInh, c);
        c.gridx = 1;
        chkBoxMInh = new JCheckBox();
        panel.add(chkBoxMInh, c);
        setCheckBoxes((Languages) cmbBoxLanguages.getSelectedItem());

        cmbBoxLanguages.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Languages l = (Languages) ((JComboBox<Languages>) e.getSource()).getSelectedItem();
                setCheckBoxes(l);
            }
        });

        btnBrowse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fChoser = new JFileChooser();
                fChoser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fChoser.showOpenDialog(CodeGeneratorDialog.this) == JFileChooser.APPROVE_OPTION) {
                    File f = fChoser.getSelectedFile();
                    txtLocation.setText(f.getAbsolutePath());
                }
            }
        });

        // Bottom panel with the buttons
        JPanel bottomPanel = new JPanel();
        getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
        JButton btnGenerate = new JButton("Generate");
        btnGenerate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    projectController.getProject().setRootNameSpace(txtRootNS.getText());
                    projectController.getProject().getObjectModel().generateClasses(true, false);
                    ((Languages) cmbBoxLanguages.getSelectedItem()).language().generate(projectController.getProject().getObjectModel(), chkBoxLibrary.isSelected(), chkBoxORM.isSelected(), chkBoxMInh.isSelected(), txtLocation.getText());
                    CodeGeneratorDialog.this.dispose();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(CodeGeneratorDialog.this, "There was an IO error, is the location correct? Otherwise compilation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(CodeGeneratorDialog.this, "There was an unspecified error. Is your OM correct? Otherwise this could be a bug", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        bottomPanel.add(btnGenerate);
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(btnCancel);

        pack();
    }

    private void setCheckBoxes(Languages l) {
        chkBoxLibrary.setEnabled(l.library());
        chkBoxORM.setEnabled(l.orm());
        chkBoxMInh.setEnabled(l.mInh());
        if (!l.orm()) {
            chkBoxORM.setSelected(false);
        }
        if (!l.mInh()) {
            chkBoxMInh.setSelected(false);
        }
        if (!l.library()) {
            chkBoxLibrary.setSelected(false);
        }
    }
}
