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

package com.aerospike.connect.inbound.model;

import com.aerospike.connect.inbound.InboundMessageTransform;
import com.aerospike.connect.inbound.operation.AerospikeCompositeRecordOperation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Config used by the custom transformer for the Inbound messages.
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public class InboundMessageTransformConfig {
    /**
     * Class to be used for converting inbound messages to the [{@link
     * com.aerospike.connect.inbound.operation.AerospikeRecordOperation}].
     */
    @JsonProperty("class")
    private final Class<? extends InboundMessageTransform<?>>
            inboundMessageTransformClass = null;

    /**
     * Custom parameters to be used by the custom transform.
     */
    @Nullable
    @JsonProperty("params")
    private final Map<String, Object> transformConfig = null;

    /**
     * Whether to allow [{@link AerospikeCompositeRecordOperation}] or not.
     */
    @JsonProperty("unsafe-composite-record-operations")
    private final boolean unsafeCompositeRecordOperation = false;
}
