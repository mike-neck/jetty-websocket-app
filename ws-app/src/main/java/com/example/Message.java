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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Value;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

interface Message {

    ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .registerModule(new JavaTimeModule().addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    String asText() throws IOException;

    boolean isAcceptable(NotificationCenter notificationCenter);
}

@Value
class SystemMessage implements Message {

    private final String text;

    @Override
    public String asText() throws IOException {
        return objectMapper.writeValueAsString(this);
    }

    @Override
    public boolean isAcceptable(final NotificationCenter notificationCenter) {
        return true;
    }
}

@Value
class UserMessage implements Message {

    private final InetSocketAddress from;
    private final String text;

    @Override
    public String asText() throws IOException {
        return objectMapper.writeValueAsString(this);
    }

    @Override
    public boolean isAcceptable(final NotificationCenter notificationCenter) {
        return notificationCenter.canSendTo(from);
    }
}
