/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.core.FaultInjector;
import com.google.common.base.Charsets;
import org.eclipse.jetty.io.ChannelEndPoint;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.SelectChannelEndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.jetty9.JettyUtils.unwrapResponse;

public class JettyFaultInjector implements FaultInjector {

    private static final byte[] GARBAGE = "lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes(Charsets.UTF_8);

    private final Response response;
    private final Socket socket;

    public JettyFaultInjector(HttpServletResponse response) {
        this.response = unwrapResponse(response);
        this.socket = socket();
    }

    @Override
    public void emptyResponseAndCloseConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    @Override
    public void malformedResponseChunk() {
        try {
            response.setStatus(200);
            response.flushBuffer();
            socket.getChannel().write(BufferUtil.toBuffer(GARBAGE));
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }

    }

    @Override
    public void randomDataAndCloseConnection() {
        try {
            socket.getChannel().write(BufferUtil.toBuffer(GARBAGE));
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    private Socket socket() {
        HttpChannel httpChannel = response.getHttpOutput().getHttpChannel();
        ChannelEndPoint ep = (ChannelEndPoint) httpChannel.getEndPoint();
        return ep.getSocket();
    }

}