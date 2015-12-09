package io.muoncore.protocol.reactivestream.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.OperationDescriptor;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReactiveStreamServerStack implements ServerProtocolStack {

    public static String REACTIVE_STREAM_PROTOCOL = "reactive-stream";

    private PublisherLookup publisherLookup;
    private Codecs codecs;
    private AutoConfiguration configuration;

    public ReactiveStreamServerStack(
            PublisherLookup publisherLookup,
            Codecs codecs,
            AutoConfiguration configuration) {
        this.publisherLookup = publisherLookup;
        this.codecs = codecs;
        this.configuration = configuration;
    }

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {
        return new ReactiveStreamServerChannel(publisherLookup, codecs, configuration);
    }

    @Override
    public ProtocolDescriptor getProtocolDescriptor() {

        List<OperationDescriptor> ops = publisherLookup.getPublishers().stream().map(
                pub -> new OperationDescriptor(pub.getName(), "[" + pub.getPublisherType() + "]")
        ).collect(Collectors.toList());

        return new ProtocolDescriptor(REACTIVE_STREAM_PROTOCOL, "Reactive Streaming", "Provides the semantics of the Reactive Stream API over a muon event protocol",
                ops);
    }
}