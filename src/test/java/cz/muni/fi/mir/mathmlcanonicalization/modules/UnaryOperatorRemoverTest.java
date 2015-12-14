/**
 * Copyright 2015 MIR@MU Project
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
package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for UnaryOperatorRemover canonicalization DOM Module.
 *
 * @author Michal Růžička
 */
public class UnaryOperatorRemoverTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new UnaryOperatorRemover();
    private static final String RESOURCE_SUBDIR = UnaryOperatorRemoverTest.class.getSimpleName() + "/";

    @BeforeClass
    public static void setUpBeforeClass() {
        DEFAULT_INSTANCE.setProperty("pmathremoveunaryoperators", "+ - \u2064 \u2212 \u2213 \u2214 \u2238 \u2295 \u2296 \u229d \u229e \u229f");
        DEFAULT_INSTANCE.setProperty("cmathremoveunaryoperators", "plus minus");
    }

    @Test
    public void testPMathMLNoNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "pmathml-no-namespace");
    }

    @Test
    public void testPMathMLWithNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "pmathml-with-namespace");
    }

    @Test
    public void testCMathMLNoNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "cmathml-no-namespace");
    }

    @Test
    public void testCMathMLWithNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "cmathml-with-namespace");
    }

    @Test
    public void testDoubleOperatorPMathMLNoNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "pmathml-double-operator-no-namespace");
    }

    @Test
    public void testDoubleOperatorPMathMLWithNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "pmathml-double-operator-with-namespace");
    }

    @Test
    public void testDoubleOperatorCMathMLNoNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "cmathml-double-operator-no-namespace");
    }

    @Test
    public void testDoubleOperatorCMathMLWithNamespace() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "cmathml-double-operator-with-namespace");
    }

}
