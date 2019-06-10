/**
 * $RCSfile$
 * $Revision$
 * $Date$
 * <p>
 * Copyright 2003-2007 Jive Software.
 * <p>
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smack;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;


import android.util.Log;


import com.yunbiao.ybsmartcheckin_live_id.xmpp.XmppManager;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.SSLException;


/**
 * Writes packets to a XMPP server. Packets are sent using a dedicated thread. Packet
 * interceptors can be registered to dynamically modify packets before they're actually
 * sent. Packet listeners can be registered to listen for all outgoing packets.
 *
 * @author Matt Tucker
 * @see Connection#addPacketInterceptor
 * @see Connection#addPacketSendingListener
 */
class PacketWriter {
    private static final String TAG = "PacketWriter";

    private Thread writerThread;
    private Thread keepAliveThread;
    private Writer writer;
    private XMPPConnection connection;
    private XmppManager xmppManager;
    private final BlockingQueue<Packet> queue;
    private boolean done;
    /**
     * Timestamp when the last stanza was sent to the server. This information is used
     * by the keep alive process to only send heartbeats when the connection has been idle.
     */
    private long lastActive = System.currentTimeMillis();

    /**
     * Creates a new packet writer with the specified connection.
     *
     * @param connection the connection.
     */
    protected PacketWriter(XMPPConnection connection) {
        this.queue = new ArrayBlockingQueue<Packet>(500, true);
        this.connection = connection;
        init();
    }

    /**
     * Initializes the writer in order to be used. It is called at the first connection and also
     * is invoked if the connection is disconnected by an error.
     */
    protected void init() {
        this.writer = connection.writer;
        done = false;

        writerThread = new Thread() {
            public void run() {
                writePackets(this);
            }
        };
        writerThread.setName("Smack Packet Writer (" + connection.connectionCounterValue + ")");
        writerThread.setDaemon(true);
    }

    /**
     * Sends the specified packet to the server.
     *
     * @param packet the packet to send.
     */
    public void sendPacket(Packet packet) {
        if (!done) {
            // Invoke interceptors for the new packet that is about to be sent. Interceptors
            // may modify the content of the packet.
            connection.firePacketInterceptors(packet);
            try {
                queue.put(packet);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            synchronized (queue) {
                queue.notifyAll();
            }
            // Process packet writer listeners. Note that we're using the sending
            // thread so it's expected that listeners are fast.
            connection.firePacketSendingListeners(packet);
        }
    }

    /**
     * Starts the packet writer thread and opens a connection to the server. The
     * packet writer will continue writing packets until {@link #shutdown} or an
     * error occurs.
     */
    public void startup() {
        writerThread.start();
    }

    /**
     * Starts the keep alive process. A white space (aka heartbeat) is going to be
     * sent to the server every 30 seconds (by default) since the last stanza was sent
     * to the server.
     *
     *
     * @param xmppManager
     * @throws Exception
     */
    void startKeepAliveProcess(XmppManager xmppManager) throws Exception {
        // Schedule a keep-alive task to run if the feature is enabled. will write
        // out a space character each time it runs to keep the TCP/IP connection open.
        this.xmppManager = xmppManager;
        int keepAliveInterval = SmackConfiguration.getKeepAliveInterval();
        if (keepAliveInterval > 0) {
            KeepAliveTask task = new KeepAliveTask(keepAliveInterval, xmppManager);
            keepAliveThread = new Thread(task);
            task.setThread(keepAliveThread);
            keepAliveThread.setDaemon(true);
            keepAliveThread.setName("Smack Keep Alive (" + connection.connectionCounterValue + ")");
            keepAliveThread.start();
        }
    }

    void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Shuts down the packet writer. Once this method has been called, no further
     * packets will be written to the server.
     */
    public void shutdown() {
        done = true;
        synchronized (queue) {
            queue.notifyAll();
        }
    }

    /**
     * Cleans up all resources used by the packet writer.
     */
    void cleanup() {
        connection.interceptors.clear();
        connection.sendListeners.clear();
    }

    /**
     * Returns the next available packet from the queue for writing.
     *
     * @return the next packet for writing.
     */
    private Packet nextPacket() {
        Packet packet = null;
        // Wait until there's a packet or we're done.
        while (!done && (packet = queue.poll()) == null) {
            try {
                synchronized (queue) {
                    queue.wait();
                }
            } catch (InterruptedException ie) {
                // Do nothing
            }
        }
        return packet;
    }

    private void writePackets(Thread thisThread) {
        try {
            // Open the stream.
            openStream();
            // Write out packets from the queue.
            while (!done && (writerThread == thisThread)) {
                Packet packet = nextPacket();
                if (packet != null) {
                    synchronized (writer) {
                        try {
                            writer.write(packet.toXML());
                            writer.flush();
                        } catch (SSLException s) {
                            s.printStackTrace();
                            connection.disconnect();
                            this.xmppManager.startReconnectionThread();
                        }
                        // Keep track of the last time a stanza was sent to the server
                        lastActive = System.currentTimeMillis();
                    }
                }
            }
            // Flush out the rest of the queue. If the queue is extremely large, it's possible
            // we won't have time to entirely flush it before the socket is forced closed
            // by the shutdown process.
            try {
                synchronized (writer) {
                    while (!queue.isEmpty()) {
                        Packet packet = queue.remove();
                        writer.write(packet.toXML());
                    }
                    writer.flush();
                }
            } catch (SSLException s) {
                connection.disconnect();
                this.xmppManager.startReconnectionThread();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Delete the queue contents (hopefully nothing is left).
            queue.clear();

            // Close the stream.
            try {
                writer.write("</stream:stream>");
                writer.flush();
            } catch (SSLException s) {
                connection.disconnect();
                this.xmppManager.startReconnectionThread();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                    // Do nothing
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            if (!done) {
                done = true;
                connection.packetReader.notifyConnectionError(ioe);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends to the server a new stream element. This operation may be requested several times
     * so we need to encapsulate the logic in one place. This message will be sent while doing
     * TLS, SASL and resource binding.
     *
     * @throws IOException If an error occurs while sending the stanza to the server.
     */
    void openStream() throws IOException {
        StringBuilder stream = new StringBuilder();
        stream.append("<stream:stream");
        stream.append(" to=\"").append(connection.getServiceName()).append("\"");
        stream.append(" xmlns=\"jabber:client\"");
        stream.append(" xmlns:stream=\"http://etherx.jabber.org/streams\"");
        stream.append(" version=\"1.0\">");
        writer.write(stream.toString());
        writer.flush();
    }

    /**
     * A TimerTask that keeps connections to the server alive by sending a space
     * character on an interval.
     */
    private class KeepAliveTask implements Runnable {

        private int delay;
        private Thread thread;
        private XmppManager xmppManager;

        public KeepAliveTask(int delay, XmppManager xmppManager) {
            this.delay = delay;
            this.xmppManager = xmppManager;
        }

        protected void setThread(Thread thread) {
            this.thread = thread;
        }

        public void run() {
            try {
                // Sleep 15 seconds before sending first heartbeat. This will give time to
                // properly finish TLS negotiation and then start sending heartbeats.
                Thread.sleep(15000);
            } catch (InterruptedException ie) {
                // Do nothing
            }
            while (!done && keepAliveThread == thread) {
                // Send heartbeat if no packet has been sent to the server for a given time
                if (System.currentTimeMillis() - lastActive >= delay) {
                    Log.i(TAG, "心跳 ");
                    try {
                        synchronized (writer) {
                            writer.write(" ");
                            writer.flush();
                        }
                    }
                    // bug fixed
                    // @will_awoke 服务端重启时，客户端在发送心跳包时，writer.flush()时会抛出
                    // SSLException，
                    // catch该exception，start ReconnectionThread
                    catch (SSLException ssl) {
                        Log.e("SSLException", ssl.toString());
                        connection.disconnect();
                        xmppManager.startReconnectionThread();
                    } catch (SocketException se) {
                        Log.e("SocketException", se.toString());
                        connection.disconnect();
                        xmppManager.startReconnectionThread();
                    } catch (IOException io) {
                        Log.e("IOException", io.toString());
                        connection.disconnect();
                        xmppManager.startReconnectionThread();
                    } catch (Exception e) {
                        Log.e("Exception", e.toString());
                        connection.disconnect();
                        xmppManager.startReconnectionThread();
                    }
                }
                try {
                    // Sleep until we should write the next keep-alive.
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    // Do nothing
                }
            }
        }
    }
}