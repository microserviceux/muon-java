package io.muoncore.extension.amqp.externalbroker

import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.ServerStacks
import io.muoncore.protocol.requestresponse.RRPTransformers
import reactor.Environment
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf({ System.getenv("BUILD_NUMBER") })
class EstablishChannelSpec extends Specification {

    def serverStacks1 = Mock(ServerStacks)
    def serverStacks2 = Mock(ServerStacks)
    def codecs = new JsonOnlyCodecs()

    def discovery = Mock(Discovery) {
        findService(_) >> Optional.of(new ServiceDescriptor("", [], [], []))
    }

    def "two transports can establish an AMQP channel between them"() {

        Environment.initializeIfEmpty()

        AMQPMuonTransport svc1 = createTransport("service1")
        AMQPMuonTransport svc2 = createTransport("tombola")

        svc2.start(discovery, serverStacks2, codecs)
        svc1.start(discovery, serverStacks1, codecs)

        when:
        def channel = svc1.openClientChannel("tombola", "requestresponse")
        channel.receive {
            println "Received a message ... "
        }

        channel.send(
                MuonMessageBuilder
                        .fromService("service1")
                        .step("somethingHappened")
                        .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                        .toService("tombola")
                        .payload([] as byte[])
                        .contentType("application/json")
                        .build())
        sleep(50)

        then:
        1 * serverStacks2.openServerChannel("requestresponse") >> Mock(ChannelConnection)

        cleanup:
        svc1.shutdown()
        svc2.shutdown()
    }

    private AMQPMuonTransport createTransport(serviceName) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory)
        svc1
    }
}
