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
 * 在方法的开头和结尾插入埋点代码
 * <p>
 * 集成 disruptor
 * <p>
 * 修正在 spring 环境中异常被全局捕获后无法获取 controller 入口方法信息的问题
 * <p>
 * 实现思路：将整个方法用 try-catch 进行包裹，捕获所有异常，最终由 root method 来进行异常的抛出或捕获
 *
 * <pre>{@code
 * public void m3(int m3a, int m3b) throws InterruptedException {
 *         try {
 *             System.out.println("m3");
 *             TimeUnit.SECONDS.sleep(1);
 *         } catch (InterruptedException e) {
 *             throw new InterruptedException();
 *         } catch (ArithmeticException e2) {
 *             e2.printStackTrace();
 *         } catch (Exception e3) {
 *
 *         }
 *     }
 * }</pre>
 * 经过 ASM 变换之后变成
 * <pre>{@code
 * public void m3(int var1, int var2) throws InterruptedException {
 *         LocalDateTime var3 = LocalDateTime.now();
 *         long var4 = System.currentTimeMillis();
 *         String var6 = null;
 *         String var7 = null;
 *         String var8 = null;
 *         String var9 = null;
 *         String var10 = null;
 *         StackTraceElement[] var11 = null;
 *         ArrayList var12 = null;
 *         if (Constant.SLOT_OUTPUT_SWITCH.get()) {
 *             var6 = TraceManager.getParentSpan();
 *             var7 = TraceManager.entrySpan();
 *             var8 = TraceContext.getTraceId();
 *             var12 = new ArrayList(2);
 *             var12.add(var1);
 *             var12.add(var2);
 *         }
 *
 *         try {
 *             try {
 *                 System.out.println("m3");
 *                 TimeUnit.SECONDS.sleep(1L);
 *             } catch (InterruptedException var14) {
 *                 if (Constant.SLOT_OUTPUT_SWITCH.get()) {
 *                     var9 = var14.getClass().getName();
 *                     var10 = var14.getMessage();
 *                     var11 = var14.getStackTrace();
 *                 }
 *
 *                 throw new InterruptedException();
 *             } catch (ArithmeticException var15) {
 *                 if (Constant.SLOT_OUTPUT_SWITCH.get()) {
 *                     var9 = var15.getClass().getName();
 *                     var10 = var15.getMessage();
 *                     var11 = var15.getStackTrace();
 *                 }
 *
 *                 var15.printStackTrace();
 *             } catch (Exception var16) {
 *                 if (Constant.SLOT_OUTPUT_SWITCH.get()) {
 *                     var9 = var16.getClass().getName();
 *                     var10 = var16.getMessage();
 *                     var11 = var16.getStackTrace();
 *                 }
 *             }
 *
 *             if (Constant.SLOT_OUTPUT_SWITCH.get() && null != var8) {
 *                 var4 = System.currentTimeMillis() - var4;
 *                 TraceManager.exitSpan();
 *                 Constant.RING_BUFFER_WORKER_POOL_FACTORY.getSpanProducer(Thread.currentThread().getName()).onData(var8, var7, var6, "zenv/slot/test/asm/HelloWorld", "m3", "(II)V", "zenv/slot/test/asm/HelloWorld#m3", true, var3, LocalDateTime.now(), var4, var9, var10, var11, var12);
 *             }
 *
 *         } catch (Throwable var17) {
 *             if (Constant.SLOT_OUTPUT_SWITCH.get() && null != var8) {
 *                 var9 = var17.getClass().getName();
 *                 var10 = var17.getMessage();
 *                 var11 = var17.getStackTrace();
 *                 var4 = System.currentTimeMillis() - var4;
 *                 TraceManager.exitSpan();
 *                 Constant.RING_BUFFER_WORKER_POOL_FACTORY.getSpanProducer(Thread.currentThread().getName()).onData(var8, var7, var6, "zenv/slot/test/asm/HelloWorld", "m3", "(II)V", "zenv/slot/test/asm/HelloWorld#m3", false, var3, LocalDateTime.now(), var4, var9, var10, var11, var12);
 *             }
 *
 *             throw var17;
 *         }
 *     }
 * }</pre>
 * 埋点程序主要对原有业务代码做以下几件事：
 * <ul>
 *     <li>1. 在代码开头声明埋点所需的局部变量</li>
 *     <li>2. 将整个业务代码进行 try-catch 以确保原始代码按正常流程走完并触发埋点输出机制</li>
 *     <li>3. 对每个 catch 块进行处理，如果原业务代码对异常进行了处理埋点则只会记录发生的异常，如果没有处理异常而是抛出异常则进行捕获后进行抛出</li>
 *     <li>4. 记录埋点必要信息并交由埋点系统进行处理</li>
 * </ul>
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/2 10:02
 */
public class SlotMethodAroundWithDisruptorAdapter2 extends AdviceAdapter {
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
    private final int methodParamsCount;

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

    /**
     * 方法开始时间本地变量索引位
     */
    private int startDateSlotIndex;

    private final Label startLabel = new Label();

    private final Label endLabel = new Label();

    private final Label handlerLabel = new Label();

    private int extraStackSize = 0;

    public SlotMethodAroundWithDisruptorAdapter2(int api,
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

    /**
     * 初始化埋点所需的本地变量
     */
    private void initLocalVar() {
        // 新增方法开始时间字段
        this.startDateSlotIndex = newLocal(LOCAL_DATE_TIME_TYPE);
        extraStackSize += LOCAL_DATE_TIME_TYPE.getSize();

        // 新增 timer 字段
        this.timerSlotIndex = newLocal(Type.LONG_TYPE);
        extraStackSize += Type.LONG_TYPE.getSize();

        // 新增父 parent id 本地变量
        this.parentSpanIdSlotIndex = newLocal(STRING_TYPE);
        extraStackSize += STRING_TYPE.getSize();

        // 新增 span id 本地变量
        this.spanIdSlotIndex = newLocal(STRING_TYPE);
        extraStackSize += STRING_TYPE.getSize();

        // 新增 traceId 字段
        this.traceIdSlotIndex = newLocal(STRING_TYPE);
        extraStackSize += STRING_TYPE.getSize();

        // 新增异常本地变量
        this.exceptionSlotIndex = newLocal(STRING_TYPE);
        extraStackSize += STRING_TYPE.getSize();

        // 新增异常信息本地变量
        this.exceptionMsgSlotIndex = newLocal(STRING_TYPE);
        extraStackSize += STRING_TYPE.getSize();

        // 新增异常调用栈本地变量
        this.exceptionStackSlotIndex = newLocal(STACK_TRAC_ELEMENT_ARRAY_TYPE);
        extraStackSize += STACK_TRAC_ELEMENT_ARRAY_TYPE.getSize();

        // 方法参数实际值列表
        this.methodParamsValueSlotIndex = newLocal(LIST_TYPE);
        extraStackSize += LIST_TYPE.getSize();

        mv.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", ASMUtils.buildMethodDesc(LOCAL_DATE_TIME_TYPE), false);
        mv.visitVarInsn(ASTORE, this.startDateSlotIndex);

        mv.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", ASMUtils.buildMethodDesc(Type.LONG_TYPE), false);
        mv.visitVarInsn(LSTORE, this.timerSlotIndex);

        ASMUtils.initPropNull(mv, this.parentSpanIdSlotIndex, STRING_TYPE);
        ASMUtils.initPropNull(mv, this.spanIdSlotIndex, STRING_TYPE);
        ASMUtils.initPropNull(mv, this.traceIdSlotIndex, STRING_TYPE);
        ASMUtils.initPropNull(mv, this.exceptionSlotIndex, STRING_TYPE);
        ASMUtils.initPropNull(mv, this.exceptionMsgSlotIndex, STRING_TYPE);
        ASMUtils.initPropNull(mv, this.exceptionStackSlotIndex, STACK_TRAC_ELEMENT_ARRAY_TYPE);
        ASMUtils.initPropNull(mv, this.methodParamsValueSlotIndex, LIST_TYPE);
    }

    /**
     * 生成 traceId,spanId,parentId
     */
    private void genReqId() {
        mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "getParentSpan", ASMUtils.buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, this.parentSpanIdSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "entrySpan", ASMUtils.buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, this.spanIdSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, TRACE_CONTEXT_TYPE.getInternalName(), "getTraceId", ASMUtils.buildMethodDesc(STRING_TYPE), false);
        mv.visitVarInsn(ASTORE, this.traceIdSlotIndex);
    }

    @Override
    protected void onMethodEnter() {
        initLocalVar();

        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", ATOMIC_BOOLEAN_TYPE.getDescriptor());
        mv.visitMethodInsn(INVOKEVIRTUAL, ATOMIC_BOOLEAN_TYPE.getInternalName(), "get", ASMUtils.buildMethodDesc(Type.BOOLEAN_TYPE), false);
        final Label slotOutputSwitchLabel = new Label();
        mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);

        genReqId();

        // 记录方法的实际入参
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

        mv.visitLabel(this.startLabel);
        super.onMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        // 方法无返回值且正常执行结束
        if (IRETURN <= opcode && opcode <= RETURN) {
            publishEvent(Boolean.TRUE);
        }

        super.onMethodExit(opcode);
    }

    /**
     * 发布事件，因为此版本是将原业务代码包裹在 try-catch 代码块中，一旦是因为抛出异常而导致程序失败的话，那么会被 ASM 插桩 catch 代码块捕
     * 获，同时记录异常类型、异常信息和异常调用栈。
     * <p>
     * 是否要输出埋点数据取决于埋点开关和 traceId 是否为空，特别对 traceId 是否为空进行说明：
     * <ul>
     *     <li>1. 因为埋点开关可以在任意时间点被开启和关闭，如果埋点开关一开始是关闭状态，那么埋点插桩的代码在方法开始执行的时候是不生效的，
     *     如果方法执行的过程中埋点开关被打开，那么方法末尾的埋点代码将会被执行，这会导致 traceId,spanId 为 null，像这种不完全的埋点数据，
     *     我们认为不需要进行输出而是选择将其丢弃；</li>
     *     <li>2. 如果埋点开关一开始是开启状态，在方法执行一半时埋点开关被关闭，那么埋点信息不会输出，之前生成的埋点信息会被丢弃。</li>
     * </ul>
     *
     * @param success 方法是否执行成功，true-成功，false-失败
     */
    private void publishEvent(boolean success) {
        // if (Constant.SLOT_OUTPUT_SWITCH.get() && null != traceId) {
        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Constant.class), "SLOT_OUTPUT_SWITCH", ATOMIC_BOOLEAN_TYPE.getDescriptor());
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", ASMUtils.buildMethodDesc(Type.BOOLEAN_TYPE), Boolean.FALSE);
        final Label slotOutputSwitchLabel = new Label();
        mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ALOAD, traceIdSlotIndex);
        mv.visitJumpInsn(IF_ACMPEQ, slotOutputSwitchLabel);

        if (!success) {
            collectExceptionInfo();
        }

        mv.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", ASMUtils.buildMethodDesc(Type.LONG_TYPE), Boolean.FALSE);
        mv.visitVarInsn(LLOAD, this.timerSlotIndex);
        mv.visitInsn(LSUB);
        mv.visitVarInsn(LSTORE, this.timerSlotIndex);

        mv.visitMethodInsn(INVOKESTATIC, TRACE_MANAGER_TYPE.getInternalName(), "exitSpan", ASMUtils.buildMethodDesc(Type.VOID_TYPE), Boolean.FALSE);

        mv.visitFieldInsn(GETSTATIC, CONSTANT_TYPE.getInternalName(), "RING_BUFFER_WORKER_POOL_FACTORY", RING_BUFFER_WORKER_POOL_FACTORY.getDescriptor());
        mv.visitMethodInsn(INVOKESTATIC, THREAD_TYPE.getInternalName(), "currentThread", ASMUtils.buildMethodDesc(THREAD_TYPE), Boolean.FALSE);
        mv.visitMethodInsn(INVOKEVIRTUAL, THREAD_TYPE.getInternalName(), "getName", ASMUtils.buildMethodDesc(STRING_TYPE), Boolean.FALSE);
        mv.visitMethodInsn(INVOKEVIRTUAL, RING_BUFFER_WORKER_POOL_FACTORY.getInternalName(), "getSpanProducer", buildMethodDesc(SPAN_PRODUCER_TYPE, STRING_TYPE), Boolean.FALSE);
        mv.visitVarInsn(ALOAD, this.traceIdSlotIndex);
        mv.visitVarInsn(ALOAD, this.spanIdSlotIndex);
        mv.visitVarInsn(ALOAD, this.parentSpanIdSlotIndex);
        mv.visitLdcInsn(this.className);
        mv.visitLdcInsn(this.methodName);
        mv.visitLdcInsn(this.methodDesc);
        mv.visitLdcInsn(this.className + "#" + this.methodName);
        mv.visitInsn(success ? ICONST_1 : ICONST_0);
        mv.visitVarInsn(ALOAD, this.startDateSlotIndex);
        mv.visitMethodInsn(INVOKESTATIC, LOCAL_DATE_TIME_TYPE.getInternalName(), "now", ASMUtils.buildMethodDesc(LOCAL_DATE_TIME_TYPE), Boolean.FALSE);
        mv.visitVarInsn(LLOAD, this.timerSlotIndex);
        mv.visitVarInsn(ALOAD, this.exceptionSlotIndex);
        mv.visitVarInsn(ALOAD, this.exceptionMsgSlotIndex);
        mv.visitVarInsn(ALOAD, this.exceptionStackSlotIndex);
        mv.visitVarInsn(ALOAD, this.methodParamsValueSlotIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL,
                SPAN_PRODUCER_TYPE.getInternalName(),
                "onData",
                ASMUtils.buildMethodDesc(
                        Type.VOID_TYPE,
                        STRING_TYPE,
                        STRING_TYPE,
                        STRING_TYPE,
                        STRING_TYPE,
                        STRING_TYPE,
                        STRING_TYPE,
                        STRING_TYPE,
                        Type.BOOLEAN_TYPE,
                        LOCAL_DATE_TIME_TYPE,
                        LOCAL_DATE_TIME_TYPE,
                        Type.LONG_TYPE,
                        STRING_TYPE,
                        STRING_TYPE,
                        STACK_TRAC_ELEMENT_ARRAY_TYPE,
                        LIST_TYPE
                ),
                Boolean.FALSE);

        mv.visitLabel(slotOutputSwitchLabel);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // 获取 catch 代码块 Label
        exceptionLabels.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mv.visitLabel(this.endLabel);

        mv.visitLabel(this.handlerLabel);

        publishEvent(Boolean.FALSE);
        // 抛出异常对象，因为在进入 catch 代码块时，栈顶元素就是异常对象
        mv.visitInsn(ATHROW);

        // 将原有业务代码用 try-catch 包裹
        // 需要确保我们添加的异常信息在原有代码中所有异常的最下方
        // 如果在 onMethodEnter 处调用，会导致埋点的异常处理在异常表的最上方，那么原有业务的所有报错都会被埋点catch住，这样会导致原有的业务系统无法正常的抛出或捕获异常
        mv.visitTryCatchBlock(this.startLabel, this.endLabel, this.handlerLabel, THROWABLE_TYPE.getInternalName());

        super.visitMaxs(maxStack + extraStackSize, maxLocals);
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
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicBoolean.class), "get", ASMUtils.buildMethodDesc(Type.BOOLEAN_TYPE), Boolean.FALSE);
            final Label slotOutputSwitchLabel = new Label();
            mv.visitJumpInsn(IFEQ, slotOutputSwitchLabel);

            collectExceptionInfo();

            mv.visitLabel(slotOutputSwitchLabel);
        }
    }

    /**
     * 收集异常信息
     * <p>
     * PS:当进入处理异常代码块时，栈顶元素为异常对象
     */
    private void collectExceptionInfo() {
        // 收集异常类型
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Object.class), "getClass", ASMUtils.buildMethodDesc(Type.getType(Class.class)), Boolean.FALSE);
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Class.class), "getName", ASMUtils.buildMethodDesc(STRING_TYPE), Boolean.FALSE);
        mv.visitVarInsn(ASTORE, this.exceptionSlotIndex);

        // 收集异常信息
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEVIRTUAL, THROWABLE_TYPE.getInternalName(), "getMessage", ASMUtils.buildMethodDesc(STRING_TYPE), Boolean.FALSE);
        mv.visitVarInsn(ASTORE, this.exceptionMsgSlotIndex);

        // 收集异常调用栈
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEVIRTUAL, THROWABLE_TYPE.getInternalName(), "getStackTrace", ASMUtils.buildMethodDesc(STACK_TRAC_ELEMENT_ARRAY_TYPE), Boolean.FALSE);
        mv.visitVarInsn(ASTORE, this.exceptionStackSlotIndex);
    }
}
