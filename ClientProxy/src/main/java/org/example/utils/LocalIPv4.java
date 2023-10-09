package org.example.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class LocalIPv4 {
    public static String get() throws IOException {
        Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
        Pattern PATTERN = Pattern.compile(
                "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        while (n.hasMoreElements()) {
            NetworkInterface e = n.nextElement();
            Enumeration<InetAddress> a = e.getInetAddresses();
            while (a.hasMoreElements()) {
                String addr = a.nextElement().getHostAddress();
                if (PATTERN.matcher(addr).matches() && !addr.startsWith("127")) {
                    return addr;
                }
            }
        }
        return null;
    }
}
