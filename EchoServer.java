import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Bostjan
 * Date: 26.4.2013
 * Time: 9:35
 * Source  http://docs.jboss.org/netty/3.2/guide/html/start.html#d0e375
 */
public class EchoServer {
     public static void main(String[] args) {

            int portNumber=8080;
                if (args.length >= 1) {
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println(String.format("Invalid argument: %s. Should be integer.", args[0]));
                System.exit(1);
            }
        }

        EchoServer echo = new EchoServer(portNumber);   // 8080 default port used
    }

    public EchoServer(int port) {
        //NioServerSocketChannelFactory  is the Reactor passing connections to event handlers
        ChannelFactory factory =
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool() , //Boss thread is the  Synchronous Event Demultiplexer
                        Executors.newCachedThreadPool());     //Initiation Dispatcher

        ServerBootstrap bootstrap = new ServerBootstrap(factory); // ServerBootstrap is the AcceptorConnector pattern

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new EchoServerHandler());
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        bootstrap.bind(new InetSocketAddress(port));   //Java Wrapper Facade to get socket
    }

   

    //Event Handler of Reactor pattern
    public class EchoServerHandler extends SimpleChannelUpstreamHandler {

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

            Channel ch = e.getChannel();
            ch.write(e.getMessage());

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getCause().printStackTrace();

            org.jboss.netty.channel.Channel ch = e.getChannel();
            ch.close();
        }

    }

}