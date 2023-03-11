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
public class TestMethodEnterAdapter extends AdviceAdapter {
    protected TestMethodEnterAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        final int timerSlotIndex = newLocal(Type.LONG_TYPE);
        super.visitMethodInsn(INVOKESTATIC, SYSTEM_TYPE.getInternalName(), "currentTimeMillis", ASMUtils.buildMethodDesc(Type.LONG_TYPE), false);
        super.visitVarInsn(LSTORE, timerSlotIndex);
        super.onMethodEnter();
    }
}
