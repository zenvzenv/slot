package zenv.slot.asm;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import zenv.slot.utils.ASMUtils;

import java.util.HashSet;
import java.util.Set;

import static zenv.slot.conf.ASMConf.*;
import static zenv.slot.utils.ASMUtils.buildMethodDesc;

/**
 * 在方法的开头初始化埋点信息
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/2 10:02
 */
@Deprecated
public class SlotMethodAroundAdapter extends AdviceAdapter {
    private final String className;
    private final String methodName;
    private final String methodDesc;
    /**
     * 埋点信息在本地变量表的索引位
     */
    private int spanSlotIndex;

    /**
     * 方法耗时在本地变量表索引位
     */
    private int timerSlotIndex;

    /**
     * 方法是否执行成功局部变量在本地变量表的索引位
     */
    private int successSlotIndex;

    /**
     * 埋点信息事件索引位
     */
    private int spanOutputEventSlotIndex;

    /**
     * 是否是静态方法
     */
    private final int isStatic;

    /**
     * 方法中所有异常 catch 开始标记
     */
    private final Set<Label> exceptionLabels = new HashSet<>();

    public SlotMethodAroundAdapter(int api,
                                   MethodVisitor mv,
                                   int access,
                                   String name,
                                   String desc,
                                   String className) {
        super(api, mv, access, name, desc);
        this.className = className;
        this.methodName = name;
        this.methodDesc = desc;
        isStatic = ((access & ACC_STATIC) != 0) ? 0 : 1;
    }

    @Override
    protected void onMethodEnter() {
        // 新增 span 本地变量
        spanSlotIndex = newLocal(SPAN_TYPE);

        // 新增 timer 字段
        timerSlotIndex = newLocal(Type.LONG_TYPE);

        // 新增 success 字段
        successSlotIndex = newLocal(Type.BOOLEAN_TYPE);

        // 新增 traceId 字段
        final int traceIdSlotIndex = newLocal(STRING_TYPE);

        // 方法参数实际值列表
        final int methodParamsValueSlotIndex = newLocal(LIST_TYPE);

        spanOutputEventSlotIndex = newLocal(SPAN_OUTPUT_EVENT);

        // 获取当前方法 span
        super.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "getCurrentSpan", buildMethodDesc(SPAN_TYPE), false);
        mv.visitVarInsn(ASTORE, spanSlotIndex);
        super.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ALOAD, spanSlotIndex);

        Label ifLabel = new Label();
        super.visitJumpInsn(IF_ACMPNE, ifLabel);
        super.visitMethodInsn(INVOKESTATIC, ASM_UTILS_TYPE.getInternalName(), "genId", buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, traceIdSlotIndex);
        mv.visitVarInsn(ALOAD, traceIdSlotIndex);
        super.visitMethodInsn(INVOKESTATIC, TRACE_CONTEXT_TYPE.getInternalName(), "setTraceId", buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), false);
        super.visitLabel(ifLabel);

        // 创建当前方法 span
        super.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "createEntrySpan", buildMethodDesc(SPAN_TYPE), false);
        mv.visitVarInsn(ASTORE, spanSlotIndex);

        // setClassName
        mv.visitVarInsn(ALOAD, spanSlotIndex);
        super.visitLdcInsn(className);
        mv.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setClassName", buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), false);

        // setMethod
        mv.visitVarInsn(ALOAD, spanSlotIndex);
        super.visitLdcInsn(methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setMethodName", buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), false);

        // setMethodDesc
        mv.visitVarInsn(ALOAD, spanSlotIndex);
        super.visitLdcInsn(methodDesc);
        mv.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setMethodDesc", buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), false);

        // setClassMethod
        mv.visitVarInsn(ALOAD, spanSlotIndex);
        super.visitLdcInsn(className + "#" + methodName);
        super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setClassMethod", buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), false);

        // 初始化 success，默认为 true - 执行成功
        super.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, successSlotIndex);

        // 初始化 timer 本地变量，获取当前时间毫秒数
        super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", buildMethodDesc(Type.LONG_TYPE), false);
        mv.visitVarInsn(LSTORE, timerSlotIndex);

        mv.visitVarInsn(ALOAD, spanSlotIndex);
        super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "getMethodParamsValue", "()Ljava/util/List;", false);
        mv.visitVarInsn(ASTORE, methodParamsValueSlotIndex);

        final Type[] types = Type.getMethodType(methodDesc).getArgumentTypes();
        if (types.length > 0) {
            int slotIndex = isStatic;
            for (Type t : types) {
                int sort = t.getSort();
                int size = t.getSize();
                final int opcode = t.getOpcode(ILOAD);
                mv.visitVarInsn(ALOAD, methodParamsValueSlotIndex);
                mv.visitVarInsn(opcode, slotIndex);
                if (sort == Type.INT) {
                    super.visitMethodInsn(INVOKESTATIC, INT_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(INT_OBJ_TYPE, Type.INT_TYPE), false);
                } else if (sort == Type.CHAR) {
                    super.visitMethodInsn(INVOKESTATIC, CHAR_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(CHAR_OBJ_TYPE, Type.CHAR_TYPE), false);
                } else if (sort == Type.LONG) {
                    super.visitMethodInsn(INVOKESTATIC, LONG_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(LONG_OBJ_TYPE, Type.LONG_TYPE), false);
                } else if (sort == Type.SHORT) {
                    super.visitMethodInsn(INVOKESTATIC, SHORT_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(SHORT_OBJ_TYPE, Type.SHORT_TYPE), false);
                } else if (sort == Type.BYTE) {
                    super.visitMethodInsn(INVOKESTATIC, BYTE_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(BYTE_OBJ_TYPE, Type.BYTE_TYPE), false);
                } else if (sort == Type.BOOLEAN) {
                    super.visitMethodInsn(INVOKEINTERFACE, BOOLEAN_OBJ_TYPE.getInternalName(), "add", buildMethodDesc(BOOLEAN_OBJ_TYPE, Type.BOOLEAN_TYPE), true);
                } else if (sort == Type.FLOAT) {
                    super.visitMethodInsn(INVOKESTATIC, FLOAT_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(FLOAT_OBJ_TYPE, Type.FLOAT_TYPE), false);
                } else if (sort == Type.DOUBLE) {
                    super.visitMethodInsn(INVOKESTATIC, DOUBLE_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(DOUBLE_OBJ_TYPE, Type.DOUBLE_TYPE), false);
                }
                super.visitMethodInsn(INVOKEINTERFACE, LIST_TYPE.getInternalName(), "add", buildMethodDesc(Type.BOOLEAN_TYPE, OBJECT_TYPE), true);
                super.visitInsn(POP);
                slotIndex += size;
            }
        }

        super.onMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        // 方法无返回值且正常执行结束
        if (IRETURN <= opcode && opcode <= RETURN) {
            // 计算最终耗时 timer
            super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", buildMethodDesc(Type.LONG_TYPE), false);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            super.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, timerSlotIndex);

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

            // 弹栈
            mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "exitSpan", buildMethodDesc(Type.VOID_TYPE), false);

            // 输出埋点信息
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKESTATIC, "zenv/slot/databus/event/CacheSpanOutputEvent", "of", "(Lzenv/slot/entity/Span;)Lzenv/slot/databus/IDataEvent;", false);
            mv.visitVarInsn(ASTORE, spanOutputEventSlotIndex);
            super.visitFieldInsn(GETSTATIC, "zenv/slot/conf/Constant", "EVENT_BLOCKING_QUEUE", "Ljava/util/concurrent/BlockingQueue;");
            mv.visitVarInsn(ALOAD, spanOutputEventSlotIndex);
            super.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/BlockingQueue", "put", "(Ljava/lang/Object;)V", true);

            /*super.visitFieldInsn(GETSTATIC, SYSTEM_TYPE.getInternalName(), "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", buildMethodDesc(Type.VOID_TYPE, OBJECT_TYPE), false);*/
        } else if (opcode == ATHROW) { // 方法抛出异常
            // 在 label 中已经设置 success 为 false，直接设置值即可
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitInsn(ICONST_0);
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


            // 弹栈
            mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "exitSpan", buildMethodDesc(Type.VOID_TYPE), false);

            // 输出埋点信息
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKESTATIC, "zenv/slot/databus/event/CacheSpanOutputEvent", "of", "(Lzenv/slot/entity/Span;)Lzenv/slot/databus/IDataEvent;", false);
            mv.visitVarInsn(ASTORE, spanOutputEventSlotIndex);
            super.visitFieldInsn(GETSTATIC, "zenv/slot/conf/Constant", "EVENT_BLOCKING_QUEUE", "Ljava/util/concurrent/BlockingQueue;");
            mv.visitVarInsn(ALOAD, spanOutputEventSlotIndex);
            super.visitMethodInsn(INVOKEINTERFACE, "java/util/concurrent/BlockingQueue", "put", "(Ljava/lang/Object;)V", true);


            /*super.visitFieldInsn(GETSTATIC, SYSTEM_TYPE.getInternalName(), "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", buildMethodDesc(Type.VOID_TYPE, OBJECT_TYPE), false);*/
        }

        super.onMethodExit(opcode);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // 获取 catch 代码块 Label
        exceptionLabels.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 2, maxLocals);
    }

    /**
     * 如果方法内部进行了 catch 则认为方法执行成功，只记录出现的异常信息，如果方法将异常抛出则认为方法执行失败
     */
    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        // 如果是异常处理代码块则将此方法调用标记为失败
        if (exceptionLabels.contains(label)) {
            super.visitInsn(DUP);
            super.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE.getInternalName(), "getClass", "()Ljava/lang/Class;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Class.class), "getName", buildMethodDesc(STRING_TYPE), false);
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setException", buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), false);

            super.visitInsn(DUP);
            super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Throwable.class), "getMessage", buildMethodDesc(STRING_TYPE), false);
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            super.visitInsn(SWAP);
            super.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setExceptionMsg", buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), false);

            /*super.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, successSlotIndex);*/
        }
    }
}
