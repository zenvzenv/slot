# Slot-agent

## 简介

业务无侵入埋点解决方案。监控**单个** Java 应用程序内部的运行情况，监控的情况包含如下信息：

1. 一次调用的开始时间、结束时间和耗时情况

2. 一次调用的方法链路，一次业务调用对应一个 traceId(traceId 全局唯一)，一次调用可能会包含多个方法的调用，每个方法对应一个 spanId，
   各个方法之间通过 parentId 来进行关联，各个 span 之间形成有向无环图(DAG)，如果存在递归方法的情况，那么每次调用到递归方法的 spanId
   也是不同的。

3. 一次调用是否成功，如果失败则会记录相关的异常信息(异常类型、异常信息、异常调用栈和方法实际入参)，需要特别注意的是一次调用判定为失败的
   条件，即如果一次调用链中某个方法发生了异常并且没有进行捕获，并且此方法的上层方法也没有进行捕获直到最上层调用异常被抛出，那么此异常将会
   被埋点系统进行捕获并记录相关信息，此次调用标记为失败。

4. 记录服务的主机名信息，如果服务存在集群的情况可以区分是哪台节点的服务产生的埋点，需要注意的是：此版本并不能跨进程和节点记录埋点记录，
   记录主机名仅仅是为了区分信息的来源。

5. 记录被调用方法的类名、方法名和方法签名

### 适用范围

1. **单机** Java 应用或**单机** Java web 服务

2. 如果服务是集群部署，集群内各个节点上的信息是能够获取到，但埋点信息无法串联集群内部的调用关系

3. 非 Java 语言的应用服务无法使用，理论上只要是运行在 JVM 之上的应用都可以使用，但只是测试了 Java，其余语言没有进行测试。

## 构建源码

### 前置准备

* Java SE Development Kits - 1.8 或更高版本
* apache-maven-3.8.1 或更高版本
* Git / Svn

### 使用 IDE 构建

如果你使用的的是 IDE(IDEA,VS,Eclipse...) 打开本项目，那么你可以使用 IDE 的 maven 构建工具来构建源码。

1. 需要先构建 `slot-repackage` 工程，因为 slot-agent 项目中使用了 `logback` 和 `disruptor` 第三方组件，为了避免和业务系统中依赖的
   组件产生冲突， `slot-agent` 项目将这两个依赖进行重新打包处理，在原有的依赖路径上添加 `zenv.slot.internal` 前缀。

2. 当 `slot-repackage` 构建完毕之后，maven 的本地仓库会创建 `${MAVEN_REPO_HOME}/zenv/` 文件夹，文件夹下会有
   `slot-repackage-disruptor` 和 `slot-repackage-logger` 两个刚刚打包的依赖组件
   > ${MAVEN_REPO_HOME} 是 maven 配置文件中的 localRepository 配置项的值

3. 当 `slot-repackage` 工程打包完毕之后，我们可以打包 `slot-agent` 工程。执行 IDE 的 maven 构建工具即可完成构建。

构建完成之后会在 `slot-agent/target` 目录中会产生 slot-agent-release.tar.gz 文件，此归档文件包含埋点运行的所有必要文件和依赖，
请不要擅自移动。

### 命令行构建

```bash
git clone https://github.com/zenvzenv/slot.git
cd slot/slot-repackage
mvn clean package -DskipTests=true
cd ../slot-agent
mvn clean package -DskipTests=true
```
构建完成后你可以在 `slot-agent/target` 目录下看到 `slot-agent-release.tar.gz` 文件。

## 如何使用

### 不通过大数据基础平台安装使用

1. 手动生成埋点配置文件(暂没有提供配置文件生成自动化工具，如果需要的话可以 issue 给我)，埋点配置文件形如

   ```properties
   # 埋点模式，all 代表无差别全监听，special 表示指定监听
   # 如果 slot.mode 是 all，那么以 slot.class.,slot.package. 开头的项将无效
   slot.mode=ALL/SPECIAL
   # 服务名
   slot.service=test
   # 方法级别
   slot.class.zenv.slot.class1=method1@(II)I;method2
   # 类级别
   slot.class.zenv.slot.class2=*
   # 包级别
   slot.package.zenv.slot=package1,packag2
   slot.package.zenv.slot1=*
   ```

    * 方法级别
        * key:以 `slot.class.` 开头，后面跟上所要埋点 method 所在的 class 的全限定名
        * value:所要埋点 method 的方法名与方法描述符，以 ',' 分割

    * 类级别
        * key:以 `slot.class.` 开头，后面跟上所要埋点 class 的权限的名
        * value:固定为 *，表示该 class 下所有方法都会被埋点

    * 包级别
        * key:以 `slot.package.` 开头，后面跟上包的层级结构
        * value:可以是当前包层级的下的包名，以 ',' 分割。可以是 '*' 即当前包层级下的所有 package 和 class

   手动编写埋点配置文件时，需要按以上格式来编写，否则会导致埋点内容与预期产生偏差，最终产生的文件形如:
   ```properties
      # 埋点模式
   slot.mode=SPECIAL
   # 埋点服务
   slot.service=test
   # 方法级别
   slot.class.zenv.service.impl.SlotManageServiceImpl=updateRetentionPolicy@(Ljava/lang/String;ILjava/lang/String;)Z
   slot.class.zenv.controller.JarParseController=generateSpecialModeProperties@(Lzenv/monitor/request/SlotGeneratePropertiesRequest;)Lzenv/monitor/response/Result;
   slot.class.zenv.service.impl.AlarmHistoryInfoServiceImpl=pageByCondition@(Lzenv/monitor/vo/AlarmInfoRequest;)Lcom/baomidou/mybatisplus/extension/plugins/pagination/Page;
   slot.class.zenv.controller.AgentLogTemplateController=changeStatus@(Lzenv/monitor/entity/ChangeTemplateStatus;)Lzenv/monitor/response/Result;
   slot.class.zenv.service.AlarmStrategyRuleService=deleteAlarmStrategyRuleByStrategyId@(Ljava/lang/Long;)V
   slot.class.zenv.service.impl.HomePageServiceImpl=health@()Ljava/util/List;,totalStatistics@()Ljava/util/Map;
   slot.class.zenv.controller.TracingController=loadSelectedStruct@(Lzenv/monitor/request/SlotLoadJarStructRequest;)Lzenv/monitor/response/Result;
   ```

2. 配置好埋点配置文件之后，将埋点配置文件上传到服务器中，记录下配置文件的绝对路径

3. 修改服务的启动命令，在原来启动命令中的所有 `-javaagent` 之后以及 `-jar` 之前添加
   `-javaagent:/absolute/path/to/slot-agent.jar=/absoult/path/to/your/properties/xxx.properties`，
   需要确保埋点相关的 `-javaagent` **有且只有一个**。**需要确保业务服务所在的机器上已经安装了埋点客户端并与埋点服务端所在机器网络能够联通，
   否则会导致埋点数据无法上报的情况**

4. 重启业务服务以生效埋点服务

## 数据查看

埋点的默认输出路径为 `/tmp/slot/data` 目录下。

### trace 数据

文件名为 `[service_name]#[hostname]#slot#trace#[yyyyMMddHHmm].csv.temp`，trace 文件中只记录 parentId 为 0 的方法记录。
相关参数说明如下：

```text
service_name:即注册在埋点配置中的名称
hostname:即业务所运行的机器主机名
yyyyMMddHHmm:数据产生的时间，以5分钟为一个周期
trace:代表这是 trace 数据
```

用户可以按 `service_name` 来找到自己的埋点输出文件。如果已经安装了监控服务端，那么用户不必关心埋点数据文件，如果没有安装监控服务端，那么需要
自己手动解析埋点数据。

trace 埋点数据文件以 csv 的形式提供，以 `"\1"` 进行分割，各个字段说明如下所示：

|     名称      |    说明     |                      示例                       |
|:-----------:|:---------:|:---------------------------------------------:|
|   traceId   | 本次调用的全局id |       5905abc8f82444558dd7c3d56a094785        |
| serviceName |    服务名    |                 monitor_agent                 |
|  hostname   |    主机名    |                    node58                     |
|  className  |    类名     |    zenv/monitor/agent/scheduler/ManageJob     |
| methodName  |    方法名    |              getSyncCollectTask               |
| methodDesc  |   方法描述符   | ()Lzenv/monitor/agent/entity/SyncCollectTask; |
|   success   | 方法是否调用成功  |                  true/false                   |
|  startDate  |  方法开始时间   |            2022-10-18T16:40:00.192            |
|   endDate   |  方法结束时间   |            2022-10-18T16:40:00.192            |
|  duration   | 方法耗时（ms）  |                       0                       |

### span 数据

文件名为 `[service_name]#[hostname]#slot#span#[yyyyMMddHHmm].csv.temp`，span 文件记录全量方法记录包含 trace 数据。相关参数说明如下：

```text
service_name:即注册在埋点配置中的名称
hostname:即业务所运行的机器主机名
yyyyMMddHHmm:数据产生的时间，以5分钟为一个周期
span:代表这是 span 数据
```

用户可以按 `service_name` 来找到自己的埋点输出文件。如果已经安装了监控服务端，那么用户不必关心埋点数据文件，如果没有安装监控服务端，那么需要
自己手动解析埋点数据。

span 埋点数据文件以 csv 的形式提供，以 `"\1"` 进行分割，各个字段说明如下所示：

|        名称         |                说明                |                      示例                       |
|:-----------------:|:--------------------------------:|:---------------------------------------------:|
|      traceId      |            本次调用的全局id             |       5905abc8f82444558dd7c3d56a094785        |
|      spanId       |           调用链中各个方法的id            |       a81fd49c7b8c4121a9137726225e9437        |
|     parentId      |             此方法的父Id              |       2795885e54524d549f4e339fc2765def        |
|    serviceName    |               服务名                |                 monitor_agent                 |
|     hostname      |               主机名                |                    node58                     |
|     className     |                类名                |    zenv/monitor/agent/scheduler/ManageJob     |
|    methodName     |               方法名                |              getSyncCollectTask               |
|    methodDesc     |              方法描述符               | ()Lzenv/monitor/agent/entity/SyncCollectTask; |
|      success      |             方法是否调用成功             |                  true/false                   |
|     startDate     |              方法开始时间              |            2022-10-18T16:40:00.192            |
|      endDate      |              方法结束时间              |            2022-10-18T16:40:00.192            |
|     duration      |             方法耗时（ms）             |                       0                       |
|     exception     |      异常类型，只要进入catch块就会进行记录       |         java.io.FileNotFoundException         |
|   exceptionMsg    |      异常信息，只要进入catch块就会进行记录       |      /xxx/es_test.properties(没有那个文件或目录)       |
|  exceptionStack   | 异常调用栈，只要进入catch块就会进行记录，’,’分割异常信息 |          method1,method2,method3,...          |
| methodParamsValue |          方法实际入参，’,’分割参数          |                     a,b,c                     |

## 日志查看

日志配置文件默认路径为 `/absolute/path/to/slot-agent/conf/logback-slot.xml`，默认的日志保留策略为1天，用户可根据实际情况
修改日志保留的时长。埋点 agent 会尽可能少的 记录日志输出以确保占用较少的磁盘空间， 如果相关更为详细的日志输出，可以修改配置文件中的日志输出级别，
可以将 `INFO` 改为 `DEBUG`。

日志默认的输出路径为 `/tmp/slot/logs`，埋点 agent 会以服务名做区分，格式为 `slot.[service_name].yyyyMMdd.log`,
`service_name` 取决于配置文件中的 `slot.service` 配置项的值。

## 注意事项

### 实体类的构造函数与静态构造函数处理

埋点 agent 会忽略所有的实例构造器( `<init>` )方法的埋点和静态构造器( `<clinit>` )的埋点。即使在埋点文件中配置了相关方法配置，此配置也不会
生效

### 实体类的 Getter/Setter 处理

埋点 agent 会将每个 class 中的属性字段的所有 Setter/Getter 方法忽视，不论该字段是 static,volatile 还是属于实例字段，都会忽略，例如：

```java
class Demo {
    private static String a;
    private String b;
    private static volatile String c;
    private final String D = "d";
}
```

上述类中的 a,b,c,D 字段都的 Setter/Getter 方法都会被忽略。

### 对于 Object 类中方法的处理

埋点 agent 会忽略 class 中的 `toStrig`, `hashCode`, `equals`, `canEquals` 方法的埋点。
