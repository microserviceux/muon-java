package io.muoncore.protocol.defaultproto

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportMessage
import io.muoncore.transport.TransportOutboundMessage
import spock.lang.Specification

class DefaultServerProtocolSpec extends Specification {

    def "default proto sends bounce messages and closes the connection"() {

        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }
        def proto = new DefaultServerProtocol(codecs)
        def channel = proto.createChannel()
        def receive = Mock(ChannelConnection.ChannelFunction)

        channel.receive(receive)

        when:
        channel.send(new TransportInboundMessage(
                "somethingHappened",
                "id",
                "targetService",
                "sourceServiceName",
                "fakeproto",
                [:],
                "text/plain",
                new byte[0], ["application/json"], TransportMessage.ChannelOperation.NORMAL))

        then:
        1 * receive.apply(_ as TransportOutboundMessage)
        1 * receive.apply(null)
    }
}