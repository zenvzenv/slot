package zenv.slot.test.asm;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import zenv.slot.utils.ASMUtils;

import static zenv.slot.conf.ASMConf.SYSTEM_TYPE;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/26 10:54
 */
public class TestMethodExitAdapter extends AdviceAdapter {
    protected TestMethodExitAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        int timerSlotIndex = 3;
        super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", ASMUtils.buildMethodDesc(Type.LONG_TYPE), false);
        super.visitVarInsn(LLOAD, timerSlotIndex);
        super.visitInsn(LSUB);
        super.visitVarInsn(LSTORE, timerSlotIndex);
        super.onMethodExit(opcode);
    }
}
