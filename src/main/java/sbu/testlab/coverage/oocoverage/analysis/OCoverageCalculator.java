package sbu.testlab.coverage.oocoverage.analysis;

import sbu.testlab.coverage.oocoverage.Coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class OCoverageCalculator {
    private static final Logger log = Logger.getLogger(OCoverageCalculator.class.getName());

    public Map<String, Set<String>> classToParents=new HashMap<>();

    public Set<String> allClasses;
    public Map<String, Set<String>> classToAllLines;
    public Map<String, Set<String>> classToCoveredLines;

    public OCoverageCalculator(File allCoverableFile, File coveredFile) throws IOException {
        readCoverageFiles(allCoverableFile,coveredFile);
    }

    /**
     * compute Object code coverage
     *
     * @return
     */
    public Map<String, Double> getObjectCoverage() {

        // result
        Map<String, Double> result = new HashMap<>();

        for (String objectName : allClasses) {
            CoverageValue coverageValue = new CoverageValue(0, 0);
            Set<String> parents=new HashSet<>();
            classToCoveredLines.forEach((ObjectColonClass, coveredLinesSet) -> {
                String onlyObject = ObjectColonClass.split("::")[0];
                String onlyClass = ObjectColonClass.split("::")[1];
                if (objectName.equals(onlyObject)) {
                    coverageValue.allLines += classToAllLines.get(onlyClass).size();
                    coverageValue.coveredLines  += coveredLinesSet.size();

                    // add parents to list of parents
                    if(!objectName.equals(onlyClass))
                        parents.add(onlyClass);
                }
            });

            result.put(objectName,coverageValue.getPercent());
            classToParents.put(objectName,parents);

       }

        return result;
    }

    /**
     * compute traditional code coverage
     *
     * @return
     */
    public Map<String, Double> getClassCoverage() {

        Map<String, Double> result = new HashMap<>();
        for (String className : allClasses) {
            //compute all covered lines of class
            Set<String> classCoveredLines = new HashSet<>();

            classToCoveredLines.forEach((ObjectColonClass, coveredLinesSet) -> {
                String onlyClass = ObjectColonClass.split("::")[1];
                if (className.equals(onlyClass)) {
                    classCoveredLines.addAll(coveredLinesSet);
                }
            });

            double allLines = classToAllLines.get(className).size();
            double coveredLines = classCoveredLines.size();

            result.put(className, coveredLines / allLines);
        }

        return result;
    }

    /**
     * compute Object code coverage
     *
     * @return
     */
    public double getPolyObjectCoverageUsingJustAnObjectWithABaseClass(String objectName, String baseClassName) {
        double numberOfAllLines=classToAllLines.get(baseClassName).size();
        String childColonParent=objectName+"::"+baseClassName;
        double numberOfCoveredLines=classToCoveredLines.get(childColonParent).size();
        return numberOfCoveredLines/numberOfAllLines;
    }

    public boolean isObjectVisitedABaseClass(String objectName, String baseClassName) {
        String childColonParent=objectName+"::"+baseClassName;
        return classToCoveredLines.containsKey(childColonParent);
    }

    private void readCoverageFiles(File coverableFile, File coveredFile) throws IOException {
        //read class-parents file from oocov-class.csv
        //List<List<String>> classes = readFromCSV(Coverage.ALL_CLASSES_FILE, ",");
        //classToParents = listToMapByFirstItem(classes);

        //read coverable lines foreach class from oocov-coverable-lines.csv
        List<List<String>> allLines = readFromCSV(coverableFile, ",");
        classToAllLines = TupleToMapByFirstItem(allLines);

        //read covered lines foreach class from oocov-coverable-lines.csv
        List<List<String>> coveredLines = readFromCSV(coveredFile, ",");
        classToCoveredLines = TupleToMapByFirstItem(coveredLines);

        allClasses=new HashSet<>();
        for (List<String> line : coveredLines) {
            String objectColonClassName = line.get(0);
            allClasses.add(objectColonClassName.split("::")[0]);
        }
    }

    /**
     * suppose a list with each list's item is a tuple.
     * this method convert this list to map that key is the first item, and value a list of second item.
     * for example, list is:
     * <pre>
     *     [
     *       [ali, 1]
     *       [reza, 15]
     *       [ali, 8]
     *       [reza, 10]
     *       [ali, 8]
     *       [mohammad, 10]
     *     ]
     * </pre>
     * the above list is convert to a map like this
     * <pre>
     *     {
     *         "ali":{1,8},
     *         "reza":{15,10},
     *         "mohammad":{10}
     *     }
     * </pre>
     *
     * @param list
     * @return
     */
    private Map<String, Set<String>> TupleToMapByFirstItem(List<List<String>> list) {
        Map<String, Set<String>> map = new HashMap<>();
        for (List<String> line : list) {
            //first item is key of map
            String key = line.get(0);

            if (map.containsKey(key)) {
                Set<String> itemSet = map.get(key);
                itemSet.add(line.get(1));
            } else {
                Set<String> value = new HashSet<>();
                value.add(line.get(1));
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * suppose a list with each list's item is list of string items.
     * this method convert this list to map that key is the first item, and other strings as a set value;
     * for example, list is:
     * <pre>
     *     [
     *       [ali, 1, 2, 2, 1, 1, 3]
     *       [reza, 15]
     *       [mohammad, 10,2,10,8]
     *     ]
     * </pre>
     * the above list is convert to a map like this
     * <pre>
     *     {
     *         "ali":{1,2,3},
     *         "reza":{15},
     *         "mohammad":{10,2,8}
     *     }
     * </pre>
     *
     * @param list
     * @return
     */
    private Map<String, Set<String>> listToMapByFirstItem(List<List<String>> list) {
        Map<String, Set<String>> map = new HashMap<>();
        for (List<String> item : list) {
            //first item is key of map, others are its value of map
            String childClass = item.get(0);
            map.put(childClass, new HashSet<String>(item.subList(1, item.size())));
        }
        return map;
    }

    private List<List<String>> readFromCSV(File file, String splitter) throws IOException {
        List<List<String>> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                if ("".equals(line.trim()))
                    continue;
                String[] lineAsList = line.split(splitter);
                result.add(Arrays.asList(lineAsList));
            }
        }
        return result;
    }

    private static class CoverageValue {
        double allLines;
        double coveredLines;

        CoverageValue(int allLines, int coveredLines) {
            this.allLines = allLines;
            this.coveredLines = coveredLines;
        }

        public Double getPercent() {
            if(allLines==0)
                return Double.NaN;
            return coveredLines/allLines;
        }
    }
}
