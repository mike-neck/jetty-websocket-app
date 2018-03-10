/*
 * Copyright 2018 Shinya Mochida
 *
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@WebSocket
@Slf4j
public class WebSocketConnection implements NotificationCenter {

    private final ConnectionManager manager;

    private Session session;
    private InetSocketAddress remoteAddress;

    private final Subscriber<Message> subscriber;

    WebSocketConnection(final Subscriber<Message> subscriber, final ConnectionManager manager) {
        this.subscriber = subscriber;
        this.manager = manager;
    }

    @Override
    public void send(final String message) {
        Mono.justOrEmpty(session)
                .filter(Session::isOpen)
                .map(Session::getRemote)
                .map(remote -> remote.sendStringByFuture(message))
                .map(WebSocketConnection::toCompletableFuture)
                .flatMap(Mono::fromFuture)
                .subscribe();
    }

    @Override
    public boolean canSendTo(final InetSocketAddress address) {
        return !remoteAddress.equals(address) && isConnectionLive();
    }

    private static <T> CompletableFuture<T> toCompletableFuture(final Future<? extends T> future) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @OnWebSocketConnect
    public void onConnect(final Session session) {
        this.remoteAddress = session.getRemoteAddress();
        this.session = session;
        log.info("new connection: {}", this.remoteAddress);
        subscriber.onNext(new SystemMessage(String.format("user enter: %s", remoteAddress)));
    }

    @OnWebSocketClose
    public void onClose(final Session session, final int closeCode, final String closeReason) {
        log.info("closing session. close code: {}, reason: {}", closeCode, closeReason);
        subscriber.onNext(new SystemMessage(String.format("user leaving: %s", remoteAddress)));
        manager.leave(remoteAddress);
    }

    @OnWebSocketMessage
    public void onMessage(final String message) {
        log.info("coming message: {}, from remote address: {}", message, remoteAddress);
        subscriber.onNext(new UserMessage(remoteAddress, message));
    }

    public boolean isConnectionLive() {
        return Optional.ofNullable(session).filter(Session::isOpen).isPresent();
    }
}
