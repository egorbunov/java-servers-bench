package ru.spbau.mit.java.benchserver;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ServerRunnerCli {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("USAGE: java -jar server.jar [PORT]");
        }
        ServerRunner server = new ServerRunner(Integer.valueOf(args[0]));

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
