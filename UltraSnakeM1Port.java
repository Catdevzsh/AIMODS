import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

class UltraSnake extends JPanel implements ActionListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int UNIT_SIZE = 20;
    private static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
    private static final int DELAY = 100;
    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts = 3;
    private int foodX;
    private int foodY;
    private char direction = 'R';
    private boolean isRunning = false;
    private boolean inMainMenu = true;
    private boolean transitioning = false;
    private int score = 0;
    private Timer timer;
    private Random random;
    private JFrame frame;
    private int loadingAngle = 0;

    public UltraSnake(JFrame frame) {
        this.frame = frame;
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame(); // Start the game immediately upon initialization
    }

    private void startTransition() {
        transitioning = true;
        timer = new Timer(DELAY, new ActionListener() {
            int alpha = 255;

            @Override
            public void actionPerformed(ActionEvent e) {
                alpha -= 10;
                if (alpha <= 0) {
                    transitioning = false;
                    timer.stop();
                    startGame();
                }
                repaint();
            }
        });
        timer.start();
    }

    public void startGame() {
        inMainMenu = false;
        bodyParts = 3;
        direction = 'R';
        score = 0;
        for (int i = 0; i < x.length; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        newFood();
        isRunning = true;
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning) {
            move();
            checkFood();
            checkCollisions();
        }
        repaint();
    }

    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    private void newFood() {
        foodX = random.nextInt((int) (WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        foodY = random.nextInt((int) (HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    private void checkFood() {
        if ((x[0] == foodX) && (y[0] == foodY)) {
            bodyParts++;
            score++;
            newFood();
        }
    }

    private void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                isRunning = false;
            }
        }

        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            isRunning = false;
        }

        if (!isRunning) {
            timer.stop();
            gameOver();
        }
    }

    private void gameOver() {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(this, "Game Over. Your score: " + score + ". Restart?", "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                startGame();
            } else {
                System.exit(0);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Call the superclass method to ensure proper painting
        if (inMainMenu && !transitioning) {
            showMainMenu(g);
        } else if (transitioning) {
            showLoading(g);
        } else if (isRunning) {
            draw(g);
        }
    }

    private void showMainMenu(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Times New Roman", Font.BOLD, 24));
        FontMetrics fontMetrics = g.getFontMetrics();

        String title = "Snake Game";
        String startMessage = "Press Z or Enter to Start";

        int titleWidth = fontMetrics.stringWidth(title);
        int startMessageWidth = fontMetrics.stringWidth(startMessage);

        int titleX = (WIDTH - titleWidth) / 2;
        int startMessageX = (WIDTH - startMessageWidth) / 2;

        int titleY = HEIGHT / 2 - 50;
        int startMessageY = HEIGHT / 2;

        g.drawString(title, titleX, titleY);
        g.drawString(startMessage, startMessageX, startMessageY);

        String instruction = "Use arrow keys to move";
        int instructionWidth = fontMetrics.stringWidth(instruction);
        int instructionX = (WIDTH - instructionWidth) / 2;
        int instructionY = HEIGHT / 2 + 50;
        g.drawString(instruction, instructionX, instructionY);
    }

    private void showLoading(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Times New Roman", Font.BOLD, 24));
        FontMetrics fontMetrics = g.getFontMetrics();

        String loadingText = "Loading...";
        int loadingTextWidth = fontMetrics.stringWidth(loadingText);
        int loadingTextX = (WIDTH - loadingTextWidth) / 2;
        int loadingTextY = HEIGHT / 2;

        g.drawString(loadingText, loadingTextX, loadingTextY);

        drawLoadingIcon(g);
    }

    private void drawLoadingIcon(Graphics g) {
        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2 + 30;
        int radius = 20;

        g.setColor(Color.white);
        g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, loadingAngle, 60);
        g.setColor(Color.black);
        g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, loadingAngle + 120, 60);

        loadingAngle = (loadingAngle + 10) % 360;
    }

    private void draw(Graphics g) {
        g.setColor(Color.red);
        g.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);

        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                g.setColor(Color.green);
            } else {
                g.setColor(new Color(45, 180, 0));
            }
            g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Times New Roman", Font.BOLD, 20));
        g.drawString("Score: " + score, (WIDTH - g.getFontMetrics().stringWidth("Score: " + score)) / 2, g.getFont().getSize());
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (inMainMenu && !transitioning && (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_ENTER)) {
                startTransition();
            } else if (!inMainMenu) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (direction != 'R') {
                            direction = 'L';
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (direction != 'L') {
                            direction = 'R';
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (direction != 'D') {
                            direction = 'U';
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (direction != 'U') {
                            direction = 'D';
                        }
                        break;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Game");
            UltraSnake gamePanel = new UltraSnake(frame);
            frame.add(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
        });
    }
}
// End of UltraSnake.java - [C] Flames Team 
