package launcher;


import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.analysis.ControlFlow;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, NotFoundException, BadBytecode, IOException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get("launcher.TestClass");
        CtMethod m = cc.getDeclaredMethod("add");

        List a=new ArrayList();
        a.add(new Object());
        a.stream();
        //add a variable of converge to method
        try {
            m.addLocalVariable("mse", ClassPool.getDefault().get("launcher.Cov"));
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        int mseCSindex = m.getMethodInfo().getCodeAttribute().getMaxLocals() - 1;

        //finde index of converge.log function
        ConstPool cp = m.getMethodInfo().getConstPool();
        int virtFunIndex = -1;
        boolean found = false;
        while (found == false) {
            ++virtFunIndex;
            try {
                int id = cp.isMember("launcher.Cov", "setBlockIndex", virtFunIndex);
                if (id != 0) {
                    found = true;
                }
            } catch (NullPointerException e) {
            } catch (ClassCastException e) {
            }
        }


        //instrument the beginning of each Block:
        int len = new ControlFlow(m).basicBlocks().length;
        for (int i = 0; i < len; ++i) {
            ControlFlow.Block thisbb = new ControlFlow(m).basicBlocks()[i]; //we have to re-evaluate the control flow every time we add new code
            CodeIterator itr = m.getMethodInfo().getCodeAttribute().iterator();

            int pos = thisbb.position();
            byte[] newCode = new byte[]{Bytecode.ALOAD, //loads the mse class
                    (byte) mseCSindex, //my mse local variable
                    Bytecode.ICONST_0, //the input to the virtual function
                    (byte) Bytecode.INVOKEVIRTUAL, //execute the virtual function
                    (byte) (virtFunIndex >> 8), //virtual function's address
                    (byte) (virtFunIndex & 0xFF)};

            int n = itr.insertAt(pos, newCode);
        }


        //dump new class to file
        byte[] bytes = cc.toBytecode();
        FileOutputStream stream = new FileOutputStream("InstTestClass.class");
        stream.write(bytes);
        stream.close();

        //        ClassPool pool = ClassPool.getDefault();
//        CtClass cc = null;
//
//        cc = pool.get("launcher.TestClass");
//
//        CtMethod m = cc.getDeclaredMethod("add");
//
//
///*
//        m.insertAt(10,"{ System.out.println(\"X=\"+x); }");
//        Class c = cc.toClass();
//        TestClass h = (TestClass) c.newInstance();
//        h.add(0);*/
//        //System.out.println(cc.toString());
//
//        MethodInfo info = m.getMethodInfo();
//        //System.out.println(info);
//
//        MethodInfo info2 = m.getMethodInfo2();
//        //System.out.println(info2);
//
//        ControlFlow cf = new ControlFlow(m);
//        ControlFlow.Block[] blocks = cf.basicBlocks();
//        //System.out.println(blocks);
//
////        for(int i=0; i<blocks.length;i++){
////            ControlFlow.Block newEvalutedBlock = new ControlFlow(m).basicBlocks()[i]; //we have to re-evaluate the control flow every time we add new code
////
////            int lineNumber = m.getMethodInfo().getLineNumber(newEvalutedBlock.position());
////            m.insertAt(lineNumber , "System.out.println(\"visit block id="+blocks[i].index()+"\");");
////        }
//        for (int i = 0; i < blocks.length; i++) {
//            ControlFlow.Block thisbb = new ControlFlow(m).basicBlocks()[i]; //we have to re-evaluate the control flow every time we add new code
//            CodeIterator itr = m.getMethodInfo().getCodeAttribute().iterator();
//
//            int pos = thisbb.position();
//            byte[] newCode = new byte[]{Bytecode.NOP};
//
//            int n = itr.insertAt(pos, newCode);
//        }
//
///*
//        int position = blocks[1].position();
//        int lineNumber = m.getMethodInfo().getLineNumber(position);
//        m.insertAt(lineNumber , "System.out.println(\"visit block id="+blocks[1].index()+"\");");
//
//*/
//
//        Class c = cc.toClass();
//
//        //dump new class to file
//        byte[] bytes = cc.toBytecode();
//        FileOutputStream stream = new FileOutputStream("InstrumentClass4.class");
//        stream.write(bytes);
//        stream.close();
//
//
//        TestClass h = (TestClass) c.newInstance();
//        h.add(0);

    }


    /**
     * Method Visitor that inserts code right before its return instruction(s),
     * using the onMethodExit(int opcode) method of the AdviceAdapter class,
     * from ASM(.ow2.org).
     * @author vijay
     *
     */
//    class MethodReturnAdapter extends AdviceAdapter {
//        public MethodReturnAdapter(
//                int api,
//                String owner,
//                int access,
//                String name,
//                String desc,
//                MethodVisitor mv) {
//            super(Opcodes.ASM4, mv, access, name, desc);
//        }
//
//        public MethodReturnAdapter(
//                MethodVisitor mv,
//                int access,
//                String name,
//                String desc) {
//            super(Opcodes.ASM4, mv, access, name, desc);
//        }
//
//        @Override
//        protected void onMethodExit(int opcode) {
//            if(opcode != Opcodes.ATHROW) {
//                mv.visitVarInsn(Opcodes.ALOAD, 42);
//                // and/or any other visit instructions.
//            }
//        }
//    }
}
