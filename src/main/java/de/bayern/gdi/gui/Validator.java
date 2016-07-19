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
import java.util.ArrayList;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class Validator {
    private ArrayList<String> javaSpaces;

    /** Holds the instance. */
    private static final class Holder {
        static final Validator INSTANCE = new Validator();
    }

    private Validator() {
        this.javaSpaces = new ArrayList<String>();
        javaSpaces.add("java.lang.");
        javaSpaces.add("java.util.");
        javaSpaces.add("javax.xml.namespace.");
        javaSpaces.add("com.vividsolutions.jts.geom.");
    }

    /**
     * returns an instance of the class.
     *
     * @return the class
     */
    public static Validator getInstance() {
        synchronized (Holder.INSTANCE) {
            return Holder.INSTANCE;
        }
    }

    /**
     * Checks if the value can be casted to the class with the classname.
     *
     * @param value     value to be casted
     * @param className to classname
     * @return true if it works; false if not
     */
    public boolean isValid(String className, String value) {
        if (value != null) {
            if (!value.equals("")) {
                try {
                    Class<?> aClass = classByName(className);
                    return isCastableTo(aClass, value);
                } catch (ClassNotFoundException ex) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCastableTo(Class myClass, String value) {
        boolean constructorTest = false;
        for (Constructor constructor : myClass.getConstructors()) {
            try {
                constructor.newInstance(value);
                constructorTest = true;
                break;
            } catch (IllegalArgumentException | InstantiationException
                    | IllegalAccessException | InvocationTargetException e) {
                constructorTest = false;
            }
        }
        return constructorTest;
    }

    private Class classByName(String className) throws
            ClassNotFoundException {
        if (!className.contains(".")) {
            className = className.substring(0, 1).toUpperCase()
                   + className.substring(1, className.length());
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            for (String namespace: javaSpaces) {
                try {
                    Class<?> aClass =
                            systemClassLoader.loadClass(namespace + className);
                    return aClass;
                } catch (ClassNotFoundException e) {

                }
            }
        }
        return null;
    }



}
