/**
 * Copyright 2013 MIRMU Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package cz.muni.fi.mir.mathmlcanonicalization;

import cz.muni.fi.mir.mathmlcanonicalization.modules.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * An input class for MathML canonicalization.
 *
 * @author David Formanek
 */
public final class MathMLCanonicalizer {

    private List<StreamModule> streamModules = new ArrayList<StreamModule>();
    private List<DOMModule> domModules = new ArrayList<DOMModule>();

    /**
     * Initializes canonicalizer with default settings
     * 
     * @return itialized canonicalizer
     */
    public static MathMLCanonicalizer getDefaultCanonicalizer() {
        String property = Settings.getProperty("modules");
        String[] modules = property.split(" ");
        List<String> listOfModules = Arrays.asList(modules);
        
        MathMLCanonicalizer result = new MathMLCanonicalizer();
        for (String moduleName : listOfModules) {
            result.addModule(moduleName);
        }
        
        return result;
    }
    
    /**
     * Initializes canonicalizer with no modules
     */
    public MathMLCanonicalizer() {    
    }
    
    /**
     * Initializes canonicalizer with initialized modules using configuration
     * 
     * @param xmlConfigurationStream XML configuration
     */
    public MathMLCanonicalizer(InputStream xmlConfigurationStream) {
        // TODO: XML loading implementation
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Adds the module to the process of canonicalization
     *
     * StreamModules are called before DOM modules and then in order of
     * addition. Each module can be added more than once if needed.
     *
     * @param module the module to be used in canonicalization
     * @return the canonizer object to allow adding more modules at once
     */
    public MathMLCanonicalizer addModule(Module module) {
        if (module instanceof StreamModule) {
            if (module instanceof DOMModule) {
                Logger.getLogger(Settings.class.getName()).log(
                    Level.INFO, "Module is stream and DOM module at the same"
                        + " time, it will be used as a stream module.");
            }
            streamModules.add((StreamModule) module);
        } else if (module instanceof DOMModule) {
            domModules.add((DOMModule) module);
        } else {
            throw new UnsupportedOperationException("Module type not supported");
        }
        return this;
    }
    
    /**
     * Adds the module by its class name.
     * Useful for setting modules from config files.
     * 
     * When the module can't be found or instantiated 
     * the module is skipped and the warning is produced.
     */
    public MathMLCanonicalizer addModule(String moduleName) {
        try {
            String fullyQualified = this.getClass().getPackage().getName() + ".modules." + moduleName;
            Class<?> moduleClass = Class.forName(fullyQualified);
            
            return addModule((Module) moduleClass.newInstance());
        } catch (ClassNotFoundException e) {
            Logger.getLogger(MathMLCanonicalizer.class.getName()).log(
                Level.WARNING, "cannot load module " + moduleName, e);
        } catch (InstantiationException e) {
            Logger.getLogger(MathMLCanonicalizer.class.getName()).log(
                Level.WARNING, "cannot instantiate module " + moduleName, e);
        } catch (IllegalAccessException e) {
            Logger.getLogger(MathMLCanonicalizer.class.getName()).log(
                Level.WARNING, "cannot access module " + moduleName, e);
        }
        return null;
    }

    /**
     * Canonicalize an input MathML stream.
     *
     * @param in input stream to be canonicalized
     * @param out canonical output stream of input
     * @throws JDOMException problem with DOM
     * @throws IOException problem with streams
     */
    public void canonicalize(InputStream in, OutputStream out)
            throws JDOMException, IOException {

        ByteArrayOutputStream outputStream = null;

        // calling stream modules
        for (StreamModule module : streamModules) {
            outputStream = module.execute(in);
            if (outputStream == null) {
                throw new IOException("Module " + module.toString() + "returned null.");
            }
            in = new ByteArrayInputStream(outputStream.toByteArray());
        }

        // do not create the JDOM representation if there are no modules
        if (domModules.isEmpty()) {
            if (streamModules.isEmpty()) {
                throw new IOException("There are no modules added.");
            }
            outputStream.writeTo(out);
            return;
        }

        // creating the JDOM representation from the stream
        SAXBuilder builder = Settings.setupSAXBuilder();
        Document document = builder.build(in);

        // calling JDOM modules
        for (DOMModule module : domModules) {
            module.execute(document);
        }

        // convertong the JDOM representation back to stream
        XMLOutputter serializer = new XMLOutputter();
        serializer.output(document, out);
    }
}
