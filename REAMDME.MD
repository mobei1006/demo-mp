#### 软件安装
首先我们需要安装prometheus 和grafana，可以从官网下载 zip 包，直接打开即可运行

- prometheus 运行：prometheus.exe
- grafana 运行：grafana-server.exe
#### 添加依赖
在我们 springboot 项目中添加如下依赖：
```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
</dependencies>
```
#### 配置文件
添加完依赖我们需要在配置文件中配置基本的内容：
```properties
# 运行端口
server.port=8088

# 配置为开启 Actuator 服务
management.endpoints.web.exposure.include=*
# demo的名称
spring.application.name=demo-mp
management.metrics.tags.application=${spring.application.name}
```
#### 测试 demo
首先我们在启动类上添加 JVM 的性能监控
```java
@SpringBootApplication
public class MPApplication {
    public static void main(String[] args) {
        SpringApplication.run(MPApplication.class, args);
    }
    
    /**
     * @author huding
     * @Date: 2024/5/22
     * @Description: 添加JVM性能指标信息
     **/
    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName){
        return registry -> registry.config().commonTags("application", applicationName);
    }
}
```
然后写一个测试的 controller
```java
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private MeterRegistry meterRegistry;
    private Counter counter;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("common", "test");
        // 公共标签
        meterRegistry.config().commonTags(tags);
        counter = Counter.builder("metrics.request.common").register(meterRegistry);
    }

    @GetMapping("/t1")
    public String t1() {
        counter.increment();
        return "ok";
    }


    @GetMapping("/t2")
    public String t2() {
        counter.increment();
        return "ok";
    }
}
```
最后启动我们的 SpringBoot 程序，然后打开[http://localhost:8088/actuator/prometheus](http://localhost:8088/actuator/prometheus)，端口号匹配上，我们可以看见程序的一些信息
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716357318895-ff919f0b-17d1-4254-b6d1-e7126da63262.png#averageHue=%23e7e7e7&clientId=ua0ca48f2-9bb1-4&from=paste&height=146&id=u78fdd775&originHeight=146&originWidth=864&originalType=binary&ratio=1&rotation=0&showTitle=false&size=11408&status=done&style=stroke&taskId=uc778fdb3-1d66-4243-b6ff-87ada55772f&title=&width=864)
到这里基本上测试用例基本上没问题了
#### 配置prometheus
我们要到prometheus 文件夹下，找到 prometheus.yml 的配置文件，我们需要让prometheus 监控我们的程序端口。添加配置如下：
```yaml
# my global config
global:
  scrape_interval: 15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# Alertmanager configuration
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          # - alertmanager:9093

# Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: "prometheus"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["localhost:9090"]

  - job_name: 'demo-mp'
    scrape_interval: 5s
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8088']
```
最后的一个 job 就是我们添加的。我们运行prometheus，然后打开 prometheus 的地址：[http://localhost:9090/](http://localhost:9090/)
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716357554834-130e1bda-0de1-41ac-8d35-f1a25eaa3241.png#averageHue=%23fefefe&clientId=ua0ca48f2-9bb1-4&from=paste&height=419&id=u3b6cd69f&originHeight=419&originWidth=1086&originalType=binary&ratio=1&rotation=0&showTitle=false&size=21835&status=done&style=stroke&taskId=u999a8baf-f905-4283-8bc2-146f598e90e&title=&width=1086)
我们可以从 Satuts--Targets 看到我们的实例：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716357597679-6e796926-dd03-4219-9b91-2cbf0aa9be53.png#averageHue=%23fcfcfb&clientId=ua0ca48f2-9bb1-4&from=paste&height=413&id=ube42ec72&originHeight=413&originWidth=1429&originalType=binary&ratio=1&rotation=0&showTitle=false&size=35944&status=done&style=stroke&taskId=u99723223-eadd-4f30-8eff-19afb9dceda&title=&width=1429)
有两个监控的节点，一个是prometheus 自己，还有一个是我们的 demo
#### grafana 展示数据
prometheus 监控的数据展示并不友好，我们需要将他导入到grafana 进行展示。首先要运行grafana 程序，然后进入地址：[http://localhost:3000/](http://localhost:3000/)，初始的用户名和密码都是 admin。
首先我们从数据源中配置数据来源
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716357774283-827dacc9-1553-4a97-927b-c01cda39fad2.png#averageHue=%231d222a&clientId=ua0ca48f2-9bb1-4&from=paste&height=113&id=u7742d927&originHeight=113&originWidth=307&originalType=binary&ratio=1&rotation=0&showTitle=false&size=7307&status=done&style=none&taskId=u78cf6429-ee98-4f0c-9b76-db7a9e1caf0&title=&width=307)
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716357791768-35c0c049-6c27-443c-abde-6e6bfdd1a385.png#averageHue=%23191c21&clientId=ua0ca48f2-9bb1-4&from=paste&height=426&id=uc1080994&originHeight=426&originWidth=724&originalType=binary&ratio=1&rotation=0&showTitle=false&size=30746&status=done&style=none&taskId=u5b86d63a-b4f1-434b-9ca4-340817f513d&title=&width=724)
然后 save 保存即可，保存也会测试是否可以正常连接。
然后就是导入具体的数据大屏展示，这里导入比较火的 JVM (Micrometer)，ID 号为 4071.
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716357876940-167c2bb1-646d-4256-b366-0a222c3158ee.png#averageHue=%23181b21&clientId=ua0ca48f2-9bb1-4&from=paste&height=640&id=ue90ff780&originHeight=640&originWidth=664&originalType=binary&ratio=1&rotation=0&showTitle=false&size=49715&status=done&style=none&taskId=u32d52f29-315d-416a-bb20-d5fcee8a07b&title=&width=664)
Load 即可，然后我们看到如下的基本信息：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716357925814-a73a46eb-ef5f-4795-968f-14e1449f8103.png#averageHue=%23181b1f&clientId=ua0ca48f2-9bb1-4&from=paste&height=811&id=u400d9d24&originHeight=811&originWidth=1597&originalType=binary&ratio=1&rotation=0&showTitle=false&size=96916&status=done&style=none&taskId=u872dec7a-0589-44a9-8984-7d8cc7526c6&title=&width=1597)
#### 添加自己的查询监控
我们可以添加在测试 demo 中自己的数据，我们在上 add 
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716358289619-05c7cb3e-26c9-4a56-856d-ed09a6a7bf07.png#averageHue=%231a1d24&clientId=ua0ca48f2-9bb1-4&from=paste&height=206&id=ub3c4a36e&originHeight=206&originWidth=414&originalType=binary&ratio=1&rotation=0&showTitle=false&size=15783&status=done&style=none&taskId=uc388d32c-5971-45c2-950c-d96e9c5b384&title=&width=414)
然后输入 query，可以看到我们的数据信息，最后 apply 即可
![image.png](https://cdn.nlark.com/yuque/0/2024/png/40783336/1716358330728-4b67e388-a6d4-4865-9338-7c325154b5a2.png#averageHue=%231a1d22&clientId=ua0ca48f2-9bb1-4&from=paste&height=776&id=uff56bdc8&originHeight=776&originWidth=1213&originalType=binary&ratio=1&rotation=0&showTitle=false&size=83991&status=done&style=none&taskId=u5ec68e65-d048-4853-aaac-1b6c55c180d&title=&width=1213)

