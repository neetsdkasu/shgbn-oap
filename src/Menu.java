import javax.microedition.lcdui.*;

class Menu
{
    private final Font
        SMALL_FONT = Font.getFont(
            0,
            0,
            ShogiBanMIDlet.WTK ? Font.SIZE_MEDIUM : Font.SIZE_SMALL
        );

    private static final int
        DISP_W = 240,
        DISP_H = 268;

    boolean[] enable;
    String[] text;
    int[] textWidth;
    int sel = 0;
    int width = 0, height = 0, offsetX, offsetY;
    int viewCount, viewTop = 0;

    Menu(String[] t)
    {
        text = t;
        enable = new boolean[t.length];
        textWidth = new int[t.length];
        for (int i = 0; i < t.length; i++)
        {
            enable[i] = true;
            textWidth[i] = SMALL_FONT.stringWidth(t[i]);
            width = Math.max(width, textWidth[i]);
        }
        width = Math.min(width + 20, 220);
        viewCount = Math.min(220 / SMALL_FONT.getHeight(), t.length);
        height = viewCount * SMALL_FONT.getHeight();
        offsetX = (DISP_W - width) / 2;
        offsetY = (DISP_H - height) / 2;
    }

    int getSelect()
    {
        return sel;
    }

    boolean keyPressed(int keyCode, int action)
    {
        switch (action)
        {
        case Canvas.UP:
            sel = (sel + text.length - 1) % text.length;
            break;
        case Canvas.DOWN:
            sel = (sel + 1) % text.length;
            break;
        default:
            return false;
        }
        if (sel < viewTop)
        {
            viewTop = sel;
        }
        else if (sel >= viewTop + viewCount)
        {
            viewTop = sel - viewCount + 1;
        }
        return true;
    }

    void paint(Graphics g)
    {
        g.setFont(SMALL_FONT);

        g.setColor(0x7F7F7F);
        g.fillRect(offsetX-2, offsetY-2, width+5, height+5);
        g.setColor(0xE0E0E0);
        g.fillRect(offsetX, offsetY, width, height);

        g.setColor(0x000000);
        for (int i = 0; i < viewCount; i++)
        {
            int p = viewTop + i;
            if (sel == p)
            {
                g.setColor(0xFFFF00);
                g.fillRect(
                    offsetX,
                    i*SMALL_FONT.getHeight() + offsetY,
                    width,
                    SMALL_FONT.getHeight()
                );
                g.setColor(0x000000);
            }
            g.drawRect(
                offsetX,
                i*SMALL_FONT.getHeight() + offsetY,
                width,
                SMALL_FONT.getHeight()
            );
            g.drawString(
                text[p],
                DISP_W / 2,
                (i+1)*SMALL_FONT.getHeight() + offsetY,
                Graphics.HCENTER|Graphics.BOTTOM
            );
        }

        g.setColor(0xFF0000);
        g.drawRect(
            offsetX,
            (sel-viewTop)*SMALL_FONT.getHeight() + offsetY,
            width,
            SMALL_FONT.getHeight()
        );

    }
}