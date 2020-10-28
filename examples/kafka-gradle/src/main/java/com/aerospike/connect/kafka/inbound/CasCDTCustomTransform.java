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

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Record;
import com.aerospike.client.Value;
import com.aerospike.client.cdt.ListOperation;
import com.aerospike.connect.inbound.AerospikeReader;
import com.aerospike.connect.inbound.Constants;
import com.aerospike.connect.inbound.InboundMessageTransform;
import com.aerospike.connect.inbound.model.InboundMessage;
import com.aerospike.connect.inbound.model.InboundMessageTransformConfig;
import com.aerospike.connect.inbound.operation.AerospikeOperateOperation;
import com.aerospike.connect.inbound.operation.AerospikePutOperation;
import com.aerospike.connect.inbound.operation.AerospikeRecordOperation;
import com.aerospike.connect.inbound.operation.AerospikeSkipRecordOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An example custom transforms that appends incoming CDR (Call Detail Record)
 * to existing record in case it exists. If the list becomes too large it
 * is trimmed.
 */
@Singleton
public class CasCDTCustomTransform implements
        InboundMessageTransform<InboundMessage<Object, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(CasCDTCustomTransform.class.getName());

    /**
     * Injected aerospike reader to read records from Aerospike.
     */
    private final AerospikeReader aerospikeReader;
    /**
     * Topic name of the current record
     */
    private final String topicName;
    /**
     * Inbound message transform config for the topic against which this class
     * is bound
     */
    private final InboundMessageTransformConfig inboundMessageTransformConfig;

    @Inject
    public CasCDTCustomTransform(AerospikeReader aerospikeReader,
                                 @Named(Constants.TOPIC_NAME_GUICE_ANNOTATION) String topicName,
                                 InboundMessageTransformConfig inboundMessageTransformConfig) {
        this.aerospikeReader = aerospikeReader;
        this.topicName = topicName;
        this.inboundMessageTransformConfig = inboundMessageTransformConfig;
    }

    @Override
    public AerospikeRecordOperation transform(
            InboundMessage<Object, Object> inboundMessage) {

        Map<String, Object> input = inboundMessage.getFields();

        // Get the Aerospike key. Name field was sent in the kafka message
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
            existingRecord = aerospikeReader.getRecord(aerospikeKey);
        } catch (AerospikeException ae) {
            // Java client throws an exception if record is not found for
            // the key in Aerospike
            logger.error("Error while getting the record", ae);
        }

        if (existingRecord == null) {
            List<Bin> bins = new ArrayList<>();

            List<String> cdrList = new ArrayList<>();
            cdrList.add(newCdr);
            bins.add(new Bin("cdrs", cdrList));

            bins.add(new Bin("topicName", topicName));
            // Add all config fields as a Bin
            bins.addAll(Objects.requireNonNull(inboundMessageTransformConfig.getTransformConfig())
                    .entrySet()
                    .stream()
                    .map(e -> new Bin(e.getKey(), e.getValue()))
                    .collect(Collectors.toList())
            );
            // Add all kafka message fields as a Bin
            bins.addAll(input
                    .entrySet()
                    .stream()
                    .map(e -> new Bin(e.getKey(), e.getValue()))
                    .collect(Collectors.toList())
            );
            return new AerospikePutOperation(aerospikeKey, null, bins);
        } else {
            // List of Aerospike operations.
            List<Operation> operations = new ArrayList<>();

            // Append the CDR if the list is small, else first truncate the
            // list.
            List<String> existingCdrs = (List<String>) existingRecord.bins.get("cdrs");

            int cdrMaxCapacity = 2;
            if (existingCdrs.size() >= cdrMaxCapacity) {
                // Trim the oldest records.
                operations.add(ListOperation.removeRange("cdrs", cdrMaxCapacity - 1, 1));
            }
            // Insert new CDR to the top of the list.
            operations.add(ListOperation.insert("cdrs", 0, Value.get(newCdr)));

            return new AerospikeOperateOperation(aerospikeKey, null, operations);
        }
    }
}