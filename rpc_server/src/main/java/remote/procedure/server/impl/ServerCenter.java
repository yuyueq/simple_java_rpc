package remote.procedure.server.impl;

import remote.procedure.server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * 服务中心的具体实现
 * </p>
 *
 * @author yuyueq
 * @since 2022-06-28
 */
public class ServerCenter implements Server {

    //map：服务端的所有可供客户端访问的接口，都注册到该map中
    //key：接口的名字“HelloService”    value：真正的HelloService实现
    private static final HashMap<String, Class> serviceRegistry = new HashMap<String, Class>();

    //连接池：连接池中存在多个连接对象，每个连接对象都可以处理一个客户请求
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static boolean isRunning = false;

    private static int port;

    public ServerCenter(int port) {
        this.port = port;
    }

    //开启服务端服务
    @Override
    public void start() throws IOException {
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(port));
        System.out.println("start server……");
        try {
            while (true) {
                //server.accept()等待客户端连接
                //监听客户端的TCP连接，接到TCP连接后将其封装成task，由线程池执行
                //客户端每次请求一次连接（发出一次请求），则服务端从连接池中获取一个线程对象去处理
                executor.execute(new ServiceTask(server.accept()));
            }
        } finally {
            server.close();
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        executor.shutdown();
    }

    @Override
    public void register(Class serviceInterface, Class impl) {
        serviceRegistry.put(serviceInterface.getName(), impl);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPort() {
        return port;
    }

    private static class ServiceTask implements Runnable {
        Socket clent = null;

        public ServiceTask(Socket client) {
            this.clent = client;
        }

        //线程所做的事
        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try {
                //接收到客户端连接及请求，处理该请求
                //将客户端发送的码流反序列化成对象，反射调用服务实现者，获取执行结果
                input = new ObjectInputStream(clent.getInputStream());
                //由于ObjectInputStream对发送数据的顺序有严格要求，所以需要参照发送的顺序逐个接收
                String serviceName = input.readUTF();
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();//方法参数类型
                Object[] arguments = (Object[]) input.readObject();//方法参数名
                //根据客户端请求，到map中找到与之对应的具体接口
                Class serviceClass = serviceRegistry.get(serviceName);
                if (serviceClass == null) {
                    throw new ClassNotFoundException(serviceName + " not found");
                }
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                //执行该方法
                Object result = method.invoke(serviceClass.newInstance(), arguments);

                // 将执行结果反序列化，通过socket发送给客户端
                output = new ObjectOutputStream(clent.getOutputStream());
                output.writeObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (clent != null) {
                    try {
                        clent.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}

