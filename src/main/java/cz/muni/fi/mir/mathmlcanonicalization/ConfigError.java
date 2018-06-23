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

/**
 * Unrecoverable configuration error.
 *
 * NB: Error subclasses are treated as an unchecked exception.
 */
public class ConfigError extends Error {

    private static final long serialVersionUID = -5662822593110473614L;

    public ConfigError(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigError(String message) {
        super(message);
    }

}
