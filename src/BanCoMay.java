import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BanCoMay extends JPanel {
    private final int SIZE = 25;
    private final int CELL = 22;
    private final int OFFSET = 24;

    private int[][] board = new int[SIZE][SIZE]; // 0 trống, 1 người, 2 máy
    private boolean isPlayerTurn = true;
    private boolean gameOver = false;
    
    // Timer logic
    private Timer gameTimer;
    private int timeLeft = 60;
    private boolean timerStarted = false;
    private boolean isPlayerFirstMove = true;
    private JLabel timerLabel;

    public BanCoMay() {
        setBackground(new Color(240, 240, 240));
        setPreferredSize(new Dimension(OFFSET * 2 + SIZE * CELL, OFFSET * 2 + SIZE * CELL));

        // Khởi tạo timer
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                if (timerLabel != null) {
                    timerLabel.setText("Thời gian: " + timeLeft + "s");
                }
                
                if (timeLeft <= 0) {
                    gameTimer.stop();
                    if (isPlayerTurn) {
                        // Người chơi hết thời gian, bỏ lượt
                        JOptionPane.showMessageDialog(BanCoMay.this, "Hết thời gian! Bạn bỏ lượt.");
                        isPlayerTurn = false;
                        SwingUtilities.invokeLater(() -> mayDanh());
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver || !isPlayerTurn) return;

                int row = (e.getY() - OFFSET) / CELL;
                int col = (e.getX() - OFFSET) / CELL;

                if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return;
                if (board[row][col] != 0) return;

                board[row][col] = 1; // người
                repaint();

                // Bắt đầu timer sau nước đầu tiên của người chơi
                if (isPlayerFirstMove) {
                    isPlayerFirstMove = false;
                    timerStarted = true;
                    timeLeft = 60;
                    gameTimer.start();
                    if (timerLabel != null) {
                        timerLabel.setText("Thời gian: " + timeLeft + "s");
                    }
                } else {
                    // Reset timer khi người chơi đi nước tiếp theo
                    timeLeft = 60;
                    if (timerLabel != null) {
                        timerLabel.setText("Thời gian: " + timeLeft + "s");
                    }
                }

                if (kiemTraThang(1)) {
                    gameOver = true;
                    gameTimer.stop();
                    JOptionPane.showMessageDialog(null, "Bạn đã thắng!");
                    return;
                }

                isPlayerTurn = false;
                gameTimer.stop(); // Dừng timer khi đến lượt máy
                SwingUtilities.invokeLater(() -> mayDanh());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // repaint to show any hover effects later
                repaint();
            }
        });
    }

    // ✅ Máy chơi có chiến thuật cơ bản
    private void mayDanh() {
        if (gameOver) return;

        int bestR = -1, bestC = -1;
        int bestScore = Integer.MIN_VALUE;

        // Duyệt toàn bộ bàn cờ, đánh giá mỗi ô trống
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    // điểm tấn công và phòng thủ
                    int score = danhGia(i, j, 2) + danhGia(i, j, 1) / 2;
                    if (score > bestScore) {
                        bestScore = score;
                        bestR = i;
                        bestC = j;
                    }
                }
            }
        }

        if (bestR == -1) return;

        board[bestR][bestC] = 2;
        repaint();

        if (kiemTraThang(2)) {
            gameOver = true;
            gameTimer.stop();
            JOptionPane.showMessageDialog(null, "Máy thắng!");
            return;
        }

        isPlayerTurn = true;
        
        // Khởi động lại timer cho lượt tiếp theo của người chơi
        if (timerStarted) {
            timeLeft = 60;
            gameTimer.start();
            if (timerLabel != null) {
                timerLabel.setText("Thời gian: " + timeLeft + "s");
            }
        }
    }

    // ✅ Hàm đánh giá điểm (AI heuristic)
    private int danhGia(int r, int c, int player) {
        int score = 0;
        int[][] dir = {{1,0},{0,1},{1,1},{1,-1}};
        for (int[] d : dir) {
            int count = 1;
            int block = 0;

            // Đếm xuôi
            int i = r + d[0], j = c + d[1];
            while (i >= 0 && i < SIZE && j >= 0 && j < SIZE && board[i][j] == player) {
                count++; i += d[0]; j += d[1];
            }
            if (i < 0 || i >= SIZE || j < 0 || j >= SIZE || (board[i][j] != 0 && board[i][j] != player)) block++;

            // Đếm ngược
            i = r - d[0]; j = c - d[1];
            while (i >= 0 && i < SIZE && j >= 0 && j < SIZE && board[i][j] == player) {
                count++; i -= d[0]; j -= d[1];
            }
            if (i < 0 || i >= SIZE || j < 0 || j >= SIZE || (board[i][j] != 0 && board[i][j] != player)) block++;

            // chặn 2 đầu thì điểm thấp hơn
            if (block == 2) continue;
            score += Math.pow(10, count);
        }
        return score;
    }

    // ✅ Kiểm tra thắng
    private boolean kiemTraThang(int player) {
        int[][] dir = {{1,0},{0,1},{1,1},{1,-1}};
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == player) {
                    for (int[] d : dir) {
                        int count = 1;
                        int r = i + d[0], c = j + d[1];
                        while (r >= 0 && r < SIZE && c >= 0 && c < SIZE && board[r][c] == player) {
                            count++; r += d[0]; c += d[1];
                        }
                        if (count >= 5) return true;
                    }
                }
            }
        }
        return false;
    }

    // ✅ Vẽ bàn cờ và quân
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ nền
        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(OFFSET, OFFSET, SIZE * CELL, SIZE * CELL);

        // Vẽ lưới
        g2.setColor(Color.GRAY);
        for (int i = 0; i <= SIZE; i++) {
            g2.drawLine(OFFSET, OFFSET + i * CELL, OFFSET + SIZE * CELL, OFFSET + i * CELL);
            g2.drawLine(OFFSET + i * CELL, OFFSET, OFFSET + i * CELL, OFFSET + SIZE * CELL);
        }

        // Vẽ quân cờ
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int x = OFFSET + j * CELL;
                int y = OFFSET + i * CELL;

                if (board[i][j] == 1) { // O người
                    g2.setStroke(new BasicStroke(4));
                    g2.setColor(new Color(0, 100, 255));
                    g2.drawOval(x + 6, y + 6, CELL - 12, CELL - 12);
                    g2.setColor(new Color(173, 216, 255, 120)); // viền sáng
                    g2.fillOval(x + 7, y + 7, CELL - 14, CELL - 14);
                } else if (board[i][j] == 2) { // X máy
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(new Color(220, 20, 60));
                    g2.drawLine(x + 8, y + 8, x + CELL - 8, y + CELL - 8);
                    g2.drawLine(x + 8, y + CELL - 8, x + CELL - 8, y + 8);
                    g2.setColor(new Color(255, 160, 160, 100)); // ánh sáng nhẹ
                    g2.drawLine(x + 10, y + 10, x + CELL - 10, y + CELL - 10);
                    g2.drawLine(x + 10, y + CELL - 10, x + CELL - 10, y + 10);
                }
            }
        }
    }

    // Method để set timer label từ form
    public void setTimerLabel(JLabel label) {
        this.timerLabel = label;
    }
    
    // Method để dừng timer khi game kết thúc
    public void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    // ✅ Test độc lập
    public static void main(String[] args) {
        JFrame f = new JFrame("Cờ caro đấu với máy");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new BanCoMay());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
