package org.spideruci.asserttracker;

import java.time.Instant;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AssertVisitor extends MethodVisitor {

    final public String methodName;
    public Boolean isTestAnnotationPresent;

    public AssertVisitor(int api, String methodName, MethodVisitor methodWriter) {
        super(api, methodWriter);
        this.methodName = methodName;
        this.isTestAnnotationPresent= false;
    }

    public AssertVisitor(int api, String methodName) {
        super(api);
        this.methodName = methodName;
        this.isTestAnnotationPresent= false;
    }

    @Override

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

        // 1. check method for annotation @ImportantLog

        // 2. if annotation present, then get important method param indexes
        //@org.junit.jupiter.api.Test()
        if ("Lorg/junit/jupiter/api/Test;".equals(desc)){
            isTestAnnotationPresent = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override

    public void visitCode() {

        // 3. if annotation present, add logging to beginning of the method

        if (this.isTestAnnotationPresent) {

            this.mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
            this.mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(StringBuilder.class)); //  "java/lang/StringBuilder"
            this.mv.visitInsn(Opcodes.DUP);
            this.mv.visitLdcInsn("recognize an @Test Annotation");
            this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(StringBuilder.class), "<init>", "(Ljava/lang/String;)V", false);

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "toString", "()Ljava/lang/String;", false);

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        }

    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {


        Boolean isAssert = name.toLowerCase().startsWith("assert");

        if (isAssert) {
            String message = "\t + Compiled at " + Instant.now().toEpochMilli() + " start:" + this.methodName + " " + name + " ";
            System.out.println(message);
            insertPrintingProbe(message);
        }
//        super.visitAnnotation("@org.junit.jupiter.api.Test()",True);

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        if (isAssert) {
            String message = "\t end:" + this.methodName + " " + name;
            System.out.println(message);
            insertPrintingProbe(message);
        }


//        return new TestVisitor(this.api);
    }

    protected void insertPrintingProbe(String str) {
        if (this.mv == null) {
            return;
        }

        // System.err
        this.mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");

        // 22: new           #51                 // class java/lang/StringBuilder
        // 25: dup
        // 26: ldc           #53                 // String \t +
        // 28: invokespecial #55                 // Method java/lang/StringBuilder."<init>":(Ljava/lang/String;)V
        // 31: invokestatic  #58                 // Method java/time/Instant.now:()Ljava/time/Instant;
        // 34: invokevirtual #64                 // Method java/time/Instant.toEpochMilli:()J
        // 37: invokevirtual #68                 // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
        // 40: ldc           #72                 // String  start:
        // 42: invokevirtual #74                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        // 61: invokevirtual #79                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;

        // new StringBuilder("\t")
        this.mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(StringBuilder.class)); //  "java/lang/StringBuilder"
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitLdcInsn("\t");
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(StringBuilder.class), "<init>", "(Ljava/lang/String;)V", false);

        // Instant.now().toEpochMilli()
        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Instant.class), "now", "()Ljava/time/Instant;", false);
        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Instant.class), "toEpochMilli", "()J", false);

        // .append(Instant.now().toEpochMilli())
        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(J)Ljava/lang/StringBuilder;", false);

        // .append("//")
        this.mv.visitLdcInsn("//");
        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        // System.nanoTime()
        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "nanoTime", "()J", false);
        
        // .append(System.nanoTime())
        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(J)Ljava/lang/StringBuilder;", false);

        // .append(`str`)
        this.mv.visitLdcInsn(str);
        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

//        //        27: aload_0
//        //        28: invokevirtual #31                 // Method java/lang/Object.getClass:()Ljava/lang/Class;
//        //        31: invokevirtual #35                 // Method java/lang/Class.getName:()Ljava/lang/String;
//        //        .append(this.getClass().getName())
//        this.mv.visitInsn(42);
//        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
//        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
//        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        // .toString()
        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "toString", "()Ljava/lang/String;", false);
        
        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }
    
}
