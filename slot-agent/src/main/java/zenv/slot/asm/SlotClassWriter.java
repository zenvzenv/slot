package zenv.slot.asm;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Type;

/**
 * 防止发生 ClassNotFoundException
 *
 * @author zhengwei AKA zenv
 * @since 2022/10/17 21:39
 */
public class SlotClassWriter extends ClassWriter {
    private final ClassLoader classLoader;

    public SlotClassWriter(int flags, ClassLoader classLoader) {
        super(flags);
        this.classLoader = classLoader;
    }

    /**
     * Returns the common super type of the two given types. The default implementation of this method
     * <i>loads</i> the two given classes and uses the java.lang.Class methods to find the common
     * super class. It can be overridden to compute this common super type in other ways, in
     * particular without actually loading any class, or to take into account the class that is
     * currently being generated by this ClassWriter, which can of course not be loaded since it is
     * under construction.
     *
     * @since ASM9.x
     * @param type1 the internal name of a class (see {@link Type#getInternalName()}).
     * @param type2 the internal name of another class (see {@link Type#getInternalName()}).
     * @return the internal name of the common super class of the two given classes (see {@link
     * Type#getInternalName()}).
     */
    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        ClassLoader classLoader = getClassLoader();
        Class<?> class1;
        try {
            class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new TypeNotPresentException(type1, e);
        }
        Class<?> class2;
        try {
            class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new TypeNotPresentException(type2, e);
        }
        if (class1.isAssignableFrom(class2)) {
            return type1;
        }
        if (class2.isAssignableFrom(class1)) {
            return type2;
        }
        if (class1.isInterface() || class2.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                class1 = class1.getSuperclass();
            } while (!class1.isAssignableFrom(class2));
            return class1.getName().replace('.', '/');
        }
    }

    protected ClassLoader getClassLoader() {
        return this.classLoader;
    }
}
