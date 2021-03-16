package sbu.testlab.coverage.oocoverage;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Util {
    private static final Logger log = Logger.getLogger(Util.class.getName());

    public static Map<String, List<String>> allParents(String packageFilter, String notContainsPhrase) {
        Map<String, List<String>> result = new HashMap<>();

        ClassLoader cl = Util.class.getClassLoader();
        try {
            Set<ClassPath.ClassInfo> classesInPackage = ClassPath
                    .from(cl)
                    .getTopLevelClassesRecursive(packageFilter);

            for (ClassPath.ClassInfo classInfo : classesInPackage) {

                //ignore class with name including notContainsPhrase
                if (!"".equals(notContainsPhrase)) {
                    if (classInfo.getName().contains(notContainsPhrase)) {
                        continue;
                    }
                }

                //ignore package-info files
                if (classInfo.getName().endsWith("package-info"))
                    continue;

                Class<?> loadedClass = null;
                try {
                    loadedClass = classInfo.load();
                } catch (RuntimeException e) {
                    log.warning("Could not load class: " + classInfo.getName() + "(error: " + e.getMessage() + ")");
                    continue;
                }

                List<String> list = getParentsNameRecursive(loadedClass, packageFilter, notContainsPhrase);
                result.put(loadedClass.getName(), list);

                String listString = "";
                for (String s : list)
                    listString += s + ", ";
                log.info("class: [" + loadedClass.getName() + "] parents: " + listString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, List<String>> allParents(String[] classes) {
        Map<String, List<String>> result = new HashMap<>();
        ClassLoader classLoader = Util.class.getClassLoader();
        for(String classname:classes){
            try {
                Class<?> clazz = classLoader.loadClass(classname);
                List<String> parents = getParentsNameRecursive(clazz, "", "");
                result.put(clazz.getName(),parents);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
        }
        return result;
    }

    public static List<String> getParentsNameRecursive(Class<?> child, String basePackage, String notContainsPhrase) {
        Class<?> superClass = child.getSuperclass();
        if (superClass == null || "java.lang.Object".equals(superClass.getName()))
            return new ArrayList<>();
        List<String> list = getParentsNameRecursive(superClass, basePackage, notContainsPhrase);

        //filter if superclass has not contain basePackage or its contains notContainsPhrase
        if (superClass.getName().contains(basePackage)) {
            if ("".equals(notContainsPhrase)) {
                list.add(superClass.getName());
            } else if (!superClass.getName().contains(notContainsPhrase)) {
                list.add(superClass.getName());
            }
        }
        return list;
    }
}
