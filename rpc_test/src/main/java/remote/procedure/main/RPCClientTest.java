package remote.procedure.main;

import remote.procedure.client.Client;
import remote.procedure.server.HelloService;

import java.net.InetSocketAddress;

/**
 * <p>
 *
 * </p>
 *
 * @author yuyueq
 * @since 2022-06-29
 */
public class RPCClientTest {
    public static void main(String[] args) {
        HelloService service = Client.getRemoteProxyObj(HelloService.class, new InetSocketAddress("127.0.0.1", 8888));
        System.out.println(service.sayHi("Hello! yuyueq_java_rpc"));
    }
}
