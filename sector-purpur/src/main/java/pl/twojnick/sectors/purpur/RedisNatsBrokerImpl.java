package pl.twojnick.sectors.purpur;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import pl.twojnick.sectors.core.config.SectorConfig;
import pl.twojnick.sectors.core.messaging.MessageBroker;
import java.util.function.Consumer;

public class RedisNatsBrokerImpl implements MessageBroker {
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;
    private StatefulRedisPubSubConnection<String, String> redisPubSub;
    private Connection natsConnection;
    private final SectorConfig config;

    public RedisNatsBrokerImpl(SectorConfig config) {
        this.config = config;
    }

    @Override
    public void connect() {
        String redisUri = "redis://" + (config.redisPassword.isEmpty() ? "" : config.redisPassword + "@")
                        + config.redisHost + ":" + config.redisPort;
        redisClient = RedisClient.create(redisUri);
        redisConnection = redisClient.connect();
        redisPubSub = redisClient.connectPubSub();

        try {
            natsConnection = Nats.connect(config.natsUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to NATS", e);
        }
    }

    @Override
    public void disconnect() {
        if (redisConnection != null) redisConnection.close();
        if (redisPubSub != null) redisPubSub.close();
        if (redisClient != null) redisClient.shutdown();
        if (natsConnection != null) {
            try {
                natsConnection.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public RedisCommands<String, String> getRedisCommands() {
        return redisConnection.sync();
    }

    @Override
    public void publishTelemetry(String subject, byte[] data) {
        natsConnection.publish(subject, data);
    }

    @Override
    public void subscribeTelemetry(String subject, Consumer<byte[]> handler) {
        Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
            handler.accept(msg.getData());
        });
        dispatcher.subscribe(subject);
    }

    @Override
    public void publishStateTransfer(String channel, String payload) {
        redisConnection.sync().publish(channel, payload);
    }

    @Override
    public void subscribeStateTransfer(String channel, Consumer<String> handler) {
        redisPubSub.addListener(new io.lettuce.core.pubsub.RedisPubSubAdapter<>() {
            @Override
            public void message(String ch, String msg) {
                if (ch.equals(channel)) handler.accept(msg);
            }
        });
        redisPubSub.sync().subscribe(channel);
    }
}
