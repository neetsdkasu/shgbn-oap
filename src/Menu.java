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
                WORDS[11], // change range view
                WORDS[12], // prev 1 step
                WORDS[13], // next 1 step
                WORDS[16], // save
                WORDS[17], // replay
                WORDS[15], // new
                WORDS[18]  // change mode
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
                WORDS[19], // move
                WORDS[20], // flip
                WORDS[21]  // change owner
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
                WORDS[19], // move
                WORDS[22], // increment
                WORDS[23]  // decrement
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
                "< 0000" + WORDS[2] + " >", // step limit
                WORDS[16], // save
                WORDS[25], // load
                WORDS[15], // new
                WORDS[24]  // change mode
            });
        }

        return editMenu;
    }

    private static Menu saveMenu = null;

    static Menu getSaveMenu()
    {
        if (saveMenu == null)
        {
            saveMenu = new Menu(6, new String[]{
                WORDS[15] + WORDS[16], // create
                WORDS[14] + WORDS[16], // overwrite
                WORDS[8] // cancel
            });
            saveMenu.setCancelItem(2);
        }

        return saveMenu;
    }

    private static Menu listProblemMenu = null;

    static Menu getListProblemMenu()
    {
        String[] list = Storage.listUpProblem();
        if (listProblemMenu != null)
        {
            if (list[0] == listProblemMenu.text[0])
            {
                return listProblemMenu;
            }
        }
        String[] items = new String[list.length + 1];
        System.arraycopy(list, 0, items, 0, list.length);
        items[items.length-1] = WORDS[8];
        listProblemMenu = new Menu(7, items);
        listProblemMenu.setCancelItem(items.length-1);
        return listProblemMenu;
    }

    private static Menu loadProblemMenu = null;

    static Menu getLoadProblemMenu()
    {
        if (loadProblemMenu == null)
        {
            loadProblemMenu = new Menu(8, new String[]{
                "",
                WORDS[25], // load
                WORDS[27], // delete
                WORDS[8]   // cancel
            });
            loadProblemMenu.setEnable(0, false);
            loadProblemMenu.setCancelItem(3);
        }

        loadProblemMenu.setText(0, listProblemMenu.getText());

        return loadProblemMenu;
    }

    private static Menu newProblemMenu = null;

    static Menu getNewProblemMenu()
    {
        if (newProblemMenu == null)
        {
            newProblemMenu = new Menu(9, new String[]{
                WORDS[29],
                WORDS[28],
                WORDS[30],
                WORDS[31],
                WORDS[8]
            });
            newProblemMenu.setCancelItem(4);
        }

        return newProblemMenu;
    }

    private static Menu listGameMenu = null;

    static Menu getListGameMenu()
    {
        String[] list = Storage.listUpGame();
        if (listGameMenu != null)
        {
            if (list[0] == listGameMenu.text[0])
            {
                return listGameMenu;
            }
        }
        String[] items = new String[list.length + 1];
        System.arraycopy(list, 0, items, 0, list.length);
        items[items.length-1] = WORDS[8];
        listGameMenu = new Menu(10, items);
        listGameMenu.setCancelItem(items.length-1);
        return listGameMenu;
    }

    private static Menu loadGameMenu = null;

    static Menu getLoadGameMenu()
    {
        if (loadGameMenu == null)
        {
            loadGameMenu = new Menu(11, new String[]{
                "",
                WORDS[25], // load
                WORDS[27], // delete
                WORDS[8]   // cancel
            });
            loadGameMenu.setEnable(0, false);
            loadGameMenu.setCancelItem(3);
        }

        loadGameMenu.setText(0, listGameMenu.getText());

        return loadGameMenu;
    }

    private static Menu newGameMenu = null;

    static Menu getNewGameMenu()
    {
        String[] list = Storage.listUpProblem();
        if (newGameMenu != null)
        {
            if (list[0] == newGameMenu.text[2])
            {
                return newGameMenu;
            }
        }
        String[] items = new String[list.length + 3];
        System.arraycopy(list, 0, items, 2, list.length);
        items[0] = WORDS[28];
        items[1] = WORDS[32];
        items[items.length-1] = WORDS[8];
        newGameMenu = new Menu(12, items);
        newGameMenu.setCancelItem(items.length-1);
        return newGameMenu;
    }

    private static final int
        BACKGROUND_COLOR = 0xE0E0E0;

    boolean[] enable;
    String[] text;
    int[] textWidth;
    int id;
    int sel = 0, cancel = -1;
    int width = 0, height = 0, offsetX, offsetY;
    int viewCount, viewTop = 0;
    int value = 0;
    boolean scroll = false;
    int scbar = 10, sch = 1, scc = 1;

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
        if (t.length > viewCount)
        {
            scroll = true;
            int page = (t.length + viewCount - 1) / viewCount;
            int sec = Math.min(20, height / page);
            scbar = height / sec;
            sch = height - scbar;
            scc = t.length - viewCount;
        }
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

    String getText()
    {
        return text[sel];
    }

    String getText(int i)
    {
        return text[i];
    }

    void setCancelItem(int i)
    {
        cancel = i;
    }

    boolean canceled()
    {
        return cancel == sel;
    }

    void setText(int pos, String t)
    {
        text[pos] = t;
        textWidth[pos] = SMALL_FONT.stringWidth(t);
        width = 0;
        for (int i = 0; i < text.length; i++)
        {
            width = Math.max(width, textWidth[i]);
        }
        width = Math.min(width + 20, 220);
        offsetX = (DISP_W - width) / 2;
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
            if (id == 5 && sel == 0)
            {
                value = Math.max(0, value - 2);
                return true;
            }
            if (!scroll)
            {
                return false;
            }
            sel = Math.max(0, sel - (viewCount - 1));
            break;
        case Canvas.RIGHT:
            if (id == 5 && sel == 0)
            {
                value = value == 0 ? 1 : (value + 2);
                return true;
            }
            if (!scroll)
            {
                return false;
            }
            sel = Math.min(text.length - 1, sel + (viewCount - 1));
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

        if (scroll)
        {
            g.setColor(GRAY);
            g.fillRect(
                offsetX+width+3,
                offsetY-2,
                7,
                height+5
            );
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(
                offsetX+width+3,
                offsetY,
                4,
                height
            );
            g.setColor(GRAY);
            g.fillRect(
                offsetX+width+3,
                offsetY + sch * viewTop / scc,
                4,
                scbar
            );
        }

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