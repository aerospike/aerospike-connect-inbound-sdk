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

package com.aerospike.connect.inbound;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.BatchRead;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.Policy;

import java.util.List;

/**
 * A reader to read record from the Aerospike database.
 */
public interface AerospikeReader {
    /**
     * Read a record from the Aerospike database.
     *
     * @param key Aerospike record key
     * @return Aerospike {@link Record} if found
     * @throws AerospikeException on error
     */
    @Deprecated
    Record getRecord(Key key) throws AerospikeException;

    /**
     * Read entire record for specified key.
     * The policy can be used to specify timeouts.
     *
     * @param policy generic configuration parameters, pass in null for defaults
     * @param key    unique record identifier
     * @return if found, return record instance.  If not found, return null.
     * @throws AerospikeException if read fails
     */
    Record get(Policy policy, Key key) throws AerospikeException;

    /**
     * Read record header and bins for specified key.
     * The policy can be used to specify timeouts.
     *
     * @param policy   generic configuration parameters, pass in null for defaults
     * @param key      unique record identifier
     * @param binNames bins to retrieve
     * @return if found, return record instance.  If not found, return null.
     * @throws AerospikeException if read fails
     */
    Record get(Policy policy, Key key, String... binNames) throws AerospikeException;

    /**
     * Read multiple records for specified batch keys in one batch call.
     * This method allows different namespaces/bins to be requested for each key in the batch.
     * The returned records are located in the same list.
     * If the BatchRead key field is not found, the corresponding record field will be null.
     * The policy can be used to specify timeouts and maximum concurrent threads.
     * <p>
     * If a batch request to a node fails, the entire batch is cancelled.
     *
     * @param policy  batch configuration parameters, pass in null for defaults
     * @param records list of unique record identifiers and the bins to retrieve.
     *                The returned records are located in the same list.
     * @throws AerospikeException if read fails
     */
    void get(BatchPolicy policy, List<BatchRead> records) throws AerospikeException;

    /**
     * Read multiple records for specified keys in one batch call.
     * The returned records are in positional order with the original key array order.
     * If a key is not found, the positional record will be null.
     * The policy can be used to specify timeouts and maximum concurrent threads.
     * <p>
     * If a batch request to a node fails, the entire batch is cancelled.
     *
     * @param policy batch configuration parameters, pass in null for defaults
     * @param keys   array of unique record identifiers
     * @return array of records
     * @throws AerospikeException if read fails
     */
    Record[] get(BatchPolicy policy, Key[] keys) throws AerospikeException;

    /**
     * Read multiple record headers and bins for specified keys in one batch call.
     * The returned records are in positional order with the original key array order.
     * If a key is not found, the positional record will be null.
     * The policy can be used to specify timeouts and maximum concurrent threads.
     * <p>
     * If a batch request to a node fails, the entire batch is cancelled.
     *
     * @param policy   batch configuration parameters, pass in null for defaults
     * @param keys     array of unique record identifiers
     * @param binNames array of bins to retrieve
     * @throws AerospikeException if read fails
     * @return array of records
     */
    Record[] get(BatchPolicy policy, Key[] keys, String... binNames) throws AerospikeException;
}
