/**
 * Copyright 2013 MIR@MU Project
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

import cz.muni.fi.mir.mathmlcanonicalization.modules.ModuleException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.jdom2.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Sample class using the canonizer.
 *
 * @author David Formanek
 */
public final class MathMLCanonicalizerCommandLineTool {

    private static final String JARFILE = "mathml-canonicalizer.jar";

    // TODO: refactoring
    /**
     * @param args the command line arguments
     * @throws javax.xml.stream.XMLStreamException an error with XML processing
     * occurs
     */
    public static void main(String[] args) throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ConfigException, FileNotFoundException, JDOMException, ModuleException, XMLStreamException {
        final Options options = new Options();
        options.addOption("c", "config-file", true, "load configuration file");
        options.addOption("d", "inject-xhtml-mathml-svg-dtd", false, "enforce injection of XHTML 1.1 plus MathML 2.0 plus SVG 1.1 DTD reference into input documents");
        options.addOption("w", "overwrite-inputs", false, "overwrite input files by canonical outputs");
        options.addOption("p", "print-default-config-file", false, "print default configuration that will be used if no config file is supplied");
        options.addOption("h", "help", false, "print help");

        final CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException ex) {
            printHelp(options);
            System.exit(1);
        }

        InputStream config = null;
        boolean overwrite = false;
        boolean dtdInjectionMode = false;
        if (line != null) {
            if (line.hasOption('c')) {
                try {
                    config = new FileInputStream(line.getOptionValue('c'));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MathMLCanonicalizerCommandLineTool.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(2);
                }
            } else {
                config = Settings.getStreamFromProperty("defaultConfig");
            }

            if (line.hasOption('d')) {
                dtdInjectionMode = true;
            }

            if (line.hasOption('w')) {
                overwrite = true;
            }

            if (line.hasOption('p')) {
                printDefaultConfig();
                System.exit(0);
            }

            if (line.hasOption('h')) {
                printHelp(options);
                System.exit(0);
            }

            final List<String> arguments = Arrays.asList(line.getArgs());
            if (arguments.size() > 0) {
                for (String arg : arguments) {
                    try {
                        List<File> files = getFiles(new File(arg));
                        for (File file : files) {
                            canonicalize(file, config, dtdInjectionMode, overwrite);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MathMLCanonicalizerCommandLineTool.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            } else {
                printHelp(options);
                System.exit(0);
            }
        }
    }

    private static void canonicalize(File file, InputStream config, boolean dtdInjectionMode, boolean overwrite) throws
            ConfigException, FileNotFoundException, JDOMException, IOException, ModuleException, XMLStreamException {
        assert file != null; // but config can be null
        MathMLCanonicalizer mlcan;
        if (config != null) {
            mlcan = new MathMLCanonicalizer(config);
        } else {
            mlcan = MathMLCanonicalizer.getDefaultCanonicalizer();
        }
        mlcan.setEnforcingXHTMLPlusMathMLDTD(dtdInjectionMode);

        if (overwrite) {
            Logger.getLogger(MathMLCanonicalizerCommandLineTool.class.getName()).log(Level.INFO, "overwriting the file {0}", file.getAbsolutePath());
            ByteArrayInputStream source = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));

            mlcan.canonicalize(source, new FileOutputStream(file));
        } else {
            mlcan.canonicalize(new FileInputStream(file), System.out);
        }
    }

    private static List<File> getFiles(File file) throws IOException {
        assert file != null;
        List<File> result = new ArrayList<File>();
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
        return result;
    }

    /**
     * Print help text.
     *
     */
    private static void printHelp(Options options) {
        System.err.println("Usage:");
        System.err.println("\tjava -jar " + JARFILE
                + " [ -c /path/to/config.xml ] [ -w ] [ -d ]"
                + " { /path/to/input.xhtml | /path/to/directory [ | ... ] }");
        System.err.println("\tjava -jar " + JARFILE + " -p");
        System.err.println("\tjava -jar " + JARFILE + " -h");
        System.err.println("Options:");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printOptions(new PrintWriter(System.err, true), 80, options, 8, 8);
    }

    private static void printDefaultConfig() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        final InputSource src = new InputSource(Settings.getStreamFromProperty("defaultConfig"));
        final Document document = Settings.documentBuilderFactory().newDocumentBuilder().parse(src);

        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        final LSOutput lsOutput = impl.createLSOutput();
        final LSSerializer writer = impl.createLSSerializer();

        Writer stringWriter = new StringWriter();
        lsOutput.setCharacterStream(stringWriter);
        lsOutput.setEncoding("UTF-8");

        writer.write(document, lsOutput);
        writer.getDomConfig().setParameter("xml-declaration", true);
        writer.getDomConfig().setParameter("format-pretty-print", true);

        System.out.print(stringWriter.toString());

    }

}
