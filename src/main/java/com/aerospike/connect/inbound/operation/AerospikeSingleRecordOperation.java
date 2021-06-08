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

package com.aerospike.connect.inbound.operation;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.policy.WritePolicy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * An Aerospike single record operation.
 */
public interface AerospikeSingleRecordOperation extends AerospikeRecordOperation {
    /**
     * @return Aerospike record Key
     */
    @Nonnull
    Key getKey();

    /**
     * @return The write policy to use for this operation. Defaults to null implying the
     * default write policy.
     */
    @Nullable
    WritePolicy getWritePolicy();

    /**
     * Error {@link com.aerospike.client.ResultCode}s to be ignored. Failed operation
     * will be considered successful if the result code is in the given set.
     *
     * <p>
     * For example, if a record should only be created once and never updated
     * in Aerospike database, set {@link WritePolicy#recordExistsAction} to
     * {@link com.aerospike.client.policy.RecordExistsAction#CREATE_ONLY} and
     * specify that an {@link AerospikeException} with resultCode
     * {@link com.aerospike.client.ResultCode#KEY_EXISTS_ERROR} should be ignored.
     * </p>
     */
    @Nonnull
    Set<Integer> getIgnorableResultCodes();
}
