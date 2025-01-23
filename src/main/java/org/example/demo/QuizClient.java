package org.example.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
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
    private PrintWriter out;
    private BufferedReader in;
    private Label questionLabel;
    private Label scoreLabel;
    private Label timerLabel;
    private TextField answerField;
    private Label resultLabel;
    private ProgressBar progressBar;
    private ImageView characterView;

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
        timerLabel = new Label("Time left: 8");
        timerLabel.setFont(Font.font("Arial", 16));
        timerLabel.setStyle("-fx-text-fill: #555555;");

        // Прогресс-бар
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: #4CAF50;");

        // Поле для ответа
        answerField = new TextField();
        answerField.setPromptText("Enter your answer...");
        answerField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        // Кнопка отправки
        Button submitButton = new Button("Submit");
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
        HBox characterContainer = new HBox();
        characterContainer.setStyle("-fx-alignment: bottom-right;");
        characterContainer.getChildren().add(characterView);

        root.getChildren().addAll(questionLabel, scoreLabel, timerLabel, progressBar, answerField, submitButton, resultLabel, characterContainer);

        Scene scene = new Scene(root, 550, 450); // Увеличили высоту для человечка
        primaryStage.setTitle("Player 1");
        primaryStage.setScene(scene);
        primaryStage.show();

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
                    Platform.runLater(() -> {
                        questionLabel.setText(question);
                        progressBar.setProgress(0);

                        characterView.setImage(new Image(getClass().getResourceAsStream("/static/gif/walk.gif")));
                    });
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
                        resultLabel.setVisible(true);
                    });
                } else if (message.startsWith("PROGRESS:")) {
                    String timeLeft = message.substring(9);
                    Platform.runLater(() -> {
                        double progress = Double.parseDouble(timeLeft) / 10000 * 1.16; // 8 секунд - общее время
                        progressBar.setProgress(progress);
                    });
                } else if (message.equals("STOP")) {
                    Platform.runLater(() -> {
                        characterView.setImage(new Image(getClass().getResourceAsStream("/static/img/stand.png")));
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