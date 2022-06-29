package remote.procedure.main;

import remote.procedure.server.HelloService;
import remote.procedure.server.Server;
import remote.procedure.server.impl.HelloServiceImpl;
import remote.procedure.server.impl.ServerCenter;

import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author yuyueq
 * @since 2022-06-29
 */
public class RPCServerTest {
    public static void main(String[] args) throws IOException {
        new Thread(() -> {
            try {
                //服务中心
                Server serviceServer = new ServerCenter(8888);
                //将需要的接口及实现类注册到服务中心
                serviceServer.register(HelloService.class, HelloServiceImpl.class);
                serviceServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
