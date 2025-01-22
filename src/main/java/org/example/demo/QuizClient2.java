package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class QuizClient2 extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private PrintWriter out;
    private BufferedReader in;
    private Label questionLabel;
    private Label scoreLabel;
    private Label timerLabel;
    private TextField answerField;
    private Label resultLabel; // Новый Label для результата

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 20;");

        // Заголовок вопроса
        questionLabel = new Label("Waiting for question...");
        questionLabel.setFont(Font.font("Arial", 18));
        questionLabel.setStyle("-fx-text-fill: #333333;");

        // Счет
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", 16));
        scoreLabel.setStyle("-fx-text-fill: #555555;");

        // Таймер
        timerLabel = new Label("Time left: 15");
        timerLabel.setFont(Font.font("Arial", 16));
        timerLabel.setStyle("-fx-text-fill: #555555;");

        // Поле для ответа
        answerField = new TextField();
        answerField.setPromptText("Enter your answer...");
        answerField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        // Кнопка отправки
        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20;");
        submitButton.setOnAction(e -> sendAnswer());

        // Лейбл для отображения результата
        resultLabel = new Label();
        resultLabel.setFont(Font.font("Arial", 14));
        resultLabel.setStyle("-fx-text-fill: #0000AA;");
        resultLabel.setVisible(false); // Скрываем по умолчанию

        // Добавляем элементы в корневой контейнер
        root.getChildren().addAll(questionLabel, scoreLabel, timerLabel, answerField, submitButton, resultLabel);

        // Создаем сцену и настраиваем Stage
        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("Player 1");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Подключаемся к серверу
        new Thread(this::connectToServer).start();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("QUESTION:")) {
                    String question = message.substring(9);
                    Platform.runLater(() -> questionLabel.setText(question));
                } else if (message.startsWith("SCORE_UPDATE:")) {
                    String scoreUpdate = message.substring(13);
                    Platform.runLater(() -> scoreLabel.setText("Score: " + scoreUpdate));
                } else if (message.startsWith("TIMER:")) {
                    String timeLeft = message.substring(6);
                    Platform.runLater(() -> timerLabel.setText("Time left: " + timeLeft));
                } else if (message.startsWith("RESULT:")) {
                    String result = message.substring(7);
                    Platform.runLater(() -> {
                        resultLabel.setText(result);
                        resultLabel.setVisible(true); // Показываем результат
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
