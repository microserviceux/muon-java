package io.muoncore.protocol.requestresponse
import io.muoncore.protocol.requestresponse.server.DynamicRequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.HandlerPredicate
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler
import io.muoncore.protocol.requestresponse.server.RequestWrapper
import spock.lang.Specification

import java.util.function.Predicate

class DynamicRequestResponseHandlersSpec extends Specification {

    def "registry accepts handlers and choose the correct one based on predicate" () {


        given:
        def registry = new DynamicRequestResponseHandlers(new StubRequestResponseServerHandler(id:8, predicate: predicate(false)))
        registry.addHandler(new StubRequestResponseServerHandler(id:1, predicate: predicate(false)))
        registry.addHandler(new StubRequestResponseServerHandler(id:2, predicate: predicate(false)))
        registry.addHandler(new StubRequestResponseServerHandler(id:3, predicate: predicate(true)))
        registry.addHandler(new StubRequestResponseServerHandler(id:4, predicate: predicate(false)))

        expect:
        registry.findHandler(new RequestMetaData("hello", "sourceservice", "target")).id == 3

    }

    def "registry accepts handlers and gives the default if none found" () {

        given:
        def registry = new DynamicRequestResponseHandlers(new StubRequestResponseServerHandler(id:8, predicate: predicate(false)))
        registry.addHandler(new StubRequestResponseServerHandler(id:1, predicate: predicate(false)))
        registry.addHandler(new StubRequestResponseServerHandler(id:2, predicate: predicate(false)))

        expect:
        registry.findHandler(new RequestMetaData("hello","sourceservice", "target")).id == 8

    }

    def predicate(result) {
        new HandlerPredicate() {
            @Override
            String resourceString() {
                return ""
            }

            @Override
            Predicate<RequestMetaData> matcher() {
                return { result }
            }
        }
    }
}

class StubRequestResponseServerHandler implements RequestResponseServerHandler {
    int id
    HandlerPredicate predicate

    @Override
    void handle(RequestWrapper request) {
        throw new IllegalStateException("Not implemented in stub!")
    }

    @Override
    Class getRequestType() {
        return Object.class;
    }
}