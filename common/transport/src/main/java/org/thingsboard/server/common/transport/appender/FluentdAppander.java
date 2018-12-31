/**
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.common.transport.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import org.fluentd.logger.FluentLogger;

import java.util.HashMap;
import java.util.Map;

public class FluentdAppander extends AppenderBase<ILoggingEvent>
{
    private static final int MSG_SIZE_LIMIT = 65335;
    private Layout<ILoggingEvent> layout;
    private String tag;
    private String remoteHost;
    private int port;
    private static FluentLogger log;


    @Override
    public void start() {
        super.start();
        this.log = FluentLogger.getLogger(null, remoteHost, port);
    }

    @Override
    protected void append(ILoggingEvent rawData) {

        String msg;
        if (layout != null) {
            msg = layout.doLayout(rawData);
        } else {
            msg = rawData.toString();
        }
        if (msg != null && msg.length() > MSG_SIZE_LIMIT) {
            msg = msg.substring(0, MSG_SIZE_LIMIT);
        }

        Map<String, Object> data = new HashMap<>(1);
        data.put("log", msg);
        log.log(tag, data);

    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
