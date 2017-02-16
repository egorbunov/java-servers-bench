package ru.spbau.mit.java.client;


import lombok.extern.slf4j.Slf4j;
import ru.spbau.mit.java.commons.proto.IntArrayMsg;

import java.io.DataOutputStream;
import java.io.IOException;

@Slf4j
class Request {
    static void writeRequest(IntArrayMsg array, DataOutputStream out) throws IOException {
        byte[] msg = array.toByteArray();
//        log.debug("Writing request with len: " + msg.length);
        out.writeInt(msg.length);
        out.flush();
        out.write(msg);
//        log.debug("OK! " + msg.length);
        out.flush();
    }
}
