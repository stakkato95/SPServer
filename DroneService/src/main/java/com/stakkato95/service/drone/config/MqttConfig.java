package com.stakkato95.service.drone.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.stakkato95.service.drone.domain.telemetry.TelemetryManager;
import com.stakkato95.service.drone.domain.telemetry.TelemetryRepository;
import com.stakkato95.service.drone.helper.Const;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;

@Configuration
public class MqttConfig {

    //    @Value("${mqtt.default-uri}")
    private static String MQTT_BROKER_URi = "tcp://localhost:1883";
    private static String SERVER_CONSUMER = "serverConsumer";

    private static final int QOS_EXACTLY_ONCE = 2;
    private static final int COMPLETION_TIMEOUT = 5000;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{MQTT_BROKER_URi});
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public IntegrationFlow mqttInFlow(MessageProducerSupport messageProducer, TelemetryManager manager) {
        return IntegrationFlows
                .from(messageProducer)
                .handle(manager::handleMqttMessage)
                .get();
    }

    @Bean
    public MessageProducerSupport mqttInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                SERVER_CONSUMER,
                mqttClientFactory(),
                Const.MQTT_TOPIC_GNSS, Const.MQTT_TOPIC_POSITION, Const.MQTT_TOPIC_ROTATION
        );
        adapter.setCompletionTimeout(COMPLETION_TIMEOUT);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(QOS_EXACTLY_ONCE);
        return adapter;
    }

    @Bean
    public TelemetryManager getTelemetryManager(ObjectMapper mapper, TelemetryRepository repo) {
        return new TelemetryManager(mapper, repo);
    }
}
