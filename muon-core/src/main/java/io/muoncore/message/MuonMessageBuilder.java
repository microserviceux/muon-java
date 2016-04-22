package io.muoncore.message;

import java.util.UUID;

public class MuonMessageBuilder {

    private String id = UUID.randomUUID().toString();
    private long created = System.currentTimeMillis();

    private String targetServiceName;
    private String sourceServiceName;
    private String protocol;
    private String step;
    private MuonMessage.Status status = MuonMessage.Status.success;
    private byte[] payload;
    private String contentType;
    private MuonMessage.ChannelOperation channelOperation = MuonMessage.ChannelOperation.normal;

    public static MuonMessageBuilder fromService(String service) {
        MuonMessageBuilder builder = new MuonMessageBuilder();
        builder.sourceServiceName = service;
        return builder;
    }

    public MuonMessageBuilder toService(String targetService) {
        this.targetServiceName = targetService;
        return this;
    }

    public MuonMessageBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public MuonMessageBuilder step(String step) {
        this.step = step;
        return this;
    }

    public MuonMessageBuilder status(MuonMessage.Status status) {
        this.status = status;
        return this;
    }

    public MuonMessageBuilder payload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public MuonMessageBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public MuonMessageBuilder operation(MuonMessage.ChannelOperation channelOperation) {
        this.channelOperation = channelOperation;
        return this;
    }

    public MuonOutboundMessage build() {
        return new MuonOutboundMessage(
                id, created, targetServiceName, sourceServiceName, protocol, step, status, payload, contentType, channelOperation);
    }
    public MuonInboundMessage buildInbound() {
        return new MuonInboundMessage(
                id, created, targetServiceName, sourceServiceName, protocol, step, status, payload, contentType, channelOperation);
    }
}
