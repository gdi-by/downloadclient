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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class Validator {

    private Validator() {
        // ignore me!
    }

    private static final String[] JAVASPACES = {
            "java.lang.",
            "java.util.",
            "javax.xml.namespace.",
            "org.locationtech.jts.geom."
    };


    /**
     * Checks if the value can be casted to the class with the classname.
     *
     * @param value     value to be casted
     * @param className to classname
     * @return true if it works; false if not
     */
    public static boolean isValid(String className, String value) {
        if (value != null && !value.isEmpty()) {
            Class<?> aClass = classByName(className);
            if (aClass != null) {
                return isCastableTo(aClass, value);
            }
        }
        //https://github.com/gdi-by/downloadclient-test/
        // issues/24#issuecomment-233619602
        return true;
    }

    private static boolean isCastableTo(Class myClass, String value) {
        for (Constructor constructor : myClass.getConstructors()) {
            try {
                constructor.newInstance(value);
                return true;
            } catch (IllegalArgumentException | InstantiationException
                    | IllegalAccessException | InvocationTargetException e) {
                // Ignore this an try the Constructors.
            }
        }
        return false;
    }

    private static Class classByName(String className) {
        if (!className.contains(".")) {
            className = className.substring(0, 1).toUpperCase()
                   + className.substring(1, className.length());
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            for (String namespace: JAVASPACES) {
                try {
                    return systemClassLoader.loadClass(namespace + className);
                } catch (ClassNotFoundException e) {
                    // Ignore this and try the other classes.
                }
            }
        }
        return null;
    }
}
