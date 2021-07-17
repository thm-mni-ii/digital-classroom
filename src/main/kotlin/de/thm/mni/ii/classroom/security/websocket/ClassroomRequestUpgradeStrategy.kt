package de.thm.mni.ii.classroom.security.websocket

import de.thm.mni.ii.classroom.security.classroom.JWTSecurity
import org.springframework.core.io.buffer.NettyDataBufferFactory
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator.getNativeResponse
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.netty.http.server.HttpServerResponse
import java.util.function.Supplier


class ClassroomRequestUpgradeStrategy(private val jwtSecurity: JWTSecurity): RequestUpgradeStrategy {
/*
    fun upgrade(
        exchange: ServerWebExchange,
        handler: WebSocketHandler,
        subProtocol: String?,
        handshakeInfoFactory: Supplier<HandshakeInfo>
    ): Mono<Void> {
        val response: ServerHttpResponse = exchange.response
        val reactorResponse: HttpServerResponse = getNativeResponse(response)
        val handshakeInfo: HandshakeInfo = handshakeInfoFactory.get()
        val bufferFactory = response.bufferFactory() as NettyDataBufferFactory
        val authentication = JWTSecurity.JWTAuthenticationConverter().convert(exchange).doOnNext {
            jwtSecurity.jwtAuthenticationManager().authenticate(it)
        }
        return if (authResult == unauthorised) Mono.just(reactorResponse.status(rejectedStatus))
            .flatMap<Void>(HttpServerResponse::send) else reactorResponse.sendWebsocket(
            subProtocol,  //
            this.maxFramePayloadLength
        )  //
        ReactorNettyRequestUpgradeStrategy
        { `in`, out ->
            val session = ReactorNettyWebSocketSession(
                `in`, out,
                handshakeInfo,
                bufferFactory,
                this.maxFramePayloadLength
            )
            handler.handle(session)
        }
    }
    */
}