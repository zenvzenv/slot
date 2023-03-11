package zenv.slot;

import zenv.slot.log.AnsiLog;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.pipeline.Pipeline;
import zenv.slot.pipeline.valve.BuildSlotClassValve;
import zenv.slot.pipeline.valve.InitValve;
import zenv.slot.pipeline.valve.InstrumentationValve;
import zenv.slot.pipeline.valve.TransformClassValve;
import zenv.slot.utils.PropertiesUtils;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

/**
 * 埋点入口
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/4 9:09
 */
public class SlotAgentBootstrap {
    /**
     * 服务名
     */
    private static final String SLOT_SERVICE = "slot.service";

    public static void premain(String slotConfFilePath, Instrumentation inst) {
        final Properties init = init(slotConfFilePath);
        AnsiLog.info("开始埋点...,埋点配置文件为 : {}", slotConfFilePath);
        final Pipeline<Properties> pipeline = new Pipeline<>("slot");
        pipeline.addLast(new InitValve());
        pipeline.addLast(new BuildSlotClassValve());
        pipeline.addLast(new TransformClassValve());
        pipeline.addLast(new InstrumentationValve(inst));

        pipeline.start(init);
    }

    /**
     * 初始化埋点配置文件
     * <p>
     * 本来初始化配置文件的操作在 {@link InitValve} 中进行初始化，但现在需要对埋点日志的文件名按服务名进行划分，需要先获取到本次埋点
     * 的服务名，所以提前到 {@link SlotAgentBootstrap} 中进行
     *
     * @param filePath 埋点配置文件路径
     * @return 埋点配置文件 properties
     */
    private static Properties init(String filePath) {
        AnsiLog.info("加载的配置文件路径为:{}", filePath);
        Properties prop = null;
        try {
            prop = PropertiesUtils.loadExternalProp(filePath);
        } catch (IOException e) {
            AnsiLog.info("配置文件不存在，程序退出...");
            System.exit(1);
        }
        // 获取服务名称已初始化埋点日志名称
        final String serviceName = prop.getProperty(SLOT_SERVICE);
        AnsiLog.debug(serviceName);
        // 初始化日志
        SlotLogUtils.initLogger(serviceName);
        return prop;
    }
}
