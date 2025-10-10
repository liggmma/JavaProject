package org.example.se05fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class TicTacToeController {

    @FXML private Label statusLabel;

    @FXML private Button b00, b01, b02,
            b10, b11, b12,
            b20, b21, b22;

    private boolean xTurn = true; // X đi trước
    private boolean gameOver = false;

    @FXML
    private void handleMove(javafx.event.ActionEvent event) {
        if (gameOver) return;

        Button btn = (Button) event.getSource();
        if (!btn.getText().isEmpty()) return; // ô đã được đánh

        btn.setText(xTurn ? "X" : "O");
        if (checkWin()) {
            statusLabel.setText("Player " + (xTurn ? "X" : "O") + " wins!");
            gameOver = true;
        } else if (isBoardFull()) {
            statusLabel.setText("It's a draw!");
            gameOver = true;
        } else {
            xTurn = !xTurn;
            statusLabel.setText("Player " + (xTurn ? "X" : "O") + "'s turn");
        }
    }

    private boolean isBoardFull() {
        return !b00.getText().isEmpty() && !b01.getText().isEmpty() && !b02.getText().isEmpty() &&
                !b10.getText().isEmpty() && !b11.getText().isEmpty() && !b12.getText().isEmpty() &&
                !b20.getText().isEmpty() && !b21.getText().isEmpty() && !b22.getText().isEmpty();
    }

    private boolean checkWin() {
        String[][] board = {
                {b00.getText(), b01.getText(), b02.getText()},
                {b10.getText(), b11.getText(), b12.getText()},
                {b20.getText(), b21.getText(), b22.getText()}
        };

        // kiểm tra hàng
        for (int i = 0; i < 3; i++) {
            if (!board[i][0].isEmpty() &&
                    board[i][0].equals(board[i][1]) &&
                    board[i][1].equals(board[i][2])) return true;
        }

        // kiểm tra cột
        for (int j = 0; j < 3; j++) {
            if (!board[0][j].isEmpty() &&
                    board[0][j].equals(board[1][j]) &&
                    board[1][j].equals(board[2][j])) return true;
        }

        // kiểm tra đường chéo
        if (!board[0][0].isEmpty() &&
                board[0][0].equals(board[1][1]) &&
                board[1][1].equals(board[2][2])) return true;

        if (!board[0][2].isEmpty() &&
                board[0][2].equals(board[1][1]) &&
                board[1][1].equals(board[2][0])) return true;

        return false;
    }

    @FXML
    private void restartGame() {
        b00.setText(""); b01.setText(""); b02.setText("");
        b10.setText(""); b11.setText(""); b12.setText("");
        b20.setText(""); b21.setText(""); b22.setText("");
        xTurn = true;
        gameOver = false;
        statusLabel.setText("Player X's turn");
    }
}
