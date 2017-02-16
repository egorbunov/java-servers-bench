package ru.spbau.mit.java.benchserver;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ServerRunnerCli {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("USAGE: java -jar server.jar [PORT]");
            System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Bad port. Must be integer");
            System.exit(1);
        }

        ServerRunner server = null;
        try {
            server = new ServerRunner(port);
        } catch (IOException e) {
            System.err.println("Error starting server: " + e);
            System.exit(1);
        }

        server.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("CMD >> ");
            String str = null;
            try {
                str = br.readLine();
            } catch (IOException e) {
                System.err.println("Error reading line: " + e);
                System.exit(1);
            }
            if (str == null || str.equals("stop")) {
                try {
                    server.stop();
                } catch (IOException e) {
                    System.err.println("Error stopping server: " + e);
                    System.exit(1);
                }
                break;
            }
        }
    }
}
