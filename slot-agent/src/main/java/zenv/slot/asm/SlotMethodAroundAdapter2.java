package zenv.slot.asm;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import zenv.slot.conf.Constant;
import zenv.slot.utils.ASMUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static zenv.slot.conf.ASMConf.*;
import static zenv.slot.utils.ASMUtils.buildMethodDesc;

/**
 * 在方法的开头初始化埋点信息
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/2 10:02
 */
@Deprecated
public class SlotMethodAroundAdapter2 extends AdviceAdapter {
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
     * 父 span id 的本地变量表索引位
     */
    private int parentSpanIdSlotIndex;

    /**
     * span id 本地变量表索引位
     */
    private int spanIdSlotIndex;

    /**
     * 埋点信息事件索引位
     */
    private int spanOutputEventSlotIndex;

    /**
     * 是否是静态方法
     */
    private final int isStatic;

    /**
     * 方法参数个数
     */
    private int methodParamsCount;

    /**
     * 方法中所有异常 catch 开始标记
     */
    private final Set<Label> exceptionLabels = new HashSet<>();

    private int traceIdSlotIndex;
    private int methodParamsValueSlotIndex;

    public SlotMethodAroundAdapter2(int api,
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
        methodParamsCount = Type.getMethodType(desc).getArgumentTypes().length;
    }

    private void initLocalVar() {
        // 新增 span 本地变量
        spanSlotIndex = newLocal(SPAN_TYPE);

        // 新增 timer 字段
        timerSlotIndex = newLocal(Type.LONG_TYPE);

        // 新增父 span id 本地变量
        parentSpanIdSlotIndex = newLocal(STRING_TYPE);

        // 新增 span id 本地变量
        spanIdSlotIndex = newLocal(STRING_TYPE);

        // 新增 traceId 字段
        traceIdSlotIndex = newLocal(STRING_TYPE);

        // 方法参数实际值列表
        methodParamsValueSlotIndex = newLocal(LIST_TYPE);

        spanOutputEventSlotIndex = newLocal(SPAN_OUTPUT_EVENT);
    }

    @Override
    protected void onMethodEnter() {
        initLocalVar();

        // final Span currentSpan = new Span();
        ASMUtils.newObjAtLocalVarTable(SPAN_TYPE, mv, spanSlotIndex);

        // long timer = System.currentTimeMillis();
        mv.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LSTORE, timerSlotIndex);

        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", "Ljava/util/concurrent/atomic/AtomicBoolean;");
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", "()Z", false);

        // if
        final Label slotOutputSwitchLabel = new Label();
        mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);
        mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "getParentSpan", buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, parentSpanIdSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "entrySpan", buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, spanIdSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, TRACE_CONTEXT_TYPE.getInternalName(), "getTraceId", buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, traceIdSlotIndex);

        ASMUtils.setObjPropValueByLocalVar(mv, SPAN_TYPE, STRING_TYPE, spanSlotIndex, traceIdSlotIndex, "setTraceId");
        ASMUtils.setObjPropValueByLocalVar(mv, SPAN_TYPE, STRING_TYPE, spanSlotIndex, spanIdSlotIndex, "setSpanId");
        ASMUtils.setObjPropValueByLocalVar(mv, SPAN_TYPE, STRING_TYPE, spanSlotIndex, parentSpanIdSlotIndex, "setParentId");

        ASMUtils.setConstantStr(mv, SPAN_TYPE, spanSlotIndex, className, "setClassName");
        ASMUtils.setConstantStr(mv, SPAN_TYPE, spanSlotIndex, methodName, "setMethodName");
        ASMUtils.setConstantStr(mv, SPAN_TYPE, spanSlotIndex, methodDesc, "setMethodDesc");
        ASMUtils.setConstantStr(mv, SPAN_TYPE, spanSlotIndex, className + "#" + methodName, "setClassMethod");
        ASMUtils.setConstantStr(mv, SPAN_TYPE, spanSlotIndex, Constant.SERVICE.get(), "setProduct");

        /*if (methodParamsCount > 0) {
            ASMUtils.newArrayListWithInitCapAtLocalVarTable(mv, methodParamsValueSlotIndex, methodParamsCount);
            final Type[] types = Type.getMethodType(methodDesc).getArgumentTypes();
            int slotIndex = isStatic;
            for (Type t : types) {
                int size = t.getSize();
                ASMUtils.addPropToArrayList(mv, t, methodParamsValueSlotIndex, slotIndex);
                slotIndex += size;
            }
            ASMUtils.setObjPropValueByLocalVar(mv, SPAN_TYPE, LIST_TYPE, spanSlotIndex, methodParamsValueSlotIndex, "setMethodParamsValue");
        }*/

        mv.visitLabel(slotOutputSwitchLabel);

        super.onMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        // 方法无返回值且正常执行结束
        if (IRETURN <= opcode && opcode <= RETURN) {
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", "Ljava/util/concurrent/atomic/AtomicBoolean;");
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", "()Z", false);

            final Label slotOutputSwitchLabel = new Label();
            mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitInsn(ICONST_1);
            mv.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setSuccess", buildMethodDesc(Type.VOID_TYPE, Type.BOOLEAN_TYPE), false);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            mv.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, timerSlotIndex);
            ASMUtils.setObjPropValueByLocalVar(mv, SPAN_TYPE, Type.LONG_TYPE, spanSlotIndex, timerSlotIndex, "setDuration");

            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", buildMethodDesc(LOCAL_DATE_TIME_TYPE), false);
            mv.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setEndDate", buildMethodDesc(Type.VOID_TYPE, LOCAL_DATE_TIME_TYPE), false);
            mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "exitSpan", "()V", false);

            publishEvent();

            mv.visitLabel(slotOutputSwitchLabel);
        } else if (opcode == ATHROW) { // 方法抛出异常
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", "Ljava/util/concurrent/atomic/AtomicBoolean;");
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", "()Z", false);
            final Label slotOutputSwitchLabel = new Label();
            mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);
            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitInsn(ICONST_0);
            mv.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setSuccess", buildMethodDesc(Type.VOID_TYPE, Type.BOOLEAN_TYPE), false);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LLOAD, timerSlotIndex);
            mv.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, timerSlotIndex);
            ASMUtils.setObjPropValueByLocalVar(mv, SPAN_TYPE, Type.LONG_TYPE, spanSlotIndex, timerSlotIndex, "setDuration");

            mv.visitVarInsn(ALOAD, spanSlotIndex);
            mv.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", buildMethodDesc(LOCAL_DATE_TIME_TYPE), false);
            mv.visitMethodInsn(INVOKEVIRTUAL, SPAN_TYPE.getInternalName(), "setEndDate", buildMethodDesc(Type.VOID_TYPE, LOCAL_DATE_TIME_TYPE), false);
            mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "exitSpan", "()V", false);

            publishEvent();

            mv.visitLabel(slotOutputSwitchLabel);
        }

        super.onMethodExit(opcode);
    }

    private void publishEvent() {
        mv.visitVarInsn(ALOAD, spanSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, "zenv/slot/databus/event/CacheSpanOutputEvent", "of", "(Lzenv/slot/entity/Span;)Lzenv/slot/databus/IDataEvent;", false);
        mv.visitVarInsn(ASTORE, spanOutputEventSlotIndex);
        mv.visitFieldInsn(GETSTATIC, "zenv/slot/conf/Constant", "DATA_BUS", "Lzenv/slot/databus/IDataBus;");
        mv.visitVarInsn(ALOAD, spanOutputEventSlotIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, "zenv/slot/databus/IDataBus", "publish", "(Lzenv/slot/databus/IDataEvent;)V", true);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // 获取 catch 代码块 Label
        exceptionLabels.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 4, maxLocals);
    }

    /**
     * 如果方法内部进行了 catch 则认为方法执行成功，只记录出现的异常信息，如果方法将异常抛出则认为方法执行失败
     */
    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        // 如果是异常处理代码块则将此方法调用标记为失败
        if (exceptionLabels.contains(label)) {
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", "Ljava/util/concurrent/atomic/AtomicBoolean;");
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", "()Z", false);
            final Label slotOutputSwitchLabel = new Label();
            mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);

            mv.visitInsn(DUP);
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

            mv.visitLabel(slotOutputSwitchLabel);
        }
    }
}
