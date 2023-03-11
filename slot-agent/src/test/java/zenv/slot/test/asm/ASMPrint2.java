package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.util.*;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/8 8:48
 */
public class ASMPrint2 {
    public static void main(String[] args) throws IOException {
        // 类的全限定名
        final String className = args[0];
        boolean asmCode = Boolean.parseBoolean(args[1]);
        final int parseOptions = ClassReader.SKIP_FRAMES;

        final Printer printer = asmCode ? new ASMifier() : new Textifier();
        final PrintWriter printWriter = new PrintWriter(System.out, true);
        final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);
        final TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, printer, printWriter);
        new ClassReader(className).accept(traceClassVisitor, parseOptions);
    }
}
