package com.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class RoundedButtonUI extends BasicButtonUI {

    private final int radius;

    public RoundedButtonUI(int radius) {
        this.radius = radius;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        AbstractButton button = (AbstractButton) c;
        button.setOpaque(false);  // Отключаем стандартный фон
        button.setBorderPainted(false);  // Отключаем стандартную границу
        button.setFocusPainted(false);  // Отключаем фокус
        button.setContentAreaFilled(false);  // Отключаем стандартную заливку
        button.setRolloverEnabled(false);  // Отключаем эффект при наведении

        // Устанавливаем кнопку в неизменяемое состояние
        button.setModel(new DefaultButtonModel() {
            @Override
            public boolean isPressed() {
                return false;  // Отключаем состояние "нажата"
            }
        });
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        AbstractButton b = (AbstractButton) c;

        // Включаем сглаживание
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Устанавливаем цвет кнопки
        g2.setColor(new Color(0, 150, 136));  // Основной цвет кнопки

        // Рисуем закругленную кнопку
        g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), radius, radius);

        // Отрисовываем текст кнопки
        FontMetrics fm = g2.getFontMetrics();
        String text = b.getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2.setColor(Color.WHITE);  // Цвет текста
        g2.drawString(text, (b.getWidth() - textWidth) / 2, (b.getHeight() + textHeight) / 2 - 3);

        g2.dispose();
    }    
    
    @Override
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
        // Отключаем фокус
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton) c;
        Dimension d = super.getPreferredSize(c);
        d.width = Math.max(400, d.width);  // Минимальная ширина 400px
        d.height = Math.max(50, d.height);  // Минимальная высота 50px
        return d;
    }
}
