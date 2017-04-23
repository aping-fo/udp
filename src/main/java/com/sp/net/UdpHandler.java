package com.sp.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import com.sp.domain.Cmd;

public class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        System.err.println(packet);
        ByteBuf data = packet.content();
        int cmd = data.readShort();
        long id = 0l; //客户端表示，比如userId
        int seq = data.readShort();
        if(cmd == Cmd.LOGIN) {
        	SessionManger.addSesion(id,new Session(ctx.channel(), packet.sender()));
        }
        else if (cmd == Cmd.ACK) {
        	SessionManger.acknowledge(id, seq);
        }
        else {
        	//TODO 其他业务 ACK
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }
}
