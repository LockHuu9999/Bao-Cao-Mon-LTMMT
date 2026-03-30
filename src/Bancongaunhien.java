import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Bancongaunhien extends JPanel implements MouseListener {
    private static final int SO_HANG = 25;
    private static final int SO_COT = 25;
    private static final int O_CO = 22;
    private static final int LE = 20; // lề trái trên

    private int[][] matran; // 0: trống, 1: X, 2: O
    private boolean luotX = true;
    private JLabel lblTrangThai;
    private Timer timer;
    private int thoiGianConLai = 45;
    private boolean ketThuc = false;
    private int hoverR = -1, hoverC = -1;

    // Online support
    private boolean onlineMode = false;
    private NetworkClient netClient = null;
    private boolean amIFirst = false;

    public Bancongaunhien(JLabel lblTrangThai) {
        this.lblTrangThai = lblTrangThai;
        this.setPreferredSize(new Dimension(SO_COT * O_CO + LE * 2, SO_HANG * O_CO + LE * 2));
        this.setBackground(new Color(255, 228, 181));
        this.addMouseListener(this);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int x = (e.getX() - LE) / O_CO;
                int y = (e.getY() - LE) / O_CO;
                if (x >= 0 && x < SO_COT && y >= 0 && y < SO_HANG) {
                    hoverR = y;
                    hoverC = x;
                } else {
                    hoverR = -1;
                    hoverC = -1;
                }
                repaint();
            }
        });

        matran = new int[SO_HANG][SO_COT];
        khoiTaoTimer();
    }

    // --- Network setup ---
    public void setNetworkClient(NetworkClient client, boolean startAsX) {
        this.netClient = client;
        this.onlineMode = client != null;
        this.amIFirst = startAsX;
        this.luotX = startAsX;
    }

    // --- Áp dụng nước đi từ đối thủ ---
    public void applyRemoteMove(int row, int col) {
        if (row < 0 || row >= SO_HANG || col < 0 || col >= SO_COT) return;
        if (matran[row][col] != 0) return;
        matran[row][col] = luotX ? 1 : 2;
        repaint();

        if (kiemTraThang(row, col)) {
            ketThuc = true;
            timer.stop();
            String nguoiThang = luotX ? "X" : "O";
            lblTrangThai.setText("Người chơi " + nguoiThang + " thắng!");
            JOptionPane.showMessageDialog(this, "Người chơi " + nguoiThang + " đã thắng!");
            return;
        }

        doiLuot();
        thoiGianConLai = 30;
    }

    private void khoiTaoTimer() {
        timer = new Timer(1000, e -> {
            if (ketThuc) return;
            thoiGianConLai--;
            lblTrangThai.setText("Lượt: " + (luotX ? "X" : "O") + " | Còn: " + thoiGianConLai + "s");

            if (thoiGianConLai <= 0) {
                ((Timer) e.getSource()).stop();
                JOptionPane.showMessageDialog(this, "Hết giờ! Mất lượt.");
                doiLuot();
                thoiGianConLai = 30;
                timer.start();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Khử răng cưa và nét đậm hơn
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.2f));

        // Vẽ lưới
        g2.setColor(new Color(70, 70, 70));
        for (int i = 0; i <= SO_HANG; i++) {
            g2.drawLine(LE, LE + i * O_CO, LE + SO_COT * O_CO, LE + i * O_CO);
        }
        for (int j = 0; j <= SO_COT; j++) {
            g2.drawLine(LE + j * O_CO, LE, LE + j * O_CO, LE + SO_HANG * O_CO);
        }

        // Padding để vẽ X/O nằm giữa
        final int PAD = Math.max(4, (int) Math.round(O_CO * 0.18));

        // Vẽ X và O
        for (int r = 0; r < SO_HANG; r++) {
            for (int c = 0; c < SO_COT; c++) {
                int x = LE + c * O_CO;
                int y = LE + r * O_CO;

                if (matran[r][c] == 1) { // X
                    g2.setColor(new Color(220, 30, 30));
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawLine(x + PAD, y + PAD, x + O_CO - PAD, y + O_CO - PAD);
                    g2.drawLine(x + O_CO - PAD, y + PAD, x + PAD, y + O_CO - PAD);
                } else if (matran[r][c] == 2) { // O
                    g2.setColor(new Color(30, 90, 220));
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawOval(x + PAD, y + PAD, O_CO - 2 * PAD, O_CO - 2 * PAD);
                }
            }
        }

        // Ô đang hover
        if (hoverR >= 0 && hoverC >= 0 && matran[hoverR][hoverC] == 0) {
            int hx = LE + hoverC * O_CO;
            int hy = LE + hoverR * O_CO;
            g2.setColor(new Color(255, 255, 0, 70));
            g2.fillRect(hx + 1, hy + 1, O_CO - 1, O_CO - 1);
            g2.setColor(new Color(180, 160, 0, 140));
            g2.drawRect(hx + 1, hy + 1, O_CO - 2, O_CO - 2);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (ketThuc) return;

        int x = (e.getX() - LE) / O_CO;
        int y = (e.getY() - LE) / O_CO;

        if (x < 0 || x >= SO_COT || y < 0 || y >= SO_HANG) return;
        if (matran[y][x] != 0) return;

        if (onlineMode) {
            int myMark = amIFirst ? 1 : 2;
            if ((luotX && myMark != 1) || (!luotX && myMark != 2)) {
                JOptionPane.showMessageDialog(this, "Chưa đến lượt bạn!");
                return;
            }
        }

        matran[y][x] = luotX ? 1 : 2;
        repaint();

        if (onlineMode && netClient != null) {
            netClient.send("MOVE " + y + " " + x);
        }

        if (kiemTraThang(y, x)) {
            ketThuc = true;
            timer.stop();
            String nguoiThang = luotX ? "X" : "O";
            lblTrangThai.setText("Người chơi " + nguoiThang + " thắng!");
            JOptionPane.showMessageDialog(this, "Người chơi " + nguoiThang + " đã thắng!");
            return;
        }

        doiLuot();
        thoiGianConLai = 30;
    }

    private void doiLuot() {
        luotX = !luotX;
        lblTrangThai.setText("Lượt: " + (luotX ? "X" : "O") + " | Còn: " + thoiGianConLai + "s");
    }

    private boolean kiemTraThang(int hang, int cot) {
        int nguoi = matran[hang][cot];
        if (nguoi == 0) return false;

        return demLienTiep(hang, cot, 0, 1, nguoi) + demLienTiep(hang, cot, 0, -1, nguoi) >= 4 ||
               demLienTiep(hang, cot, 1, 0, nguoi) + demLienTiep(hang, cot, -1, 0, nguoi) >= 4 ||
               demLienTiep(hang, cot, 1, 1, nguoi) + demLienTiep(hang, cot, -1, -1, nguoi) >= 4 ||
               demLienTiep(hang, cot, 1, -1, nguoi) + demLienTiep(hang, cot, -1, 1, nguoi) >= 4;
    }

    private int demLienTiep(int hang, int cot, int dH, int dC, int nguoi) {
        int dem = 0;
        for (int i = 1; i < 5; i++) {
            int newH = hang + i * dH;
            int newC = cot + i * dC;
            if (newH < 0 || newH >= SO_HANG || newC < 0 || newC >= SO_COT) break;
            if (matran[newH][newC] == nguoi) dem++;
            else break;
        }
        return dem;
    }

    public void startRandomMatch() {
        luotX = new Random().nextBoolean();
        ketThuc = false;
        thoiGianConLai = 30;
        lblTrangThai.setText("Người đi trước: " + (luotX ? "X" : "O") + " | Còn: 30s");
        timer.start();
        repaint();
    }

    public void choiLai() {
        for (int i = 0; i < SO_HANG; i++) {
            for (int j = 0; j < SO_COT; j++) {
                matran[i][j] = 0;
            }
        }
        ketThuc = false;
        luotX = new Random().nextBoolean();
        thoiGianConLai = 30;
        lblTrangThai.setText("Bắt đầu ván mới! Người đi trước: " + (luotX ? "X" : "O"));
        repaint();
        if (timer != null) {
            timer.stop();
            timer.start();
        }
    }
    public Bancongaunhien(JLabel lbl1, JLabel lbl2) {
    this.lblTrangThai = lbl1; // chỉ cần dùng 1 label chính để hiển thị
    this.setPreferredSize(new Dimension(SO_COT * O_CO + LE * 2, SO_HANG * O_CO + LE * 2));
    this.setBackground(new Color(255, 228, 181));
    this.addMouseListener(this);

    addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            int x = (e.getX() - LE) / O_CO;
            int y = (e.getY() - LE) / O_CO;
            if (x >= 0 && x < SO_COT && y >= 0 && y < SO_HANG) {
                hoverR = y;
                hoverC = x;
            } else {
                hoverR = -1;
                hoverC = -1;
            }
            repaint();
        }
    });

    matran = new int[SO_HANG][SO_COT];
    khoiTaoTimer();
}


    // Chuột không dùng
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
