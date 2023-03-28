package com.vcvnc.vpn.tunnel;

import com.vcvnc.vpn.nat.NatSession;
import com.vcvnc.vpn.nat.NatSessionManager;
import com.vcvnc.vpn.tcpip.IPHeader;
import com.vcvnc.vpn.utils.CommonMethods;
import com.vcvnc.vpn.utils.DebugLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

public class RemoteTcpTunnel extends TcpTunnel {
    private int ip;
    private int port;
    byte[] header = new byte[48];
    NatSession session;

    public RemoteTcpTunnel(InetSocketAddress serverAddress, InetSocketAddress destAddress, Selector selector, short portKey) throws IOException {
        super(serverAddress, selector, portKey);
        this.name="RemoteTcpTunnel";
        ip = CommonMethods.ipStringToInt(CommonMethods.ipBytesToString(destAddress.getAddress().getAddress()));
        port = destAddress.getPort();
        IPHeader ipheader = new IPHeader(header, 0);
        ipheader.setHeaderLength(20);
        ipheader.setSourceIP(ip);
        ipheader.setDestinationIP(port);
        ipheader.setProtocol(IPHeader.TCP);
        session = NatSessionManager.getSession(portKey);

    }

    @Override
    protected void onConnected() throws Exception {
        //*
        ByteBuffer buffer = ByteBuffer.wrap(header);
        int byteSendSum = 0;
        while (buffer.hasRemaining()) {
            int byteSent = mInnerChannel.write(buffer);
            byteSendSum += byteSent;
            if (byteSent == 0) {
                break; //不能再发送了，终止循环
            }
        }
        //*/
        onTunnelEstablished();
    }

    @Override
    protected boolean isTunnelEstablished() {
        return true;
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {


        //System.out.printf("%s: ", this.mDestAddress);
        //System.out.println("################## afterReceived ################## "+ buffer.limit());
        /*
        byte[] data = buffer.array();
        for(int i = 0;i<buffer.limit();i++){
            System.out.printf("0x%s ", Integer.toHexString(data[i]));
        }
        System.out.println();

        //*/
        refreshSessionAfterRead(buffer.limit());

    }

    @Override
    protected ByteBuffer beforeSend(ByteBuffer buffer) throws Exception {

        //System.out.printf("%s: ", this.mDestAddress);
        //System.out.println("##################beforeSend##################" + buffer.limit());
        /*

        int len = buffer.limit();
        byte[] data = buffer.array();
        for(int i = 0;i<buffer.limit();i++){
            System.out.printf("0x%s ", Integer.toHexString(data[i]));
        }
        System.out.println();


        byte[] dataCopy = new byte[len + header.length];
        System.arraycopy(header, 0, dataCopy, 0, header.length);
        System.arraycopy(data, 0, dataCopy, header.length, len);

        ByteBuffer buf = ByteBuffer.wrap(dataCopy);
        //*/

        return buffer;

    }

    private void refreshSessionAfterRead(int size) {
        session.lastRefreshTime = System.currentTimeMillis();
        session.receivePacketNum++;
        session.receiveByteNum += size;
    }

    @Override
    protected void onDispose() {

    }
}
