package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import lombok.Getter;
import lombok.Setter;
import zenv.slot.asm.MethodVoidExitAdapter;
import zenv.slot.entity.SlotMethod;

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
public class TestClassVisitor2 extends ClassVisitor {
    public TestClassVisitor2(int api, ClassVisitor classVisitor) {
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
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, interfaces);
        // 排除构造方法和静态构造方法
        if (null != mv && !"<init>".equals(name) && !"<clinit>".equals(name)) {
            final boolean isAbstract = (access & ACC_ABSTRACT) == ACC_ABSTRACT;
            final boolean isNative = (access & ACC_NATIVE) == ACC_NATIVE;
            // 非抽象、本地方法
            if (!isAbstract && !isNative) {
                final SlotMethod slotMethod = new SlotMethod();
                mv = new MethodVoidExitAdapter(api, mv, access, name, desc);
//                mv = new TestMethodExitAdapter(api, mv, access, name, desc);
//                final Type methodType = Type.getMethodType(desc);
//                mv = new MethodAdapter(api, mv, desc);
            }
        }
        return mv;
    }
}
