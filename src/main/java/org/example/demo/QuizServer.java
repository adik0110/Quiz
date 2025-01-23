package org.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class QuizServer {
    private static final String[] QUESTIONS = {
            "What is the capital of France?",
            "What is 2 + 2?",
            "What is the color of the sky?",
            "Which programming language is this game written in?"
    };
    private static final String[] ANSWERS = {"Paris", "4", "Blue", "Java"};

    private static int currentQuestionIndex = 0;
    private static int player1Score = 0;
    private static int player2Score = 0;
    private static volatile String player1Answer = null;
    private static volatile String player2Answer = null;

    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Player 1 connected");
            Socket opponentSocket = serverSocket.accept();
            System.out.println("Player 2 connected");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedReader opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 PrintWriter opponentOut = new PrintWriter(opponentSocket.getOutputStream(), true)) {

                while (currentQuestionIndex < QUESTIONS.length) {
                    String question = QUESTIONS[currentQuestionIndex];
                    player1Answer = null;
                    player2Answer = null;

                    // Отправляем вопрос обоим игрокам
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

                        // Обновляем таймер только раз в секунду
                        if (currentTime - lastTimerUpdate >= 1000) {
                            out.println("TIMER:" + timeLeft);
                            opponentOut.println("TIMER:" + timeLeft);
                            lastTimerUpdate = currentTime;
                        }

                        // Проверяем ответ от первого игрока
                        if (in.ready()) {
                            player1Answer = in.readLine();

                            if (player1Answer != null && player1Answer.equalsIgnoreCase(ANSWERS[currentQuestionIndex])) {
                                player1Score++;
                                out.println("STOP");
                                opponentOut.println("STOP");
                                out.println("RESULT:Correct! You earned a point!");
                                opponentOut.println("RESULT:Player 1 answered correctly!");
                                questionEnded = true; // Завершаем вопрос
                            } else {
                                out.println("RESULT:Incorrect!");
                            }
                        }

                        // Проверяем ответ от второго игрока
                        if (opponentIn.ready()) {
                            player2Answer = opponentIn.readLine();

                            if (player2Answer != null && player2Answer.equalsIgnoreCase(ANSWERS[currentQuestionIndex])) {
                                player2Score++;
                                out.println("STOP");
                                opponentOut.println("STOP");
                                opponentOut.println("RESULT:Correct! You earned a point!");
                                out.println("RESULT:Player 2 answered correctly!");
                                questionEnded = true; // Завершаем вопрос
                            } else {
                                opponentOut.println("RESULT:Incorrect!");
                            }
                        }

                        // Добавляем небольшую задержку
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println((currentTime - startTime));
                        out.println("PROGRESS:" + (currentTime - startTime));
                        opponentOut.println("PROGRESS:" + (currentTime - startTime));
                    }

                    // Если время вышло и никто не ответил правильно
                    if (!questionEnded) {
                        out.println("TIMER:0");
                        opponentOut.println("TIMER:0");
                        out.println("STOP");
                        opponentOut.println("STOP");
                        out.println("RESULT:Time's up! No one answered correctly.");
                        opponentOut.println("RESULT:Time's up! No one answered correctly.");
                    }

                    // Обновляем счет
                    out.println("SCORE_UPDATE:" + player1Score);
                    opponentOut.println("SCORE_UPDATE:" + player2Score);

                    // Пауза перед следующим вопросом
                    try {
                        Thread.sleep(3000); // Ждем 3 секунды
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    currentQuestionIndex++;
                }

                // Отправка финального результата
                if (player1Score > player2Score) {
                    out.println("RESULT:You won!");
                    opponentOut.println("RESULT:You lost!");
                } else if (player1Score < player2Score) {
                    out.println("RESULT:You lost!");
                    opponentOut.println("RESULT:You won!");
                } else {
                    out.println("RESULT:Draw!");
                    opponentOut.println("RESULT:Draw!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}