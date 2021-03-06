package io.muoncore.message;

public class MuonOutboundMessage extends MuonMessage {

    public MuonOutboundMessage(String id, long created, String targetServiceName, String targetInstance, String sourceServiceName, String protocol, String step, Status status, byte[] payload, String contentType, ChannelOperation channelOperation) {
        super(id, created, targetServiceName, targetInstance, sourceServiceName, protocol, step, status, payload, contentType, channelOperation);
    }

    public MuonInboundMessage toInbound() {
        return new MuonInboundMessage(
                getId(), getCreated(), getSourceServiceName(), null, getTargetServiceName(),
          getProtocol(), getStep(), getStatus(), getPayload(), getContentType(), getChannelOperation());
    }
}
