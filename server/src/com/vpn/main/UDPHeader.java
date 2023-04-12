// tashvik and pawan
package com.easyvpn.main;

public class UDPHeader {
  
    static final short offset_src_port = 0; 
    static final short offset_dest_port = 2; 
    static final short offset_tlen = 4; 
    static final short offset_crc = 6; 

    public byte[] mData;
    public int mOffset;

    public UDPHeader(byte[] data, int offset) { // creating the custom UDP header 
        mData = data;
        mOffset = offset;
    }

    public short getSourcePort() {    // fetch the src port
        return CommonMethods.readShort(mData, mOffset + offset_src_port);
    }

    public void setSourcePort(short value) {  // intialize the source port 
        CommonMethods.writeShort(mData, mOffset + offset_src_port, value);
    }

    public short getDestinationPort() {      // fetch dest port 
        return CommonMethods.readShort(mData, mOffset + offset_dest_port);
    }

    public void setDestinationPort(short value) { // initalize destination port 
        CommonMethods.writeShort(mData, mOffset + offset_dest_port, value);
    }

    public int getTotalLength() { 
        return CommonMethods.readShort(mData, mOffset + offset_tlen) & 0xFFFF;
    }

    public void setTotalLength(int value) {
        CommonMethods.writeShort(mData, mOffset + offset_tlen, (short) value);
    }

    public short getCrc() {
        return CommonMethods.readShort(mData, mOffset + offset_crc);
    }

    public void setCrc(short value) {
        CommonMethods.writeShort(mData, mOffset + offset_crc, value);
    }

    @Override // Overriden function to return the addresses in String format 
    public String toString() {
        return String.format("%d->%d", getSourcePort() & 0xFFFF, getDestinationPort() & 0xFFFF);
    }
}
