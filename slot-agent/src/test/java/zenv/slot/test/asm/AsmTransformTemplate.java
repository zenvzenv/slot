package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.FileUtils;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 14:17
 */
public class AsmTransformTemplate {
    public static void transform(String classFilePath) {
        try {
            final String filePath = FileUtils.getFilePath(classFilePath);
            final byte[] bytes = FileUtils.readBytes(filePath);

            final ClassReader cr = new ClassReader(bytes);
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//            final CheckClassAdapter cca = new CheckClassAdapter(cw, false);
            final int api = Opcodes.ASM5;

            final ClassVisitor cv = new TestClassVisitor1(api, cw);
//        final ClassVisitor cv2 = new TestClassVisitor2(api, cv);
//        final ClassVisitor cv = new TestClassVisitor1(api, cw);

            final int parsingOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
            cr.accept(cv, parsingOptions);

            final byte[] bytes1 = cw.toByteArray();
            FileUtils.writeBytes(filePath, bytes1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SlotLogUtils.initLogger("test");
        transform("zenv/slot/test/asm/HelloWorld.class");
    }
}
