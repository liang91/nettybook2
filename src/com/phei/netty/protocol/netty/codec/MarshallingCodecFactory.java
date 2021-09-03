/*
 * Copyright 2013-2018 Lilinfeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phei.netty.protocol.netty.codec;

import org.jboss.marshalling.*;

import java.io.IOException;

public final class MarshallingCodecFactory {
    private static final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
    private static final MarshallingConfiguration configuration = new MarshallingConfiguration();
    static {
        configuration.setVersion(5);
    }

    protected static Marshaller buildMarshaller() throws IOException {
        return marshallerFactory.createMarshaller(configuration);
    }

    protected static Unmarshaller buildUnMarshaller() throws IOException {
        return marshallerFactory.createUnmarshaller(configuration);
    }
}
