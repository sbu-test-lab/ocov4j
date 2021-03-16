package sbu.testlab.coverage.oocoverage;

import javassist.Modifier;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.logging.Logger;

public class ASMInstrumentTransformer implements ClassFileTransformer {
    private static final Logger log = Logger.getLogger(ASMInstrumentTransformer.class.getName());

    private final String INSTRUMENT_CLASS = "sbu/testlab/coverage/oocoverage/Coverage";
    private final String INSTRUMENT_VISIT_METHOD = "visitLine";
    private final String INSTRUMENT_VISIT_METHOD_SIG = "(Ljava/lang/Object;Ljava/lang/String;)V";

    private String classNameToInstrument;
    private String classNameNotContains;

    final public static Map<String, Class<?>> instrumentedClasses=new HashMap<>();

    public ASMInstrumentTransformer(String classNameToInstrument) {
        this.classNameToInstrument = classNameToInstrument;
        this.classNameNotContains = "";
    }

    public ASMInstrumentTransformer(String classNameToInstrument, String classNameNotContains) {
        this.classNameToInstrument = classNameToInstrument;
        this.classNameNotContains = classNameNotContains;
    }

    @Override
    public byte[] transform(
            ClassLoader classLoader,//
            String className, //
            Class<?> classBeingRedefined, //
            ProtectionDomain protectionDomain, //
            byte[] classfileBytes) throws IllegalClassFormatException {

        if (!"".equals(classNameNotContains))
            if (className.contains(classNameNotContains))
                return classfileBytes;

        //ignore package-info files
        if (className.endsWith("package-info"))
            return classfileBytes;

        if (className.contains(classNameToInstrument)) {
            log.info("try to instrument class " + className);
            //read class byte array
            ClassReader cr = new ClassReader(classfileBytes);
            ClassNode classNode = new ClassNode();
            cr.accept(classNode, 0);

            for (MethodNode methodNode : classNode.methods) {

                //ignore static initializers
                if (methodNode.name.contains("<clinit>"))
                    continue;

                //ignore static methods
                if ((methodNode.access & Opcodes.ACC_STATIC) != 0) {
                    continue;
                }

                // if method is a constructor, and there is a call to super constructor we should instrument after this super-constructor call
                boolean superConstructorFound=false;
                AbstractInsnNode lineAfterInvokeSuperConstructor=null;
                if (methodNode.name.contains("<init>")) {
                    Iterator<AbstractInsnNode> instructionIter = methodNode.instructions.iterator();
                    while (instructionIter.hasNext()) {
                        AbstractInsnNode currentInstruction = instructionIter.next();
                        if(currentInstruction.getOpcode()==Opcodes.INVOKESPECIAL){
                            superConstructorFound=true;
                        }
                        if(superConstructorFound && currentInstruction.getType() == AbstractInsnNode.LINE){
                            lineAfterInvokeSuperConstructor = currentInstruction;
                            break;
                        }
                    }
                }

                String instrumentLines = "";
                String covDetails = "";

                Iterator<AbstractInsnNode> instructionsNode = methodNode.instructions.iterator();
                boolean superConstructorIgnored=!superConstructorFound;

                while (instructionsNode.hasNext()) {
                    AbstractInsnNode currentInstruction = instructionsNode.next();
                    if (currentInstruction.getType() == AbstractInsnNode.LINE) {

                        // we couldn't instrument any code before the super() statement in constructors.
                        //ignore super-constructor call if exists
                        if(!superConstructorIgnored) {
                            if (!currentInstruction.equals(lineAfterInvokeSuperConstructor))
                                continue;
                            superConstructorIgnored=true;
                        }

                        //get current line of source code
                        Object lineNumber = null;
                        try {
                            lineNumber = FieldUtils.readField(currentInstruction, "line", true);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            continue;
                        }


                        //string coverage details
                        String splitter = ",";
                        covDetails = classNode.name + splitter + lineNumber + splitter + methodNode.name + splitter + methodNode.desc;

                        //record all coverable lines
                        Coverage.recordLineNumber(covDetails);

                        //instrument code
                        InsnList instrumentCode = new InsnList();
                        instrumentCode.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
                        instrumentCode.add(new org.objectweb.asm.tree.LdcInsnNode(covDetails));
                        instrumentCode.add(new org.objectweb.asm.tree.MethodInsnNode(Opcodes.INVOKESTATIC, INSTRUMENT_CLASS, INSTRUMENT_VISIT_METHOD, INSTRUMENT_VISIT_METHOD_SIG));

                        // instrument code
                        methodNode.instructions.insert(currentInstruction, instrumentCode);

                        //make strings of all lines for logging purpose
                        instrumentLines = instrumentLines + lineNumber + ",";

                        //update max stack of method
                        methodNode.visitMaxs(Math.max(methodNode.maxStack, 2), methodNode.maxLocals);
                    }
                }

                //log the method and lines that are instrumented
//                if (!"".equals(instrumentLines))
//                    log.info(covDetails + "," + instrumentLines);
            }

            log.info("class " + className + " instrumented");

            //We are done now. so dump the instrumented bytes of the class
            ClassWriter cw = new ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(cw);
            return cw.toByteArray();
        }

        return classfileBytes;
    }

}
