/**
 * Copyright 2013 MIRMU

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package cz.muni.fi.mir.mathmlcanonicalization;

import cz.muni.fi.mir.mathmlcanonicalization.modules.ModuleException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.JDOMException;

/**
 * Sample class using the canonizer.
 * 
 * @author David Formanek
 */
public final class MathMLCanonicalization {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String inputFilePath = null;
        
        File config = null;
        if (args.length < 1) {
            System.err.println("Usage:\n\tjava -jar "
                    + new File(cz.muni.fi.mir.mathmlcanonicalization.
                        MathMLCanonicalization.class.getProtectionDomain().
                        getCodeSource().getLocation().getFile()).getName()
                    + " \"/path/to/input.xhtml\" [\"/path/to/config.xml\"]");
            System.exit(1);
        } else {
            if (args.length == 2) {
                config = new File(args[1]);
            }
            inputFilePath = args[0];
        }
        try {
            GraphicalUserInterface GUI = new GraphicalUserInterface();
            
            List<File> files = getFiles(new File(inputFilePath));
            for (File f : files) {
                canonicalize(f, config);
            }
        } catch (Exception ex) {
            Logger.getLogger(MathMLCanonicalization.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private static void canonicalize(File f, File config) {
        try {
            System.out.println(f.getAbsolutePath());
            
            FileInputStream configInputStream = null;
            if (config != null) {
                configInputStream = new FileInputStream(config);
            }
            
            MathMLCanonicalizer mlcan = new MathMLCanonicalizer(configInputStream);
            mlcan.canonicalize(new FileInputStream(f), System.out);
            
            //MathMLCanonicalizer.getDefaultCanonicalizer().canonicalize(new FileInputStream(f), System.out);
            
        } catch (JDOMException ex) {
            Logger.getLogger(MathMLCanonicalization.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MathMLCanonicalization.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ModuleException ex) {
            Logger.getLogger(MathMLCanonicalization.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static List<File> getFiles(File file) throws IOException {
        List<File> result = new ArrayList<File>();
        if (file.canRead()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        result.addAll(getFiles(files[i]));
                    }
                }
            } else {
                result.add(file);
            }
        }
        return result;
    }
    
    
}