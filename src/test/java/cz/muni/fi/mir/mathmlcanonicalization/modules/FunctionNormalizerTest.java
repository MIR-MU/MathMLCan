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
package cz.muni.fi.mir.mathmlcanonicalization.modules;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for FunctionNormalizer canonicalization DOM Module.
 *
 * @author David Formanek
 */
public class FunctionNormalizerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new FunctionNormalizer();
    private static final String RESOURCE_SUBDIR = FunctionNormalizerTest.class.getSimpleName() + "/";

    @BeforeClass
    public static void setUpBeforeClass() {
        DEFAULT_INSTANCE.setProperty("functionoperators", "\u2061");
    }
    
    @Test
    public void testFunction() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "function");
    }

    @Test
    public void testFunction2() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "function2");
    }

    @Test
    public void testFunction3() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "function3");
    }

    @Test
    public void testSine() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "sin");
    }
}
