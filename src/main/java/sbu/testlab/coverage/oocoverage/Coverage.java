package sbu.testlab.coverage.oocoverage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

public class Coverage {
    private static final Logger log = Logger.getLogger(Coverage.class.getName());

    public static final String COVERED_FILE = "oocov-covered-lines.csv";
    public static final String COVERABLE_FILE = "oocov-coverable-lines.csv";
    public static final String ALL_CLASSES_FILE = "oocov-classes.csv";
    static Map<String, Set<String>> coverageMatrix=new HashMap<>();

    public static void cleanCoverageFiles() {
        //clean cover data files
        try {
                Files.deleteIfExists(Paths.get(COVERED_FILE));
                Files.deleteIfExists(Paths.get(COVERABLE_FILE));
                Files.deleteIfExists(Paths.get(ALL_CLASSES_FILE));
            } catch (IOException e) {
                e.printStackTrace();
                log.warning("Could not delete existing coverage files");
            }
    }

    public static void extractAllClassAndParents(String basePackage, String notContainsPhrase){
        PrintWriter pw = null;
        try {
            File file = new File(ALL_CLASSES_FILE);
            FileWriter fw = new FileWriter(file, true);
            pw = new PrintWriter(fw);

            //go through map of class and parents
            Map<String, List<String>> classParentsMap = Util.allParents(basePackage,notContainsPhrase);
            for(Map.Entry<String, List<String>> item:classParentsMap.entrySet()){
                String parents="";
                for(String parent:item.getValue())
                    parents+=","+parent;
                if("".equals(parents))
                    pw.println(item.getKey());
                else
                    pw.println(item.getKey()+parents);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

    }
    public static void visitLine(Object caller, String coverageDetailsStr) {

        //System.out.println("caller: "+caller+" --- "+ coverageDetailsStr);

        String[] coverageDetails=coverageDetailsStr.split(",");

        //details variables
        String className=coverageDetails[0].replaceAll("/",".");
        String lineNumber=coverageDetails[1];

        String objectClassKey=caller.getClass().getName()+"::"+className;

        Set<String> coveredLineNumber= coverageMatrix.get(objectClassKey);

        if(coveredLineNumber==null) {
            coveredLineNumber = new HashSet<>();
            coverageMatrix.put(objectClassKey,coveredLineNumber);
        }

        if(!coveredLineNumber.contains(lineNumber)){
            coveredLineNumber.add(lineNumber);
            appendLineNumberToCoveredFile(objectClassKey,lineNumber);
        }

    }

    public static void recordLineNumber(String coverableDetailsStr){
        String[] coverageDetails=coverableDetailsStr.split(",");

        //details variables
        String className=coverageDetails[0].replaceAll("/",".");
        String lineNumber=coverageDetails[1];

        appendLineNumberToCoverableFile(className,lineNumber);
    }

    private static void appendLineNumberToCoveredFile(String s, String lineNumber) {
        String newData = s+","+lineNumber+"\n";

        if(!Files.exists(Paths.get(COVERED_FILE))){
            try {
                Files.createFile(Paths.get(COVERED_FILE));
            } catch (IOException e) {
                e.printStackTrace();
                log.warning("could not create coverage file");
            }
        }

        try {
            Files.write(
                    Paths.get(COVERED_FILE),
                    newData.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            log.warning("could not create coverage file");
        }
    }

    private static void appendLineNumberToCoverableFile(String s, String lineNumber) {
        String newData = s+","+lineNumber+"\n";

        if(!Files.exists(Paths.get(COVERABLE_FILE))){
            try {
                Files.createFile(Paths.get(COVERABLE_FILE));
            } catch (IOException e) {
                e.printStackTrace();
                log.warning("could not create coverage file");
            }
        }

        try {
            Files.write(
                    Paths.get(COVERABLE_FILE),
                    newData.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            log.warning("error in writing coverage file data");
        }
    }

    public static void recordClassParents(String clazz, List<String> parents){
        String line=clazz;
        for(String parentName:parents)
            line+=","+parentName;

        appendClassNamesToClassAndParentsFile(line);
    }
    private static void appendClassNamesToClassAndParentsFile(String line) {

        if(!Files.exists(Paths.get(ALL_CLASSES_FILE))){
            try {
                Files.createFile(Paths.get(ALL_CLASSES_FILE));
            } catch (IOException e) {
                e.printStackTrace();
                log.warning("could not create class-and-parents file");
            }
        }

        try {
            Files.write(
                    Paths.get(ALL_CLASSES_FILE),
                    line.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            log.warning("error in writing class-and-parents file data");
        }
    }

    public static void extractAllClassAndParents(String[] classes) {
        PrintWriter pw = null;
        try {
            File file = new File(ALL_CLASSES_FILE);
            FileWriter fw = new FileWriter(file, true);
            pw = new PrintWriter(fw);

            //go through map of class and parents
            Map<String, List<String>> classParentsMap = Util.allParents(classes);
            for(Map.Entry<String, List<String>> item:classParentsMap.entrySet()){
                String parents="";
                for(String parent:item.getValue())
                    parents+=","+parent;
                if("".equals(parents))
                    pw.println(item.getKey());
                else
                    pw.println(item.getKey()+parents);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
