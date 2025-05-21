package io.github.palexdev.imcache.transforms;

import java.awt.*;
import java.awt.image.BufferedImage;

/// A class that implements the [Transform] interface to add text to a [BufferedImage] given the text and the position.
/// Further customizations include positions offsets, the font and the color.
///
/// The text location is specified by the [Position] enum, while the offsets allow for a finer control.
public class AddText implements Transform {
    //================================================================================
    // Properties
    //================================================================================
    private final String text;
    private final Position position;
    private int xOffset;
    private int yOffset;
    private Font font;
    private Color color;

    //================================================================================
    // Constructors
    //================================================================================
    public AddText(String text, Position position) {
        this(text, position, 0, 0, new Font(null, Font.PLAIN, 12), Color.WHITE);
    }

    public AddText(String text, Position position, int xOffset, int yOffset, Font font, Color color) {
        this.text = text;
        this.position = position;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.font = font;
        this.color = color;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public BufferedImage transform(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType());
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);

        // Init text and enable antialiasing
        g.setFont(font);
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Find text dimensions and line height
        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();

        // Split text into lines
        String[] lines = text.split("\n");

        // Compute x and y depending on the position
        int totalTextH = lineHeight * lines.length;
        int[] basePosition = position.computePosition(w, h, metrics.stringWidth(lines[0]), totalTextH);
        int x = basePosition[0] + xOffset;
        int y = basePosition[1] + yOffset;

        for (String line : lines) {
            int lineX = x;
            if (position.isVerticalCenter()) {
                lineX = (src.getWidth() - metrics.stringWidth(line)) / 2 + xOffset;
            } else if (position.isVerticalRight()) {
                lineX = src.getWidth() - metrics.stringWidth(line) + xOffset;
            }
            g.drawString(line, lineX, y);
            y += lineHeight;
        }
        g.dispose();
        return out;
    }

    //================================================================================
    // Setters
    //================================================================================
    public AddText setXOffset(int xOffset) {
        this.xOffset = xOffset;
        return this;
    }

    public AddText setYOffset(int yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    public AddText setFont(Font font) {
        this.font = font;
        return this;
    }

    public AddText setColor(Color color) {
        this.color = color;
        return this;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum Position {
        TOP_LEFT {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{0, textH};
            }
        },
        TOP_CENTER {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{(imgW - textW) / 2, textH};
            }
        },
        TOP_RIGHT {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{imgW - textW, textH};
            }
        },
        CENTER_LEFT {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{0, (imgH - textH) / 2 + textH};
            }
        },
        CENTER {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{(imgW - textW) / 2, (imgH - textH) / 2 + textH};
            }
        },
        CENTER_RIGHT {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{imgW - textW, (imgH - textH) / 2 + textH};
            }
        },
        BOTTOM_LEFT {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{0, imgH - textH + textH / 2};
            }
        },
        BOTTOM_CENTER {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{(imgW - textW) / 2, imgH - textH + textH / 2};
            }
        },
        BOTTOM_RIGHT {
            @Override
            public int[] computePosition(int imgW, int imgH, int textW, int textH) {
                return new int[]{imgW - textW, imgH - textH + textH / 2};
            }
        };

        public abstract int[] computePosition(int imgW, int imgH, int textW, int textH);

        public boolean isVerticalCenter() {
            return this == Position.TOP_CENTER ||
                   this == Position.CENTER ||
                   this == Position.BOTTOM_CENTER;
        }

        public boolean isVerticalRight() {
            return this == Position.TOP_RIGHT ||
                   this == Position.CENTER_RIGHT ||
                   this == Position.BOTTOM_RIGHT;
        }
    }
}
