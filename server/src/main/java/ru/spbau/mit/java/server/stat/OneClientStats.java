package ru.spbau.mit.java.server.stat;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OneClientStats {
    private final List<Long> requestProcTimes;
    private final List<Long> sortingTimes;

    public OneClientStats() {
        requestProcTimes = new ArrayList<>();
        sortingTimes = new ArrayList<>();
    }
}
