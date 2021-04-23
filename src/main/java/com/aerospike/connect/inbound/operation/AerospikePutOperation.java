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

package com.aerospike.connect.inbound.operation;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.policy.WritePolicy;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Represents a standard Aerospike KVS put operation.
 */
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public class AerospikePutOperation implements AerospikeSingleRecordOperation {
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
     * The record bins to put.
     */
    private final List<Bin> bins;

    /**
     * @see AerospikeSingleRecordOperation#getIgnorableResultCodes()
     */
    @SuppressWarnings("FieldMayBeFinal")
    private List<Integer> ignorableResultCodes = Collections.emptyList();
}
