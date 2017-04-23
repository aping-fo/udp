package com.sp.net;

import io.netty.channel.socket.DatagramPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManger {

	private final static Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

	public static void addSesion(long id, Session session) {
		if (sessionMap.containsKey(id)) { //重连
			Session session2 = sessionMap.get(id);
			session2.setChannel(session.getChannel());
			session = null;
		}
		else{
			sessionMap.put(id, session);
		}
	}

	public static void removeSesion(long id) {
		Session session = sessionMap.remove(id);
		if (session != null) {
			session.close();
		}
	}

	public static void scheduleRto() {
		for (Session session : sessionMap.values()) {
			session.rto();
		}
	}

	public static void acknowledge(long id, int seq) {
		Session session = sessionMap.get(id);
		if (session != null) {
			session.removePacket(seq);
		}
	}

	public static boolean addPacket(long id, int cmd, int length, int seq,
			DatagramPacket packet) {
		boolean ret = true;
		Session session = sessionMap.get(id);
		if (session != null) {
			session.addPacket(Packet.valueOf(cmd, length, seq, packet));
		} else {
			// TODO 记录日志，为啥没有?没有登录?
			// 关闭连接?
			ret = false;
		}
		return ret;
	}
}
