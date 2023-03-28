package com.vcvnc.vpn.tunnel;

import com.vcvnc.vpn.nat.NatSession;
import com.vcvnc.vpn.nat.NatSessionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {

    public static RemoteTcpTunnel remoteTcpTunnel;

    public static TcpTunnel wrap(SocketChannel channel, Selector selector) {
        TcpTunnel tunnel = new RawTcpTunnel(channel, selector);
        NatSession session = NatSessionManager.getSession((short) channel.socket().getPort());
        if (session != null) {
            tunnel.setIsHttpsRequest(session.isHttpsSession);
        }
        return tunnel;
    }

    public static TcpTunnel createTunnelByConfig(InetSocketAddress serverAddress, InetSocketAddress destAddress, Selector selector, short portKey) throws IOException {
        return new RemoteTcpTunnel(serverAddress, destAddress, selector, portKey);
    }

}
