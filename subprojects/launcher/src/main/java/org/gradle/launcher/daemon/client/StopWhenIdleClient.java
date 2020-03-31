/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.launcher.daemon.client;

import org.gradle.internal.id.IdGenerator;
import org.gradle.launcher.daemon.protocol.StopWhenIdle;
import org.gradle.launcher.daemon.registry.DaemonInfo;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.launcher.daemon.server.api.DaemonStateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class StopWhenIdleClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopWhenIdleClient.class);

    private final DaemonConnector connector;
    private final IdGenerator<UUID> idGenerator;
    private final DaemonRegistry daemonRegistry;

    public StopWhenIdleClient(DaemonConnector connector, IdGenerator<UUID> idGenerator, DaemonRegistry daemonRegistry) {
        this.connector = connector;
        this.idGenerator = idGenerator;
        this.daemonRegistry = daemonRegistry;
    }

    public void stopWhenIdle() {
        for (DaemonInfo daemonInfo : daemonRegistry.getAll()) {
            DaemonStateControl.State state = daemonInfo.getState();
            if (state != DaemonStateControl.State.Idle) {
                continue;
            }
            DaemonClientConnection connection = connector.maybeConnect(daemonInfo);
            if (connection == null) {
                continue;
            }

            // TODO consider using DaemonStopClient instead
            // TODO logging
            // TODO maybe force-stop after a timeout
            connection.dispatch(new StopWhenIdle(idGenerator.generateId(), connection.getDaemon().getToken()));
        }
    }
}
