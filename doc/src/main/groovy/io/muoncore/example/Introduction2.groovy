package io.muoncore.example
import io.muoncore.spring.annotations.EnableMuon
import io.muoncore.spring.annotations.MuonController
import io.muoncore.spring.annotations.MuonRequestListener
import org.springframework.boot.autoconfigure.SpringBootApplication

@Grapes([
        @Grab('io.muoncore:muon-transport-amqp:6.4-SNAPSHOT'),
        @Grab('io.muoncore:muon-discovery-amqp:6.4-SNAPSHOT'),
        @Grab('io.muoncore:muon-spring:6.4-SNAPSHOT')])
@SpringBootApplication
@MuonController                        //(1) Enable Muon and set up
@EnableMuon(serviceName = "gateway")   //(2)
class Introduction2 {

    @MuonRequestListener(path = "/")   //(3) An RPC Endpoint
    def myRpcEndpoint(Map data) {
        return data
    }
}


