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

package com.aerospike.connect.kafka.inbound;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.connect.inbound.InboundMessageTransform;
import com.aerospike.connect.inbound.model.InboundMessage;
import com.aerospike.connect.inbound.operation.AerospikeDeleteOperation;
import com.aerospike.connect.inbound.operation.AerospikePutOperation;
import com.aerospike.connect.inbound.operation.AerospikeRecordOperation;
import org.apache.kafka.connect.sink.SinkRecord;

import static java.util.Collections.singletonList;

/**
 * Custom inbound transform example with kafka tombstone record.
 */
public final class KafkaTombstoneInboundMessageTransform
        implements InboundMessageTransform<InboundMessage<Object, SinkRecord>> {

    @Override
    public AerospikeRecordOperation transform(
            InboundMessage<Object, SinkRecord> inboundMessage) {
        // Kafka tombstone record has non-null key and null payload
        if (inboundMessage.getMessage().value() == null) {
            return new AerospikeDeleteOperation(
                    new Key("test", null, "jumbo_jet"), null);
        }
        return new AerospikePutOperation(new Key("test", null, "kevin"), null,
                singletonList(new Bin("name",
                        inboundMessage.getFields().get("name"))));
    }
}
