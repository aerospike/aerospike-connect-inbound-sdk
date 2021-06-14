/*
 *
 *  Copyright 2012-2021 Aerospike, Inc.
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

import com.aerospike.client.Key;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.connect.inbound.operation.AerospikeSingleRecordOperation;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A message from an external system like Kafka, Pulsar, etc processed by a
 * Aerospike inbound connector.
 *
 * The message is processed by the Aerospike inbound connector as specified in
 * its config and converted to a InboundMessage.
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class InboundMessage<K, M> {
    /**
     * The key of the message. Is present only if the external system associates
     * a key with the message.
     * <p>
     * For example in Kafka it will be the value returned by
     * org.apache.kafka.connect.connector.SinkRecord#key method.
     * </p>
     */
    @Nullable
    private final K messageKey;

    /**
     * The un-parsed raw message from the external system.
     * <p>
     * For example in Kafka it will be an instance of
     * org.apache.kafka.connect.sink.SinkRecord.
     * </p>
     */
    private final M message;

    /**
     * Aerospike record key extracted from the message by the Aerospike inbound
     * connector. It will be present only if the Aerospike inbound connector
     * config specifies extracting an Aerospike key from the incoming message.
     */
    @Nullable
    private final Key key;

    /**
     * Aerospike write policy generated from the message by the Aerospike
     * inbound connector. It will be present only if the Aerospike inbound
     * connector config specifies extracting {@link WritePolicy} attributes
     * from the incoming message.
     */
    @Nullable
    private final WritePolicy writePolicy;

    /**
     * Fields extracted from the message as per the bins config specified
     * for the Aerospike inbound connector.
     */
    private final Map<String, Object> fields;

    /**
     * @see AerospikeSingleRecordOperation#getIgnoreErrorCodes()
     */
    @Nonnull
    private final Set<Integer> ignoreErrorCodes;

    /**
     * Return the key of the message. Is present only if the external system
     * associates a key with the message.
     *
     * <p>
     * For example in Kafka it will be the value returned by
     * org.apache.kafka.connect.connector.SinkRecord#key method.
     * </p>
     */
    public Optional<K> getMessageKey() {
        return Optional.ofNullable(messageKey);
    }

    /**
     * Return the Aerospike record key extracted from the message by the
     * Aerospike inbound connector. It will be present only if the Aerospike
     * inbound connector config specifies extracting an Aerospike key from the
     * incoming message.
     */
    public Optional<Key> getKey() {
        return Optional.ofNullable(key);
    }

    /**
     * Return the Aerospike write policy generated from the message by the
     * Aerospike inbound connector. It will be present only if the Aerospike
     * inbound connector config specifies extracting {@link WritePolicy}
     * attributes from the incoming message.
     */
    public Optional<WritePolicy> getWritePolicy() {
        return Optional.ofNullable(writePolicy);
    }
}
