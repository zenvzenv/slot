package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import zenv.slot.asm.MethodStackMapFrame02Visitor;
import zenv.slot.utils.FileUtils;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 14:17
 */
public class AsmAnalyzerFrame {
    public static void transform(String classFilePath) {
        final String filePath = FileUtils.getFilePath(classFilePath);
        final byte[] bytes = FileUtils.readBytes(filePath);

        final ClassReader cr = new ClassReader(bytes);
        final int api = Opcodes.ASM5;

        final ClassVisitor cv = new MethodStackMapFrame02Visitor(api, null, bytes);

        final int parsingOptions = ClassReader.EXPAND_FRAMES;
        cr.accept(cv, parsingOptions);
    }

    public static void main(String[] args) {
        transform("zenv/slot/test/asm/HelloWorldTarget.class");
    }
}
