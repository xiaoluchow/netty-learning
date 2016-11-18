package netty.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {

	private Selector selector;
	
	private ServerSocketChannel serverSocketChannel;
	
	private volatile boolean stop;
	
	public MultiplexerTimeServer(int port) {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress(port), 1024);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void run() {
		while (!stop) {
			try {
				selector.select(1000L);
				Set<SelectionKey> selectedKeySet = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeySet.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					if (key != null) {
						key.cancel();
						if (key.channel() != null) {
							key.channel().close();
						}
					}
					handleInput(key);
				}
				if (selector != null) {
					selector.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void handleInput(SelectionKey key) {
		if (key.isValid()) {
			if (key.isAcceptable()) {
				ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
				try {
					SocketChannel socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);
					socketChannel.register(selector, SelectionKey.OP_READ);
					if (key.isReadable()) {
						SocketChannel sc = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						int readByte = sc.read(buffer);
						if (readByte > 0) {
							buffer.flip();
							byte[] bytes = new byte[buffer.remaining()];
							buffer.get(bytes);
							String body = new String(bytes, "utf-8");
							System.out.println("server accept:" + body);
							doWrite(sc, body);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	private void doWrite(SocketChannel sc, String body) {
		if (body != null && body.trim().length() > 0) {
			try {
				byte[] bytes = body.getBytes("utf8");
				ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
				byteBuffer.put(bytes);
				byteBuffer.flip();
				sc.write(byteBuffer);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
 