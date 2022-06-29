package remote.procedure.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * <p>
 * 客户端具体实现
 * </p>
 *
 * @author yuyueq
 * @since 2022-06-28
 */
public class Client {
    //获取代表服务端接口的动态代理对象（HelloService）
    //serviceInterface：请求的接口名
    //addr：待请求的服务端的ip:端口
    @SuppressWarnings("unchecked")
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress addr) {
        /**
         * newProxyInstance(a,b,c)
         *
         * a：类加载器：需要代理哪个类（例如HelloService）接口，就需要将HelloService的类加载器传入第一个参数
         * b：需要代理的对象，具备哪些方法  --接口（java中是单继承，多实现，所以会实现多个接口）
         * c：InvocationHandler
         */
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Socket socket = null;
                        ObjectOutputStream output = null;
                        ObjectInputStream input = null;
                        try {
                            //客户端向服务端发送请求，请求某一个具体的接口
                            socket = new Socket();
                            socket.connect(addr);

                            //将远程服务调用所需的接口类、方法名、参数列表等编码后发送给服务提供者
                            output = new ObjectOutputStream(socket.getOutputStream());
                            output.writeUTF(serviceInterface.getName());
                            output.writeUTF(method.getName());
                            output.writeObject(method.getParameterTypes());
                            output.writeObject(args);

                            //同步阻塞等待服务器返回应答，获取应答后返回
                            input = new ObjectInputStream(socket.getInputStream());
                            return input.readObject();
                        } finally {
                            if (socket != null) socket.close();
                            if (output != null) output.close();
                            if (input != null) input.close();
                        }
                    }
                });
    }
}
