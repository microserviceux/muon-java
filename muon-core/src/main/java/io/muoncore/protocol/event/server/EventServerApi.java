package io.muoncore.protocol.event.server;

import io.muoncore.ServerRegistrarSource;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.CodecsSource;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.event.Event;

public interface EventServerApi extends ServerRegistrarSource, CodecsSource {

    default void registerEventSink(ChannelConnection.ChannelFunction<Event> sinkEvent) {
        if (getProtocolStacks().getProtocolDescriptors().stream().filter(proto -> proto.getProtocolScheme().equals("event")).findFirst().isPresent()) {
            throw new MuonException("Attempted to register an Event handler more than once. This is an error");
        }
        getProtocolStacks().registerServerProtocol(new EventServerProtocolStack(
                sinkEvent, getCodecs()));
    }
}