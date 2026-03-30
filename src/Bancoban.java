import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;

public class Bancoban extends JPanel {
    private static final int SIZE = 25;
    private static final int CELL_SIZE = 24;
    private int[][] board = new int[SIZE][SIZE];
    private boolean xTurn = true;
    private boolean gameOver = false;
    private Stack<int[]> moves = new Stack<>();
    private Stack<int[]> redoStack = new Stack<>();
    private java.util.List<int[]> winningLine = null;

    // === Timer cooldown 45 giây mỗi lượt ===
    private Timer cooldownTimer;
    private int timeLeft = 45;
    private JLabel lblNguoiChoi1, lblNguoiChoi2;
    private boolean timerStarted = false;
    private boolean isFirstMove = true;

    private int hoverRow = -1, hoverCol = -1;

    public Bancoban(JLabel lblNguoiChoi1, JLabel lblNguoiChoi2) {
        this.lblNguoiChoi1 = lblNguoiChoi1;
        this.lblNguoiChoi2 = lblNguoiChoi2;

        setPreferredSize(new Dimension(SIZE * CELL_SIZE, SIZE * CELL_SIZE));
        setBackground(new Color(230, 230, 230));

        // --- Sự kiện click chuột ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver) return;

                int row = e.getY() / CELL_SIZE;
                int col = e.getX() / CELL_SIZE;

                if (row < SIZE && col < SIZE && board[row][col] == 0) {
                    board[row][col] = xTurn ? 1 : 2;
                    moves.push(new int[]{row, col, xTurn ? 1 : 2});
                    redoStack.clear();
                    repaint();

                    // Bắt đầu timer sau nước đầu tiên
                    if (isFirstMove) {
                        isFirstMove = false;
                        timerStarted = true;
                        timeLeft = 45;
                        cooldownTimer.start();
                        updateTimerLabel();
                    } else {
                        // Reset timer khi người chơi đi nước tiếp theo
                        timeLeft = 45;
                        updateTimerLabel();
                    }

                    cooldownTimer.stop(); // ✅ dừng đếm khi vừa đánh

                    if (checkWin(row, col)) {
                        String winner = xTurn ? "Người chơi X" : "Người chơi O";
                        JOptionPane.showMessageDialog(Bancoban.this,
                                winner + " đã chiến thắng!");
                        gameOver = true;
                        cooldownTimer.stop();
                        return;
                    }

                    doiLuot(); // ✅ sang lượt mới → tự reset 45 giây
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1; hoverCol = -1; repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = e.getY() / CELL_SIZE;
                int col = e.getX() / CELL_SIZE;
                if (row != hoverRow || col != hoverCol) {
                    hoverRow = row; hoverCol = col; repaint();
                }
            }
        });

        // --- Khởi tạo bộ đếm cooldown ---
        cooldownTimer = new Timer(1000, _ -> {
            if (gameOver || !timerStarted) return;
            timeLeft--;

            updateTimerLabel();

            if (timeLeft <= 0) {
                cooldownTimer.stop();
                // ✅ Hết giờ → thông báo mất lượt và tự động đổi lượt
                String currentPlayer = xTurn ? "Người chơi X" : "Người chơi O";
                JOptionPane.showMessageDialog(Bancoban.this, 
                    currentPlayer + " đã mất lượt do hết thời gian!");
                doiLuot();
            }
        });

        // Không bắt đầu timer ngay, chờ nước đầu tiên
        if (lblNguoiChoi1 != null) lblNguoiChoi1.setText("Chờ nước đầu tiên...");
        if (lblNguoiChoi2 != null) lblNguoiChoi2.setText("Chờ nước đầu tiên...");
    }


    // --- Cập nhật hiển thị ---
    private void updateTimerLabel() {
        if (lblNguoiChoi1 != null && lblNguoiChoi2 != null) {
            if (xTurn) {
                lblNguoiChoi1.setText("⏳ " + timeLeft + "s");
                lblNguoiChoi2.setText("Đợi...");
            } else {
                lblNguoiChoi2.setText("⏳ " + timeLeft + "s");
                lblNguoiChoi1.setText("Đợi...");
            }
        }
    }

    // --- Đổi lượt ---
    private void doiLuot() {
        if (gameOver) return;

        xTurn = !xTurn;
        timeLeft = 45;
        
        if (timerStarted) {
            cooldownTimer.restart();
        }

        if (xTurn) {
            lblNguoiChoi1.setText("⏳ " + timeLeft + "s");
            lblNguoiChoi2.setText("Đợi...");
        } else {
            lblNguoiChoi2.setText("⏳ " + timeLeft + "s");
            lblNguoiChoi1.setText("Đợi...");
        }

        repaint();
    }

    // --- Kiểm tra thắng ---
    private boolean checkWin(int row, int col) {
        int player = board[row][col];
        int[][] dirs = {{0,1},{1,0},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int dr = d[0], dc = d[1];
            java.util.List<int[]> line = new ArrayList<>();
            int r = row, c = col;
            while (r-dr>=0 && r-dr<SIZE && c-dc>=0 && c-dc<SIZE && board[r-dr][c-dc]==player) {
                r -= dr; c -= dc;
            }
            int sr = r, sc = c;
            while (sr>=0 && sr<SIZE && sc>=0 && sc<SIZE && board[sr][sc]==player) {
                line.add(new int[]{sr, sc});
                sr += dr; sc += dc;
            }
            if (line.size() >= 5) {
                winningLine = line;
                return true;
            }
        }
        return false;
    }

    // --- Vẽ bàn cờ ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GRAY);

        for (int i = 0; i <= SIZE; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, SIZE * CELL_SIZE);
            g.drawLine(0, i * CELL_SIZE, SIZE * CELL_SIZE, i * CELL_SIZE);
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                if (board[i][j] == 1) {
                    g2.setColor(Color.RED);
                    g2.drawLine(x + 8, y + 8, x + CELL_SIZE - 8, y + CELL_SIZE - 8);
                    g2.drawLine(x + CELL_SIZE - 8, y + 8, x + 8, y + CELL_SIZE - 8);
                } else if (board[i][j] == 2) {
                    g2.setColor(Color.BLUE);
                    g2.drawOval(x + 6, y + 6, CELL_SIZE - 12, CELL_SIZE - 12);
                }
            }
        }

        // Hover
        if (hoverRow >= 0 && hoverRow < SIZE && hoverCol >= 0 && hoverCol < SIZE && board[hoverRow][hoverCol] == 0) {
            g2.setColor(new Color(255, 255, 0, 60));
            g2.fillRect(hoverCol * CELL_SIZE + 1, hoverRow * CELL_SIZE + 1, CELL_SIZE - 1, CELL_SIZE - 1);
        }

        // Highlight chiến thắng
        if (winningLine != null && !winningLine.isEmpty()) {
            g2.setColor(new Color(0, 255, 0, 90));
            for (int[] p : winningLine) {
                g2.fillRect(p[1] * CELL_SIZE + 1, p[0] * CELL_SIZE + 1, CELL_SIZE - 1, CELL_SIZE - 1);
            }
        }
    }

    // --- Chơi lại ---
    public void choiLai() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = 0;

        xTurn = true;
        gameOver = false;
        winningLine = null;
        timerStarted = false;
        isFirstMove = true;
        timeLeft = 45;
        cooldownTimer.stop();
        
        // Reset hiển thị về trạng thái ban đầu
        if (lblNguoiChoi1 != null) lblNguoiChoi1.setText("Chờ nước đầu tiên...");
        if (lblNguoiChoi2 != null) lblNguoiChoi2.setText("Chờ nước đầu tiên...");
        
        repaint();
    }
}
