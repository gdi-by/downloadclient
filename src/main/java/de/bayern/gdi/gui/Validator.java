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

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
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
    private javax.validation.Validator validator;
    private Map<String,Reflections> reflectionsList;
    private static Validator instance;

    private Validator() {
        this.validator = Validation
                .buildDefaultValidatorFactory().getValidator();
        this.reflectionsList = new HashMap<String, Reflections>();
    }

    /**
     * returns an instance of the class
     * @return the class
     */
    public static Validator getInstance() {
        if (instance == null) {
            instance = new Validator();
        }
        return instance;
    }

    /**
     * Checks if the value can be casted to the class with the classname.
     * @param value value to be casted
     * @param className to classname
     * @return true if it works; false if not
     */
    public boolean isValid(String className, String value) {
        if (value != null || !value.equals("")) {
            try {
                Class<?> aClass = classByName(className);
                return isCastableTo(aClass, value);
            } catch (ClassNotFoundException ex) {
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean isCastableTo(Class myClass, String value) {
        //java.lang.IllegalArgumentException
        Set<? extends ConstraintViolation<?>> constraintViolations;
        try {
            constraintViolations = validator.validateValue(myClass,
                    "value",
                    value);
        } catch (IllegalArgumentException e ) {
            return false;
        }
        if (constraintViolations.isEmpty()) {
            return true;
        }
        return false;
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
            for(Class oneClass: allClasses) {
                if(oneClass.getName().toLowerCase().endsWith("." + className
                        .toLowerCase())) {
                    return oneClass;
                }
            }
            //When not returned yet, we won't find it
            throw new ClassNotFoundException("Class " + className + "not in " +
                    "java.utils or java.lang found");
        }
        return Class.forName(className);
    }

    private Reflections reflectionForPackage(String pacakgeName) {
        if(this.reflectionsList.containsKey(pacakgeName)) {
            return this.reflectionsList.get(pacakgeName);
        }
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());
        Reflections refl = new Reflections(new
                ConfigurationBuilder().setScanners(
                    new SubTypesScanner(false
                                /* don't exclude Object.class */),
                    new ResourcesScanner()
                ).setUrls(ClasspathHelper.forClassLoader(
                    classLoadersList.toArray(new ClassLoader[0])
                )).filterInputsBy(new FilterBuilder().
                    include(FilterBuilder.prefix(pacakgeName))
                )
            );
        this.reflectionsList.put(pacakgeName, refl);
        return refl;
    }

}
