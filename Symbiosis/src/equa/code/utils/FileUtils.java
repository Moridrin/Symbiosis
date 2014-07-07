/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import equa.controller.SwingProjectController;

/**
 *
 * @author S. Roubtsov
 */
public class FileUtils {

    public static void saveGeneratedIntoFolder(String classname, ArrayList<String> generated, String language, String... folder) {
        SwingProjectController projectController = SwingProjectController.getReference();
        String currentDirName = folder.length > 0 ? folder[0] : projectController.getProject().getFile().getAbsolutePath();

        currentDirName = currentDirName.substring(0, currentDirName.lastIndexOf(File.separator)) + File.separator
                + "Generated." + language;
        File theFolder = new File(currentDirName);
        if (!theFolder.exists()) {
            theFolder.mkdir();
        }

        currentDirName = currentDirName + File.separatorChar + projectController.getProject().getName();
        theFolder = new File(currentDirName);
        if (!theFolder.exists()) {
            theFolder.mkdir();
        }

        try {
            String classFileName = currentDirName + File.separator + classname + "." + language;
            PrintWriter pw = new PrintWriter(new FileOutputStream(classFileName));
            for (String codeLine : generated) {
                pw.println(codeLine);
            }
            //     System.out.println("Class " + classname + " generated and stored into " + classFileName);
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
