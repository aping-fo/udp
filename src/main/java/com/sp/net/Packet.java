package com.sp.net;

import io.netty.channel.socket.DatagramPacket;

public class Packet {
	// 协议头
	public final short cmd; // 请求指令
	public final short lenght; // 包体长度
	public final int seq; // 包序列ID
	public final DatagramPacket data; // 包体

	public long time; //收到数据包时间,不参与网络传输
	public int count; //重传次数，一般重传3次
	
	private Packet(short cmd,short length, int seq, DatagramPacket data) {
		this.cmd = cmd;
		this.seq = seq;
		this.lenght = length;
		this.data = data;
		time = System.currentTimeMillis();
	}

	public static Packet valueOf(int cmd, int length,int seq, DatagramPacket data) {
		Packet packet = new Packet((short)cmd,(short)length, seq, data);
		return packet;
	}
}
