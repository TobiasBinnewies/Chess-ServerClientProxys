package org.example.rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class RpcReader extends BufferedReader {
    public RpcReader(Reader in) {
        super(in);
    }

    public RpcMessage readRpcMessage() throws IOException {
        try {
            String msg = super.readLine();
//            System.out.println(Thread.currentThread().threadId() + ": Reading: " + msg);
            return new RpcMessage(msg);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String readLine() {
        throw new RuntimeException("RpcReader#readLine Not implemented");
    }
}