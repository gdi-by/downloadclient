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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */

public class Validator {
    private Map<String, Reflections> reflectionsList;
    
    /** Holds the instance. */
    private static final class Holder {
        static final Validator INSTANCE = new Validator();
    }

    private Validator() {
        this.reflectionsList = new HashMap<String, Reflections>();
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
            // Some Basetypes are present in the java.lang package
            Reflections langreflections = reflectionForPackage("java.lang");
            Set<Class<? extends Object>> allClasses =
                    langreflections.getSubTypesOf(Object.class);
            //others are in the java.utils package
            Reflections utilsreflections = reflectionForPackage("java.utils");
            allClasses.addAll(utilsreflections.getSubTypesOf(Object.class));
            //Stuff like QName is in javax.xml.namespace
            Reflections javaxNamespaceReflection = reflectionForPackage(
                    "javax.xml.namespace"
            );
            allClasses.addAll(javaxNamespaceReflection
                    .getSubTypesOf(Object.class));
            //Geometries will also be found: com.vividsolutions.jts.geom
            Reflections geometryReflection = reflectionForPackage(
                    "com.vividsolutions.jts.geom"
            );
            allClasses.addAll(geometryReflection.getSubTypesOf(Object.class));
            for (Class oneClass : allClasses) {
                if (oneClass.getName().toLowerCase().endsWith("." + className
                        .toLowerCase())) {
                    return oneClass;
                }
            }
            //When not returned yet, we won't find it
            throw new ClassNotFoundException("Class " + className + "not in "
                    + "java.utils, java.lang found or "
                    + "com.vividsolutions.jts.geom");
        }
        return Class.forName(className);
    }

    private Reflections reflectionForPackage(String packageName) {
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());
        Reflections refl = new Reflections(
                new ConfigurationBuilder().setScanners(
                new SubTypesScanner(false
                                /* don't exclude Object.class */),
                new ResourcesScanner()
            ).setUrls(ClasspathHelper.forClassLoader(
                classLoadersList.toArray(new ClassLoader[0])
            )).filterInputsBy(new FilterBuilder().
                include(FilterBuilder.prefix(packageName))
            )
        );
        this.reflectionsList.put(packageName, refl);
        return refl;
    }

}
