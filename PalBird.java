import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class PalBird extends JPanel implements ActionListener, KeyListener, MouseListener {

    int boardWidth = 400;
    int boardHeight = 600;

    Timer gameLoop;
    Timer pipeSpawner;

    int birdX = 100;
    int birdY = 300;
    int birdSize = 30;

    int velocityY = 0;
    int gravity = 1;

    int pipeSpeed = 4;
    int gap = 160;

    int score = 0;
    int bestScore = 0;
    int level = 1;

    boolean gameOver = false;

    Rectangle resetButton = new Rectangle(130, 320, 140, 40);

    ArrayList<Rectangle> pipes = new ArrayList<>();
    Random random = new Random();

    File scoreFile = new File("bestscore.txt");

    public PalBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.cyan);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        loadBestScore();

        gameLoop = new Timer(20, this);
        gameLoop.start();

        pipeSpawner = new Timer(1500, e -> addPipe());
        pipeSpawner.start();
    }

    void addPipe() {
        int pipeWidth = 60;
        int pipeHeight = random.nextInt(300) + 50;

        pipes.add(new Rectangle(boardWidth, 0, pipeWidth, pipeHeight));
        pipes.add(new Rectangle(boardWidth, pipeHeight + gap, pipeWidth, boardHeight));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!gameOver) {

            velocityY += gravity;
            birdY += velocityY;

            Rectangle bird = new Rectangle(birdX, birdY, birdSize, birdSize);

            for (Rectangle pipe : pipes) {
                pipe.x -= pipeSpeed;

                if (pipe.intersects(bird)) {
                    gameOver = true;
                }
            }

            pipes.removeIf(pipe -> {
                if (pipe.x + pipe.width < 0) {
                    score++;
                    if (score > bestScore) {
                        bestScore = score;
                        saveBestScore();
                    }
                    return true;
                }
                return false;
            });

            if (score > 0 && score % 10 == 0) {
                level = score / 10 + 1;
                pipeSpeed = 4 + level;
                gap = Math.max(100, 160 - level * 5);
            }

            if (birdY < 0 || birdY > boardHeight) {
                gameOver = true;
            }
        }

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Bird
        g.setColor(Color.yellow);
        g.fillOval(birdX, birdY, birdSize, birdSize);

        // Pipes
        g.setColor(Color.green);
        for (Rectangle pipe : pipes) {
            g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
        }

        // Score
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Best: " + bestScore, 20, 55);
        g.drawString("Level: " + level, 20, 80);

        if (gameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Game Over", 110, 250);

            // Reset Button
            g.setColor(Color.white);
            g.fillRect(resetButton.x, resetButton.y, resetButton.width, resetButton.height);

            g.setColor(Color.black);
            g.drawRect(resetButton.x, resetButton.y, resetButton.width, resetButton.height);
            g.drawString("RESET", 170, 347);
        }
    }

    void resetGame() {
        birdY = 300;
        velocityY = 0;
        pipes.clear();
        score = 0;
        level = 1;
        pipeSpeed = 4;
        gap = 160;
        gameOver = false;
    }

    void saveBestScore() {
        try (PrintWriter writer = new PrintWriter(scoreFile)) {
            writer.println(bestScore);
        } catch (Exception ignored) {}
    }

    void loadBestScore() {
        if (scoreFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
                bestScore = Integer.parseInt(reader.readLine());
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (!gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -12;
        }

        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            resetGame();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {

        if (!gameOver) {
            velocityY = -12;  // Mouse click = jump
        } else {
            if (resetButton.contains(e.getPoint())) {
                resetGame();
            }
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pal Bird PRO 🐦");
        PalBird game = new PalBird();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
