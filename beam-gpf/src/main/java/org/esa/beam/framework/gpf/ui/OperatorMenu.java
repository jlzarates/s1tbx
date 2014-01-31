/*
 * Copyright (C) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.framework.gpf.ui;

import com.bc.ceres.binding.dom.DomElement;
import com.bc.ceres.binding.dom.XppDomElement;
import com.jidesoft.action.CommandMenuBar;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.XppDomWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.io.xml.xppdom.XppDom;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.framework.ui.AbstractDialog;
import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.framework.ui.ModalDialog;
import org.esa.beam.framework.ui.UIUtils;
import org.esa.beam.util.Debug;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.util.io.FileUtils;
import org.xmlpull.mxp1.MXParser;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * WARNING: This class belongs to a preliminary API and may change in future releases.
 * <p/>
 * Provides an operator menu with action for loading, saving and displaying the parameters of an operator
 * in the file menu section and actions for help and about in the help menu section.
 *
 * @author Norman Fomferra
 * @author Marco Zühlke
 */
public class OperatorMenu {

    private final Component parentComponent;
    private final OperatorParameterSupport parameterSupport;
    private final Class<? extends Operator> opType;
    private final AppContext appContext;
    private final String helpId;
    private final Action loadParametersAction;
    private final Action saveParametersAction;
    private final Action displayParametersAction;
    private final Action aboutAction;
    private final String lastDirPreferenceKey;

    public OperatorMenu(Component parentComponent,
                        Class<? extends Operator> opType,
                        OperatorParameterSupport parameterSupport,
                        AppContext appContext,
                        String helpId) {
        this.parentComponent = parentComponent;
        this.parameterSupport = parameterSupport;
        this.opType = opType;
        this.appContext = appContext;
        this.helpId = helpId;
        lastDirPreferenceKey = opType.getCanonicalName() + ".lastDir";
        loadParametersAction = new LoadParametersAction();
        saveParametersAction = new SaveParametersAction();
        displayParametersAction = new DisplayParametersAction();
        aboutAction = new AboutOperatorAction();
    }

    /**
     * Creates the default menu.
     *
     * @return The menu
     */
    public JMenuBar createDefaultMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(loadParametersAction);
        fileMenu.add(saveParametersAction);
        fileMenu.addSeparator();
        fileMenu.add(displayParametersAction);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(createHelpMenuItem());
        helpMenu.add(aboutAction);

        final JMenuBar menuBar;
        if (SystemUtils.isRunningOnMacOS()) {
            menuBar = new JMenuBar();
        } else {
            menuBar = new CommandMenuBar();
        }
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JMenuItem createHelpMenuItem() {
        JMenuItem menuItem = new JMenuItem("Help");
        if (helpId != null && !helpId.isEmpty()) {
            HelpSys.enableHelpOnButton(menuItem, helpId);
        } else {
            menuItem.setEnabled(false);
        }
        return menuItem;
    }

    private class LoadParametersAction extends AbstractAction {

        private static final String TITLE = "Load Parameters";

        LoadParametersAction() {
            super(TITLE + "...");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(createParameterFileFilter());
            fileChooser.setDialogTitle(TITLE);
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            applyCurrentDirectory(fileChooser);
            int response = fileChooser.showDialog(parentComponent, "Load");
            if (JFileChooser.APPROVE_OPTION == response) {
                try {
                    preserveCurrentDirectory(fileChooser);
                    readFromFile(fileChooser.getSelectedFile());
                } catch (Exception e) {
                    Debug.trace(e);
                    JOptionPane.showMessageDialog(parentComponent, "Could not load parameters.\n" + e.getMessage(),
                                                  TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && parameterSupport != null;
        }

        private void readFromFile(File selectedFile) throws Exception {
            try (FileReader reader = new FileReader(selectedFile)) {
                DomElement domElement = readXml(reader);
                parameterSupport.fromDomElement(domElement);
            }
        }

        private DomElement readXml(Reader reader) throws IOException {
            try (BufferedReader br = new BufferedReader(reader)) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                return new XppDomElement(createDom(sb.toString()));
            }
        }

        private XppDom createDom(String xml) {
            XppDomWriter domWriter = new XppDomWriter();
            new HierarchicalStreamCopier().copy(new XppReader(new StringReader(xml), new MXParser()), domWriter);
            return domWriter.getConfiguration();
        }
    }

    private class SaveParametersAction extends AbstractAction {

        private static final String TITLE = "Save Parameters";

        SaveParametersAction() {
            super(TITLE + "...");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            final FileNameExtensionFilter parameterFileFilter = createParameterFileFilter();
            fileChooser.addChoosableFileFilter(parameterFileFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setDialogTitle(TITLE);
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            applyCurrentDirectory(fileChooser);
            int response = fileChooser.showDialog(parentComponent, "Save");
            if (JFileChooser.APPROVE_OPTION == response) {
                try {
                    preserveCurrentDirectory(fileChooser);
                    File selectedFile = fileChooser.getSelectedFile();
                    selectedFile = FileUtils.ensureExtension(selectedFile,
                                                             "." + parameterFileFilter.getExtensions()[0]);
                    String xmlString = parameterSupport.toDomElement().toXml();
                    writeToFile(xmlString, selectedFile);
                } catch (Exception e) {
                    Debug.trace(e);
                    JOptionPane.showMessageDialog(parentComponent, "Could not save parameters.\n" + e.getMessage(),
                                                  TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && parameterSupport != null;
        }

        private void writeToFile(String s, File outputFile) throws IOException {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
                bw.write(s);
            }
        }
    }

    private FileNameExtensionFilter createParameterFileFilter() {
        return new FileNameExtensionFilter("BEAM GPF Parameter Files (XML)", "xml");
    }

    private class DisplayParametersAction extends AbstractAction {

        DisplayParametersAction() {
            super("Display Parameters...");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                DomElement domElement = parameterSupport.toDomElement();
                showMessageDialog("Parameters", new JTextArea(domElement.toXml()));
            } catch (Exception e) {
                Debug.trace(e);
                showMessageDialog("Parameters", new JLabel("Failed to convert parameters to XML."));
            }
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && parameterSupport != null;
        }
    }


    private class AboutOperatorAction extends AbstractAction {

        AboutOperatorAction() {
            super("About...");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            showMessageDialog("About " + getOperatorName(), new JLabel(getOperatorDescription()));
        }
    }

    private void showMessageDialog(String title, Component component) {
        final ModalDialog modalDialog = new ModalDialog(UIUtils.getRootWindow(parentComponent),
                                                        title,
                                                        AbstractDialog.ID_OK,
                                                        null); /*I18N*/
        modalDialog.setContent(component);
        modalDialog.show();
    }

    String getOperatorName() {
        OperatorMetadata operatorMetadata = opType.getAnnotation(OperatorMetadata.class);
        if (operatorMetadata != null) {
            return operatorMetadata.alias();
        }
        return opType.getName();
    }

    String getOperatorDescription() {
        OperatorMetadata operatorMetadata = opType.getAnnotation(OperatorMetadata.class);
        if (operatorMetadata != null) {
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<h2>").append(operatorMetadata.alias()).append(" Operator</h2>");
            sb.append("<table>");
            sb.append("  <tr><td><b>Name:</b></td><td><code>").append(operatorMetadata.alias()).append(
                    "</code></td></tr>");
            sb.append("  <tr><td><b>Full name:</b></td><td><code>").append(opType.getName()).append(
                    "</code></td></tr>");
            sb.append("  <tr><td><b>Purpose:</b></td><td>").append(operatorMetadata.description()).append("</td></tr>");
            sb.append("  <tr><td><b>Authors:</b></td><td>").append(operatorMetadata.authors()).append("</td></tr>");
            sb.append("  <tr><td><b>Version:</b></td><td>").append(operatorMetadata.version()).append("</td></tr>");
            sb.append("  <tr><td><b>Copyright:</b></td><td>").append(operatorMetadata.copyright()).append("</td></tr>");
            sb.append("</table>");
            sb.append("</html>");
            return makeHtmlConform(sb.toString());
        }
        return "No operator metadata available.";
    }

    private static String makeHtmlConform(String text) {
        return text.replace("\n", "<br/>");
    }

    private void applyCurrentDirectory(JFileChooser fileChooser) {
        String homeDirPath = SystemUtils.getUserHomeDir().getPath();
        String lastDir = appContext.getPreferences().getPropertyString(lastDirPreferenceKey, homeDirPath);
        fileChooser.setCurrentDirectory(new File(lastDir));
    }

    private void preserveCurrentDirectory(JFileChooser fileChooser) {
        String lastDir = fileChooser.getCurrentDirectory().getAbsolutePath();
        appContext.getPreferences().setPropertyString(lastDirPreferenceKey, lastDir);
    }

}
