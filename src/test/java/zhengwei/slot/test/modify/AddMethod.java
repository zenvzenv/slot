package zhengwei.slot.test.modify;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/21 16:24
 */
public class AddMethod extends ClassWriter {
    public AddMethod(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    public AddMethod(int flags) {
        super(flags);
    }


}
