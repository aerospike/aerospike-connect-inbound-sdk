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

package com.aerospike.connect.kafka.inbound;

import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.Value;
import com.aerospike.client.cdt.CTX;
import com.aerospike.client.cdt.ListOperation;
import com.aerospike.client.cdt.ListReturnType;
import com.aerospike.client.cdt.MapOperation;
import com.aerospike.client.cdt.MapPolicy;
import com.aerospike.connect.inbound.InboundMessageTransformer;
import com.aerospike.connect.inbound.model.InboundMessage;
import com.aerospike.connect.inbound.operation.AerospikeOperateOperation;
import com.aerospike.connect.inbound.operation.AerospikeRecordOperation;
import com.aerospike.connect.inbound.operation.AerospikeSkipRecordOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An example message transformer that modifies a rocket dealerships inventory
 * and sales record upon the sale of a rocket.
 */
@Singleton
public class CDTMessageTransformer implements
        InboundMessageTransformer<InboundMessage<Object, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(CDTMessageTransformer.class.getName());

    @Override
    public AerospikeRecordOperation transform(InboundMessage<Object, Object> input) {
        Map<String, Object> fields = input.getFields();

        // Get the Aerospike key.
        String key = (String) fields.get("key");

        if (key == null) {
            logger.warn("Invalid missing key");
            return new AerospikeSkipRecordOperation();
        }

        // Aerospike key.
        Key aerospikeKey = new Key("used-rocket-dealership", null, key);

        /*
          Rocket Map {

               model: String;
               manufacturer: String
               thrust: Integer;
               price: Double;
               .
               .
          }
         */
        @SuppressWarnings("unchecked")
        Map<String, ?> rocket = (Map<String, ?>) fields.get("rocket");

        /*
          This rocket has just been sold by the dealership, we need to
          remove it from the inventory and record our profits.
         */

        // List to hold Aerospike CDT operations.
        List<Operation> operations = new ArrayList<>();

        /*
          The "inventory" bin holds the dealerships list of rockets for sale.
          Lets remove the rocket from the "inventory" bin.
         */
        operations.add(ListOperation.removeByValue("inventory",
                Value.get(rocket), ListReturnType.NONE));

        /*
          Now we need to update our sales record to show how many rockets have
          been sold, our profits. The sales record looks like this:

          sales-record {
            list-of-sold:     List<Rocket>
            num-rockets-sold: Integer
            gross-profit:     Double
          }
         */
        operations.add(ListOperation.append("list-of-sold", Value.get(rocket),
                CTX.mapKey(Value.get("sales-record"))));
        operations.add(MapOperation.increment(new MapPolicy(), "sales-record"
                , Value.get("num-rockets-sold"), Value.get(1)));
        operations.add(MapOperation.increment(new MapPolicy(), "sales-record",
                Value.get("gross_profit"), Value.get(rocket.get("profit"))));

        /*
          Lastly, we will update the top sales person.

          top-sales-person {
            first-name: String
            last-name:  String
          }
         */
        Map<Value, Value> topSalesPerson = new HashMap<>();
        topSalesPerson.put(Value.get("first-name"), Value.get("Elon"));
        topSalesPerson.put(Value.get("last-name"), Value.get("Musk"));

        operations.add(MapOperation.putItems(new MapPolicy(), "top-sales-person",
                topSalesPerson));
        return new AerospikeOperateOperation(aerospikeKey, input.getWritePolicy().orElse(null), operations);
    }
}
