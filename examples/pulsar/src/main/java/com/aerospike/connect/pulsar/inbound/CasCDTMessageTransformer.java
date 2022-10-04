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

package com.aerospike.connect.pulsar.inbound;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.cdt.ListOperation;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.connect.inbound.AerospikeReader;
import com.aerospike.connect.inbound.InboundMessageTransformer;
import com.aerospike.connect.inbound.model.InboundMessage;
import com.aerospike.connect.inbound.model.InboundMessageTransformerConfig;
import com.aerospike.connect.inbound.operation.AerospikeOperateOperation;
import com.aerospike.connect.inbound.operation.AerospikePutOperation;
import com.aerospike.connect.inbound.operation.AerospikeRecordOperation;
import com.aerospike.connect.inbound.operation.AerospikeSkipRecordOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An example message transformer that appends incoming CDR (Call Detail Record)
 * to existing record in case it exists. If the list becomes too large it
 * is trimmed.
 */
@Singleton
public class CasCDTMessageTransformer implements
        InboundMessageTransformer<InboundMessage<Object, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(CasCDTMessageTransformer.class.getName());

    /**
     * Injected aerospike reader to read records from Aerospike.
     */
    private final AerospikeReader aerospikeReader;
    /**
     * Inbound message transformer config for the topic against which this class is bound.
     */
    private final InboundMessageTransformerConfig inboundMessageTransformerConfig;

    @Inject
    public CasCDTMessageTransformer(AerospikeReader aerospikeReader,
                                    InboundMessageTransformerConfig inboundMessageTransformerConfig) {
        this.aerospikeReader = aerospikeReader;
        this.inboundMessageTransformerConfig = inboundMessageTransformerConfig;
    }

    @Override
    public AerospikeRecordOperation transform(
            InboundMessage<Object, Object> inboundMessage) {

        Map<String, Object> input = inboundMessage.getFields();

        // Get the Aerospike key. Name field was sent in the pulsar message
        Object key = input.get("name");

        if (key == null) {
            logger.error("invalid message " + input);
            return new AerospikeSkipRecordOperation();
        }

        String newCdr = "cdr_" + System.currentTimeMillis();

        // Aerospike key.
        Key aerospikeKey = new Key("test", null, (String) key);

        Record existingRecord = null;
        // Read existing record.
        try {
            existingRecord = aerospikeReader.get(null, aerospikeKey);
        } catch (AerospikeException ae) {
            // Java client throws an exception if record is not found for
            // the key in Aerospike
            logger.error("Error while getting the record", ae);
        }

        WritePolicy writePolicy = inboundMessage.getWritePolicy().orElse(null);
        if (existingRecord == null) {
            List<Bin> bins = new ArrayList<>();

            List<String> cdrList = new ArrayList<>();
            cdrList.add(newCdr);
            bins.add(new Bin("cdrs", cdrList));

            bins.add(new Bin("topicName",
                    Objects.requireNonNull(inboundMessageTransformerConfig.getTransformerConfig()).get("topicName")));
            // Add all config fields as a Bin
            bins.addAll(Objects.requireNonNull(inboundMessageTransformerConfig.getTransformerConfig())
                    .entrySet()
                    .stream()
                    .map(e -> new Bin(e.getKey(), e.getValue()))
                    .collect(Collectors.toList())
            );
            // Add all pulsar message fields as a Bin
            bins.addAll(input
                    .entrySet()
                    .stream()
                    .map(e -> new Bin(e.getKey(), e.getValue()))
                    .collect(Collectors.toList())
            );
            // These error codes are sent in inboundMessage by Aerospike if you have configured them in
            // aerospike-pulsar-inbound.yml.
            Set<Integer> ignoreErrorCodes = inboundMessage.getIgnoreErrorCodes();
            return new AerospikePutOperation(aerospikeKey, writePolicy, bins, ignoreErrorCodes);
        } else {
            // List of Aerospike operations.
            List<Operation> operations = new ArrayList<>();

            // Append the CDR if the list is small, else first truncate the
            // list.
            @SuppressWarnings("unchecked")
            List<String> existingCdrs = (List<String>) existingRecord.bins.get("cdrs");

            int cdrMaxCapacity = 2;
            if (existingCdrs.size() >= cdrMaxCapacity) {
                // Trim the oldest records.
                operations.add(ListOperation.removeRange("cdrs", cdrMaxCapacity - 1, 1));
            }
            // Insert new CDR to the top of the list.
            operations.add(ListOperation.insert("cdrs", 0, Value.get(newCdr)));

            return new AerospikeOperateOperation(aerospikeKey, writePolicy, operations);
        }
    }
}
