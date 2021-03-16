package sbu.testlab.coverage.oocoverage.analysis;

import com.google.common.base.Joiner;
import dnl.utils.text.table.TextTable;
import picocli.CommandLine;
import sbu.testlab.coverage.oocoverage.Coverage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command
public class OCoverageCalculationCommand implements Callable<Integer> {
    public static final String POLY_FILE = "poly.csv";

    final File TEMP_DIRECTORY = new File(".temp");

    @CommandLine.Option(names = {"-p", "--poly"}, paramLabel = "<calculate poly coverage>", description = "calculate poly coverage from a config file in option --config")
    boolean calculatePolyCoverage = false;

    @CommandLine.Option(names = {"--poly-config-file"}, paramLabel = "<config file for calculate poly coverage>", description = "address of config file for calculate poly coverage", defaultValue = POLY_FILE)
    File polyConfigFile;

    @CommandLine.Option(names = {"-d", "--details"}, paramLabel = "<show details>", description = "Show details of class names and numbers related to coverage calculation")
    boolean showDetails = false;

    @CommandLine.Option(names = {"-a", "--all-file"}, paramLabel = "<cover items data file>", description = "The address of data file contains all cover items information", defaultValue = Coverage.COVERABLE_FILE)
    File allCoverableFile;

    @CommandLine.Option(names = {"-c", "--cover-file"}, paramLabel = "<covered items data file>", description = "The address of data file contains all covered items information", defaultValue = Coverage.COVERED_FILE)
    File coveredFile;

    @CommandLine.Parameters(paramLabel = "Tests", description = "Only calculate coverage for these classes")
    String[] classNames;


    @Override
    public Integer call() {
        if(calculatePolyCoverage)
            return callPolyObjectCoverage();
        return callForObjectCoverage();
    }

    public Integer callPolyObjectCoverage() {
        //check precondition
        if (!polyConfigFile.exists()) {
            System.out.println("ERROR: for calculating poly-coverage you should specify a config file contains class names and corresponding coverage csv files => " + polyConfigFile.getPath() + " dose not exist");
            return 1;
        }
        if (classNames == null || classNames.length == 0) {
            System.out.println("ERROR: You should specify at least one class file as a base class for calculating poly-coverage ");
            return 1;
        }

        //read poly config file
        Map<String, File> classToCoverageFile = null;
        try {
            classToCoverageFile = readFromCSV(polyConfigFile, ",");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: An exception occurred through reading poly config file");
            return 1;
        }

        //check each class file exists
        for (File coverageFile : classToCoverageFile.values())
            if (!coveredFile.exists()) {
                System.out.println("ERROR: one of coverage file specified in config file dose not exist => " + coverageFile.getPath());
                return 1;
            }

        //calculate poly coverage
        String baseClassName = classNames[0];
        OCoverageCalculator oCoverageCalculator;
        StringBuilder descendentClasses=new StringBuilder();
        StringBuilder descendentClassesWithoutNamespace=new StringBuilder();
        double result = 0.0;
        int numberOfChildClass = 0;
        for (Map.Entry<String, File> classFileTuple : classToCoverageFile.entrySet()) {
            try {
                String objectName=classFileTuple.getKey();
                oCoverageCalculator = new OCoverageCalculator(allCoverableFile, classFileTuple.getValue());
                if(!oCoverageCalculator.isObjectVisitedABaseClass(objectName,baseClassName))
                    continue;
                result += oCoverageCalculator.getPolyObjectCoverageUsingJustAnObjectWithABaseClass(classFileTuple.getKey(), baseClassName);
                numberOfChildClass++;

                if(descendentClasses.length()>0) {
                    descendentClasses.append(", ");
                    descendentClassesWithoutNamespace.append(", ");
                }
                descendentClasses.append(objectName);
                descendentClassesWithoutNamespace.append(withoutNamespace(objectName));

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("ERROR: an error occurred through reading one of data coverage files => " + classFileTuple.getValue().getPath());
                return 1;
            }
        }

        StringBuilder textResult=new StringBuilder();
        textResult.append("===ocov4j poly coverage result===");
        textResult.append(System.lineSeparator());
        textResult.append("poly coverage: "+String.format("%.3f",result/numberOfChildClass));
        textResult.append(System.lineSeparator());
        textResult.append("class: "+withoutNamespace(baseClassName));
        textResult.append(System.lineSeparator());
        textResult.append("class (namespaced): "+baseClassName);
        textResult.append(System.lineSeparator());
        textResult.append("descendents class: "+descendentClassesWithoutNamespace.toString());
        textResult.append(System.lineSeparator());
        textResult.append("descendents class (namespaced): "+descendentClasses.toString());

        //save final result to file
        try (PrintStream out = new PrintStream(new FileOutputStream("poly-coverage-result.txt"))) {

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("ERROR: an error occurred through writing text result to file");
        }

        return 0;
    }

    public Integer callForObjectCoverage() {
        //check precondition
        if (!allCoverableFile.exists()) {
            System.out.println("ERROR: The <cover items data file> directory does not exist : " + allCoverableFile.getPath() + "\n" + "You should first run <ocov.jar> as a java-agent through running junit tests to extract this file for you");
            return 1;
        }
        if (!coveredFile.exists()) {
            System.out.println("ERROR: The <covered items data file>> directory does not exist : " + allCoverableFile.getPath() + "\n" + "You should first run <ocov.jar> as a java-agent through running junit tests to extract this file for you");
            return 1;
        }

        OCoverageCalculator oCoverageCalculator;
        try {
            oCoverageCalculator = new OCoverageCalculator(allCoverableFile, coveredFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERROR: an error ocurred through reading data files");
            return 1;
        }

        Map<String, Double> objectCoverages = oCoverageCalculator.getObjectCoverage();
        Map<String, Double> classCoverages = oCoverageCalculator.getClassCoverage();

        if (showDetails) {
            System.out.println("\n=== Details Data ===");
            System.out.println("All Coverable Lines: \n" +
                    Joiner
                            .on("\n")
                            .withKeyValueSeparator("=")
                            .join(oCoverageCalculator.classToAllLines)
            );
        }

        System.out.println("\n=== Coverage Data ===");

        System.out.println("\nClass = [Parents...] \n" +
                Joiner
                        .on("\n")
                        .withKeyValueSeparator("=")
                        .join(oCoverageCalculator.classToParents)
        );

        //make table of coverage
        String[][] tableData = new String[objectCoverages.size()][];
        int row = 0;
        for (Map.Entry<String, Double> entry : objectCoverages.entrySet()) {
            String clazz = entry.getKey();
            Double coverage = entry.getValue();
            tableData[row] = new String[3];
            tableData[row][0] = clazz;
            tableData[row][1] = String.format("%.3f", coverage);
            tableData[row][2] = classCoverages.get(clazz) != null ? String.format("%.3f", classCoverages.get(clazz)) + "" : "-";
            row++;
        }

        //print final result
        System.out.println("\nObject Coverage Table");
        TextTable textTable;
        textTable = new TextTable(new String[]{"Class", "Object line Coverage", "Traditional line Coverage"}, tableData);
        textTable.setSort(0);
        textTable.setAddRowNumbering(true);
        textTable.printTable();

        //save final result to file
        try (PrintStream out = new PrintStream(new FileOutputStream("coverage-result.txt"))) {
            out.println("\nClass = [Parents...] \n" +
                    Joiner
                            .on("\n")
                            .withKeyValueSeparator("=")
                            .join(oCoverageCalculator.classToParents)
            );
            textTable.printTable(out, 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("ERROR: an error occurred through writing text result to file");
        }

        try (PrintStream out = new PrintStream(new FileOutputStream("coverage-result.csv"))) {
            textTable.toCsv(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("ERROR: an error occurred through writing csv result to file");
        }


        return 0;
    }

    private Map<String, File> readFromCSV(File file, String splitter) throws IOException {
        Map<String, File> result = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                if ("".equals(line.trim()))
                    continue;
                String[] classAndFile = line.split(splitter);
                result.put(classAndFile[0].trim(), new File(classAndFile[0].trim()));
            }
        }
        return result;
    }

    private String withoutNamespace(String objectName) {
        return objectName.substring(objectName.lastIndexOf(".")+1,objectName.length()-1);
    }

}
