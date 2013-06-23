package cz.muni.fi.mir.mathmlcanonicalization;

import cz.muni.fi.mir.mathmlcanonicalization.modules.ModuleException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.JDOMException;
import org.swixml.SwingEngine;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author RobSis
 */
public class GraphicalUserInterface {
    
    private static final String DESCRIPTOR = "gui.layout";
    private SwingEngine swix;
    
    public JPanel pnl;
    
    public JTextArea textarea;
    public JTextArea statusbar;
    
    public JLabel config;
    
    public JFileChooser fc = new JFileChooser();
    
    public Action quit = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            System.exit(0);
        }
    };
    
    public Action selectConfig = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = fc.showOpenDialog(pnl);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File configFile = fc.getSelectedFile();
                config.setText(configFile.getAbsolutePath());
            }
            
            if (returnVal == JFileChooser.CANCEL_OPTION) {
                config.setText("");
            }
        }
    };
    
    public Action canonicalize = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String configPath = config.getText();
            FileInputStream configInputStream = null;
            try {
                if (configPath != null && !configPath.isEmpty()) {
                    configInputStream = new FileInputStream(configPath);
                }
            } catch (FileNotFoundException ex) {
                statusbar.setText(ex.getMessage());
            }            
            
            MathMLCanonicalizer mlcan = null;
            if (configPath != null && !configPath.isEmpty()) {
                try {
                    mlcan = new MathMLCanonicalizer(configInputStream);
                } catch (ConfigException ex) {
                    Logger.getLogger(GraphicalUserInterface.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    statusbar.setText(ex.getMessage());
                    return;
                }
            } else {
                mlcan = MathMLCanonicalizer.getDefaultCanonicalizer();
            }

            OutputStream output = new ByteArrayOutputStream();
            try {
                InputStream input = new ByteArrayInputStream(textarea.getText().getBytes("UTF-8"));
                statusbar.setText("");
                mlcan.canonicalize(input, output);
                textarea.setText(output.toString());
            } catch (JDOMException ex) {
                Logger.getLogger(GraphicalUserInterface.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                statusbar.setText(ex.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(GraphicalUserInterface.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                statusbar.setText(ex.getMessage());
            } catch (ModuleException ex) {
                Logger.getLogger(GraphicalUserInterface.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                statusbar.setText(ex.getMessage());
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GraphicalUserInterface.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                statusbar.setText(ex.getMessage());
            }
        }
    };
    
    public GraphicalUserInterface() throws Exception {
        swix = new SwingEngine(this);
        InputStream layoutXml = this.getClass().getResourceAsStream(Settings.getProperty(DESCRIPTOR));
        swix.render(layoutXml);
        swix.getRootComponent().setVisible(true);
    }
}