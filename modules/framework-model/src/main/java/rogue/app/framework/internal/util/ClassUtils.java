/*
 * Copyright 2013, Rogue.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rogue.app.framework.internal.util;

import com.google.common.base.Preconditions;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods that work on the structure of a class.
 */
public final class ClassUtils
{
    private static final String CLASS_NAME = ClassUtils.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * The set of marker files that we would look for, to identify the code paths that need to be searched.
     */
    private static final String[] MARKER_FILES = {"META-INF/persistence.xml", "META-INF/orm.xml", "META-INF/beans.xml"};
    private static Reflections REFLECTIONS;

    private ClassUtils()
    {
    }


    /**
     * Searches for the given annotation on both the class hierarchy as well as its interface hierarchy.
     *
     * @param clazz           the class to introspect
     * @param annotationClazz the annotation to fetch
     * @return the annotation or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A getAnnotation(Class clazz, Class<A> annotationClazz)
    {
        if (clazz == null || annotationClazz == null)
        {
            return null;
        }

        A annotation = (A) clazz.getAnnotation(annotationClazz);
        if (annotation == null)
        {
            // Check on the interfaces.
            Class[] interfaces = clazz.getInterfaces();
            for (Class c : interfaces)
            {
                annotation = (A) c.getAnnotation(annotationClazz);
                if (annotation != null)
                {
                    break;
                }
            }
        }

        return annotation;
    }

    /**
     * Get the first field belonging to the given class that has the specified annotation.
     *
     * @param clazz           the class to scan.
     * @param annotationClazz the annotation to look for.
     * @return the first field belonging to the given class that has the specified annotation, or null if no such field
     *         was found.
     */
    public static Field getAnnotatedField(Class clazz, Class<? extends Annotation> annotationClazz)
    {
        Preconditions.checkNotNull(clazz, "Class reference cannot be null");
        Preconditions.checkNotNull(annotationClazz, "Annotation class reference cannot be null");

        while (clazz != null)
        {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields)
            {
                if (f.isAnnotationPresent(annotationClazz))
                {
                    return f;
                }
            }

            // move to the parent class.
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Search for methods, in the class hierarchy as well as the interface hierarchy, which have the specified
     * annotation assigned to them. If no matches were found, an empty list is returned.
     *
     * @param clazz           the class to introspect
     * @param annotationClazz the annotation to look for
     * @return a list of Method objects.
     */
    public static List<Method> getAnnotatedMethods(Class clazz,
                                                   Class<? extends Annotation> annotationClazz)
    {
        Map<String, Method> methodMetadataMap = new HashMap<>();
        Method[] methods = clazz.getMethods();
        for (Method m : methods)
        {
            if (m.isAnnotationPresent(annotationClazz) && !methodMetadataMap.containsKey(m.getName()))
            {
                methodMetadataMap.put(m.getName(), m);
            }
        }

        // Now scan for the methods associated with all the interfaces
        Class[] interfaces = clazz.getInterfaces();
        for (Class c : interfaces)
        {
            methods = c.getMethods();
            for (Method m : methods)
            {
                if (m.isAnnotationPresent(annotationClazz) && !methodMetadataMap.containsKey(m.getName()))
                {
                    methodMetadataMap.put(m.getName(), m);
                }
            }
        }

        List<Method> metadataList = new ArrayList<Method>();
        metadataList.addAll(methodMetadataMap.values());
        return Collections.unmodifiableList(metadataList);
    }

    public static Iterator<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation)
    {
        initReflections();

        Set<Class<?>> classes = REFLECTIONS.getTypesAnnotatedWith(annotation);
        return classes != null ? classes.iterator() : null;
    }

    public static Iterator<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation)
    {
        initReflections();
        Set<Method> methods = REFLECTIONS.getMethodsAnnotatedWith(annotation);
        return methods != null ? methods.iterator() : null;
    }

    private static void initReflections()
    {
        final String METHOD_NAME = "initReflections";
        if (REFLECTIONS == null)
        {
            Set<URL> processedUrls = new HashSet<>();
            for (String marker : MARKER_FILES)
            {
                try
                {
                    Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(marker);
                    while (resources.hasMoreElements())
                    {
                        URL url = resources.nextElement();
                        if ("jar".equalsIgnoreCase(url.getProtocol()))
                        {
                            try
                            {
                                JarURLConnection conn = (JarURLConnection) url.openConnection();
                                processedUrls.add(conn.getJarFileURL());
                            }
                            catch (IOException e)
                            {
                                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to open jar file.", e);
                            }
                        }
                        else if ("file".equalsIgnoreCase(url.getProtocol()))
                        {
                            String file = url.getFile();
                            int idx = file.indexOf(marker);
                            if (idx != -1)
                            {
                                file = file.substring(0, idx);
                            }
                            processedUrls.add(new File(file).toURI().toURL());
                        }
                        else
                        {
                            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Ignoring unhandled protocol: " + url);
                        }

                    }
                }
                catch (IOException e)
                {
                    LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                                "Failed to parse marker files in the runtime paths");
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                "Failed to parse marker files in the runtime paths", e);
                }
            }
            REFLECTIONS = new Reflections(new TypeAnnotationsScanner(), new MethodAnnotationsScanner(),
                                          processedUrls.toArray());
        }
    }
}
