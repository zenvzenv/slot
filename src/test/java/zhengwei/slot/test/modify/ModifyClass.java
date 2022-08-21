package zhengwei.slot.test.modify;


import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/20 11:39
 */
public class ModifyClass extends ClassVisitor {
    private String owner;
    private boolean isInterface;

    public ModifyClass(int api, ClassVisitor classVisitor) {
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
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (!isInterface && null != mv && !"<init>".equals(name) && !"<clinit>".equals(name)) {
            final boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            final boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod && !isNativeMethod) {
                mv = new MethodTimerAdapter(api, access, name, descriptor, mv);
            }
        }
        return mv;
    }

    static final class MethodTimerAdapter extends LocalVariablesSorter {
        private final String methodName;
        private final String methodDesc;
        private int slotOutputSlotIndex;
        private int timerSlotIndex;

        protected MethodTimerAdapter(int api, int access, String name, String descriptor, MethodVisitor methodVisitor) {
            super(api, access, descriptor, methodVisitor);
            this.methodName = name;
            this.methodDesc = descriptor;
        }

        @Override
        public void visitCode() {
            // 生成埋点信息对象
            {
                this.slotOutputSlotIndex = newLocal(Type.getObjectType("Lzhengwei/slot/entity/SlotOutput;"));
                mv.visitTypeInsn(NEW, "zhengwei/slot/entity/SlotOutput");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "zhengwei/slot/entity/SlotOutput", "<init>", "()V", false);
                mv.visitVarInsn(ASTORE, slotOutputSlotIndex);
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitMethodInsn(INVOKESTATIC, "java/util/UUID", "randomUUID", "()Ljava/util/UUID;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/UUID", "toString", "()Ljava/lang/String;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "zhengwei/slot/entity/SlotOutput", "setSpanId", "(Ljava/lang/String;)V", false);
            }
            // 生成 timer 字段并初始化
            {
                this.timerSlotIndex = newLocal(Type.LONG_TYPE);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitVarInsn(LSTORE, timerSlotIndex);
            }
            super.visitCode();
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitVarInsn(LLOAD, timerSlotIndex);
                mv.visitInsn(LSUB);
                mv.visitMethodInsn(INVOKEVIRTUAL, "zhengwei/slot/entity/SlotOutput", "setDuration", "(J)V", false);
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitInsn(ICONST_1);
                mv.visitMethodInsn(INVOKEVIRTUAL, "zhengwei/slot/entity/SlotOutput", "setSuccess", "(Z)V", false);
                mv.visitMethodInsn(GETSTATIC, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
            } else if (opcode == ATHROW) {
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitVarInsn(LLOAD, timerSlotIndex);
                mv.visitInsn(LSUB);
                mv.visitMethodInsn(INVOKEVIRTUAL, "zhengwei/slot/entity/SlotOutput", "setDuration", "(J)V", false);
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitInsn(ICONST_0);
                mv.visitMethodInsn(INVOKEVIRTUAL, "zhengwei/slot/entity/SlotOutput", "setSuccess", "(Z)V", false);
                mv.visitMethodInsn(GETSTATIC, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
            }
            /* else if ((opcode >= IRETURN && opcode <= RETURN)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitVarInsn(LLOAD, timerSlotIndex);
                mv.visitInsn(LSUB);
                mv.visitMethodInsn(INVOKEVIRTUAL, "zhengwei/slot/entity/SlotOutput", "setDuration", "(J)V", false);
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitVarInsn(ALOAD, slotOutputSlotIndex);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
                mv.visitVarInsn(ILOAD, 6);
            }*/
            super.visitInsn(opcode);
        }
    }
}
