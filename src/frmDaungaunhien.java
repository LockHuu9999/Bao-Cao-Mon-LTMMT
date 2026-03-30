import java.awt.BorderLayout;
import javax.swing.JOptionPane;

/**
 * Form Đấu Ngẫu Nhiên – bố cục và cooldown giống frmDauvoiban
 * Bàn cờ 600x600, mỗi lượt 10s, hết giờ thì mất lượt.
 */
public class frmDaungaunhien extends javax.swing.JFrame {

    private Bancongaunhien banco;

    public frmDaungaunhien() {
        initComponents();
        UITheme.apply(this);
        setTitle("Đấu Ngẫu Nhiên");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // Gắn bàn cờ ngẫu nhiên vào panel
        banco = new Bancongaunhien(lblDemnguoc1, lblDemnguoc2);
        pnlBanco.setLayout(new BorderLayout());
        pnlBanco.add(banco, BorderLayout.CENTER);

        // Cập nhật kích thước 600x600
        pnlBanco.setPreferredSize(new java.awt.Dimension(600, 600));
        pnlBanco.revalidate();
        pnlBanco.repaint();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }


    private void initComponents() {

        txtNguoichoi1 = new javax.swing.JTextField();
        txtNguoichoi2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        lblDemnguoc1 = new javax.swing.JLabel();
        lblDemnguoc2 = new javax.swing.JLabel();
        pnlBanco = new javax.swing.JPanel();
        btnChoilai = new javax.swing.JButton();
        btnThoat = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtNguoichoi1.setText("Người chơi 1");

        txtNguoichoi2.setText("Người chơi 2");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setText("V/S");

        lblDemnguoc1.setText("Đếm ngược");

        lblDemnguoc2.setText("Đếm ngược");

        javax.swing.GroupLayout pnlBancoLayout = new javax.swing.GroupLayout(pnlBanco);
        pnlBanco.setLayout(pnlBancoLayout);
        pnlBancoLayout.setHorizontalGroup(
            pnlBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        pnlBancoLayout.setVerticalGroup(
            pnlBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        btnChoilai.setText("Chơi lại");
        btnChoilai.addActionListener(evt -> btnChoilaiActionPerformed(evt));

        btnThoat.setText("Thoát");
        btnThoat.addActionListener(evt -> btnThoatActionPerformed(evt));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(150, 150, 150)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNguoichoi1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDemnguoc1))
                        .addGap(15, 15, 15)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDemnguoc2)
                            .addComponent(txtNguoichoi2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(pnlBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(170, 170, 170)
                        .addComponent(btnChoilai)
                        .addGap(100, 100, 100)
                        .addComponent(btnThoat)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNguoichoi1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNguoichoi2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDemnguoc1)
                    .addComponent(jLabel1)
                    .addComponent(lblDemnguoc2))
                .addGap(10, 10, 10)
                .addComponent(pnlBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChoilai)
                    .addComponent(btnThoat))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnChoilaiActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn bắt đầu lại ván mới không?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            banco.choiLai();
            JOptionPane.showMessageDialog(this, "Bắt đầu ván mới!");
        }
    }

    private void btnThoatActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn thoát và quay lại màn hình chính không?",
                "Thoát", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new frmHome().setVisible(true);
            this.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(() -> new frmDaungaunhien().setVisible(true));
    }

    // =============== Biến giao diện ==================
    private javax.swing.JButton btnChoilai;
    private javax.swing.JButton btnThoat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblDemnguoc1;
    private javax.swing.JLabel lblDemnguoc2;
    private javax.swing.JPanel pnlBanco;
    private javax.swing.JTextField txtNguoichoi1;
    private javax.swing.JTextField txtNguoichoi2;
}
