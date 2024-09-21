package com.ui;

import java.awt.*;
import javax.swing.border.AbstractBorder;

public class RoundedBorderUI extends AbstractBorder {
    private int radius;

    public RoundedBorderUI(int radius) {
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);  // Цвет границы
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(10, 15, 10, 15);  // Внутренние отступы
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
