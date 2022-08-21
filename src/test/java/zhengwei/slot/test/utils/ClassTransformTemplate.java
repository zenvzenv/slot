package zhengwei.slot.test.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import zhengwei.slot.test.modify.ModifyClass;
import zhengwei.slot.test.modify.ModifyClass3;
import zhengwei.slot.utils.FileUtils;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/10 15:24
 */
public class ClassTransformTemplate {
    public static void main(String[] args) {
        // zhengwei/asm/classread/HelloWord01.class
        String relativePath = "zhengwei/slot/test/modify/HelloWorld2.class";
        final String filePath = FileUtils.getFilePath(relativePath);
        final byte[] bytes = FileUtils.readBytes(filePath);

        final ClassReader cr = new ClassReader(bytes);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        int api = Opcodes.ASM9;
        // 在此处实现不同的业务逻辑
        final ClassVisitor cv = new ModifyClass(api, cw);

        int parsingOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
        cr.accept(cv, parsingOptions);

        final byte[] newClassBytes = cw.toByteArray();

        FileUtils.writeBytes(filePath, newClassBytes);
    }
}
