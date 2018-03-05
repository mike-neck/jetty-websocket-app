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

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Handler {

    private static final Handler instance = new Handler();

    private Handler() {}

    private final Map<WebSocketConnection, FluxSink<String>> connections = new HashMap<>();

    private final Map<WebSocketConnection, Flux<String>> publishers = new HashMap<>();

    Flux<String> newConnection(final WebSocketConnection connection) {
        return Flux.create(emitter -> {
            connections.put(connection, emitter);
            emitter.onDispose(() -> {
                connections.remove(connection);
                publishers.remove(connection);
            });
        });
    }

    void closeConnection(final WebSocketConnection connection) {
        final Optional<Flux<String>> toBeDisconnected = Optional.ofNullable(publishers.remove(connection));
        toBeDisconnected.ifPresent(flux -> flux.cancelOn(Schedulers.single()));
    }

    void sendMessage(final WebSocketConnection connection, final String message) {
        connections.keySet().stream()
                .filter(con -> !con.equals(connection))
                .map(connections::get)
                .forEach(sink -> sink.next(message));
    }

    private void broadCast(final String message) {
        Flux.fromIterable(connections.keySet())
                .subscribeOn(Schedulers.elastic())
                .subscribe(connection -> connection.send(message));
    }

    static void connect(final WebSocketConnection connection) {
        final Flux<String> flux = instance.newConnection(connection).map(
                message -> String.format("[%s] message: %s, address: %s", OffsetDateTime.now(), message, connection.remoteAddress));
        instance.publishers.put(connection, flux);
        flux.subscribe(instance::broadCast);
    }

    static void close(final WebSocketConnection connection) {
        instance.closeConnection(connection);
    }

    static void send(final WebSocketConnection connection, final String message) {
        instance.sendMessage(connection, message);
    }
}
