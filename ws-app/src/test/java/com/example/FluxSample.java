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

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

@Slf4j
class FluxSample {

    @DisplayName("サンプルケース")
    @Test
    void sampleCase() throws InterruptedException {
        Loggers.useSl4jLoggers();

        final Connection leftConnection = new Connection(0, "left");
        final Connection rightConnection = new Connection(2, "right");

        final EmitterProcessor<Message> leftFlux = EmitterProcessor.create();
        final EmitterProcessor<Message> rightFlux = EmitterProcessor.create();

        final Flux<Message> center = Flux.merge(leftFlux, rightFlux);

        center.subscribe(message -> log.info("[center] stream   -   center:            {}", message.text));
        center.subscribe(leftConnection::receiveMessage);
        center.subscribe(rightConnection::receiveMessage);

        final CountDownLatch latch = new CountDownLatch(2);

        Flux.defer(() -> Flux.interval(Duration.ofMillis(200L), Duration.ofMillis(500L)))
                .map(l -> String.format("[left: %d]", l))
                .map(leftConnection::newMessage)
                .take(16)
                .doOnTerminate(latch::countDown)
                .subscribe(leftFlux::onNext);

        Flux.defer(() -> Flux.interval(Duration.ofMillis(300L), Duration.ofMillis(400L)))
                .map(r -> String.format("[right: %d]", r))
                .map(rightConnection::newMessage)
                .take(20)
                .doOnTerminate(latch::countDown)
                .subscribe(rightFlux::onNext);

        latch.await();
    }

    @Value
    private static class Message {
        private final int id;
        private final String name;
        private final String text;
    }

    @Value
    private static class Connection {
        private final int id;
        private final String name;

        Message newMessage(final String text) {
            return new Message(id, name, text);
        }

        void receiveMessage(final Message message) {
            if (this.id == message.id) {
                return;
            }

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < id; i++) {
                IntStream.range(0, 12)
                        .mapToObj(n -> ' ')
                        .forEach(sb::append);
            }
            log.info("[{}] receive from:{}:{}{}",
                    String.format("%6s", this.name),
                    String.format("%6s", message.name),
                    sb.toString(),
                    message.text);
        }
    }
}
