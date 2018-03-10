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

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

@WebSocket
@Slf4j
public class WebSocketConnection {

    private Session session;
    InetSocketAddress remoteAddress;

    private final Delivery delivery;

    public WebSocketConnection(final Delivery delivery) {
        this.delivery = delivery;
    }

    Future<Void> send(final String message) {
        return session.getRemote().sendStringByFuture(message);
    }

    @OnWebSocketConnect
    public void onConnect(final Session session) {
        this.remoteAddress = session.getRemoteAddress();
        this.session = session;
        log.info("incoming request from: {}", remoteAddress);
        Handler.connect(this);
    }

    @OnWebSocketClose
    public void onClose(final Session session, final int closeCode, final String closeReason) {
        log.info("closing session. close code: {}, reason: {}", closeCode, closeReason);
        Handler.close(this);
    }

    @OnWebSocketMessage
    public void onMessage(final String message) {
        log.info("coming message: {}, from remote address: {}", message, remoteAddress);
        Handler.send(this, message);
    }

    public void close() {
        session.close();
    }
}
