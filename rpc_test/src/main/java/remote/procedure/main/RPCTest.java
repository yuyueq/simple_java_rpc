package remote.procedure.main;

import remote.procedure.client.Client;
import remote.procedure.server.HelloService;
import remote.procedure.server.Server;
import remote.procedure.server.impl.HelloServiceImpl;
import remote.procedure.server.impl.ServerCenter;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author yuyueq
 * @since 2022-06-28
 */

public class RPCTest {
    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Server serviceServer = new ServerCenter(8888);
                    serviceServer.register(HelloService.class, HelloServiceImpl.class);
                    serviceServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        HelloService service = Client.getRemoteProxyObj(HelloService.class, new InetSocketAddress("127.0.0.1", 8888));
        System.out.println(service.sayHi("Hello! yuyueq_java_rpc"));
    }
}
