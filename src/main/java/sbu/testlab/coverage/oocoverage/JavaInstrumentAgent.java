package sbu.testlab.coverage.oocoverage;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class JavaInstrumentAgent {
    private static final Logger log = Logger.getLogger(JavaInstrumentAgent.class.getName());
    static String containsPhrase, notContainsPhrase;
    /**
     * As soon as the JVM initializes, This  method will be called.
     * Configs for intercepting will be read and added to Transformer so that Transformer will intercept when the
     * corresponding Java Class and Method is loaded.
     *
     * @param agentArgs       The list of agent arguments
     * @param instrumentation The instrumentation object
     * @throws InstantiationException While  an instantiation of object cause an error.
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) throws InstantiationException {

        log.info("Starting OO-Coverage Instrument Agent");
        log.info("Class starting with this package will be instrumented: "+agentArgs);
        Coverage.cleanCoverageFiles();

        if(agentArgs.contains(",")) {
            String[] args = agentArgs.split(",");
            containsPhrase = args[0].trim();
            notContainsPhrase = args[1].trim();
        }
        else {
            containsPhrase=agentArgs.trim();
        }

        ASMInstrumentTransformer transformer=new ASMInstrumentTransformer(
                containsPhrase.replaceAll("\\.","/"),//
                notContainsPhrase.replaceAll("\\.","/"));

        instrumentation.addTransformer(transformer);

        //Coverage.extractAllClassAndParents(containsPhrase,notContainsPhrase);
    }
}

