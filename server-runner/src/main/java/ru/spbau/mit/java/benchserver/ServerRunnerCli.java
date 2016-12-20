package ru.spbau.mit.java.benchserver;


import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerRunnerCli {
    public static void main(String[] args) throws IOException {
        ServerRunner server = new ServerRunner(6666);

        server.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("CMD >> ");
            String str = br.readLine();
            if (str == null || str.equals("stop")) {
                server.stop();
                break;
            }
        }
    }
}
