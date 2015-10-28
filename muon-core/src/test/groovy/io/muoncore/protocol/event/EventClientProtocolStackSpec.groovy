package io.muoncore.protocol.event
import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.event.client.EventClientProtocolStack
import io.muoncore.protocol.requestresponse.Response
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventClientProtocolStackSpec extends Specification {

    def "Stack converts events to transport messages"() {

        def capturedFunction
        def config = new AutoConfiguration(serviceName: "tombola")
        def discovery = Mock(Discovery) {
            findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], [], []))
        }

        def clientChannel = Mock(ChannelConnection) {
            receive(_) >> { func ->
                capturedFunction = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(func[0])
            }
        }

        def transportClient = Mock(TransportClient) {
            openClientChannel() >> clientChannel
        }

        def eventProto = new EventClientProtocolStack() {
            @Override
            TransportClient getTransportClient() {
                return transportClient
            }

            @Override
            Discovery getDiscovery() {
                discovery
            }

            @Override
            Codecs getCodecs() {
                return new JsonOnlyCodecs()
            }

            @Override
            AutoConfiguration getConfiguration() {
                return config
            }
        }

        when:
        def future = eventProto.event(new Event("simples", "myParent", "myService", []))

        and: "A response comes back from the remote"
        Thread.start {
            Thread.sleep(100)
            capturedFunction(new TransportInboundMessage(
                    "response",
                    "id",
                    "targetService",
                    "sourceServiceName",
                    "fakeproto",
                    ["status":"200"],
                    "text/plain",
                    new byte[0]))
        }

        sleep(50)

        then:
        capturedFunction != null
        1 * clientChannel.send(_)
        new PollingConditions().eventually {
            future.get() instanceof Response
        }
    }

    def "Stack sends all with the event protocol set"() {

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.of(new ServiceDescriptor("tombola", [], [], []))
        }
        def config = new AutoConfiguration(serviceName: "tombola")

        def clientChannel = Mock(ChannelConnection)

        def transportClient = Mock(TransportClient) {
            openClientChannel() >> clientChannel
        }

        def eventProto = new EventClientProtocolStack() {
            @Override
            TransportClient getTransportClient() {
                return transportClient
            }

            @Override
            Discovery getDiscovery() {
                discovery
            }

            @Override
            Codecs getCodecs() {
                return new JsonOnlyCodecs()
            }

            @Override
            AutoConfiguration getConfiguration() {
                return config
            }
        }

        when:
        eventProto.event(new Event("simples", "myParent", "myService", []))
        sleep(50)

        then:
        1 * clientChannel.send({ it.protocol == "event" })
    }

    def "Sends a 404 response if no eventstore service found"() {

        def discovery = Mock(Discovery) {
            findService(_) >> Optional.empty()
        }
        def config = new AutoConfiguration(serviceName: "tombola")

        def clientChannel = Mock(ChannelConnection)

        def transportClient = Mock(TransportClient) {
            openClientChannel() >> clientChannel
        }

        def eventProto = new EventClientProtocolStack() {
            @Override
            TransportClient getTransportClient() {
                return transportClient
            }

            @Override
            Discovery getDiscovery() {
                discovery
            }

            @Override
            Codecs getCodecs() {
                return new JsonOnlyCodecs()
            }

            @Override
            AutoConfiguration getConfiguration() {
                return config
            }
        }

        when:
        def response = eventProto.event(new Event("simples", "myParent", "myService", [])).get()

        then:
        response
        response.status == 404
    }
}
