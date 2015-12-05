package io.muoncore.extension.amqp;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AMQPMuonTransport implements MuonTransport {

    public final static String HEADER_PROTOCOL = "PROTOCOL";
    public final static String HEADER_REPLY_TO = "REPLY_TO";
    public final static String HEADER_RECEIVE_QUEUE = "LISTEN_ON";
    public final static String HEADER_SOURCE_SERVICE = "SOURCE_SERVICE";

    private Logger log = Logger.getLogger(AMQPMuonTransport.class.getName());
    private String rabbitUrl;
    private List<AmqpChannel> channels;
    private ServiceQueue serviceQueue;
    private AmqpChannelFactory channelFactory;

    public AMQPMuonTransport(
            String url,
            ServiceQueue serviceQueue,
            AmqpChannelFactory channelFactory) {
        channels = new ArrayList<>();
        this.channelFactory = channelFactory;
        this.rabbitUrl = url;
        this.serviceQueue = serviceQueue;

        log.info("Connecting to AMQP host at " + rabbitUrl);
    }

    @Override
    public void shutdown() {
        serviceQueue.shutdown();
        new ArrayList<>(channels).stream().forEach(AmqpChannel::shutdown);
    }

    @Override
    public ChannelConnection<TransportOutboundMessage, TransportInboundMessage> openClientChannel(String serviceName, String protocol) {
        AmqpChannel channel = channelFactory.createChannel();

        channel.onShutdown(msg -> {
            channels.remove(channel);
        });

        channel.initiateHandshake(serviceName, protocol);
        channels.add(channel);
        Channel<TransportOutboundMessage, TransportInboundMessage> intermediate = Channels.channel("AMQPChannel", "");

        Channels.connect(intermediate.right(), channel);

        return intermediate.left();
    }

    public void start(ServerStacks serverStacks) {
        serviceQueue.onHandshake( handshake -> {
            AmqpChannel channel = channelFactory.createChannel();
            channel.respondToHandshake(handshake);

            Channels.connect(channel,
            serverStacks.openServerChannel(handshake.getProtocol()));

            channels.add(channel);
        });
    }

    @Override
    public String getUrlScheme() {
        return "amqp";
    }

    @Override
    public URI getLocalConnectionURI() {
        try {
            return new URI(rabbitUrl);
        } catch (URISyntaxException e) {
            throw new MuonTransportFailureException("Invalid URI is provided: " + rabbitUrl, e);
        }
    }

    public int getNumberOfActiveChannels() {
        return channels.size();
    }
}
