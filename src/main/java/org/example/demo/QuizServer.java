package org.example.demo;

import java.io.*;
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
    private static volatile String player1Name = null;
    private static volatile String player2Name = null;

    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            // Ожидаем подключения первого игрока
            Socket clientSocket = serverSocket.accept();
            System.out.println("Player 1 connected");
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Запрашиваем имя первого игрока
            out.println("ENTER_NAME");
            player1Name = in.readLine();
            System.out.println("Player 1 name: " + player1Name);
            out.println("WAITING_FOR_PLAYER_2");

            // Ожидаем подключения второго игрока
            Socket opponentSocket = serverSocket.accept();
            System.out.println("Player 2 connected");
            BufferedReader opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
            PrintWriter opponentOut = new PrintWriter(opponentSocket.getOutputStream(), true);

            // Запрашиваем имя второго игрока
            opponentOut.println("ENTER_NAME");
            player2Name = opponentIn.readLine();
            System.out.println("Player 2 name: " + player2Name);

            // Уведомляем обоих игроков о начале игры
            out.println("START_GAME");
            opponentOut.println("START_GAME");

            // Основной игровой цикл
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
                            out.println("RESULT:Правильно! Вы зарабатываете очко!");
                            opponentOut.println("RESULT:" + player1Name + " ответил правильно!");
                            questionEnded = true; // Завершаем вопрос
                        } else {
                            out.println("RESULT:Неверный ответ!");
                        }
                    }

                    // Проверяем ответ от второго игрока
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
                    out.println("RESULT:Время вышло! Никто не ответил правильно.");
                    opponentOut.println("RESULT:Время вышло! Никто не ответил правильно.");
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
            String finalScores = "FINAL_SCORE:" + player1Score + "," + player2Score + "," + player1Name + "," + player2Name;
            out.println(finalScores);
            opponentOut.println(finalScores);

            // Отправка текстового результата
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