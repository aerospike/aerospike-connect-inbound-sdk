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

package com.aerospike.connect.inbound.model;

import com.aerospike.client.Key;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link InboundMessage}.
 */
class InboundMessageTest {
    @Test
    void testErrorOnDefaultConstructor() {
        Exception e = assertThrows(IllegalStateException.class, InboundMessage::new);
        assertEquals("Cannot create InboundMessage", e.getMessage());
    }

    @Test
    void testSuccessOnParameterisedConstructor() {
        assertDoesNotThrow(() -> new InboundMessage<Object, Object>(
                "messageKey", "message", new Key("test", "demo", 1), null, Collections.emptyMap(), Collections.emptySet()
        ));
    }
}
