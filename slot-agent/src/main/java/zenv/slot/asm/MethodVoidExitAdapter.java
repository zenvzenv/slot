package zenv.slot.asm;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import static zenv.slot.conf.ASMConf.*;
import static zenv.slot.utils.ASMUtils.buildMethodDesc;

/**
 * 无返回值方法退出/抛出异常时对埋点信息处理
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/24 8:59
 */
@Deprecated
public class MethodVoidExitAdapter extends MethodVisitor {
    private final int spanSlotIndexOffset = SPAN_TYPE.getSize();
    private int spanSlotIndex;
    private final int timerSlotIndexOffset = Type.LONG_TYPE.getSize();
    private int timerSlotIndex;
    private final int successSlotIndexOffset = Type.BOOLEAN_TYPE.getSize();
    private int successSlotIndex;
    private final int traceIdSlotIndexOffset = STRING_TYPE.getSize();
    private int traceIdSlotIndex;
    private final int methodParamsValueSlotIndexOffset = LIST_TYPE.getSize();
    private int methodParamsValueSlotIndex;
    private String methodName;
    private final String methodDesc;
    private final boolean isStatic;

    public MethodVoidExitAdapter(int i, MethodVisitor methodVisitor, int access, String name, String desc) {
        super(i, methodVisitor);
        this.methodName = name;
        this.methodDesc = desc;
        isStatic = (access & ACC_STATIC) != 0;
    }

    /*@Override
    protected void onMethodEnter() {
        final int alreadySlotIndex = getAlreadySlotIndex();
        spanSlotIndex = alreadySlotIndex + spanSlotIndexOffset;
        timerSlotIndex = spanSlotIndex + timerSlotIndexOffset;
        successSlotIndex = timerSlotIndex + successSlotIndexOffset;
        traceIdSlotIndex = successSlotIndex + traceIdSlotIndexOffset;
        methodParamsValueSlotIndex = traceIdSlotIndex + methodParamsValueSlotIndexOffset;
        timerSlotIndex = timerSlotIndex - 1;
        super.onMethodEnter();
    }*/

    @Override
    public void visitCode() {
        final int alreadySlotIndex = getAlreadySlotIndex();
        spanSlotIndex = alreadySlotIndex + spanSlotIndexOffset;
        timerSlotIndex = spanSlotIndex + timerSlotIndexOffset;
        successSlotIndex = timerSlotIndex + successSlotIndexOffset;
        traceIdSlotIndex = successSlotIndex + traceIdSlotIndexOffset;
        methodParamsValueSlotIndex = traceIdSlotIndex + methodParamsValueSlotIndexOffset;
        timerSlotIndex = timerSlotIndex - 1;
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        // 方法无返回值且正常执行结束
        if (opcode == RETURN) {
            // 计算最终耗时 timer
            super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", buildMethodDesc(Type.LONG_TYPE), false);
            super.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitInsn(LSUB);
            super.visitVarInsn(LSTORE, timerSlotIndex);

            // 将耗时设置到埋点信息对象中
            super.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setDuration", buildMethodDesc(Type.VOID_TYPE, Type.LONG_TYPE), false);

            // 无异常返回设置方法执行成功
            super.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitVarInsn(ILOAD, successSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setSuccess", buildMethodDesc(Type.VOID_TYPE, Type.BOOLEAN_TYPE), false);

            // 设置方法结束时间
            super.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", buildMethodDesc(LOCAL_DATE_TIME_TYPE), false);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setEndDate", buildMethodDesc(Type.VOID_TYPE, LOCAL_DATE_TIME_TYPE), false);

            // 输出埋点信息
            super.visitFieldInsn(GETSTATIC, SYSTEM_TYPE.getInternalName(), "out", "Ljava/io/PrintStream;");
            super.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", buildMethodDesc(Type.VOID_TYPE, OBJECT_TYPE), false);
        }/* else if (opcode == ATHROW) { // 方法抛出异常
            // 在 label 中已经设置 success 为 false，直接设置值即可
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitVarInsn(ILOAD, successSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setSuccess", buildMethodDesc(Type.VOID_TYPE, Type.BOOLEAN_TYPE), false);

            super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", buildMethodDesc(Type.LONG_TYPE), false);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, timerSlotIndex);
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setDuration", buildMethodDesc(Type.VOID_TYPE, Type.LONG_TYPE), false);

            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", buildMethodDesc(LOCAL_DATE_TIME_TYPE), false);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setEndDate", buildMethodDesc(Type.VOID_TYPE, LOCAL_DATE_TIME_TYPE), false);


            super.visitFieldInsn(GETSTATIC, SYSTEM_TYPE.getInternalName(), "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", buildMethodDesc(Type.VOID_TYPE, OBJECT_TYPE), false);
        }*/
        super.visitInsn(opcode);
    }

    /*@Override
    protected void onMethodExit(int opcode) {
        // 方法无返回值且正常执行结束
        if (opcode == RETURN) {
            // 计算最终耗时 timer
            super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", buildMethodDesc(Type.LONG_TYPE), false);
            super.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitInsn(LSUB);
            super.visitVarInsn(LSTORE, timerSlotIndex);

            // 将耗时设置到埋点信息对象中
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setDuration", buildMethodDesc(Type.VOID_TYPE, Type.LONG_TYPE), false);

            // 无异常返回设置方法执行成功
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitVarInsn(ILOAD, successSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setSuccess", buildMethodDesc(Type.VOID_TYPE, Type.BOOLEAN_TYPE), false);

            // 设置方法结束时间
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", buildMethodDesc(LOCAL_DATE_TIME_TYPE), false);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setEndDate", buildMethodDesc(Type.VOID_TYPE, LOCAL_DATE_TIME_TYPE), false);

            // 输出埋点信息
            super.visitFieldInsn(GETSTATIC, SYSTEM_TYPE.getInternalName(), "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", buildMethodDesc(Type.VOID_TYPE, OBJECT_TYPE), false);
        }*//* else if (opcode == ATHROW) { // 方法抛出异常
            // 在 label 中已经设置 success 为 false，直接设置值即可
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitVarInsn(ILOAD, successSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setSuccess", buildMethodDesc(Type.VOID_TYPE, Type.BOOLEAN_TYPE), false);

            super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", buildMethodDesc(Type.LONG_TYPE), false);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, timerSlotIndex);
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setDuration", buildMethodDesc(Type.VOID_TYPE, Type.LONG_TYPE), false);

            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", buildMethodDesc(LOCAL_DATE_TIME_TYPE), false);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setEndDate", buildMethodDesc(Type.VOID_TYPE, LOCAL_DATE_TIME_TYPE), false);


            super.visitFieldInsn(GETSTATIC, SYSTEM_TYPE.getInternalName(), "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", buildMethodDesc(Type.VOID_TYPE, OBJECT_TYPE), false);
        }*//*

        super.onMethodExit(opcode);
    }*/

    @Override
    public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i) {
        super.visitLocalVariable(s, s1, s2, label, label1, i);
    }

    /**
     * 获得当前已经分配的本地变量表的 slot index
     */
    private int getAlreadySlotIndex() {
        final Type type = Type.getType(methodDesc);
        final Type[] argumentTypes = type.getArgumentTypes();
        int localIndex = isStatic ? 0 : 1;
        for (Type argumentType : argumentTypes) {
            localIndex += argumentType.getSize();
        }
        return localIndex - 1;
    }
}
