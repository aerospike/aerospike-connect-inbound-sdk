/*
 *
 *  Copyright 2012-2020 Aerospike, Inc.
 *
 *  Portions may be licensed to Aerospike, Inc. under one or more contributor
 *  license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package com.aerospike.connect.inbound.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Incoming message from the external system like Kafka, Pulsar via Aerospike
 * com.aerospike.connect.inbound connector
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class InboundMessage<K, M> {
    /**
     * Record key of the external system can be null if there is no key for the message.
     */
    @Nullable
    private final K key;

    /**
     * Un-parsed raw message from the external system.
     */
    private final M message;

    /**
     * Mapping of the field name to it's value of the message coming from the
     * external system.
     */
    private final Map<String, Object> fields;
}
