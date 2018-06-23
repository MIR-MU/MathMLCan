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
 * Test for MrowNormalizer canonicalization DOM Module.
 *
 * @author David Formanek
 */
public class MrowNormalizerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new MrowNormalizer();
    private static final Module CONFIGURED_INSTANCE = new MrowNormalizer();
    private static final String RESOURCE_SUBDIR = MrowNormalizerTest.class.getSimpleName() + "/";

    public MrowNormalizerTest() {
        CONFIGURED_INSTANCE.setProperty("open", "left");
        CONFIGURED_INSTANCE.setProperty("close", "right");
        CONFIGURED_INSTANCE.setProperty("wrapInside", "1");
        CONFIGURED_INSTANCE.setProperty("wrapOutside", "1");
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        DEFAULT_INSTANCE.setProperty("wrapOutside", "1");
        DEFAULT_INSTANCE.setProperty("wrapInside", "1");
        DEFAULT_INSTANCE.setProperty("open", "( [ {");
        DEFAULT_INSTANCE.setProperty("close", ") ] }");
        DEFAULT_INSTANCE.setProperty("childCount.msqrt", "1");
        DEFAULT_INSTANCE.setProperty("childCount.mfrac", "2");
        DEFAULT_INSTANCE.setProperty("childCount.mroot", "2");
        DEFAULT_INSTANCE.setProperty("childCount.mstyle", "1");
        DEFAULT_INSTANCE.setProperty("childCount.merror", "1");
        DEFAULT_INSTANCE.setProperty("childCount.mpadded", "1");
        DEFAULT_INSTANCE.setProperty("childCount.mphantom", "1");
        DEFAULT_INSTANCE.setProperty("childCount.mfenced", "0");
        DEFAULT_INSTANCE.setProperty("childCount.menclose", "1");
        DEFAULT_INSTANCE.setProperty("childCount.msub", "2");
        DEFAULT_INSTANCE.setProperty("childCount.msup", "2");
        DEFAULT_INSTANCE.setProperty("childCount.msubsup", "3");
        DEFAULT_INSTANCE.setProperty("childCount.munder", "2");
        DEFAULT_INSTANCE.setProperty("childCount.munderover", "3");
        DEFAULT_INSTANCE.setProperty("childCount.mtd", "1");
        DEFAULT_INSTANCE.setProperty("childCount.mscarry", "1");
        DEFAULT_INSTANCE.setProperty("childCount.math", "1");
        DEFAULT_INSTANCE.setProperty("childCount.mrow", "1");
    }

    @Test
    public void testSquareRoot() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "sqrt");
    }

    @Test
    public void testTuple() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "tuple");
    }

    @Test
    public void testParentheses1() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "parentheses1");
    }

    @Test
    public void testParentheses2() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "parentheses2");
    }

    @Test
    public void testParentheses3() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "parentheses3");
    }

    @Test
    public void testParentheses4() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "parentheses4");
    }

    @Test
    public void testParentheses5() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "parentheses5");
    }

    @Test
    public void testInterval() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "interval");
    }

    @Test
    public void testFrac() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "frac");
    }

    @Test
    public void testMixed1() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "mixed1");
    }

    @Test
    public void testMixed2() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "mixed2");
    }

    @Test
    public void testMixed3() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "mixed3");
    }

    @Test
    public void testMixed4() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "mixed4");
    }

    @Test
    public void testConfigured() {
        testXML(CONFIGURED_INSTANCE, RESOURCE_SUBDIR + "configured");
    }

}
