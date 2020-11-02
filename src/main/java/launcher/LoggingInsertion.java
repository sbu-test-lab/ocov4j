package launcher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.analysis.ControlFlow;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class LoggingInsertion {

        public static void main(String[] args) throws Exception{
            InputStream in=LoggingInsertion.class.getResourceAsStream("TestClass.class");

            ClassReader cr=new ClassReader(in);
            ClassNode classNode=new ClassNode();
            cr.accept(classNode, 0);

            for(MethodNode methodNode:classNode.methods){
                System.out.println(methodNode.name+"  "+methodNode.desc);


                Iterator<AbstractInsnNode> instructionsNode=methodNode.instructions.iterator();
                while(instructionsNode.hasNext()) {
                    AbstractInsnNode currentInstruction = instructionsNode.next();
                    if(currentInstruction.getType()==AbstractInsnNode.LINE){

                        //get current line of source cod
                        Object lineNumber = FieldUtils.readField(currentInstruction, "line", true);
                        
                        //instrument code
                        InsnList instrumentCode=new InsnList();
                        instrumentCode.add(new VarInsnNode(Opcodes.ALOAD,0));
                        instrumentCode.add(new LdcInsnNode(methodNode.name+" - line number: "+lineNumber));
                        instrumentCode.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "launcher/Cov", "log", "(Ljava/lang/Object;Ljava/lang/String;)V"));

                        methodNode.instructions.insert(currentInstruction, instrumentCode);
                        System.out.println("instrument is add");
                    }
                }

                //We are done now. so dump the class
            ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
            classNode.accept(cw);
            DataOutputStream dout=new DataOutputStream(new FileOutputStream(new File("LoggingTest.class")));
            dout.write(cw.toByteArray());
            dout.flush();
            dout.close();

            }
            //Let's move through all the methods

//            for(MethodNode classNode){
//                System.out.println(methodNode.name+"  "+methodNode.desc);
//
//                    //Lets insert the begin logger
//                    InsnList beginList=new InsnList();
//                    beginList.add(new LdcInsnNode(methodNode.name));
//                    beginList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/geekyarticles/asm/Logger", "logMethodStart", "(Ljava/lang/String;)V"));
//
//                    Iterator<AbstractInsnNode> insnNodes=methodNode.instructions.iterator();
//                    while(insnNodes.hasNext()){
//                        System.out.println(insnNodes.next().getOpcode());
//                    }
//
//                    methodNode.instructions.insert(beginList);
//                    System.out.println(methodNode.instructions);
//
//                    //A method can have multiple places for return
//                    //All of them must be handled.
//                    insnNodes=methodNode.instructions.iterator();
//                    while(insnNodes.hasNext()){
//                        AbstractInsnNode insn=insnNodes.next();
//                        System.out.println(insn.getOpcode());
//
//                        if(insn.getOpcode()==Opcodes.IRETURN
//                                ||insn.getOpcode()==Opcodes.RETURN
//                                ||insn.getOpcode()==Opcodes.ARETURN
//                                ||insn.getOpcode()==Opcodes.LRETURN
//                                ||insn.getOpcode()==Opcodes.DRETURN){
//                            InsnList endList=new InsnList();
//                            endList.add(new LdcInsnNode(methodNode.name));
//                            endList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/geekyarticles/asm/Logger", "logMethodReturn", "(Ljava/lang/String;)V"));
//                            methodNode.instructions.insertBefore(insn, endList);
//                        }
//
//
//                }
//            }
//
//            //We are done now. so dump the class
//            ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
//            classNode.accept(cw);
//
//
////            File outDir=new File("loggingClass.class");
////            outDir.mkdirs();
//            DataOutputStream dout=new DataOutputStream(new FileOutputStream(new File("LoggingTest.class")));
//            dout.write(cw.toByteArray());
//            dout.flush();
//            dout.close();

        }

        ControlFlow.Block[] getBasicBlocks(String clazz, String methodNma) throws NotFoundException, BadBytecode {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get("launcher.TestClass");
            CtMethod m = cc.getDeclaredMethod("add");
            return new ControlFlow(m).basicBlocks();
        }

    }
