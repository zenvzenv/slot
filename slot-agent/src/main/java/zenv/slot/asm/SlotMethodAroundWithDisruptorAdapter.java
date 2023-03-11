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

/**
 * 在方法的开头和结尾插入埋点代码
 * <p>
 * 集成 disruptor
 * <p>
 * 如果是非 spring 项目可以使用此类来进行转换
 * <pre>{@code
 * public int m4(int a, int b) {
 *         int c = a + b;
 *         String s = " aaa";
 *         double d = (double)(a + b);
 *         System.out.println("aaaaa");
 *         return c;
 *     }
 * }</pre>
 * 经过 ASM 转换之后变成
 * <pre>{@code
 * public int m4(int a, int b) {
 *         long var3 = System.currentTimeMillis();
 *         String var5 = null;
 *         String var6 = null;
 *         String var7 = null;
 *         Object var8 = null;
 *         Object var9 = null;
 *         ArrayList var10 = null;
 *         if (Constant.SLOT_OUTPUT_SWITCH.get()) {
 *             var5 = TraceManager.getParentSpan();
 *             var6 = TraceManager.entrySpan();
 *             var7 = TraceContext.getTraceId();
 *             var10 = new ArrayList(2);
 *             var10.add(a);
 *             var10.add(b);
 *         }
 *
 *         int c = a + b;
 *         String s = " aaa";
 *         double d = (double)(a + b);
 *         System.out.println("aaaaa");
 *         if (Constant.SLOT_OUTPUT_SWITCH.get()) {
 *             var3 = System.currentTimeMillis() - var3;
 *             TraceManager.exitSpan();
 *             Constant.RING_BUFFER_WORKER_POOL_FACTORY.getSpanProducer(Thread.currentThread().getName()).onData(var7, var6, var5, "zenv/slot/test/asm/HelloWorld", "m4", "(II)I", "zenv/slot/test/asm/HelloWorld#m4", true, LocalDateTime.now(), var3, (String)var8, (String)var9, var10);
 *         }
 *
 *         return c;
 *     }
 * }</pre>
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/2 10:02
 */
@Deprecated
public class SlotMethodAroundWithDisruptorAdapter extends AdviceAdapter {
    private final String className;
    private final String methodName;
    private final String methodDesc;
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

    /**
     * 一次调用链跟踪 id
     */
    private int traceIdSlotIndex;

    /**
     * 实际传入到方法的参数列表
     */
    private int methodParamsValueSlotIndex;

    /**
     * 异常本地变量表索引位
     */
    private int exceptionSlotIndex;

    /**
     * 异常信息本地变量表索引位
     */
    private int exceptionMsgSlotIndex;

    /**
     * 当发生异常时记录异常调用栈
     */
    private int exceptionStackSlotIndex;

    public SlotMethodAroundWithDisruptorAdapter(int api,
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
        // 新增 timer 字段
        this.timerSlotIndex = newLocal(Type.LONG_TYPE);

        // 新增父 span id 本地变量
        this.parentSpanIdSlotIndex = newLocal(STRING_TYPE);

        // 新增 span id 本地变量
        this.spanIdSlotIndex = newLocal(STRING_TYPE);

        // 新增 traceId 字段
        this.traceIdSlotIndex = newLocal(STRING_TYPE);

        // 新增异常本地变量
        this.exceptionSlotIndex = newLocal(STRING_TYPE);

        // 新增异常信息本地变量
        this.exceptionMsgSlotIndex = newLocal(STRING_TYPE);

        // 新增异常调用栈本地变量
        this.exceptionStackSlotIndex = newLocal(STACK_TRAC_ELEMENT_ARRAY_TYPE);

        // 方法参数实际值列表
        this.methodParamsValueSlotIndex = newLocal(LIST_TYPE);
    }

    @Override
    protected void onMethodEnter() {
        initLocalVar();

        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LSTORE, timerSlotIndex);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, parentSpanIdSlotIndex);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, spanIdSlotIndex);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, traceIdSlotIndex);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, exceptionSlotIndex);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, exceptionMsgSlotIndex);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, exceptionStackSlotIndex);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, methodParamsValueSlotIndex);

        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", ATOMIC_BOOLEAN_TYPE.getDescriptor());
        mv.visitMethodInsn(INVOKEVIRTUAL, ATOMIC_BOOLEAN_TYPE.getInternalName(), "get", "()Z", false);
        final Label slotOutputSwitchLabel = new Label();
        mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);

        mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "getParentSpan", ASMUtils.buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, parentSpanIdSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "entrySpan", ASMUtils.buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, spanIdSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, TRACE_CONTEXT_TYPE.getInternalName(), "getTraceId", ASMUtils.buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, traceIdSlotIndex);

        if (methodParamsCount > 0) {
            ASMUtils.newArrayListWithInitCapAtLocalVarTable(mv, methodParamsValueSlotIndex, methodParamsCount);
            final Type[] types = Type.getMethodType(methodDesc).getArgumentTypes();
            int slotIndex = isStatic;
            for (Type t : types) {
                int size = t.getSize();
                ASMUtils.addPropToArrayList(mv, t, methodParamsValueSlotIndex, slotIndex);
                slotIndex += size;
            }
        }

        mv.visitLabel(slotOutputSwitchLabel);
        super.onMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        // 方法无返回值且正常执行结束
        if (IRETURN <= opcode && opcode <= RETURN) {
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", ATOMIC_BOOLEAN_TYPE.getDescriptor());
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", "()Z", false);
            final Label slotOutputSwitchLabel = new Label();
            mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);

            publishEvent(ICONST_1);

            mv.visitLabel(slotOutputSwitchLabel);
        } else if (opcode == ATHROW) { // 方法抛出异常
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", ATOMIC_BOOLEAN_TYPE.getDescriptor());
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", "()Z", false);
            final Label slotOutputSwitchLabel = new Label();
            mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);

            publishEvent(ICONST_0);

            mv.visitLabel(slotOutputSwitchLabel);
        }

        super.onMethodExit(opcode);
    }

    private void publishEvent(int success) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(LLOAD, timerSlotIndex);
        mv.visitInsn(LSUB);
        mv.visitVarInsn(LSTORE, timerSlotIndex);

        mv.visitMethodInsn(INVOKESTATIC, "zenv/slot/trace/TraceManager", "exitSpan", "()V", false);

        mv.visitFieldInsn(GETSTATIC, "zenv/slot/conf/Constant", "RING_BUFFER_WORKER_POOL_FACTORY", "Lzenv/slot/disruptor/RingBufferWorkerPoolFactory;");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "zenv/slot/disruptor/RingBufferWorkerPoolFactory", "getSpanProducer", "(Ljava/lang/String;)Lzenv/slot/disruptor/SpanProducer;", false);
        mv.visitVarInsn(ALOAD, traceIdSlotIndex);
        mv.visitVarInsn(ALOAD, spanIdSlotIndex);
        mv.visitVarInsn(ALOAD, parentSpanIdSlotIndex);
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitLdcInsn(methodDesc);
        mv.visitLdcInsn(className + "#" + methodName);
        mv.visitInsn(success);
        mv.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", "()Ljava/time/LocalDateTime;", false);
        mv.visitVarInsn(LLOAD, timerSlotIndex);
        mv.visitVarInsn(ALOAD, exceptionSlotIndex);
        mv.visitVarInsn(ALOAD, exceptionMsgSlotIndex);
        mv.visitVarInsn(ALOAD, exceptionStackSlotIndex);
        mv.visitVarInsn(ALOAD, methodParamsValueSlotIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, "zenv/slot/disruptor/SpanProducer", "onData", ASMUtils.buildMethodDesc(Type.VOID_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, Type.BOOLEAN_TYPE, LOCAL_DATE_TIME_TYPE, Type.LONG_TYPE, STRING_TYPE, STRING_TYPE, STACK_TRAC_ELEMENT_ARRAY_TYPE, LIST_TYPE), false);

    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // 获取 catch 代码块 Label
        exceptionLabels.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 16, maxLocals);
    }

    /**
     * 如果方法内部进行了 catch 则认为方法执行成功，只记录出现的异常信息，如果方法将异常抛出则认为方法执行失败
     */
    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        // 如果是异常处理代码块则将此方法调用标记为失败
        if (exceptionLabels.contains(label)) {
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", ATOMIC_BOOLEAN_TYPE.getDescriptor());
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", "()Z", false);
            final Label slotOutputSwitchLabel = new Label();
            mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);

            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
            mv.visitVarInsn(ASTORE, exceptionSlotIndex);

            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Throwable.class), "getMessage", "()Ljava/lang/String;", false);
            mv.visitVarInsn(ASTORE, exceptionMsgSlotIndex);

            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
            mv.visitVarInsn(ASTORE, exceptionStackSlotIndex);

            mv.visitLabel(slotOutputSwitchLabel);
        }
    }
}
