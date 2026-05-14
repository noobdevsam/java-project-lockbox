package com.lockbox.bridge;

import com.lockbox.db.VaultDAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class NativeHost {
    static void main(String[] args) {
        MessageDispatcher dispatcher = new MessageDispatcher(new VaultDAO());
        
        try (InputStream in = System.in;
             OutputStream out = System.out) {
            
            while (true) {
                byte[] lenBytes = new byte[4];
                int read = in.read(lenBytes);
                if (read == -1) {
                    System.err.println("NativeHost: Input stream closed. Exiting.");
                    break;
                }
                if (read != 4) {
                    System.err.println("NativeHost: Read " + read + " bytes, expected 4.");
                    continue;
                }
                
                int len = ByteBuffer.wrap(lenBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
                System.err.println("NativeHost: Received message length: " + len);
                
                byte[] msgBytes = new byte[len];
                int totalRead = 0;
                while (totalRead < len) {
                    int r = in.read(msgBytes, totalRead, len - totalRead);
                    if (r == -1) break;
                    totalRead += r;
                }
                
                String request = new String(msgBytes, StandardCharsets.UTF_8);
                String response = dispatcher.handle(request);
                byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
                
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
