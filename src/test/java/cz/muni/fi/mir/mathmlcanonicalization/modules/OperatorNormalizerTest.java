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

import org.junit.Test;

/**
 * Test for OperatorNormalizer canonicalization DOM Module.
 *
 * @author David Formanek
 */
public class OperatorNormalizerTest extends AbstractModuleTest {

    private static final Module DEFAULT_INSTANCE = new OperatorNormalizer();

    @Test
    public void testMultiplicationCdot() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/multiplication-cdot");
    }

    @Test
    public void testMultiplicationBlank() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/multiplication-blank");
    }

    @Test
    public void testUnification() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/operator-unification");
    }

    @Test
    public void testUnicode() {
        // not working for normalized multibyte symbols
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/unicode");
    }

    @Test
    public void testIdentifierReplacing() {
        testXML(DEFAULT_INSTANCE, "operatorNormalizer/identifier-replacement");
    }
}
