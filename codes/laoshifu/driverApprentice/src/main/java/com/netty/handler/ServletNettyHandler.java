package com.netty.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

public class ServletNettyHandler extends
		SimpleChannelInboundHandler<FullHttpRequest> {

	private final Servlet servlet;

	private final ServletContext servletContext;

	public ServletNettyHandler(Servlet servlet) {
		this.servlet = servlet;
		this.servletContext = servlet.getServletConfig().getServletContext();
	}

	private MockHttpServletRequest createServletRequest(
			FullHttpRequest fullHttpRequest) {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest(
				this.servletContext);
		try {
			UriComponents uriComponents = UriComponentsBuilder.fromUriString(
					fullHttpRequest.uri()).build();

			servletRequest.setRequestURI(uriComponents.getPath());
			servletRequest.setPathInfo(uriComponents.getPath());

			servletRequest
					.setMethod(fullHttpRequest.method().name().toString());

			if (uriComponents.getScheme() != null) {
				servletRequest.setScheme(uriComponents.getScheme());
			}
			if (uriComponents.getHost() != null) {
				servletRequest.setServerName(uriComponents.getHost());
			}
			if (uriComponents.getPort() != -1) {
				servletRequest.setServerPort(uriComponents.getPort());
			}

			Set headerNameSet = fullHttpRequest.headers().names();
			Iterator headerNames = headerNameSet.iterator();
			while (headerNames.hasNext()) {
				String name = headerNames.next().toString();
				servletRequest.addHeader(name,
						fullHttpRequest.headers().get(name));
			}

			// for (String name : fullHttpRequest.headers().names()) {
			// servletRequest.addHeader(name,
			// fullHttpRequest.headers().get(name));
			// }

			ByteBuf buf = fullHttpRequest.content();
			int readable = buf.readableBytes();
			byte[] bytes = new byte[readable];
			buf.readBytes(bytes);
			String contentStr = UriUtils.decode(new String(bytes, "UTF-8"),
					"UTF-8");
			for (String params : contentStr.split("&")) {
				String[] para = params.split("=");
				if (para.length > 1) {
					servletRequest.addParameter(para[0], para[1]);
				} else {
					servletRequest.addParameter(para[0], "");
				}
			}

			if (uriComponents.getQuery() != null) {
				String query = UriUtils.decode(uriComponents.getQuery(),
						"UTF-8");
				servletRequest.setQueryString(query);
			}

			for (Entry<String, List<String>> entry : uriComponents
					.getQueryParams().entrySet()) {
				for (String value : entry.getValue()) {
					servletRequest.addParameter(
							UriUtils.decode(entry.getKey(), "UTF-8"),
							UriUtils.decode(value, "UTF-8"));
				}
			}
		} catch (UnsupportedEncodingException ex) {
			// shouldn't happen
		}

		return servletRequest;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			sendError(ctx, INTERNAL_SERVER_ERROR);
		}
	}

	private static void sendError(ChannelHandlerContext ctx,
			HttpResponseStatus status) {
		ByteBuf content = Unpooled.copiedBuffer("Failure: " + status.toString()
				+ "\r\n", CharsetUtil.UTF_8);

		FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
				HTTP_1_1, status, content);
		fullHttpResponse.headers().add(CONTENT_TYPE,
				"text/plain; charset=UTF-8");

		// Close the connection as soon as the error message is sent.
		ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	protected void messageReceived(ChannelHandlerContext channelHandlerContext,
			FullHttpRequest fullHttpRequest) throws Exception {
		
		if (!fullHttpRequest.decoderResult().isSuccess()) {
			sendError(channelHandlerContext, BAD_REQUEST);
			return;
		}

		MockHttpServletRequest servletRequest = createServletRequest(fullHttpRequest);
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();

		this.servlet.service(servletRequest, servletResponse);

		HttpResponseStatus status = HttpResponseStatus.valueOf(servletResponse
				.getStatus());
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		for (String name : servletResponse.getHeaderNames()) {
			for (Object value : servletResponse.getHeaderValues(name)) {
				response.headers().addObject(name, value);
			}
		}

		// Write the initial line and the header.
		channelHandlerContext.write(response);

		InputStream contentStream = new ByteArrayInputStream(
				servletResponse.getContentAsByteArray());

		// Write the content and flush it.
		ChannelFuture writeFuture = channelHandlerContext
				.writeAndFlush(new ChunkedStream(contentStream));
		writeFuture.addListener(ChannelFutureListener.CLOSE);
		//主动释放资源，当多个handler传递时候，在最后一个释放资源，每个handler都释放也不报错
		fullHttpRequest.retain();
		

	}
}
