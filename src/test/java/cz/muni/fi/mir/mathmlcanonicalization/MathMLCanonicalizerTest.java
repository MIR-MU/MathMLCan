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
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import org.jdom2.JDOMException;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test cases for MathMLCanonicalizer class.
 */
public class MathMLCanonicalizerTest {

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

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
            MathMLCanonicalizer mlcan = new MathMLCanonicalizer(configStream);
        } catch (ConfigException ex) {
            e = ex;
        }
        assertNotNull(e);
        assertEquals(Settings.getProperty("existing"), "value");
        assertFalse(Settings.isProperty("nonExisting"));
    }
}
