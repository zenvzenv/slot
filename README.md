# Slot

业务埋点功能开源工程，本工程提供以下功能：

1. 埋点 agent，业务无痕埋点的核心，业务系统可以通过此 agent 可以对业务代码无侵入埋点从而可以感知到业务系统内部的运行情况

2. 埋点系统配套的 web 服务，web 服务会展示业务系统的埋点信息，从服务概览、详情、链路追踪和方法详情来展示业务系统内部的运行情况

## slot-repackage

`slot-agent` 所依赖的一些第三方工具包，包括 `disruptor`, `logback`, `asm` 工具，主要是为了防止 agent 依赖的工具包与业务系统的工具
包有冲突，从而 自己重新打包。具体参见 `slot-repackage` 的 
[README.md]()

## slot-agent

`slot-agent` 是业务无痕埋点的核心，用户可以不修改原有的业务代码对业务代码进行埋点监控，具体参见 `slot-agent` 的 
[README.md]()
