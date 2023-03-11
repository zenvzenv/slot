package zenv.slot.transform;

import zenv.slot.asm.SlotClassWriter;
import zenv.slot.asm.SpecialSlotClassVisitor;
import zenv.slot.entity.SlotInfo;
import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.Set;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/8 17:21
 */
public class SlotTransformer implements ClassFileTransformer {
    private static final Logger log = SlotLogUtils.getLogger(SlotTransformer.class);
    private final Set<SlotInfo> slotInfos;

    public SlotTransformer(Set<SlotInfo> slotInfos) {
        this.slotInfos = slotInfos;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        log.debug("加载原始类 - {}", className);
        final Iterator<SlotInfo> it = slotInfos.iterator();
        while (it.hasNext()) {
            final SlotInfo next = it.next();
            log.debug("匹配前缀 - {}", next.getPrefix());
            if (className.startsWith(next.getPrefix()) && !className.contains("$")) {
                log.debug("匹配到 - {}", className);
                try {
                    final ClassReader cr = new ClassReader(classfileBuffer);
                    final SlotClassWriter cw = new SlotClassWriter(ClassWriter.COMPUTE_FRAMES, loader);
//                    final ClassVisitor cca = new CheckClassAdapter(cw);
                    final ClassVisitor cv = new SpecialSlotClassVisitor(Opcodes.ASM5, cw, next.getPrefix(), next.getMethod());
                    final int parsingOptions = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
                    cr.accept(cv, parsingOptions);
                    if (next.getType().equals(SlotInfo.CLASS_TYPE)) it.remove();
                    return cw.toByteArray();
                } catch (Throwable e) {
                    log.error("埋点发生错误", e);
                }
            }
        }
        return null;
    }
}
