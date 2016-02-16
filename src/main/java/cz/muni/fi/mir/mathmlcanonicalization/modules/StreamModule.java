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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Modules processing the input as a stream (no DOM)
 *
 * @author David Formanek
 */
public interface StreamModule extends Module {

    /**
     * Executes the canonicalization module.
     *
     * Returned {@link ByteArrayOutputStream} can be converted to
     * {@link InputStream} instance using
     * {@link ByteArrayInputStream#ByteArrayInputStream(byte[])}.
     *
     * @param input input stream to be processed
     * @return the result in accordance with the module specification
     * @throws ModuleException when cannot transform the input by this module
     */
    public ByteArrayOutputStream execute(InputStream input) throws ModuleException;

}
