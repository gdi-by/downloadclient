/*
 * DownloadClient Geodateninfrastruktur Bayern
 *
 * (c) 2016 GSt. GDI-BY (gdi.bayern.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bayern.gdi.gui;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class Validator {

    private Validator() {

    }

    /**
     * Checks if the value can be casted to the class with the classname.
     * @param value value to be casted
     * @param className to classname
     * @return true if it works; false if not
     */
    public boolean isValid(String value, String className) {
        if (value != null || !value.equals("")) {
            javax.validation.Validator jxvalidator = Validation
                    .buildDefaultValidatorFactory().getValidator();
            try {
                Class<?> aClass = Class.forName(className);
                Set<? extends ConstraintViolation<?>> constraintViolations =
                        jxvalidator.validateValue(aClass,
                                value,
                                null);
                if (constraintViolations.size() == 0) {
                    return true;
                }
                return false;
            } catch (ClassNotFoundException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

}
