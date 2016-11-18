package netty.nio;

public class TimeServer {

	public static void main(String[] args) {
		new Thread(new MultiplexerTimeServer(8080), "nio-test");
	}
}
