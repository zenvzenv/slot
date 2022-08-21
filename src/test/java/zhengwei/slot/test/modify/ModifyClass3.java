package zhengwei.slot.test.modify;


import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/20 11:39
 */
public class ModifyClass3 extends ClassVisitor {
    private String owner;
    private boolean isInterface;

    public ModifyClass3(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        owner = name;
        isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println(name);
        if (name.equals("add")) {
            // 复制方法
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            cw.visitMethod(access, "add", "(JII)V", signature, exceptions);

            cw.visitEnd();
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    static final class OriginalMethod extends MethodVisitor {
        protected OriginalMethod(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitParameter(String name, int access) {
            mv.visitParameter(name, access);
            super.visitParameter(name, access);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            mv.visitAnnotationDefault();
            return super.visitAnnotationDefault();
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            mv.visitAnnotation(descriptor, visible);
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            mv.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
            return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
            mv.visitAnnotableParameterCount(parameterCount, visible);
            super.visitAnnotableParameterCount(parameterCount, visible);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
            mv.visitParameterAnnotation(parameter, descriptor, visible);
            return super.visitParameterAnnotation(parameter, descriptor, visible);
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            mv.visitAttribute(attribute);
            super.visitAttribute(attribute);
        }

        @Override
        public void visitCode() {
            mv.visitCode();
            super.visitCode();
        }

        @Override
        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            mv.visitFrame(type, numLocal, local, numStack, stack);
            super.visitFrame(type, numLocal, local, numStack, stack);
        }

        @Override
        public void visitInsn(int opcode) {
            mv.visitInsn(opcode);
            super.visitInsn(opcode);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            mv.visitIntInsn(opcode, operand);
            super.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitVarInsn(int opcode, int varIndex) {
            mv.visitVarInsn(opcode, varIndex);
            super.visitVarInsn(opcode, varIndex);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            mv.visitTypeInsn(opcode, type);
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            mv.visitFieldInsn(opcode, owner, name, descriptor);
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
            mv.visitMethodInsn(opcode, owner, name, descriptor);
            super.visitMethodInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            mv.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            mv.visitJumpInsn(opcode, label);
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            mv.visitLabel(label);
            super.visitLabel(label);
        }

        @Override
        public void visitLdcInsn(Object value) {
            mv.visitLdcInsn(value);
            super.visitLdcInsn(value);
        }

        @Override
        public void visitIincInsn(int varIndex, int increment) {
            mv.visitIincInsn(varIndex, increment);
            super.visitIincInsn(varIndex, increment);
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            mv.visitTableSwitchInsn(min, max, dflt, labels);
            super.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            mv.visitLookupSwitchInsn(dflt, keys, labels);
            super.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            mv.visitMultiANewArrayInsn(descriptor, numDimensions);
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            mv.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
            return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            mv.visitTryCatchBlock(start, end, handler, type);
            super.visitTryCatchBlock(start, end, handler, type);
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            mv.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
            return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
            mv.visitLocalVariable(name, descriptor, signature, start, end, index);
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
            mv.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
            return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            mv.visitLineNumber(line, start);
            super.visitLineNumber(line, start);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitMaxs(maxStack, maxLocals);
            super.visitMaxs(maxStack, maxLocals);
        }

        @Override
        public void visitEnd() {
            mv.visitEnd();
            super.visitEnd();
        }
    }
}
