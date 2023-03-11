package zenv.slot.utils;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

import java.util.ArrayList;

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import static zenv.slot.conf.ASMConf.*;
import static zenv.slot.conf.Constant.SEQUENCE_ID_GENERATOR;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 19:31
 */
public final class ASMUtils {
    private static final String CONSTRUCTION_METHOD_NAME = "<init>";
    private static final String CONSTRUCTION_METHOD_DESC = "()V";

    /**
     * 构建方法描述符
     *
     * @param returnType 返回类型
     * @param paramTypes 参数类型
     * @return 方法描述符
     */
    public static String buildMethodDesc(Type returnType, Type... paramTypes) {
        if (null == paramTypes || 0 == paramTypes.length) {
            return "()" + returnType.getDescriptor();
        } else {
            final StringBuilder desc = new StringBuilder();
            desc.append("(");
            for (Type paramType : paramTypes) {
                desc.append(paramType.getDescriptor());
            }
            desc.append(")").append(returnType.getDescriptor());
            return desc.toString();
        }
    }

    public synchronized static String genId() {
        return String.valueOf(SEQUENCE_ID_GENERATOR.nextId());
    }

    /**
     * 初始化对象(调用无参构造器)，并存储在指定的本地变量索引位
     *
     * @param type      需要初始化的对象的类型
     * @param mv        mv
     * @param slotIndex 本地变量表索引位
     */
    public static void newObjAtLocalVarTable(Type type, MethodVisitor mv, int slotIndex) {
        mv.visitTypeInsn(NEW, type.getInternalName());
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, type.getInternalName(), CONSTRUCTION_METHOD_NAME, CONSTRUCTION_METHOD_DESC, Boolean.FALSE);
        mv.visitVarInsn(Opcodes.ASTORE, slotIndex);
    }

    /**
     * 初始化带有初始容量的 ArrayList
     *
     * @param mv        mv
     * @param slotIndex ArrayList 即将要存放的本地变量表索引位
     * @param initCap   ArrayList 的初始长度
     */
    public static void newArrayListWithInitCapAtLocalVarTable(MethodVisitor mv, int slotIndex, int initCap) {
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        if (0 <= initCap && initCap <= 5) {
            mv.visitInsn(initCap + 3);
        } else if (5 < initCap && initCap <= ((1 << 7) - 1)) {
            mv.visitIntInsn(BIPUSH, initCap);
        }
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(ArrayList.class), CONSTRUCTION_METHOD_NAME, buildMethodDesc(Type.VOID_TYPE, Type.INT_TYPE), Boolean.FALSE);
        mv.visitVarInsn(ASTORE, slotIndex);
    }

    /**
     * 向 ArrayList 中添加元素
     *
     * @param mv                 mv
     * @param type               元素类型
     * @param arrayListSlotIndex ArrayList 本地变量表索引位
     * @param slotIndex          元素本地变量表索引位
     */
    public static void addPropToArrayList(MethodVisitor mv, Type type, int arrayListSlotIndex, int slotIndex) {
        int sort = type.getSort();
        final int opcode = type.getOpcode(ILOAD);
        mv.visitVarInsn(ALOAD, arrayListSlotIndex);
        mv.visitVarInsn(opcode, slotIndex);
        if (sort == Type.INT) {
            mv.visitMethodInsn(INVOKESTATIC, INT_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(INT_OBJ_TYPE, Type.INT_TYPE), Boolean.FALSE);
        } else if (sort == Type.CHAR) {
            mv.visitMethodInsn(INVOKESTATIC, CHAR_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(CHAR_OBJ_TYPE, Type.CHAR_TYPE), Boolean.FALSE);
        } else if (sort == Type.LONG) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(LONG_OBJ_TYPE, Type.LONG_TYPE), Boolean.FALSE);
        } else if (sort == Type.SHORT) {
            mv.visitMethodInsn(INVOKESTATIC, SHORT_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(SHORT_OBJ_TYPE, Type.SHORT_TYPE), Boolean.FALSE);
        } else if (sort == Type.BYTE) {
            mv.visitMethodInsn(INVOKESTATIC, BYTE_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(BYTE_OBJ_TYPE, Type.BYTE_TYPE), Boolean.FALSE);
        } else if (sort == Type.BOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(BOOLEAN_OBJ_TYPE, Type.BOOLEAN_TYPE), Boolean.FALSE);
        } else if (sort == Type.FLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, FLOAT_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(FLOAT_OBJ_TYPE, Type.FLOAT_TYPE), Boolean.FALSE);
        } else if (sort == Type.DOUBLE) {
            mv.visitMethodInsn(INVOKESTATIC, DOUBLE_OBJ_TYPE.getInternalName(), "valueOf", buildMethodDesc(DOUBLE_OBJ_TYPE, Type.DOUBLE_TYPE), Boolean.FALSE);
        }
        mv.visitMethodInsn(INVOKEINTERFACE, LIST_TYPE.getInternalName(), "add", buildMethodDesc(Type.BOOLEAN_TYPE, OBJECT_TYPE), Boolean.TRUE);
        mv.visitInsn(POP);
    }

    /**
     * 将本地变量表中的数据设置到对象的字段中
     *
     * @param mv                mv
     * @param objType           对象类型
     * @param propType          被设置的值类型
     * @param objSlotIndex      对象的本地变量表索引
     * @param propertySlotIndex 字段值的本地变量表索引位
     * @param setMethodStr      set 方法名
     */
    public static void setObjPropValueByLocalVar(MethodVisitor mv, Type objType, Type propType, int objSlotIndex, int propertySlotIndex, String setMethodStr) {
        mv.visitVarInsn(objType.getOpcode(ILOAD), objSlotIndex);
//        mv.visitVarInsn(ALOAD, objSlotIndex);
        mv.visitVarInsn(propType.getOpcode(ILOAD), propertySlotIndex);
//        mv.visitVarInsn(ALOAD, propertySlotIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, objType.getInternalName(), setMethodStr, buildMethodDesc(Type.VOID_TYPE, propType), Boolean.FALSE);
    }

    /**
     * 设置常量字符串
     *
     * @param mv           mv
     * @param objType      对象类型
     * @param objSlotIndex 对象本地变量索引位
     * @param constant     字符串
     * @param methodName   方法名称
     */
    public static void setConstantStr(MethodVisitor mv, Type objType, int objSlotIndex, String constant, String methodName) {
        mv.visitVarInsn(ALOAD, objSlotIndex);
        mv.visitLdcInsn(constant);
        mv.visitMethodInsn(INVOKEVIRTUAL, objType.getInternalName(), methodName, buildMethodDesc(Type.VOID_TYPE, STRING_TYPE), Boolean.FALSE);
    }

    /**
     * 初始化变量为 null
     *
     * @param mv        mv
     * @param slotIndex 变量本地变量表索引位
     * @param type      变量类型
     */
    public static void initPropNull(MethodVisitor mv, int slotIndex, Type type) {
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(type.getOpcode(ISTORE), slotIndex);
    }
}
