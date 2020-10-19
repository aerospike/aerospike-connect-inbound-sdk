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

package com.aerospike.connect.inbound;

import com.aerospike.connect.inbound.operation.AerospikeRecordOperation;

import java.util.List;

/**
 * Generate one or more [{@link AerospikeRecordOperation]}s from the com.aerospike.connect.inbound
 * record coming from some external system.
 *
 * <p>
 * <ul>
 *   <li> If you annotate your implementation with
 *   <a href="https://docs.oracle.com/javaee/7/api/javax/inject/Singleton.html">@Singleton</a>,
 *   it has to be thread safe because the same instance can be used by multiple threads.
 *   </li>
 *   <li> If not annotated with Singleton, then we will create a new instance of transform for every incoming message.
 *   </li>
 * </ul>
 * </p>
 * @param <T> incoming message type
 */
public interface InboundMessageTransform<T> {
    /**
     * Transforms generic input message/record into a list of [{@link
     * AerospikeRecordOperation} s to apply on to the Aerospike database. These
     * will not be transactional.
     */
    List<AerospikeRecordOperation> transform(T input);
}
