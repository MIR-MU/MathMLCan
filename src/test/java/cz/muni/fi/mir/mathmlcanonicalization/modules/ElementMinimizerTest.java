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
 * Test for ElementMinimizer canonicalization stream module
 *
 * @author David Formanek
 */
public class ElementMinimizerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new ElementMinimizer();
    private static final String RESOURCE_SUBDIR = ElementMinimizerTest.class.getSimpleName() + "/";

    @BeforeClass
    public static void setUpBeforeClass() {
        DEFAULT_INSTANCE.setProperty("remove", "mspace maligngroup malignmark mstyle mpadded menclose maction");
        DEFAULT_INSTANCE.setProperty("remove_all", "mphantom merror");
        DEFAULT_INSTANCE.setProperty("keepAttributes", "mathvariant=bold encoding");
        DEFAULT_INSTANCE.setProperty("keepAttributes.mfrac", "linethickness=0");
        DEFAULT_INSTANCE.setProperty("keepAttributes.cn", "base type");
        DEFAULT_INSTANCE.setProperty("keepAttributes.ci", "type");
        DEFAULT_INSTANCE.setProperty("keepAttributes.set", "type=normal type=multiset");
        DEFAULT_INSTANCE.setProperty("keepAttributes.tendsto", "type=above type=below type=two-sided");
        DEFAULT_INSTANCE.setProperty("keepAttributes.interval", "closure");
        DEFAULT_INSTANCE.setProperty("keepAttributes.declare", "nargs occurrence");
        DEFAULT_INSTANCE.setProperty("keepAttributes.mfenced", "open close");
    }

    @Test
    public void testPhantom() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "mphantom");
    }

    @Test
    public void testFraction() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "mfrac");
    }

    @Test
    public void testComments() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "comments");
    }

    @Test
    public void testAttributes() {
        testXML(DEFAULT_INSTANCE, RESOURCE_SUBDIR + "attributes");
    }

}
