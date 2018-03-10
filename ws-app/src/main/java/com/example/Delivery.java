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
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.util.Loggers;

@Slf4j
public class Delivery {

    private final FluxProcessor<String, String> delivery;

    public Delivery(final FluxProcessor<String, String> delivery) {
        this.delivery = delivery;
    }

    Flux<String> publisher() {
        return this.delivery;
    }

    Subscriber<String> subscriber() {
        return this.delivery;
    }

    public static void main(String[] args) throws InterruptedException {
        Loggers.useSl4jLoggers();
    }

}
