package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.FieldVisitor;
import lombok.Getter;
import lombok.Setter;
import zenv.slot.asm.SlotMethodAroundWithDisruptorAdapter2;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_NATIVE;

/**
 * 指定 class,method 来进行埋点监控
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/2 9:20
 */
@Setter
@Getter
public class TestClassVisitor1 extends ClassVisitor {
    private String owner;

    public TestClassVisitor1(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    /**
     * @param version    java 版本
     * @param access     访问控制符
     * @param name       类名
     * @param signature  泛型
     * @param superName  父类
     * @param interfaces 实现的接口
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        owner = name;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        System.out.println(name);
        return super.visitField(access, name, desc, signature, value);
    }

    /**
     * @param access     方法访问控制符
     * @param name       方法名
     * @param desc       方法描述符
     * @param signature  泛型
     * @param interfaces 实现的接口
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] interfaces) {
        System.out.println(name + "-" + desc);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, interfaces);
        // 排除构造方法和静态构造方法
        if (null != mv && !"<init>".equals(name) && !"<clinit>".equals(name)) {
            final boolean isAbstract = (access & ACC_ABSTRACT) == ACC_ABSTRACT;
            final boolean isNative = (access & ACC_NATIVE) == ACC_NATIVE;
            // 非抽象、本地方法
            if (!isAbstract && !isNative) {
//                mv = new SlotMethodAroundAdapter2(api, mv, access, name, desc, owner);
//                mv = new SlotMethodAroundWithDisruptorAdapter(api, mv, access, name, desc, owner);
                mv = new SlotMethodAroundWithDisruptorAdapter2(api, mv, access, name, desc, owner);
//                mv = new TestMethodEnterAdapter(api, mv, access, name, desc);
            }
        }
        return mv;
    }
}
