package zenv.slot.utils;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 14:17
 */
public class AsmTransformTemplate {
    public static void transform(String classFilePath) {
        final String filePath = FileUtils.getFilePath(classFilePath);
        final byte[] bytes = FileUtils.readBytes(filePath);

        final ClassReader cr = new ClassReader(bytes);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final int api = Opcodes.ASM5;

        final ClassVisitor cv = null;

        final int parsingOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
        cr.accept(cv, parsingOptions);

        final byte[] bytes1 = cw.toByteArray();
        FileUtils.writeBytes(filePath, bytes1);
    }
}
