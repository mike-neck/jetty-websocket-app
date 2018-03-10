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
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import reactor.core.publisher.EmitterProcessor;

@Slf4j
public class Creator implements WebSocketCreator {

    private static final Creator instance = new Creator();

    static Creator getInstance() {
        return instance;
    }

    private Creator() {}

    @Override
    public Object createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
        
        final Delivery delivery = new Delivery(EmitterProcessor.create());
        return new WebSocketConnection(delivery);
    }
}
