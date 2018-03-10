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
import org.eclipse.jetty.websocket.servlet.*;

import javax.servlet.annotation.WebServlet;
import java.time.LocalDateTime;

@WebServlet(urlPatterns = "ws")
@Slf4j
public class WebSocketAppServlet extends WebSocketServlet {

    private final LocalDateTime instantiated;

    public WebSocketAppServlet() {
        instantiated = LocalDateTime.now();
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        log.info("configuring instance: {}", this);
        log.info("configure application");
        factory.setCreator(Creator.getInstance());
    }

    @Override
    public String toString() {
        return "WebSocketAppServlet[" + instantiated.toString() + "]";
    }
}
