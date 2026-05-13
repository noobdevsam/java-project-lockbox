package com.lockbox.bridge;

import java.io.*;
import java.nio.*;
import com.lockbox.db.VaultDAO;

public class NativeHost {
    public static void main(String[] args) {
        MessageDispatcher dispatcher = new MessageDispatcher(new VaultDAO());
        
        try (InputStream in = System.in;
             OutputStream out = System.out) {
            
            while (true) {
                byte[] lenBytes = new byte[4];
                if (in.read(lenBytes) != 4) break;
                
                int len = ByteBuffer.wrap(lenBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
                byte[] msgBytes = new byte[len];
                
                int read = 0;
                while (read < len) {
                    read += in.read(msgBytes, read, len - read);
                }
                
                String request = new String(msgBytes, "UTF-8");
                String response = dispatcher.handle(request);
                byte[] respBytes = response.getBytes("UTF-8");
                
                byte[] respLen = ByteBuffer.allocate(4)
                                           .order(ByteOrder.LITTLE_ENDIAN)
                                           .putInt(respBytes.length)
                                           .array();
                
                out.write(respLen);
                out.write(respBytes);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error in NativeHost: " + e.getMessage());
        }
    }
}
