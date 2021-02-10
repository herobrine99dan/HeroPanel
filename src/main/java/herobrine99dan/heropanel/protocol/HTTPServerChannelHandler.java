package herobrine99dan.heropanel.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HTTPServerChannelHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
		Channel channel = (Channel) o;
		channel.pipeline().addFirst("UniportWebServerChannelHandler", new HTTPServerConnectionChannel());
		super.channelRead(ctx, o);
	}
	
}
