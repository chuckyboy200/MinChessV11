package com.cb200.minchess.main;

import java.util.Scanner;

import com.cb200.minchess.perft.Perft;

public class UCI {
    
    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            switch(input) {
                case "uci":
                    System.out.println("id name Your Chess Engine");
                    System.out.println("id author Your Name");
                    // additional identification info here
                    System.out.println("uciok");
                    break;
                case "isready":
                    System.out.println("readyok");
                    break;
                case "ucinewgame": // start a new game
                    break;
                case "position": // set up the board position                  
                    break;
                case "go": // start calculating the best move
                    break;
                case "quit":
                    scanner.close();
                    return;
                case "perft":
                    Perft.all();
            }
        }
    }
}
