// group contribution
package com.easyvpn.main;
public class TCPHeader {

    public static final int FIN = 1;
    public static final int SYN = 2;
    public static final int RST = 4;
    public static final int PSH = 8;
    public static final int ACK = 16;
    public static final int URG = 32;

    static final short offset_src_port = 0; 
    static final short offset_dest_port = 2; 
    static final int offset_seq = 4; 
    static final int offset_ack = 8; 
    static final byte offset_lenres = 12; 
    static final byte offset_flag = 13; 
    static final short offset_win = 14; 
    static final short offset_crc = 16; 
    static final short offset_urp = 18; 

    public byte[] mData;
    public int mOffset;

    public TCPHeader(byte[] data, int offset) {
        mData = data;
        mOffset = offset;
    }

    public int getHeaderLength() {
        int lenres = mData[mOffset + offset_lenres] & 0xFF;
        return (lenres >> 4) * 4;
    }

    public short getSourcePort() {
        return CommonMethods.readShort(mData, mOffset + offset_src_port);
    }

    public void setSourcePort(short value) {
        CommonMethods.writeShort(mData, mOffset + offset_src_port, value);
    }

    public short getDestinationPort() {
        return CommonMethods.readShort(mData, mOffset + offset_dest_port);
    }

    public void setDestinationPort(short value) {
        CommonMethods.writeShort(mData, mOffset + offset_dest_port, value);
    }

    public byte getFlag() {
        return mData[mOffset + offset_flag];
    }

    public short getCrc() {
        return CommonMethods.readShort(mData, mOffset + offset_crc);
    }

    public void setCrc(short value) {
        CommonMethods.writeShort(mData, mOffset + offset_crc, value);
    }

    public int getSeqID() {
        return CommonMethods.readInt(mData, mOffset + offset_seq);
    }

    public int getAckID() {
        return CommonMethods.readInt(mData, mOffset + offset_ack);
    }

    @Override
    public String toString() {
        return String.format("%s%s%s%s%s%s %d->%d %s:%s",
                (getFlag() & SYN) == SYN ? "SYN" : "",
                (getFlag() & ACK) == ACK ? "ACK" : "",
                (getFlag() & PSH) == PSH ? "PSH" : "",
                (getFlag() & RST) == RST ? "RST" : "",
                (getFlag() & FIN) == FIN ? "FIN" : "",
                (getFlag() & URG) == URG ? "URG" : "",
                getSourcePort() & 0xFFFF,
                getDestinationPort() & 0xFFFF,
                getSeqID(),
                getAckID());
    }
}
