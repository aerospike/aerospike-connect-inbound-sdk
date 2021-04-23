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

import com.aerospike.connect.inbound.operation.AerospikeRecordOperation;

/**
 * Generate an {@link AerospikeRecordOperation} from an incoming message.
 *
 * <ul>
 *   <li> If you annotate your implementation with
 *   <a href="https://docs.oracle.com/javaee/7/api/javax/inject/Singleton.html">@Singleton</a>,
 *   it has to be thread safe because the same instance can be used by multiple threads.
 *   </li>
 *   <li> If not annotated with Singleton, then a new instance of transformer will be created for every incoming
 *   message.
 *   </li>
 * </ul>
 *
 * @param <T> incoming message type
 */
public interface InboundMessageTransformer<T> {
    /**
     * Transforms generic input message/record into {@link AerospikeRecordOperation} to apply on the Aerospike
     * database.
     *
     * @param input Inbound message from the external system.
     * @return the operation to apply.
     */
    AerospikeRecordOperation transform(T input);
}
