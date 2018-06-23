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
 * Test for ScriptNormalizer canonicalization DOM Module.
 *
 * @author David Formanek
 */
public class ScriptNormalizerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new ScriptNormalizer();
    private static final String RESOURCE_SUBDIR = ScriptNormalizerTest.class.getSimpleName() + "/";

    @BeforeClass
    public static void setUpBeforeClass() {
        DEFAULT_INSTANCE.setProperty("swapscripts", "true");
        DEFAULT_INSTANCE.setProperty("splitscriptselements", "mi");
        DEFAULT_INSTANCE.setProperty("unifyscripts", "true");
    }

    @Test
    public void testInvalidScript() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "invalid-scripts");
    }

    @Test
    public void testNestedSuperscript() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "sub-sup");
    }

    @Test
    public void testComplexNestedSuperscript() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "nested-sub-sup");
    }

    @Test
    public void testSubsup() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "subsup");
    }

    @Test
    public void testComplexSubsup() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "complexsubsup");
    }

    @Test
    public void testUnderOver() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "underover");
    }

}
