package org.example.RPC;

import java.io.PrintWriter;
import java.io.Writer;

public class RpcWriter extends PrintWriter {
    public RpcWriter(Writer out) {
        super(out);
    }

    @Override
    public void println(String x) {
        // Not implemented
        throw new RuntimeException("RpcWriter#println Not implemented");
    }

    public void sendRpcMessage(RpcMessage msg) {
        System.out.println(Thread.currentThread().threadId() + ": Writing: " + msg.toString());
        super.println(msg);
        super.flush();
    }

    public void sendSuccess() {
        sendRpcMessage(new RpcMessage(0, "success"));
    }

    public void sendMessage(int code, String msg) {
        sendRpcMessage(new RpcMessage(code, msg));
    }

}
