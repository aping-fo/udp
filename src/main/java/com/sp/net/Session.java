package com.sp.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Session {
	/** 重传时间间隔，单位毫秒，下一次重传为RTO*2，以2的指数增加重传时间 */
	private static final int RTO = 200;
	private static final int RTO_TIMES = 5;
	/**
	 * 最多缓存多少个离线包，实际可以用配置，在这里直接写死
	 */
	private static final int MaxOfflinePack = 500;
	private AtomicInteger seq = new AtomicInteger(1);
	private Channel channel; // 提供多个连接?
	private InetSocketAddress sender;
	private Map<Integer, Packet> packetMap = new ConcurrentHashMap<>(); //由于有定时器，暂时用个这玩意

	public Session(Channel channel, InetSocketAddress sender) {
		this.channel = channel;
		this.sender = sender;
	}

	
	public Channel getChannel() {
		return channel;
	}


	public void setChannel(Channel channel) {
		if(this.channel != null) {
			this.channel.close();
		}
		this.channel = channel;
	}


	public ChannelId id() {
		return channel.id();
	}

	public boolean send(int cmd, byte[] data) {
		int seq1 = seq.getAndIncrement();
		ByteBuf buf = Unpooled.buffer(data.length + 8);
		buf.writeShort(cmd);
		buf.writeShort(seq1);
		buf.writeInt(data.length);
		buf.writeBytes(data);
		boolean ret = true;
		try {
			DatagramPacket packet = new DatagramPacket(buf, sender);
			channel.writeAndFlush(new DatagramPacket(buf, sender));
			addPacket(Packet.valueOf(cmd, data.length, seq1, packet));
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	public void addPacket(Packet packet) {
		if (packetMap.size() < MaxOfflinePack) {
			packetMap.put(packet.seq, packet);
		}
	}

	public void removePacket(int id) {
		packetMap.remove(id);
	}

	public void close() {
		if (channel != null) {
			channel.close();
		}
		packetMap.clear();
		sender = null;
		channel = null;
		packetMap = null;
	}

	public boolean isOpen() {
		return channel.isOpen();
	}

	public void rto() { // 检测可能不是那么精准
		if (!isOpen())
			return;
		for (Packet packet : packetMap.values()) {
			if(packet.count >= RTO_TIMES) { //估计是网络断开，连不上来了
				close();
				break;
			}
			if (System.currentTimeMillis() > packet.time) {
				channel.writeAndFlush(packet);
				packet.count += 1;
				packet.time = System.currentTimeMillis() + RTO * (1 << packet.count);
			}
		}
	}
}
