/*
 * $Id: ModalDialog.java,v 1.3 2007/04/18 13:01:13 norman Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.framework.ui;

import org.esa.beam.framework.help.HelpSys;
import org.esa.beam.util.Debug;
import org.esa.beam.util.logging.BeamLogManager;

import javax.help.BadIDException;
import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * The <code>ModalDialog</code> is a helper class to quickly construct modal dialogs. The dialogs created with this
 * class have a unique border and font and a standard button row for the typical buttons like 'OK', 'Cancel' etc.
 * <p/>
 * <p>Instances of a modal dialog are created with a parent component, a title, the actual dialog content component, and
 * a bit-combination of the standard buttons to be used.
 * <p/>
 * <p>The dialog can be used directly or the class is overridden in order to override the methods <code>onOK</code>,
 * <code>onCancel</code> etc. which are automatically called if a user presses the corresponding button.
 * <p/>
 * <p>A limited way of input validation is provided by the  <code>verifyUserInput</code> method which can be overridden
 * in order to return <code>false</code> if a user input is invalid. In this case the <code>onOK</code>,
 * <code>onYes</code> and <code>onNo</code> methods are NOT called.
 *
 * @author Norman Fomferra
 * @version $Revision: 1.3 $  $Date: 2007/04/18 13:01:13 $
 */
public class ModalDialog {

    public static final int ID_OK = 0x0001;
    public static final int ID_YES = 0x0002;
    public static final int ID_NO = 0x0004;
    public static final int ID_CANCEL = 0x0008;
    public static final int ID_RESET = 0x0010;
    public static final int ID_HELP = 0x0020;
    public static final int ID_OTHER = 0x8000;
    public static final int ID_OK_CANCEL = ID_OK | ID_CANCEL;
    public static final int ID_OK_CANCEL_HELP = ID_OK_CANCEL | ID_HELP;
    public static final int ID_YES_NO = ID_YES | ID_NO;

    private JDialog _dialog;
    private Window _parent;
    private int _buttonID;
    private int _buttonMask;
    private Dimension _minimumSize;

    // Java help support
    private String _helpID;
    private HelpBroker _helpBroker;
    private JButton _helpButton;
    private Component _content;


    public ModalDialog(Window parent, String title, int buttonMask, String helpID) {
        this(parent, title, buttonMask, null, helpID);
    }

    public ModalDialog(Window parent, String title, Object content, int buttonMask, String helpID) {
        this(parent, title, content, buttonMask, null, helpID);
    }

    public ModalDialog(Window parent, String title, Object content, int buttonMask, Object[] otherButtons,
                       String helpID) {
        this(parent, title, buttonMask, otherButtons, helpID);
        setContent(content);
    }

    public ModalDialog(Window parent, String title, int buttonMask, Object[] otherButtons, String helpID) {
        _parent = parent;
        if (parent instanceof Frame || parent == null) {
            _dialog = new JDialog((Frame) parent, title, true);
        } else if (parent instanceof Dialog) {
            _dialog = new JDialog((Dialog) parent, title, true);
        } else {
            throw new IllegalArgumentException("'parent' must be either a dialog or a frame");
        }
        // todo - enable if we at the end need it
//        _dialog.addComponentListener(createResizeListener());
        _buttonMask = buttonMask;
        setButtonID(0);
        createUI(otherButtons);
        setHelpID(helpID);
    }

    protected void createUI(Object[] otherButtons) {

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));

        int insetSize = UIDefaults.INSET_SIZE;
        JPanel contentPane = new JPanel(new BorderLayout(0, insetSize + insetSize / 2));
        contentPane.setBorder(UIDefaults.DIALOG_BORDER);
        contentPane.add(buttonRow, BorderLayout.SOUTH);

        _dialog.setContentPane(contentPane);

        Vector buttons = new Vector();
        int leftButtonCount = 0;

        if (otherButtons != null) {
            for (int i = 0; i < otherButtons.length; i++) {
                if (otherButtons[i] instanceof String) {
                    JButton otherButton = new JButton((String) otherButtons[i]);
                    otherButton.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            setButtonID(ID_OTHER);
                            if (verifyUserInput()) {
                                onOther();
                            }
                        }
                    });
                    buttons.addElement(otherButton);
                } else if (otherButtons[i] instanceof AbstractButton) {
                    AbstractButton otherButton = (AbstractButton) otherButtons[i];
                    otherButton.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            setButtonID(ID_OTHER);
                            if (verifyUserInput()) {
                                onOther();
                            }
                        }
                    });
                    buttons.addElement(otherButton);
                }
                leftButtonCount++;
            }
        }

        if ((_buttonMask & ID_RESET) != 0) {
            JButton button = new JButton(" Reset "); /*I18N*/
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    final int buttonID = ID_RESET;
                    setButtonID(buttonID);
                    onReset();
                }
            });
            buttons.addElement(button);
            leftButtonCount++;
        }
        if ((_buttonMask & ID_OK) != 0) {
            JButton button = new JButton(" OK ");  /*I18N*/
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setButtonID(ID_OK);
                    if (verifyUserInput()) {
                        onOK();
                    }
                }
            });
            buttons.addElement(button);
            button.setDefaultCapable(true);
            _dialog.getRootPane().setDefaultButton(button);
        }
        if ((_buttonMask & ID_YES) != 0) {
            JButton button = new JButton(" Yes ");  /*I18N*/
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setButtonID(ID_YES);
                    if (verifyUserInput()) {
                        onYes();
                    }
                }
            });
            buttons.addElement(button);
            button.setDefaultCapable(true);
            _dialog.getRootPane().setDefaultButton(button);
        }
        if ((_buttonMask & ID_NO) != 0) {
            JButton button = new JButton(" No "); /*I18N*/
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setButtonID(ID_NO);
                    if (verifyUserInput()) {
                        onNo();
                    }
                }
            });
            buttons.addElement(button);
        }
        if ((_buttonMask & ID_CANCEL) != 0) {
            JButton button = new JButton(" Cancel ");  /*I18N*/
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setButtonID(ID_CANCEL);
                    onCancel();
                }
            });
            buttons.addElement(button);
            button.setVerifyInputWhenFocusTarget(false);
        }
        if ((_buttonMask & ID_HELP) != 0) {
            JButton button = new JButton(" Help "); /*I18N*/
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setButtonID(ID_HELP);
                    onHelp();
                }
            });
            buttons.addElement(button);
            _helpButton = button;
        }

        for (int i = 0; i < buttons.size(); i++) {
            if (i == leftButtonCount) {
                buttonRow.add(Box.createRigidArea(new Dimension(6, 0)));
                buttonRow.add(Box.createHorizontalGlue());
            }
            if (i != 0) {
                buttonRow.add(Box.createRigidArea(new Dimension(6, 0)));
            }
            buttonRow.add((Component) buttons.elementAt(i));
        }

        _dialog.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                setButtonID(ID_CANCEL);
                onCancel();
            }
        });
    }

    protected void setButtonID(final int buttonID) {
        _buttonID = buttonID;
    }

    protected void cancelDialog() {
        setButtonID(ID_CANCEL);
        onCancel();
    }

    private void initHelpBroker() {
        HelpSet helpSet = HelpSys.getHelpSet();
        if (helpSet != null) {
            _helpBroker = helpSet.createHelpBroker();
            if (_helpBroker instanceof DefaultHelpBroker) {
                DefaultHelpBroker defaultHelpBroker = (DefaultHelpBroker) _helpBroker;
                defaultHelpBroker.setActivationWindow(getJDialog());
            }
        }
    }

    public String getHelpID() {
        return _helpID;
    }

    public void setHelpID(String helpID) {
        _helpID = helpID;
        updateHelpID();
    }

    private void updateHelpID() {
        if (_helpID == null) {
            return;
        }
        if (_helpBroker == null) {
            initHelpBroker();
        }
        if (_helpBroker == null) {
            return;
        }
        HelpSet helpSet = _helpBroker.getHelpSet();
        try {
            _helpBroker.setCurrentID(_helpID);
        } catch (BadIDException e) {
            Logger systemLogger = BeamLogManager.getSystemLogger();
            if (systemLogger != null) {
                systemLogger.severe("ModalDialog: '" + _helpID + "' is not a valid helpID");
            } else {
                Debug.trace(e);
            }
        }
        if (helpSet == null) {
            return;
        }
        if (getJDialog() != null) {
            _helpBroker.enableHelpKey(getJDialog(), _helpID, helpSet);
        }
        if (getJDialog().getContentPane() != null) {
            _helpBroker.enableHelpKey(getJDialog().getContentPane(), _helpID, helpSet);
        }
        if (_helpButton != null) {
            _helpBroker.enableHelpKey(_helpButton, _helpID, helpSet);
            _helpBroker.enableHelpOnButton(_helpButton, _helpID, helpSet);
        }
    }

    public JDialog getJDialog() {
        return _dialog;
    }

    public Window getParent() {
        return _parent;
    }

    public int show() {
        setButtonID(ID_CANCEL);
        _dialog.pack();
        _minimumSize = _dialog.getSize();
        center();
        _dialog.setResizable(true);
        _dialog.setVisible(true);
        return getButtonID();
    }


    public void hide() {
        _dialog.setVisible(false);
    }

    public void center() {
        UIUtils.centerComponent(_dialog, _parent);
    }

    public int getButtonID() {
        return _buttonID;
    }

    public void setContent(Object content) {
        Component comp = null;
        if (content instanceof Component) {
            comp = (Component) content;
        } else {
            comp = new JLabel(content.toString());
        }
        if (_content != null) {
            _dialog.getContentPane().remove(_content);
        }
        _content = comp;
        _dialog.getContentPane().add(_content, BorderLayout.CENTER);
        _dialog.validate();
        updateHelpID();
    }

    public void showErrorDialog(String errorMessage) {
        showMessageDialog(errorMessage, JOptionPane.ERROR_MESSAGE);
    }

    public void showInformationDialog(String infoMessage) {
        showMessageDialog(infoMessage, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showWarningDialog(String warningMessage) {
        showMessageDialog(warningMessage, JOptionPane.WARNING_MESSAGE);
    }

    protected void onOK() {
        hide();
    }

    protected void onYes() {
        hide();
    }

    protected void onNo() {
        hide();
    }

    protected void onCancel() {
        hide();
    }

    protected void onHelp() {
        if (_helpID == null) {
            showInformationDialog("Sorry, no help theme available."); /*I18N*/
        }
    }

    protected void onReset() {
    }

    protected void onOther() {
        hide();
    }

    protected boolean verifyUserInput() {
        return true;
    }

    private void showMessageDialog(String message, int messageType) {
        JOptionPane.showMessageDialog(getJDialog(), message, getJDialog().getTitle(), messageType);
    }

// todo - enable if we at the end need it
//    private ComponentAdapter createResizeListener() {
//        return new ComponentAdapter() {
//
//            public void componentResized(ComponentEvent e) {
//                if (_minimumSize != null) {
//                    final Dimension size = e.getComponent().getSize();
//                    size.height = _minimumSize.height > size.height ? _minimumSize.height : size.height;
//                    size.width = _minimumSize.width > size.width ? _minimumSize.width : size.width;
//                    e.getComponent().setSize(size);
//                }
//            }
//        };
//    }
}
