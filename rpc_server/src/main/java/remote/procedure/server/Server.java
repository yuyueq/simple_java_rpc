package remote.procedure.server;

import java.io.IOException;

public interface Server {

    void start() throws IOException;

    void stop();

    void register(Class serviceInterface, Class impl);

    boolean isRunning();

    int getPort();
}
