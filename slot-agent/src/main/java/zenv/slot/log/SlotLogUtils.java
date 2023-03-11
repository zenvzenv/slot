package zenv.slot.log;

import zenv.slot.internal.ch.qos.logback.classic.LoggerContext;
import zenv.slot.internal.ch.qos.logback.classic.joran.JoranConfigurator;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.internal.org.slf4j.LoggerFactory;
import zenv.slot.SlotAgentBootstrap;
import zenv.slot.utils.SystemUtils;

import java.io.File;
import java.lang.instrument.Instrumentation;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/22 18:08
 */
public class SlotLogUtils {
    private static LoggerContext LOGGER_CONTEXT;

    /**
     * 初始化日志，以不同的服务名对日志文件进行划分，例如：monitor 的日志名为 slot.monitor.yyyyMMdd.log
     * <p>
     * 初始化时机：在埋点配置文件加载之后就会来加载日志系统
     *
     * @param serviceName 服务名
     * @see SlotAgentBootstrap#premain(String, Instrumentation)
     */
    public static void initLogger(String serviceName) {
        System.setProperty("serviceName", serviceName);
        String logConf = SystemUtils.getSlotHome() + File.separator + "conf" + File.separator + "logback-slot.xml";
        AnsiLog.debug("slot log file : " + logConf);
        final File configFile = new File(logConf);
        if (!configFile.isFile()) {
            AnsiLog.error("can not find slot logging config: " + configFile);
        }
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();
            final JoranConfigurator joranConfigurator = new JoranConfigurator();
            joranConfigurator.setContext(loggerContext);
            joranConfigurator.doConfigure(configFile.toURI().toURL());

            LOGGER_CONTEXT = loggerContext;
        } catch (Throwable e) {
            AnsiLog.error("try to load slot logging config file error:" + configFile, e);
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        return LOGGER_CONTEXT.getLogger(clazz);
    }
}
