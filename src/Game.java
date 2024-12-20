package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game implements KeyListener {

    public static void main(String[] args) {
        Game game = new Game();
    }

    private GamePanel gamePanel;

    public Game() {
        JFrame frame = new JFrame("Battleship");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Display main menu
        JPanel menuPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(100, 60, 81, 52);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLabel = new JLabel("Battleship Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        menuPanel.add(titleLabel, gbc);

        gbc.gridy++;
        JButton vsAiButton = new JButton("VS AI");
        vsAiButton.setPreferredSize(new Dimension(150, 40));
        vsAiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDifficultySelection(frame);
            }
        });
        menuPanel.add(vsAiButton, gbc);

        gbc.gridy++;
        JButton vsPlayerButton = new JButton("VS Player");
        vsPlayerButton.setPreferredSize(new Dimension(150, 40));
        vsPlayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startPlayerVsPlayerGame(frame);
            }
        });
        menuPanel.add(vsPlayerButton, gbc);

        frame.getContentPane().add(menuPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private void showDifficultySelection(JFrame frame) {
        String[] options = new String[] {"Easy", "Medium", "Hard"};
        String message = "Easy will make moves entirely randomly,\nMedium will focus on areas where it finds ships," +
                "\nand Hard will make smarter choices over Medium.";
        int difficultyChoice = JOptionPane.showOptionDialog(null, message,
                "Choose an AI Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (difficultyChoice != -1) {
            frame.getContentPane().removeAll();

            gamePanel = new GamePanel(difficultyChoice);
            frame.getContentPane().add(gamePanel);

            frame.addKeyListener(this);
            frame.revalidate();
            frame.repaint();
        }
    }

    private void startPlayerVsPlayerGame(JFrame frame) {
        frame.getContentPane().removeAll();
        gamePanel = new GamePanel(-1); // -1 to indicate Player vs Player mode
        frame.getContentPane().add(gamePanel);
        frame.addKeyListener(this);
        frame.revalidate();
        frame.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gamePanel != null) {
            gamePanel.handleInput(e.getKeyCode());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

}
