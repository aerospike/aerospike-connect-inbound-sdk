/*
 *
 *  Copyright 2012-2025 Aerospike, Inc.
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

package com.aerospike.connect.inbound.operation;

import com.aerospike.client.Key;
import com.aerospike.client.Operation;
import com.aerospike.client.policy.WritePolicy;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Represents an Aerospike single record transaction specified as a list of
 * {@link Operation}s.
 */
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class AerospikeOperateOperation implements AerospikeSingleRecordOperation {
    /**
     * @see AerospikeSingleRecordOperation#getKey()
     */
    @Nonnull
    private final Key key;

    /**
     * @see AerospikeSingleRecordOperation#getWritePolicy()
     */
    @Nullable
    private final WritePolicy writePolicy;

    /**
     * Operations to be executed on the Aerospike database record with the given
     * {@link #key}
     */
    private final List<Operation> operations;

    /**
     * @see AerospikeSingleRecordOperation#getIgnoreErrorCodes()
     */
    @SuppressWarnings("FieldMayBeFinal")
    @Nonnull
    private Set<Integer> ignoreErrorCodes = Collections.emptySet();

    /**
     * Default constructor with comment to suppress javadoc warning.
     */
    public AerospikeOperateOperation() {
        throw new IllegalStateException("Cannot create AerospikeOperateOperation");
    }
}
