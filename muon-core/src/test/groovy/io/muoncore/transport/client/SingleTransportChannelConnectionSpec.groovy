package io.muoncore.transport.client
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.GsonCodec
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.transport.MuonTransport
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportMessage
import io.muoncore.transport.TransportOutboundMessage
import reactor.Environment
import spock.lang.Specification

class SingleTransportChannelConnectionSpec extends Specification {

    def "transport channel requires full connection before sending"() {

        Environment.initializeIfEmpty()

        def transport = Mock(MuonTransport)
        def dispatcher = Mock(TransportMessageDispatcher)

        def connection = new SingleTransportClientChannelConnection(transport, dispatcher, Environment.sharedDispatcher())

        when:
        connection.send(outbound("mymessage", "myService1", "requestresponse"))

        then:
        thrown(IllegalStateException)
    }

    def "transport channel to a service is opened for every new service/proto combo seen"() {

        Environment.initializeIfEmpty()

        def transport = Mock(MuonTransport)
        def dispatcher = Mock(TransportMessageDispatcher)

        def connection = new SingleTransportClientChannelConnection(transport, dispatcher, Environment.sharedDispatcher())
        connection.receive({})

        when:
        connection.send(outbound("mymessage", "myService1", "requestresponse"))
        connection.send(outbound("mymessage", "myService1", "requestresponse"))
        connection.send(outbound("mymessage", "myService2", "requestresponse"))
        connection.send(outbound("mymessage", "myService3", "requestresponse"))
        connection.send(outbound("mymessage", "myService1", "wibble"))
        connection.send(outbound("mymessage", "myService2", "simple"))

        and: "a message that is the same combo as previously seen on this channel"
        connection.send(outbound("mymessage", "myService2", "simple"))
        sleep(100)

        then:


        1 * transport.openClientChannel("myService1", "requestresponse") >> Stub(ChannelConnection)
        1 * transport.openClientChannel("myService2", "requestresponse") >> Stub(ChannelConnection)
        1 * transport.openClientChannel("myService3", "requestresponse") >> Stub(ChannelConnection)
        1 * transport.openClientChannel("myService1", "wibble") >> Stub(ChannelConnection)
        1 * transport.openClientChannel("myService2", "simple") >> Stub(ChannelConnection)

        0 * transport._
    }

    def "all inbound messages on the channels are pushed into the function for back propogation along the channel"() {

        Environment.initializeIfEmpty()

        def channelFunctions = []

        //capture the receive functions being generated and passed to our mock functions.
        //slightly elaborate, we are stepping two levels into the interaction mocks.
        def transport = Mock(MuonTransport) {
            openClientChannel(_, _) >> {
                def c = Mock(ChannelConnection) {
                    receive(_) >> { func ->
                        channelFunctions << new ChannelFunctionExecShimBecauseGroovyCantCallLambda(func[0])
                    }
                }
                return c
            }
        }

        def receive = Mock(ChannelConnection.ChannelFunction)
        def dispatcher = Mock(TransportMessageDispatcher)

        def connection = new SingleTransportClientChannelConnection(transport, dispatcher, Environment.sharedDispatcher())
        connection.receive(receive)

        when:
        connection.send(outbound("mymessage", "myService1", "requestresponse"))
        connection.send(outbound("mymessage", "myService2", "requestresponse"))
        connection.send(outbound("mymessage", "myService3", "requestresponse"))

        and: "Messages sent down all functions"
        sleep(100)
        channelFunctions[0].call(inbound("id", "source", "requestresponse"))
        channelFunctions[1].call(inbound("id", "source", "requestresponse"))
        channelFunctions[2].call(inbound("id", "source", "requestresponse"))
        channelFunctions[2].call(inbound("id", "source", "requestresponse"))
        channelFunctions[1].call(inbound("id", "source", "requestresponse"))
        channelFunctions[0].call(inbound("id", "source", "requestresponse"))
        channelFunctions[1].call(inbound("id", "source", "requestresponse"))
        sleep(100)

        then:
        channelFunctions.size() == 3
        7 * receive.apply(_)
    }

    def "shutdown is propagated and then shuts down the channel"() {

        Environment.initializeIfEmpty()

//        def channelFunctions = []
        def connections = [Mock(ChannelConnection)]

        //capture the receive functions being generated and passed to our mock functions.
        //slightly elaborate, we are stepping two levels into the interaction mocks.
        def transport = Mock(MuonTransport) {
            openClientChannel(_, _) >> {
                return connections[0]
            }
        }

        def receive = Mock(ChannelConnection.ChannelFunction)
        def dispatcher = Mock(TransportMessageDispatcher)

        def connection = new SingleTransportClientChannelConnection(transport, dispatcher, Environment.sharedDispatcher())
        connection.receive(receive)

        when:
        connection.send(outbound("mymessage", "myService1", "requestresponse"))
        connection.shutdown()

        and: "Messages sent down all functions"
        sleep(100)

        then:
        1 * connections[0].shutdown()
    }

    def inbound(id, service, protocol) {
        new TransportInboundMessage(
                "somethingHappened",
                id,
                service,
                "localservice",
                protocol,
                [:],
                "application/json",
                new GsonCodec().encode([:]), ["application/json"], TransportMessage.ChannelOperation.NORMAL)
    }

    def outbound(id, service, protocol) {
        new TransportOutboundMessage(
                "somethingHappened",
                id,
                service,
                "localservice",
                protocol,
                [:],
                "application/json",
                new GsonCodec().encode([:]), ["application/json"])
    }
}