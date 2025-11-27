package com.secureview.desktop.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern theme for SecureView with premium design elements.
 */
public class ModernTheme {
    
    // Color Palette - Modern Dark Theme
    public static final Color PRIMARY_DARK = new Color(0x0F172A);      // Deep navy
    public static final Color SECONDARY_DARK = new Color(0x1E293B);    // Slate
    public static final Color ACCENT_BLUE = new Color(0x3B82F6);      // Bright blue
    public static final Color ACCENT_PURPLE = new Color(0x8B5CF6);     // Purple
    public static final Color SUCCESS_GREEN = new Color(0x10B981);   // Green
    public static final Color WARNING_ORANGE = new Color(0xF59E0B);   // Orange
    public static final Color ERROR_RED = new Color(0xEF4444);        // Red
    public static final Color TEXT_PRIMARY = new Color(0xF8FAFC);     // Almost white
    public static final Color TEXT_SECONDARY = new Color(0x94A3B8);  // Light gray
    public static final Color BORDER_COLOR = new Color(0x334155);     // Border gray
    public static final Color CARD_BG = new Color(0x1E293B);          // Card background
    public static final Color HOVER_BG = new Color(0x334155);          // Hover state
    
    // Gradients
    public static GradientPaint getPrimaryGradient(int width, int height) {
        return new GradientPaint(
            0, 0, new Color(0x3B82F6),
            width, height, new Color(0x8B5CF6)
        );
    }
    
    public static GradientPaint getSuccessGradient(int width, int height) {
        return new GradientPaint(
            0, 0, new Color(0x10B981),
            width, height, new Color(0x059669)
        );
    }
    
    // Fonts
    public static Font getTitleFont() {
        return new Font("Segoe UI", Font.BOLD, 24);
    }
    
    public static Font getHeadingFont() {
        return new Font("Segoe UI", Font.BOLD, 18);
    }
    
    public static Font getBodyFont() {
        return new Font("Segoe UI", Font.PLAIN, 14);
    }
    
    public static Font getSmallFont() {
        return new Font("Segoe UI", Font.PLAIN, 12);
    }
    
    // Borders
    public static Border getRoundedBorder(int radius) {
        return BorderFactory.createEmptyBorder(12, 16, 12, 16);
    }
    
    public static Border getCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        );
    }
    
    // Custom rounded panel
    public static class RoundedPanel extends JPanel {
        private int cornerRadius = 12;
        private Color backgroundColor = CARD_BG;
        
        public RoundedPanel() {
            setOpaque(false);
        }
        
        public RoundedPanel(int radius) {
            this();
            this.cornerRadius = radius;
        }
        
        public RoundedPanel(int radius, Color bg) {
            this(radius);
            this.backgroundColor = bg;
        }
        
        public void setCornerRadius(int radius) {
            this.cornerRadius = radius;
            repaint();
        }
        
        public void setBackgroundColor(Color color) {
            this.backgroundColor = color;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            g2.setColor(backgroundColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            
            g2.dispose();
        }
    }
    
    // Modern button with gradient
    public static class ModernButton extends JButton {
        private boolean isPrimary = false;
        private Color hoverColor = HOVER_BG;
        
        public ModernButton(String text) {
            super(text);
            setupButton();
        }
        
        public ModernButton(String text, boolean primary) {
            super(text);
            this.isPrimary = primary;
            setupButton();
        }
        
        private void setupButton() {
            setFont(getBodyFont());
            setForeground(TEXT_PRIMARY);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (isPrimary) {
                hoverColor = ACCENT_BLUE;
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            if (isPrimary) {
                // Gradient background for primary buttons
                GradientPaint gradient = getPrimaryGradient(width, height);
                g2.setPaint(gradient);
            } else {
                g2.setColor(getModel().isPressed() ? HOVER_BG.darker() : 
                           (getModel().isRollover() ? hoverColor : CARD_BG));
            }
            
            g2.fill(new RoundRectangle2D.Double(0, 0, width, height, 8, 8));
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    // Status badge component
    public static class StatusBadge extends JLabel {
        private Color badgeColor = TEXT_SECONDARY;
        
        public StatusBadge(String text) {
            super(text);
            setupBadge();
        }
        
        public StatusBadge(String text, Color color) {
            super(text);
            this.badgeColor = color;
            setupBadge();
        }
        
        private void setupBadge() {
            setFont(getSmallFont());
            setForeground(TEXT_PRIMARY);
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            g2.setColor(badgeColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, width, height, height / 2, height / 2));
            
            g2.dispose();
            super.paintComponent(g);
        }
        
        public void setBadgeColor(Color color) {
            this.badgeColor = color;
            repaint();
        }
    }
    
    // Animated progress bar
    public static class AnimatedProgressBar extends JProgressBar {
        private Timer animationTimer;
        private int pulseValue = 0;
        
        public AnimatedProgressBar() {
            super(0, 100);
            setupProgressBar();
        }
        
        private void setupProgressBar() {
            setStringPainted(true);
            setForeground(ACCENT_BLUE);
            setBackground(SECONDARY_DARK);
            setBorderPainted(false);
            setFont(getSmallFont());
            
            // Pulse animation
            animationTimer = new Timer(50, e -> {
                pulseValue = (pulseValue + 5) % 360;
                repaint();
            });
        }
        
        public void startAnimation() {
            if (!animationTimer.isRunning()) {
                animationTimer.start();
            }
        }
        
        public void stopAnimation() {
            if (animationTimer.isRunning()) {
                animationTimer.stop();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int progress = (int) (width * (getValue() / 100.0));
            
            // Background
            g2.setColor(SECONDARY_DARK);
            g2.fill(new RoundRectangle2D.Double(0, 0, width, height, height / 2, height / 2));
            
            // Progress with gradient
            if (progress > 0) {
                GradientPaint gradient = new GradientPaint(
                    0, 0, ACCENT_BLUE,
                    progress, 0, ACCENT_PURPLE
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, progress, height, height / 2, height / 2));
            }
            
            // Text
            if (getString() != null && !getString().isEmpty()) {
                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getString());
                int x = (width - textWidth) / 2;
                int y = (height + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getString(), x, y);
            }
            
            g2.dispose();
        }
    }
}

