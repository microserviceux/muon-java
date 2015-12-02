package io.muoncore.memory.transport;

import com.google.common.eventbus.EventBus;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DefaultInMemClientChannelConnection implements InMemClientChannelConnection {

    private EventBus eventBus;
    private ChannelFunction<TransportInboundMessage> inboundFunction;

    private String targetService;
    private String protocol;

    private CountDownLatch handshakeControl = new CountDownLatch(1);
    private ChannelConnection<TransportInboundMessage, TransportOutboundMessage> serverChannel;

    public DefaultInMemClientChannelConnection(String targetService, String protocol, EventBus eventBus) {
        this.eventBus = eventBus;
        this.protocol = protocol;
        this.targetService = targetService;
    }

    @Override
    public void receive(ChannelFunction<TransportInboundMessage> function) {
        this.inboundFunction = function;
        eventBus.post(new OpenChannelEvent(targetService, protocol, this));
    }

    @Override
    public void send(TransportOutboundMessage message) {
        try {
            handshakeControl.await(1, TimeUnit.SECONDS);
            if (serverChannel == null) {
                throw new MuonTransportFailureException("Server channel did not connect within the required timeout. This is a bug", new NullPointerException());
            }
            serverChannel.send(message.toInbound());
        } catch (InterruptedException e) {
            throw new MuonTransportFailureException("Unable to connect, no remote server attached to the channel within the timeout", e);
        }
    }

    @Override
    public void attachServerConnection(ChannelConnection<TransportInboundMessage, TransportOutboundMessage> serverChannel) {
        this.serverChannel = serverChannel;
        serverChannel.receive( msg -> {
            inboundFunction.apply(msg.toInbound());
        });
        handshakeControl.countDown();
    }
}
