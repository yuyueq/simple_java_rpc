package remote.procedure.server.impl;

import remote.procedure.server.HelloService;

/**
 * <p>
 *
 * </p>
 *
 * @author yuyueq
 * @since 2022-06-28
 */
public class HelloServiceImpl implements HelloService {
    public String sayHi(String name) {
        return "Hi, " + name;
    }

}
