package zenv.slot.test.asm.exception;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.FileUtils;

public class HelloWorldTransformCore {
    public static void main(String[] args) {
        SlotLogUtils.initLogger("test");
        String relative_path = "zenv/slot/test/asm/exception/HelloWorld.class";
        String filepath = FileUtils.getFilePath(relative_path);
        byte[] bytes1 = FileUtils.readBytes(filepath);

        //（1）构建ClassReader
        ClassReader cr = new ClassReader(bytes1);

        //（2）构建ClassWriter
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final CheckClassAdapter cca = new CheckClassAdapter(cw);

        //（3）串连ClassVisitor
        int api = Opcodes.ASM5;
        ClassVisitor cv = new MethodWithWholeTryCatchVisitor(api, cca, "test", "(Ljava/lang/String;I)V");


        //（4）结合ClassReader和ClassVisitor
        int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
        cr.accept(cv, parsingOptions);

        //（5）生成byte[]
        byte[] bytes2 = cw.toByteArray();

        FileUtils.writeBytes(filepath, bytes2);
    }
}