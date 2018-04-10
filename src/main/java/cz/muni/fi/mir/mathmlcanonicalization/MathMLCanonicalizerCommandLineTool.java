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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
    private static final Logger LOGGER = Logger.getLogger(MathMLCanonicalizerCommandLineTool.class.getName());

    private static final String OPTION_CONFIG = "c";
    private static final String OPTION_CONFIG_LONG = "config-file";
    private static final String OPTION_INJECTION = "d";
    private static final String OPTION_INJECTION_LONG = "inject-xhtml-mathml-svg-dtd";
    private static final String OPTION_OVERWRITE = "w";
    private static final String OPTION_OVERWRITE_LONG = "overwrite-inputs";
    private static final String OPTION_PRINT_DEFAULT_CONFIG = "p";
    private static final String OPTION_PRINT_DEFAULT_CONFIG_LONG = "print-default-config-file";
    private static final String OPTION_HELP = "h";
    private static final String OPTION_HELP_LONG = "help";

    // TODO: refactoring
    /**
     * @param args the command line arguments
     * @throws javax.xml.stream.XMLStreamException an error with XML processing
     * occurs
     */
    public static void main(String[] args) throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ConfigException, FileNotFoundException, JDOMException, ModuleException, XMLStreamException {
        final Options options = createOptions();
        final CommandLineParser parser = new DefaultParser();
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
            if (line.hasOption(OPTION_CONFIG)) {
                try {
                    config = new FileInputStream(line.getOptionValue(OPTION_CONFIG));
                } catch (FileNotFoundException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    System.exit(2);
                }
            } else {
                config = Settings.getStreamFromProperty("defaultConfig");
            }

            if (line.hasOption(OPTION_INJECTION)) {
                dtdInjectionMode = true;
            }

            if (line.hasOption(OPTION_OVERWRITE)) {
                overwrite = true;
            }

            if (line.hasOption(OPTION_PRINT_DEFAULT_CONFIG)) {
                printDefaultConfig();
                System.exit(0);
            }

            if (line.hasOption(OPTION_HELP)) {
                printHelp(options);
                System.exit(0);
            }

            final List<String> arguments = Arrays.asList(line.getArgs());
            if (arguments.size() > 0) {

                byte[] configContent = IOUtils.toByteArray(config);

                for (String arg : arguments) {
                    try {
                        List<File> files = getFiles(new File(arg));
                        for (File file : files) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(configContent);
                            canonicalize(file, byteArrayInputStream, dtdInjectionMode, overwrite);
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            } else {
                printHelp(options);
                System.exit(0);
            }

            if (config != null) {
                config.close();
            }
        }
    }

    private static Options createOptions() {
        Options options = new Options();

        options.addOption(Option
                .builder(OPTION_CONFIG)
                .longOpt(OPTION_CONFIG_LONG)
                .desc("Load configuration file.")
                .argName("arg")
                .hasArg()
                .build()
        );

        options.addOption(Option
                .builder(OPTION_INJECTION)
                .longOpt(OPTION_INJECTION_LONG)
                .desc("Enforce injection of XHTML 1.1 plus MathML 2.0 plus SVG 1.1 DTD reference into input documents.")
                .hasArg(false)
                .build()
        );

        options.addOption(Option
                .builder(OPTION_OVERWRITE)
                .longOpt(OPTION_OVERWRITE_LONG)
                .desc("Overwrite input files by produced canonical outputs.")
                .hasArg(false)
                .build()
        );

        options.addOption(Option
                .builder(OPTION_PRINT_DEFAULT_CONFIG)
                .longOpt(OPTION_PRINT_DEFAULT_CONFIG_LONG)
                .desc("Print default configuration that will be used if no config file is supplied.")
                .hasArg(false)
                .build()
        );

        options.addOption(Option
                .builder(OPTION_HELP)
                .longOpt(OPTION_HELP_LONG)
                .desc("Print help (this screen).")
                .hasArg(false)
                .build()
        );

        return options;
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
            LOGGER.log(Level.INFO, "overwriting the file {0}", file.getAbsolutePath());
            ByteArrayInputStream source = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));

            mlcan.canonicalize(source, new FileOutputStream(file));
        } else {
            mlcan.canonicalize(new FileInputStream(file), System.out);
        }
    }

    private static List<File> getFiles(File file) throws IOException {
        assert file != null;
        List<File> result = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    result.addAll(getFiles(f));
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
        final String RUN_JAR = "java -jar";
        final String PATH_TO_CONFIG = "/path/to/config.xml";
        final String PATH_TO_FILE = "/path/to/file.xhtml";
        final String PATH_TO_DIR = "/path/to/directory";
        final String PATH = "/path/to/input";

        PrintWriter output = new PrintWriter(System.out, true);

        output.println("Usage:");
        output.printf("\t%s %s [ -%s <%s> ] [ -%s ] [ -%s ] <%s>...\n",
                RUN_JAR, JARFILE,
                OPTION_CONFIG, PATH_TO_CONFIG,
                OPTION_OVERWRITE,
                OPTION_INJECTION,
                PATH);
        output.printf("\t%s %s -%s | --%s\n",
                RUN_JAR, JARFILE,
                OPTION_PRINT_DEFAULT_CONFIG, OPTION_PRINT_DEFAULT_CONFIG_LONG);
        output.printf("\t%s %s -%s | --%s\n",
                RUN_JAR, JARFILE,
                OPTION_HELP, OPTION_HELP_LONG);
        output.printf("\nNB: <%s> is %s or %s\n\n",
                PATH, PATH_TO_FILE, PATH_TO_DIR);
        output.println("Options:");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printOptions(output, 80, options, 8, 8);
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
        lsOutput.setEncoding(StandardCharsets.UTF_8.name());

        writer.write(document, lsOutput);
        writer.getDomConfig().setParameter("xml-declaration", true);
        writer.getDomConfig().setParameter("format-pretty-print", true);

        System.out.print(stringWriter.toString());

    }

}
