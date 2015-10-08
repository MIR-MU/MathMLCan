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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jdom2.JDOMException;
import org.junit.Test;

import cz.muni.fi.mir.mathmlcanonicalization.modules.ModuleException;
import cz.muni.fi.mir.mathmlcanonicalization.modules.OperatorNormalizerTest;

/**
 * Test cases for MathMLCanonicalizer class.
 */
public class MathMLCanonicalizerTest {

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    
    /**
     * Test resources relative to package of OperatorNormalizerTest class.
     */
    private static final String[] TEST_RESOURCES = {
        "ElementMinimizerTest/attributes.original.xml",
        "ElementMinimizerTest/comments.original.xml",
        "ElementMinimizerTest/mfrac.original.xml",
        "ElementMinimizerTest/mphantom.original.xml",
        "FunctionNormalizerTest/function2.original.xml",
        "FunctionNormalizerTest/function3.original.xml",
        "FunctionNormalizerTest/function.original.xml",
        "FunctionNormalizerTest/sin.original.xml",
        "MfencedReplacerTest/blank-separators.original.xml",
        "MfencedReplacerTest/interval-configured.original.xml",
        "MfencedReplacerTest/interval.original.xml",
        "MfencedReplacerTest/nested.original.xml",
        "MfencedReplacerTest/no-children.original.xml",
        "MfencedReplacerTest/sequence-separators.original.xml",
        "MrowNormalizerTest/configured.original.xml",
        "MrowNormalizerTest/frac.original.xml",
        "MrowNormalizerTest/interval.original.xml",
        "MrowNormalizerTest/mixed1.original.xml",
        "MrowNormalizerTest/mixed2.original.xml",
        "MrowNormalizerTest/mixed3.original.xml",
        "MrowNormalizerTest/mixed4.original.xml",
        "MrowNormalizerTest/parentheses1.original.xml",
        "MrowNormalizerTest/parentheses2.original.xml",
        "MrowNormalizerTest/parentheses3.original.xml",
        "MrowNormalizerTest/parentheses4.original.xml",
        "MrowNormalizerTest/parentheses5.original.xml",
        "MrowNormalizerTest/sqrt.original.xml",
        "MrowNormalizerTest/tuple.original.xml",
        "OperatorNormalizerTest/identifier-replacement.original.xml",
        "OperatorNormalizerTest/multiplication-blank.original.xml",
        "OperatorNormalizerTest/multiplication-cdot.original.xml",
        "OperatorNormalizerTest/operator2identifier.original.xml",
        "OperatorNormalizerTest/operator-unification.original.xml",
        "OperatorNormalizerTest/unicode.original.xml",
        "ScriptNormalizerTest/complexsubsup.original.xml",
        "ScriptNormalizerTest/invalid-scripts.original.xml",
        "ScriptNormalizerTest/nested-sub-sup.original.xml",
        "ScriptNormalizerTest/sub-sup.original.xml",
        "ScriptNormalizerTest/subsup.original.xml",
        "ScriptNormalizerTest/underover.original.xml"
    };
    
    public MathMLCanonicalizerTest() {
    }

    @Test
    public void testLoadingProperties() throws
            UnsupportedEncodingException, IOException, JDOMException, ModuleException {
        final String config = XML_DECLARATION
                + "<config>"
                + " <property name=\"existing\">value</property>"
                + " <property name=\"nonExisting\">value</property>"
                + "</config>";
        final InputStream configStream = new ByteArrayInputStream(config.getBytes("UTF-8"));

        Settings.setProperty("existing", "");

        Throwable e = null;
        try {
            new MathMLCanonicalizer(configStream);
        } catch (ConfigException ex) {
            e = ex;
        }
        assertNotNull(e);
        assertEquals(Settings.getProperty("existing"), "value");
        assertFalse(Settings.isProperty("nonExisting"));
    }
    
    @Test
    public void shouldCreateDefaultCanonicalizer() throws Exception {
        MathMLCanonicalizer canonicalizer = MathMLCanonicalizer.getDefaultCanonicalizer();

        for (String resource : TEST_RESOURCES) {
            canonicalizer.canonicalize( inputStream(resource), new ByteArrayOutputStream() );
            // we don't check result; it just should not throw an exception
        }
    }
    
    private InputStream inputStream(String resource) {
        return OperatorNormalizerTest.class.getResourceAsStream(resource);
    }

}
