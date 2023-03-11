package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.Type;
import zenv.slot.utils.ASMUtils;

import static zenv.slot.conf.ASMConf.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/23 9:37
 */
public class TestRun {
    public static void main(String[] args) throws Exception {
        /*final HelloWorld helloWorld = new HelloWorld();
        helloWorld.m3(1, 2);*/
//        final HelloWorldTarget2 helloWorldTarget = new HelloWorldTarget2();
//        helloWorldTarget.m5("a");
//        System.out.println(getLocalIndex());
        System.out.println(ASMUtils.buildMethodDesc(Type.VOID_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, Type.BOOLEAN_TYPE, LOCAL_DATE_TIME_TYPE, Type.LONG_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, LIST_TYPE));
    }

    private static int getLocalIndex() {
        final Type type = Type.getType("()V");
        final boolean isStatic = true;
        final Type[] argumentTypes = type.getArgumentTypes();
        int localIndex = isStatic ? 0 : 1;
        for (Type argumentType : argumentTypes) {
            localIndex += argumentType.getSize();
        }
        localIndex = localIndex - 1;
        return Math.max(localIndex, 0);
    }
}
