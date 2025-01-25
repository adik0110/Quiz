package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class QuizClient extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int TOTAL_QUESTIONS = 4; // Общее количество вопросов

    private PrintWriter out;
    private BufferedReader in;
    private Label questionLabel;
    private Label scoreLabel;
    private Label timerLabel;
    private TextField answerField;
    private Label resultLabel;
    private ProgressBar progressBar;
    private ImageView characterView;
    private String playerName;

    private VBox root;
    private TextField nameField;
    private Button startButton;
    private HBox characterContainer;
    private Button submitButton;
    private Label namePromptLabel;

    private int player1Score = 0; // Счет игрока 1
    private int player2Score = 0; // Счет игрока 2
    private String player1Name = ""; // Имя игрока 1
    private String player2Name = ""; // Имя игрока 2

    @Override
    public void start(Stage primaryStage) {
        root = new VBox(20);
        root.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 20;");

        // Надпись "Введите свое имя"
        namePromptLabel = new Label("Введите свое имя:");
        namePromptLabel.setFont(Font.font("Arial", 16));
        namePromptLabel.setStyle("-fx-text-fill: #333333;");

        // Поле для ввода имени
        nameField = new TextField();
        nameField.setPromptText("Имя");
        nameField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        // Кнопка "Start"
        startButton = new Button("Начать игру");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        startButton.setOnAction(e -> startGame());

        // Добавляем надпись, поле ввода имени и кнопку в корневой контейнер
        root.getChildren().addAll(namePromptLabel, nameField, startButton);

        // Заголовок вопроса
        questionLabel = new Label("Ждем 2 игрока");
        questionLabel.setFont(Font.font("Arial", 18));
        questionLabel.setStyle("-fx-text-fill: #333333;");

        // Счет
        scoreLabel = new Label("Счет: 0");
        scoreLabel.setFont(Font.font("Arial", 16));
        scoreLabel.setStyle("-fx-text-fill: #555555;");

        // Таймер
        timerLabel = new Label("Осталось времени: 8");
        timerLabel.setFont(Font.font("Arial", 16));
        timerLabel.setStyle("-fx-text-fill: #555555;");

        // Прогресс-бар
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: #4CAF50;");

        // Поле для ответа
        answerField = new TextField();
        answerField.setPromptText("Введите ответ");
        answerField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        // Кнопка отправки ответа
        submitButton = new Button("Отправить");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        submitButton.setOnAction(e -> sendAnswer());

        // Результат
        resultLabel = new Label();
        resultLabel.setFont(Font.font("Arial", 14));
        resultLabel.setStyle("-fx-text-fill: #0000AA;");
        resultLabel.setVisible(false);

        // ImageView для отображения человечка
        characterView = new ImageView();
        characterView.setFitWidth(100);
        characterView.setFitHeight(100);
        characterView.setPreserveRatio(true);

        // Контейнер для человечка
        characterContainer = new HBox();
        characterContainer.setStyle("-fx-alignment: bottom-right;");
        characterContainer.getChildren().add(characterView);

        Scene scene = new Scene(root, 550, 450);
        primaryStage.setTitle("Игра-квиз");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame() {
        playerName = nameField.getText().trim();

        // Проверка, что имя не пустое
        if (playerName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Имя не может быть пустым");
            alert.setContentText("Пожалуйста, введите ваше имя.");
            alert.showAndWait();
            return;
        }

        // Скрываем поле ввода имени, надпись и кнопку "Start"
        root.getChildren().removeAll(namePromptLabel, nameField, startButton);

        // Добавляем интерфейс игры
        root.getChildren().addAll(questionLabel, scoreLabel, timerLabel, progressBar, answerField, submitButton, resultLabel, characterContainer);

        // Подключаемся к серверу
        new Thread(this::connectToServer).start();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Отправляем имя на сервер
            out.println(playerName);

            // Ожидание начала игры
            String message = in.readLine();
            if (message.equals("WAITING_FOR_PLAYER_2")) {
                Platform.runLater(() -> {
                    questionLabel.setText("Ждем 2 игрока");
                });
            }

            // Основной игровой цикл
            while ((message = in.readLine()) != null) {
                if (message.startsWith("START_GAME")) {
                    Platform.runLater(() -> {
                        questionLabel.setText("Игра началась!");
                    });
                } else if (message.startsWith("QUESTION:")) {
                    String question = message.substring(9);
                    Platform.runLater(() -> {
                        questionLabel.setText(question);
                        progressBar.setProgress(0);

                        characterView.setImage(new Image(getClass().getResourceAsStream("/static/gif/walk.gif")));
                    });
                } else if (message.startsWith("SCORE_UPDATE:")) {
                    String scoreUpdate = message.substring(13);
                    Platform.runLater(() -> {
                        scoreLabel.setText("Счет: " + scoreUpdate);
                    });
                } else if (message.startsWith("TIMER:")) {
                    String timeLeft = message.substring(6);
                    Platform.runLater(() -> timerLabel.setText("Осталось времени: " + timeLeft));
                } else if (message.startsWith("RESULT:")) {
                    String result = message.substring(7);
                    Platform.runLater(() -> {
                        resultLabel.setText(result);
                        resultLabel.setVisible(true);
                    });
                } else if (message.startsWith("PROGRESS:")) {
                    String timeLeft = message.substring(9);
                    Platform.runLater(() -> {
                        double progress = Double.parseDouble(timeLeft) / 10000 * 1.16;
                        progressBar.setProgress(progress);
                    });
                } else if (message.equals("STOP")) {
                    Platform.runLater(() -> {
                        characterView.setImage(new Image(getClass().getResourceAsStream("/static/img/stand.png")));
                    });
                } else if (message.startsWith("FINAL_SCORE:")) {
                    // Получаем финальные результаты
                    String[] scores = message.substring(12).split(",");
                    if (scores.length >= 4) { // Проверяем, что массив содержит достаточно элементов
                        player1Score = Integer.parseInt(scores[0]);
                        player2Score = Integer.parseInt(scores[1]);
                        player1Name = scores[2];
                        player2Name = scores[3];

                        // Переходим на финальный экран
                        Platform.runLater(this::showFinalResults);
                    } else {
                        System.err.println("Ошибка: некорректное сообщение FINAL_SCORE");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showFinalResults() {
        // Очищаем текущий интерфейс
        root.getChildren().clear();

        // Создаем заголовок с результатом
        Label finalResultLabel = new Label();
        if (player1Score > player2Score) {
            finalResultLabel.setText(player1Name + " выиграл(а)!");
        } else if (player1Score < player2Score) {
            finalResultLabel.setText(player2Name + " выиграл(а)!");
        } else {
            finalResultLabel.setText("Ничья!");
        }
        finalResultLabel.setFont(Font.font("Arial", 24));
        finalResultLabel.setStyle("-fx-text-fill: #333333;");

        // Создаем контейнер для гистограммы
        VBox chartContainer = new VBox(20);
        chartContainer.setStyle("-fx-alignment: center;");

        // Полоска для первого игрока
        HBox player1Bar = createBar(player1Name, player1Score, "#4CAF50"); // Зеленый цвет
        // Полоска для второго игрока
        HBox player2Bar = createBar(player2Name, player2Score, "#2196F3"); // Синий цвет

        // Добавляем полоски в контейнер
        chartContainer.getChildren().addAll(player1Bar, player2Bar);

        // Добавляем заголовок и гистограмму в корневой контейнер
        root.getChildren().addAll(finalResultLabel, chartContainer);
    }

    private HBox createBar(String playerName, int score, String color) {
        // Контейнер для полоски и текста
        VBox barContainer = new VBox(5);
        barContainer.setStyle("-fx-alignment: center;");

        // Текст с именем игрока и количеством очков
        Label scoreLabel = new Label(playerName + ": " + score + "/" + TOTAL_QUESTIONS);
        scoreLabel.setFont(Font.font("Arial", 16));
        scoreLabel.setStyle("-fx-text-fill: #333333;");

        // Полоска (ProgressBar)
        ProgressBar bar = new ProgressBar((double) score / TOTAL_QUESTIONS); // Нормализованное значение
        bar.setPrefWidth(300); // Ширина полоски
        bar.setPrefHeight(20); // Высота полоски
        bar.setStyle("-fx-accent: " + color + ";"); // Цвет полоски

        // Добавляем текст и полоску в контейнер
        barContainer.getChildren().addAll(scoreLabel, bar);

        // Общий контейнер для выравнивания
        HBox container = new HBox();
        container.setStyle("-fx-alignment: center;");
        container.getChildren().add(barContainer);

        return container;
    }

    private void sendAnswer() {
        String answer = answerField.getText();
        if (out != null && !answer.isEmpty()) {
            out.println(answer);
            answerField.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}