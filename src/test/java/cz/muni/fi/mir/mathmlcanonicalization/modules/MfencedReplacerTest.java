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
 * Test for MfencedReplacer canonicalization DOM Module.
 *
 * @author David Formanek
 */
public class MfencedReplacerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new MfencedReplacer();
    private static final Module CONFIGURED_INSTANCE = new MfencedReplacer();
    private static final String RESOURCE_SUBDIR = MfencedReplacerTest.class.getSimpleName() + "/";

    public MfencedReplacerTest() {
        setPrintProcessed(false);
        CONFIGURED_INSTANCE.setProperty("open", "[[");
        CONFIGURED_INSTANCE.setProperty("close", "]]");
        CONFIGURED_INSTANCE.setProperty("separators", ";");
        CONFIGURED_INSTANCE.setProperty("forceopen", "1");
        CONFIGURED_INSTANCE.setProperty("forceclose", "1");
        CONFIGURED_INSTANCE.setProperty("forceseparators", "1");
        CONFIGURED_INSTANCE.setProperty("innermrow", "1");
        CONFIGURED_INSTANCE.setProperty("outermrow", "0");
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        DEFAULT_INSTANCE.setProperty("outermrow", "1");
        DEFAULT_INSTANCE.setProperty("innermrow", "1");
        DEFAULT_INSTANCE.setProperty("open", "(");
        DEFAULT_INSTANCE.setProperty("close", ")");
        DEFAULT_INSTANCE.setProperty("separators", ",");
        DEFAULT_INSTANCE.setProperty("forceopen", "0");
        DEFAULT_INSTANCE.setProperty("forceclose", "0");
        DEFAULT_INSTANCE.setProperty("forceseparators", "0");
    }

    @Test
    public void testSimpleInterval() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "interval");
    }

    @Test
    public void testMoreSeparators() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "sequence-separators");
    }

    @Test
    public void testBlankSeparators() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "blank-separators");
    }

    @Test
    public void testNoChildren() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "no-children");
    }

    @Test
    public void testNestedMfenced() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "nested");
    }

    @Test
    public void testConfigured() {
        testXML(CONFIGURED_INSTANCE, RESOURCE_SUBDIR + "interval-configured");
    }

}
