/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 - 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.test.basic;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.platform.DefaultClientEndpointConfiguration;
import org.glassfish.tyrus.platform.main.Server;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests the correct processing of binary message and replying.
 *
 * @author Stepan Kopriva (stepan.kopriva at oracle.com)
 */
public class BinaryMessageTest {
    private static final byte[] BINARY_MESSAGE = new byte[]{1,2,3,4};

    private CountDownLatch messageLatch;

    private byte[] receivedMessage;

    @Ignore
    @Test
    public void testHello() {
        Server server = new Server(org.glassfish.tyrus.test.basic.bean.BinaryBean.class);
        server.start();
        try {
            messageLatch = new CountDownLatch(1);

            ClientManager client = ClientManager.createClient();
            client.connectToServer(new TestEndpointAdapter() {

                @Override
                public void onOpen(Session session) {
                    try {
                        session.getRemote().sendBytes(ByteBuffer.wrap(BINARY_MESSAGE));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                public void onMessage(byte[] message) {
                    receivedMessage = message;
                    messageLatch.countDown();
                }
            }, new DefaultClientEndpointConfiguration(new URI("ws://localhost:8025/websockets/tests/binary")));
            messageLatch.await(5, TimeUnit.SECONDS);
            Assert.assertArrayEquals("The received message is the same as the sent one", receivedMessage, BINARY_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            server.stop();
        }
    }
}
