package com.vcvnc.vpn.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class RawTcpTunnel extends TcpTunnel {

    public RawTcpTunnel(SocketChannel innerChannel, Selector selector) {
        super(innerChannel, selector);
    }

    public RawTcpTunnel(InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
        super(serverAddress, selector, portKey);

    }

    @Override
    protected void onConnected() throws Exception {
        onTunnelEstablished();
    }

    @Override
    protected boolean isTunnelEstablished() {
        return true;
    }

    @Override
    protected ByteBuffer beforeSend(ByteBuffer buffer) throws Exception {
        return buffer;
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {

    }

    @Override
    protected void onDispose() {

    }
}
