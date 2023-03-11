package zenv.slot.utils;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.util.ASMifier;
import jdk.internal.org.objectweb.asm.util.Printer;
import jdk.internal.org.objectweb.asm.util.Textifier;
import jdk.internal.org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/8 8:48
 */
public class ASMPrint {
    public static void main(String[] args) throws IOException {
        // 类的全限定名
        final String className = args[0];
        boolean asmCode = Boolean.parseBoolean(args[1]);
        final int parseOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;

        final Printer printer = asmCode ? new ASMifier() : new Textifier();
        final PrintWriter printWriter = new PrintWriter(System.out, true);
        final TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, printer, printWriter);
        new ClassReader(className).accept(traceClassVisitor, parseOptions);
    }
}
