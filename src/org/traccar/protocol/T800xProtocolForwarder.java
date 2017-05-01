package org.traccar.protocol;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.traccar.BaseProtocolForwarder;
import org.traccar.GlobalChannelFactory;
import org.traccar.helper.Log;

public class T800xProtocolForwarder extends BaseProtocolForwarder {
  
  private final Object trafficLock = new Object();
  
  private volatile Channel outboundChannel;
  
  public T800xProtocolForwarder(T800xProtocol protocol) {
    super(protocol);
  }
  
  @Override
  protected void channelConnected(Channel channel) {
    final String headerLog = headerLog(channel);
    try {
      
      // suspend incoming traffic until client bootstrap connected
      // or failed to attempt to the remote host
      final Channel inboundChannel = channel;
      //inboundChannel.setReadable(false);
      
      // log it
      Log.debug(headerLog
          + "T800xProtocolForwarder setup client bootstrap , with "
          + ": handler = ForwarderOutboundHandler , remoteHost = "
          + getRemoteHost() + " , remotePort = " + getRemotePort());
      
      // setup client bootstrap
      ClientBootstrap clientBootstrap = new ClientBootstrap();
      clientBootstrap.setFactory(GlobalChannelFactory.getClientFactory());
      
      // add forwarder outbound handler
      clientBootstrap.getPipeline().addLast("T800xForwarderOutboundHandler",
          new T800xProtocolForwarderOutboundHandler(headerLog, inboundChannel));
      
      // connect to remote host
      ChannelFuture channelFuture = clientBootstrap
          .connect(new InetSocketAddress(getRemoteHost(), getRemotePort()));
      
      // setup outbound channel
      outboundChannel = channelFuture.getChannel();
      
      // validate the future connection
      channelFuture.addListener(new ChannelFutureListener() {
        public void operationComplete(ChannelFuture channelFuture) {
          Log.debug(headerLog + "T800xProtocolForwarder client bootstrap "
              + "on operation complete : isSuccess = "
              + channelFuture.isSuccess());
          // resume incoming traffic because the client bootstrap
          // is connected and ready
          //inboundChannel.setReadable(true);
        }
      });
      
    } catch (Exception e) {
      Log.warning(headerLog
          + "T800xProtocolForwarder failed channel connected , " + e);
    }
  }
  
  @Override
  protected void channelDisconnected(Channel channel) {
    final String headerLog = headerLog(channel);
    try {
      
      // log it
      Log.debug(headerLog + "T800xProtocolForwarder closing outbound channel");
      
      // close on flush for outbound channel
      closeOnFlush(outboundChannel);
      
    } catch (Exception e) {
      Log.warning(headerLog
          + "T800xProtocolForwarder failed channel disconnected , " + e);
    }
  }
  
  @Override
  protected void channelInterestChanged(Channel channel) {
    final String headerLog = headerLog(channel);
    try {
      
      // ...
      
    } catch (Exception e) {
      Log.warning(headerLog
          + "T800xProtocolForwarder failed channel interest changed , " + e);
    }
  }
  
  @Override
  protected boolean forwardMessage(Channel channel, ChannelBuffer channelBuffer) {
    boolean result = false;
    final String headerLog = headerLog(channel);
    try {
      synchronized (trafficLock) {
        
        // forward message
        write(outboundChannel, channelBuffer);
        
        // result as true
        result = true;
        
      }
    } catch (Exception e) {
      Log.warning(headerLog + "T800xProtocolForwarder failed forward message , "
          + e);
    }
    return result;
  }
  
  @Override
  protected void exceptionCaught(ExceptionEvent exceptionEvent, Channel channel) {
    final String headerLog = headerLog(channel);
    try {
      Log.debug(headerLog + "T800xProtocolForwarder caught exception "
          + exceptionEvent);
      
      // ...
      
    } catch (Exception e) {
      Log.warning(headerLog
          + "T800xProtocolForwarder failed exception caught , " + e);
    }
  }
  
  private class T800xProtocolForwarderOutboundHandler extends
      SimpleChannelUpstreamHandler {
    
    private final String headerLog;
    private final Channel inboundChannel;
    
    public T800xProtocolForwarderOutboundHandler(String headerLog,
        Channel inboundChannel) {
      this.headerLog = headerLog;
      this.inboundChannel = inboundChannel;
    }
    
    @Override
    public void channelConnected(ChannelHandlerContext channelHandlerContext,
        ChannelStateEvent channelStateEvent) throws Exception {
      Log.debug(headerLog
          + "T800xProtocolForwarderOutboundHandler channel connected");
    }
    
    @Override
    public void channelDisconnected(
        ChannelHandlerContext channelHandlerContext,
        ChannelStateEvent channelStateEvent) throws Exception {
      try {
        Log.debug(headerLog
            + "T800xProtocolForwarderOutboundHandler channel disconnected");
        
        Log.debug(headerLog
            + "T800xProtocolForwarderOutboundHandler closing inbound channel");
        
        // close on flush for inbound channel
        closeOnFlush(inboundChannel);
        
      } catch (Exception e) {
        Log.warning(headerLog
            + "T800xProtocolForwarderOutboundHandler failed channel disconnected , "
            + e);
      }
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext channelHandlerContext,
        final MessageEvent messageEvent) {
      ChannelBuffer channelBuffer = (ChannelBuffer) messageEvent.getMessage();
      
      Log.debug(headerLog
          + "T800xProtocolForwarderOutboundHandler message received : "
          + ChannelBuffers.hexDump(channelBuffer));
      
      // has command inside ?
     /* 
      String commandText = T800xProtocol.readCommandText(ChannelBuffers
          .copiedBuffer(channelBuffer));
      if ((commandText == null) || (commandText.equals(""))) {
        return;
      }
    */  
      // if found command than forward back to inbound channel
      
      synchronized (trafficLock) {
        String commandText = "readCommandText Not Implemented";
        Log.debug(headerLog
            + "T800xProtocolForwarderOutboundHandler forward command : "
            + commandText);
        
        // forward to inbound
        write(inboundChannel, ChannelBuffers.copiedBuffer(channelBuffer));
        
      }
      
    }
    
    @Override
    public void channelInterestChanged(
        ChannelHandlerContext channelHandlerContext,
        ChannelStateEvent channelStateEvent) {
      // ... nothing to do
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext,
        ExceptionEvent exceptionEvent) {
      Log.debug(headerLog
          + "T800xProtocolForwarderOutboundHandler caught exception "
          + exceptionEvent);
    }
    
  } // private class T800xProtocolForwarderOutboundHandler
  
}
