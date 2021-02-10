package herobrine99dan.heropanel.protocol;

import java.net.InetSocketAddress;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.netty.channel.ChannelHandlerContext;

public class HTTPRequestEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	protected HTTPConnection connection;

	private static String case404 = "<!DOCTYPE html><html><body><h1>Bad Request: 404 Site not found!</h1></body></html>";

	protected String toSend;

	protected String contentType = "text/html";

	private boolean bytes = false;

	private HTTPResponseCode httpcode;

	private InetSocketAddress ip;

	private ChannelHandlerContext chx;

	public byte[] byteArr;

	public HTTPRequestEvent(HTTPConnection request, ChannelHandlerContext ctx) {
		this.connection = request;
		this.chx = ctx;
		this.httpcode = HTTPResponseCode.Code200;
		if(!(ctx.channel().remoteAddress() instanceof InetSocketAddress)) {
			throw new RuntimeException("remoteAddress() didn't give a 'InetSocketAddress' object");
		}
		this.ip = (InetSocketAddress) ctx.channel().remoteAddress();
	}

	public HTTPResponseCode getHttpcode() {
		return httpcode;
	}

	public void setHttpcode(HTTPResponseCode httpcode) {
		this.httpcode = httpcode;
	}

	public String getContentType() {
		return contentType;
	}

	public boolean useByteArray() {
		return this.bytes;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return this.chx;
	}

	public InetSocketAddress getAddress() {
		return this.ip;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HTTPConnection getConnection() {
		return this.connection;
	}
	
	public String getHost() {
		return this.getConnection().host;
	}
	
	public String getMethod() {
		return this.getConnection().method;
	}
	
	public String getNgrokIp() {
		return this.getConnection().ngrokIp;
	}

	public static void setCase404(String document) {
		case404 = document;
	}

	public static String getCase404() {
		return case404;
	}

	public void setMessage(String htmlDocument) {
		this.bytes = false;
		this.toSend = htmlDocument;
	}

	public String toSend() {
		return (this.toSend == null) ? case404 : this.toSend;
	}

	public byte[] raw() {
		return this.bytes ? this.byteArr : this.toSend.getBytes();
	}

	public void setImage(byte[] toSend) {
		this.bytes = true;
		this.byteArr = toSend;
	}
}
