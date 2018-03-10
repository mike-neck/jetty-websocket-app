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
import reactor.core.publisher.EmitterProcessor;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
public class ConnectionManager {

    private final Map<InetSocketAddress, WebSocketConnection> connections = new HashMap<>();

    private synchronized void refreshMap() {
        final Set<InetSocketAddress> removeKeys = connections.entrySet().stream()
                .filter(entry -> !entry.getValue().isConnectionLive())
                .map(Map.Entry::getKey)
                .collect(toSet());
        removeKeys.forEach(connections::remove);
    }

    synchronized void leave(final InetSocketAddress address) {
        connections.remove(address);
        log.info("current connection: {}", connections.size());
    }

    private Collection<WebSocketConnection> connections() {
        refreshMap();
        final Collection<WebSocketConnection> connections = this.connections.values();
        return Collections.unmodifiableCollection(connections);
    }

    private Collection<WebSocketConnection> receivableConnections(final Message message) {
        return connections().stream()
                .filter(connection -> connection.canTransfer(message))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    synchronized WebSocketConnection newConnection(final InetSocketAddress address) {
        log.info("current connection: {}", connections.size() + 1);
        final EmitterProcessor<Message> subscriber = EmitterProcessor.create();
        subscriber.subscribe(message -> receivableConnections(message).forEach(connection -> connection.send(message)));
        return connections.computeIfAbsent(address, a -> new WebSocketConnection(subscriber, this));
    }
}
