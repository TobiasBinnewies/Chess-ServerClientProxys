package org.example.RPC;

public class RpcMessage {
    private final int code;
    private final String[] args;
    public static char ARGS = 0x1E;
    public static char TYPE = 0x1F;

    public RpcMessage(int code, String... s) {
        this.code = code;
        this.args = s;
    }

    public RpcMessage(String msg) {
        String[] parts = msg.split(String.valueOf(TYPE));
        code = Integer.parseInt(parts[0]);
        if (parts.length == 1) {
            args = new String[0];
            return;
        }
        args = parts[1].split(String.valueOf(ARGS));
    }

    public int getCode() {
        return code;
    }

    public String getArg(int i) {
        return args[i];
    }

    public String getMessage() {
        return String.join(String.valueOf(','), args);
    }

    @Override
    public String toString() {
        return String.valueOf(code) +
                TYPE +
                String.join(String.valueOf(ARGS), args);
    }
}
