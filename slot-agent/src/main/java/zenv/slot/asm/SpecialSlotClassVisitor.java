package zenv.slot.asm;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import lombok.Getter;
import lombok.Setter;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static jdk.internal.org.objectweb.asm.Opcodes.*;
import static zenv.slot.conf.Constant.*;


/**
 * 指定 class,method 来进行埋点监控
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/2 9:20
 */
@Setter
@Getter
public class SpecialSlotClassVisitor extends ClassVisitor {
    private static final Logger log = SlotLogUtils.getLogger(SpecialSlotClassVisitor.class);
    private final String slotPrefix;
    private final String slotSuffix;
    private String owner;
    private boolean isInterface;

    /**
     * 存放 class 中的所有字段(小写)
     * <p>
     * 形如 name,age...
     */
    private static final Set<String> FIELD_SET = new HashSet<>();

    /**
     * 类中的 toString, hashCode, equals 方法等
     */
    private static final Predicate<String> EXCLUDE_OBJECT_STRING = (name) -> {
        for (String s : SLOT_EXCLUDE_METHOD_NAME) {
            if (s.equals(name)) {
                log.info("匹配到需要排除的方法 - {}", name);
                return true;
            }
        }
        return false;
    };

    /**
     * 匹配 set/get/is 开头并且以字段名(小写)结尾的方法
     * <p>
     * 形如 setname,getname,isboolean
     */
    private static final Predicate<String> EXCLUDE_SETTER_GETTER = (name) -> {
        for (String f : FIELD_SET) {
            for (String prefix : SLOT_EXCLUDE_METHOD_PREFIX) {
                if (name.startsWith(prefix) && name.toLowerCase().endsWith(f)) {
                    log.info("匹配到需要排除的方法前缀，前缀为 - {}， 方法名为 - {}", prefix, name);
                    return true;
                }
            }
        }
        return false;
    };

    public SpecialSlotClassVisitor(int api, ClassVisitor classVisitor, String slotPrefix, String slotSuffix) {
        super(api, classVisitor);
        this.slotPrefix = slotPrefix;
        this.slotSuffix = slotSuffix;
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
        this.owner = name;
        this.isInterface = (access & ACC_INTERFACE) == ACC_INTERFACE;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * 获取到 class 中的字段
     *
     * @param access    访问控制符
     * @param name      字段名称
     * @param desc      字段描述符
     * @param signature 注解
     * @param value     字段值
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FIELD_SET.add(name.toLowerCase());
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
        log.debug("class - {}, name - {}, desc - {}", owner, name, desc);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, interfaces);
        // 排除构造方法、静态构造方法和内部类方法
        if (null != mv && !CONSTRUCTOR_METHOD_NAME.equals(name) && !STATIC_CONSTRUCTORS_METHOD_NAME.equals(name) && !name.contains("$")) {
            final boolean isAbstract = (access & ACC_ABSTRACT) == ACC_ABSTRACT;
            final boolean isNative = (access & ACC_NATIVE) == ACC_NATIVE;
            // 排除抽象方法、本地方法
            if (!this.isInterface && !isAbstract && !isNative) {
                // 排除 toString,Setter,Getter 方法
                if (!excludeMethod(name)) {
                    // package=*
                    // package.class=*
                    if (slotSuffix.equals("*")) {
                        log.info("修改 {} - {} - {}", owner, name, desc);
                        mv = new SlotMethodAroundWithDisruptorAdapter2(api, mv, access, name, desc, this.owner);
                    } else {
                        final String[] methods = slotSuffix.split("[,]");
                        for (String method : methods) {
                            final String[] methodInfo = method.split("[@]");
                            // class=method 包含重载方法
                            if (methodInfo.length == 1 && methodInfo[0].equals(name)) {
                                log.info("修改 {} - {} - {}", owner, name, desc);
                                mv = new SlotMethodAroundWithDisruptorAdapter2(api, mv, access, name, desc, this.owner);
                            }
                            // class=methodName@methodDesc 不包含重载方法
                            else if (methodInfo.length == 2 && methodInfo[0].equals(name) && methodInfo[1].equals(desc)) {
                                log.info("修改 {} - {} - {}", owner, name, desc);
                                mv = new SlotMethodAroundWithDisruptorAdapter2(api, mv, access, name, desc, this.owner);
                            }
                        }
                    }
                }
            }
        }
        return mv;
    }

    /**
     * 剔除指定方法名、指定方法前缀方法
     *
     * @return true-匹配到符合方法名和方法前缀的方法;false-未匹配到指定的方法名和方法前缀的方法
     */
    private static boolean excludeMethod(String methodName) {
        return EXCLUDE_OBJECT_STRING.or(EXCLUDE_SETTER_GETTER).test(methodName);
    }
}
