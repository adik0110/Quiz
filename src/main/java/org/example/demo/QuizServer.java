package org.example.demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class QuizServer {
    private static final String[] QUESTIONS = {
            "Какая столица Франции?",
            "Сколько будет 2 + 2?",
            "Какого цвета небо?",
            "На каком языке программирования написано это приложение?"
    };
    private static final String[] ANSWERS = {"Париж", "4", "Голубое", "Java"};

    private static int currentQuestionIndex = 0;
    private static int player1Score = 0;
    private static int player2Score = 0;
    private static volatile String player1Answer = null;
    private static volatile String player2Answer = null;
    private static volatile String player1Name = null;
    private static volatile String player2Name = null;

    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            Socket clientSocket = serverSocket.accept();
            System.out.println("Player 1 connected");
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("ENTER_NAME");
            player1Name = in.readLine();
            System.out.println("Player 1 name: " + player1Name);
            out.println("WAITING_FOR_PLAYER_2");

            Socket opponentSocket = serverSocket.accept();
            System.out.println("Player 2 connected");
            BufferedReader opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
            PrintWriter opponentOut = new PrintWriter(opponentSocket.getOutputStream(), true);

            opponentOut.println("ENTER_NAME");
            player2Name = opponentIn.readLine();
            System.out.println("Player 2 name: " + player2Name);

            out.println("START_GAME");
            opponentOut.println("START_GAME");

            while (currentQuestionIndex < QUESTIONS.length) {
                String question = QUESTIONS[currentQuestionIndex];
                player1Answer = null;
                player2Answer = null;

                out.println("QUESTION:" + question);
                opponentOut.println("QUESTION:" + question);
                out.println("RESULT:" + " ");
                opponentOut.println("RESULT:" + " ");

                long startTime = System.currentTimeMillis();
                long lastTimerUpdate = 0;
                boolean questionEnded = false;

                while (System.currentTimeMillis() - startTime < 8700 && !questionEnded) {
                    long currentTime = System.currentTimeMillis();
                    long timeLeft = 8 - (currentTime - startTime) / 1000;

                    if (currentTime - lastTimerUpdate >= 1000) {
                        out.println("TIMER:" + timeLeft);
                        opponentOut.println("TIMER:" + timeLeft);
                        lastTimerUpdate = currentTime;
                    }

                    if (in.ready()) {
                        player1Answer = in.readLine();

                        if (player1Answer != null && player1Answer.equalsIgnoreCase(ANSWERS[currentQuestionIndex])) {
                            player1Score++;
                            out.println("STOP");
                            opponentOut.println("STOP");
                            out.println("RESULT:Правильно! Вы зарабатываете очко!");
                            opponentOut.println("RESULT:" + player1Name + " ответил правильно!");
                            questionEnded = true; // Завершаем вопрос
                        } else {
                            out.println("RESULT:Неверный ответ!");
                        }
                    }

                    if (opponentIn.ready()) {
                        player2Answer = opponentIn.readLine();

                        if (player2Answer != null && player2Answer.equalsIgnoreCase(ANSWERS[currentQuestionIndex])) {
                            player2Score++;
                            out.println("STOP");
                            opponentOut.println("STOP");
                            opponentOut.println("RESULT:Правильно! Вы зарабатываете очко!");
                            out.println("RESULT:" + player2Name + " ответил правильно!");
                            questionEnded = true; // Завершаем вопрос
                        } else {
                            opponentOut.println("RESULT:Неверный ответ!");
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println((currentTime - startTime));
                    out.println("PROGRESS:" + (currentTime - startTime));
                    opponentOut.println("PROGRESS:" + (currentTime - startTime));
                }

                if (!questionEnded) {
                    out.println("TIMER:0");
                    opponentOut.println("TIMER:0");
                    out.println("STOP");
                    opponentOut.println("STOP");
                    out.println("RESULT:Время вышло! Никто не ответил правильно.");
                    opponentOut.println("RESULT:Время вышло! Никто не ответил правильно.");
                }

                out.println("SCORE_UPDATE:" + player1Score);
                opponentOut.println("SCORE_UPDATE:" + player2Score);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                currentQuestionIndex++;
            }

            String finalScores = "FINAL_SCORE:" + player1Score + "," + player2Score + "," + player1Name + "," + player2Name;
            out.println(finalScores);
            opponentOut.println(finalScores);

            if (player1Score > player2Score) {
                out.println("RESULT:Вы выиграли!");
                opponentOut.println("RESULT:Вы проиграли!");
            } else if (player1Score < player2Score) {
                out.println("RESULT:Вы проиграли!");
                opponentOut.println("RESULT:Вы выиграли!");
            } else {
                out.println("RESULT:Ничья!");
                opponentOut.println("RESULT:Ничья!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}