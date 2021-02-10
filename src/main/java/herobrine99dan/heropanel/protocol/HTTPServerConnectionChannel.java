package herobrine99dan.heropanel.protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HTTPServerConnectionChannel extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
		if (ByteBuf.class.isAssignableFrom(o.getClass())) {
			final ByteBuf originalBuffer = (ByteBuf) o;
			ByteBuf buf = originalBuffer.copy();
			boolean isHTTP = false;
			try (ByteBufInputStream stream = new ByteBufInputStream(buf);
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
				HTTPConnection connection = new HTTPConnection();
				connection.parse(stream, reader);
				isHTTP = connection.isHttp;
				if(isHTTP) {
					this.sendWebsite(ctx, originalBuffer.copy(), connection);
				}
			}
			if (!isHTTP) {
				super.channelRead(ctx, o);
			}
		}
	}
	
	private byte[] stringToASCII(String s) {
		return s.getBytes(StandardCharsets.US_ASCII);
	}
	
	private static final byte[] blankLine = new byte[] { 13, 10 };

	private void sendWebsite(ChannelHandlerContext ctx, ByteBuf b, HTTPConnection connection) {
		HTTPRequestEvent event = new HTTPRequestEvent(connection, ctx);
		b.clear();
		Bukkit.getServer().getPluginManager().callEvent(event);
		b.writeBytes(stringToASCII("HTTP/1.1 " + event.getHttpcode().getResponse()));
		b.writeBytes(blankLine);
		b.writeBytes(stringToASCII("Content-Type: " + event.getContentType()));
		b.writeBytes(blankLine);
		b.writeBytes(stringToASCII("Connection: close"));
		b.writeBytes(blankLine);
		b.writeBytes(blankLine);
		if (event.useByteArray()) {
			b.writeBytes(event.byteArr);
		} else {
			b.writeBytes(event.toSend().getBytes());
		}
		ctx.writeAndFlush(b);
		ctx.close();
	}

}
