package sbu.testlab.coverage.oocoverage.analysis;

import com.google.common.base.Joiner;
import picocli.CommandLine;
import sbu.testlab.coverage.oocoverage.Coverage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class OCoverageLauncher {

    public static void main(String[] args) throws IOException {
        int exitCode;
        exitCode = new CommandLine(new OCoverageCalculationCommand()).execute(args);
        System.exit(exitCode);

       /* if(args.length==0){
            System.out.println("You should specify at least one class name to obtain coverages");
            return;
        }

        Coverage.extractAllClassAndParents(args);

        System.out.println("=============== All Class with Parents ==================");
        OCoverageCalculator oCoverage=new OCoverageCalculator();
        System.out.println(
                Joiner
                        .on("\n")
                        .withKeyValueSeparator("=")
                        .join(oCoverage.classToParents)
        );

        System.out.println("=============== All Coverable Lines ==================");
        System.out.println(
                Joiner
                        .on("\n")
                        .withKeyValueSeparator("=")
                        .join(oCoverage.classToAllLines)
        );

        System.out.println("=============== Number of All Coverable lines ==================");
        for(Map.Entry<String, Set<String>> classes:oCoverage.classToAllLines.entrySet()){
            System.out.println(classes.getKey()+": "+classes.getValue().size());
        }

        System.out.println("================= Covered Object::Class lines ================");
        System.out.println(
                Joiner
                        .on("\n")
                        .withKeyValueSeparator("=")
                        .join(oCoverage.classToCoveredLines)
        );



        System.out.println("================= Object Coverage ================");
        System.out.println(
                Joiner
                        .on("\n")
                        .withKeyValueSeparator("=")
                        .join(oCoverage.getObjectCoverage())
        );

        System.out.println("================= Traditional Class Coverage ================");
        System.out.println(
                Joiner
                        .on("\n")
                        .withKeyValueSeparator("=")
                        .join(oCoverage.getClassCoverage())
        );*/
    }

}




