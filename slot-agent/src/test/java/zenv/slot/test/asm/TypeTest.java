package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.Type;

import java.util.Arrays;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/1 16:17
 */
public class TypeTest {
    public static void main(String[] args) {
        final Type type = Type.getType(String.class);
        System.out.println(type.getClassName());
        System.out.println(type.getInternalName());
        System.out.println(type.getDescriptor());
    }
}
