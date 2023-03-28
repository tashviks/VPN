package com.vcvnc.vpn.proxy;

import com.vcvnc.vpn.KeyHandler;
import com.vcvnc.vpn.Packet;
import com.vcvnc.vpn.ProxyConfig;
import com.vcvnc.vpn.VPNLog;
import com.vcvnc.vpn.service.FirewallVpnService;
import com.vcvnc.vpn.utils.AppDebug;
import com.vcvnc.vpn.utils.DebugLog;
import com.vcvnc.vpn.utils.SocketUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RemoteUDPTunnel implements KeyHandler, Runnable {

    private static final String TAG = RemoteUDPTunnel.class.getSimpleName();
    private final FirewallVpnService vpnService;
    private Selector selector;
    private SelectionKey selectionKey;

    private DatagramChannel channel;
    private final ConcurrentLinkedQueue<Packet> toNetWorkPackets = new ConcurrentLinkedQueue<>();
    private static final int HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    String ipAndPort;
    private boolean isClose = false;

    public RemoteUDPTunnel(FirewallVpnService vpnService) {
        this.vpnService = vpnService;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        initConnection();
        Thread thread = new Thread(this, "RemoteUDPTunnel");
        thread.start();
    }


    private void processKey(SelectionKey key) {
        if (key.isWritable()) {
            processSend();
        } else if (key.isReadable()) {
            processReceived();
        }
        updateInterests();
    }

    private void processReceived() {
        //VPNLog.d(TAG, "processReceived:" + ipAndPort);
        ByteBuffer receiveBuffer = SocketUtils.getByteBuffer();
        // Leave space for the header
        //receiveBuffer.position(HEADER_SIZE);
        int readBytes = 0;
        try {
            readBytes = channel.read(receiveBuffer);
        } catch (Exception e) {
            VPNLog.d(TAG, "failed to read udp datas ");
            return;
        }
        if (readBytes == -1) {
            close();
            VPNLog.d(TAG, "read  data error :" + ipAndPort);
        } else if (readBytes == 0) {
            VPNLog.d(TAG, "read no data :" + ipAndPort);
        } else {
            //VPNLog.d(TAG, "read  data :readBytes:" + readBytes + "ipAndPort:" + ipAndPort);
            /*
            byte[] dataCopy = new byte[readBytes];
            System.arraycopy(receiveBuffer.array(), 0, dataCopy, 0, readBytes);
            ByteBuffer buf = ByteBuffer.wrap(dataCopy);

            try {
                Packet packet = new Packet(buf);
                if(packet.isTCP())
                    System.out.println(packet.getIp4Header().sourceAddress+" recv size:"+readBytes+" "+packet);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

             */

            vpnService.write(receiveBuffer.array(), 0, readBytes);

        }
    }

    private void processSend() {
        //VPNLog.d(TAG, "processWriteUDPData " + ipAndPort);
        Packet toNetWorkPacket = getToNetWorkPackets();
        if (toNetWorkPacket == null) {
            VPNLog.d(TAG, "write data  no packet ");
            return;
        }
        try {
            ByteBuffer payloadBuffer = toNetWorkPacket.backingBuffer;
            payloadBuffer.position(0);
            while (payloadBuffer.hasRemaining()) {
                channel.write(payloadBuffer);
            }

        } catch (IOException e) {
            VPNLog.w(TAG, "Network write error: " + ipAndPort, e);
            //vpnServer.closeUDPConn(this);
        }
    }

    public boolean initConnection() {

        InetAddress destinationAddress = null;
        try {
            destinationAddress = InetAddress.getByName(ProxyConfig.serverIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int destinationPort = ProxyConfig.serverPort;

        ipAndPort = destinationAddress.toString()+":"+ProxyConfig.serverPort;
        VPNLog.d(TAG, "init  ipAndPort:" + ipAndPort);
        try {
            channel = DatagramChannel.open();
            vpnService.protect(channel.socket());
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(destinationAddress, destinationPort));
            selector.wakeup();
            selectionKey = channel.register(selector,
                    SelectionKey.OP_READ, this);
        } catch (IOException e) {
            SocketUtils.closeResources(channel);
            return false;
        }
        return true;
    }

    public void processPacket(Packet packet) {
        //System.out.println("-------------"+packet);
        addToNetWorkPacket(packet);
    }

    public void close() {
        System.out.println("------------ close ----------");
        isClose = true;
        try {
            if (selector != null) {
                selector.close();
                selector = null;
            }
            if (selectionKey != null) {
                selectionKey.cancel();
            }
            if (channel != null) {
                channel.close();
            }

        } catch (Exception e) {
            VPNLog.w(TAG, "error to close UDP channel IpAndPort" + ipAndPort + ",error is " + e.getMessage());
        }

    }


    Packet getToNetWorkPackets() {
        return toNetWorkPackets.poll();
    }

    void addToNetWorkPacket(Packet packet) {
        toNetWorkPackets.offer(packet);
        updateInterests();
    }

    DatagramChannel getChannel() {
        return channel;
    }

    void updateInterests() {
        int ops;
        if (toNetWorkPackets.isEmpty()) {
            ops = SelectionKey.OP_READ;
        } else {
            ops = SelectionKey.OP_WRITE | SelectionKey.OP_READ;
        }
        selector.wakeup();
        selectionKey.interestOps(ops);
        //VPNLog.d(TAG, "updateInterests ops:" + ops + ",ip" + ipAndPort);
    }

    @Override
    public void onKeyReady(SelectionKey key) {
        processKey(key);
    }

    @Override
    public void run() {
        try {
            while (!isClose) {
                int select = selector.select();
                if (select == 0) {
                    Thread.sleep(1);
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            Object attachment = key.attachment();
                            if (attachment instanceof KeyHandler) {
                                ((KeyHandler) attachment).onKeyReady(key);
                            }

                        } catch (Exception ex) {
                            if (AppDebug.IS_DEBUG) {
                                ex.printStackTrace(System.err);
                            }

                            DebugLog.e("TcpProxyServer iterate SelectionKey catch an exception: %s", ex);
                        }
                    }
                    keyIterator.remove();
                }


            }
        } catch (Exception e) {
            if (AppDebug.IS_DEBUG) {
                e.printStackTrace(System.err);
            }

            DebugLog.e("TcpProxyServer catch an exception: %s", e);
        } finally {
            this.close();
            DebugLog.i("TcpServer thread exited.");
        }
    }

}
