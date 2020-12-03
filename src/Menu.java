import javax.microedition.lcdui.*;

final class Menu implements GConstants
{
    private static Menu rankUpMenu = null;

    static Menu getRankUpMenu()
    {
        if (rankUpMenu == null)
        {
            rankUpMenu = new Menu(1, new String[]{WORDS[6], WORDS[7]});
        }

        return rankUpMenu;
    }

    private static Menu gameMenu = null;

    static Menu getGameMenu()
    {
        if (gameMenu == null)
        {
            gameMenu = new Menu(2, new String[]{
                WORDS[11],
                WORDS[12],
                WORDS[13],
                WORDS[16],
                WORDS[17],
                WORDS[15],
                WORDS[18]
            });
        }

        return gameMenu;
    }

    private static Menu editMenuOnBan = null;

    static Menu getEditMenuOnBan()
    {
        if (editMenuOnBan == null)
        {
            editMenuOnBan = new Menu(3, new String[]{
                WORDS[19],
                WORDS[20],
                WORDS[21]
            });
        }

        return editMenuOnBan;
    }

    private static Menu editMenuOnHand = null;

    static Menu getEditMenuOnHand()
    {
        if (editMenuOnHand == null)
        {
            editMenuOnHand = new Menu(4, new String[]{
                WORDS[19],
                WORDS[22],
                WORDS[23]
            });
        }

        return editMenuOnHand;
    }

    private static Menu editMenu = null;

    static Menu getEditMenu()
    {
        if (editMenu == null)
        {
            editMenu = new Menu(5, new String[]{
                "< 0000" + WORDS[2] + " >",
                WORDS[16],
                WORDS[25],
                WORDS[15],
                WORDS[24]
            });
        }

        return editMenu;
    }

    private static final int
        BACKGROUND_COLOR = 0xE0E0E0;

    boolean[] enable;
    String[] text;
    int[] textWidth;
    int id;
    int sel = 0;
    int width = 0, height = 0, offsetX, offsetY;
    int viewCount, viewTop = 0;
    int value = 0;

    Menu(int num, String[] t)
    {
        id = num;
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

    Menu cleanUp()
    {
        sel = 0;
        value = 0;
        return this;
    }

    void setValue(int v)
    {
        value = v;
    }

    int getValue()
    {
        return value;
    }

    void setEnable(int index, boolean e)
    {
        enable[index] = e;
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
        case Canvas.LEFT:
            if (id != 5 || sel != 0)
            {
                return false;
            }
            value = Math.max(0, value - 2);
            break;
        case Canvas.RIGHT:
            if (id != 5 || sel != 0)
            {
                return false;
            }
            value = value == 0 ? 1 : (value + 2);
            break;
        case Canvas.FIRE:
            return !enable[sel];
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

        g.setColor(GRAY);
        g.fillRect(offsetX-2, offsetY-2, width+5, height+5);
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(offsetX, offsetY, width, height);

        g.setColor(BLACK);
        for (int i = 0; i < viewCount; i++)
        {
            int p = viewTop + i;
            if (sel == p)
            {
                g.setColor(YELLOW);
                g.fillRect(
                    offsetX,
                    i*SMALL_FONT.getHeight() + offsetY,
                    width,
                    SMALL_FONT.getHeight()
                );
            }
            g.setColor(BLACK);
            g.drawRect(
                offsetX,
                i*SMALL_FONT.getHeight() + offsetY,
                width,
                SMALL_FONT.getHeight()
            );
            g.setColor(enable[p] ? BLACK : GRAY);
            String s = text[p];
            if (id == 5 && p == 0)
            {
                s = (value == 0 ? "  " : "< ")
                  + Integer.toString(value) + WORDS[2] + " >";
            }
            g.drawString(
                s,
                DISP_W / 2,
                (i+1)*SMALL_FONT.getHeight() + offsetY,
                Graphics.HCENTER|Graphics.BOTTOM
            );
        }

        g.setColor(RED);
        g.drawRect(
            offsetX,
            (sel-viewTop)*SMALL_FONT.getHeight() + offsetY,
            width,
            SMALL_FONT.getHeight()
        );

    }
}