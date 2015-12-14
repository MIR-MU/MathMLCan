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

import org.jdom2.Document;

/**
 * Modules processing the input using Document Object Model
 *
 * @author David Formanek
 */
public interface DOMModule extends Module {

    /**
     * Executes the canonicalization module
     *
     * @param doc document to be modified according to the module specification
     * @throws ModuleException when cannot transform the input by this module
     */
    public void execute(Document doc) throws ModuleException;

}
