package de.thm.mni.ii.classroom.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import reactor.core.publisher.SignalType

class UserWebSocketController {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val clients: MutableList<RSocketRequester> = ArrayList()


    @ConnectMapping("/classroom")
    fun connectClient(requester: RSocketRequester, @Payload client: String) {
        requester.rsocket()!!
            .onClose()
            .doFirst {

                // Add all new clients to a client list
                logger.info("Client: {} CONNECTED.", client)
                clients.add(requester)
            }
            .doOnError { error: Throwable? ->
                // Warn when channels are closed by clients
                logger.warn("Channel to client {} CLOSED", client)
            }
            .doFinally { consumer: SignalType? ->
                // Remove disconnected clients from the client list
                clients.remove(requester)
                logger.info("Client {} DISCONNECTED", client)
            }
            .subscribe()

        // Callback to client, confirming connection
        requester.route("client-status")
            .data("OPEN")
            .retrieveFlux(String::class.java)
            .doOnNext { s: String? ->
                logger.info(
                    "Client: {} Free Memory: {}.",
                    client,
                    s
                )
            }
            .subscribe()
    }
}