package ru.spbau.mit.java.server.stat;


import lombok.Data;

@Data
public class OneRequestStats {
    private final long receiveSendGapNs;
    private final long requestProcNs;
}
