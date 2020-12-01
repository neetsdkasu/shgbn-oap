import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

final class ShogiBan extends GameCanvas implements GConstants
{
    private static final int
        BACKGROUND_COLOR = 0xFFB000,
        KOMA_COLOR = 0xFFE010,
        CURSOR_COLOR = CYAN,
        LINE_COLOR = BLACK,
        SPACE = 4,
        CELL_SIZE = 20,
        HANDS_CELL_WIDTH = 9*CELL_SIZE/8,
        OPPO_HAND_OFFSET_Y = 20,
        NUMBER_H_OFFSET_Y = OPPO_HAND_OFFSET_Y+CELL_SIZE+SPACE,
        BAN_OFFSET_X = (DISP_W - 9*CELL_SIZE) / 2,
        BAN_OFFSET_Y = NUMBER_H_OFFSET_Y + 12,
        MY_HAND_OFFSET_Y = BAN_OFFSET_Y + 9*CELL_SIZE + SPACE,
        NUMBER_V_OFFSET_X = BAN_OFFSET_X + 9*CELL_SIZE + SPACE;

    private static int curX, curY, selX, selY;

    private static Image backgroundImage;
    private static Sprite komaStamp, modeMark;
    private static TiledLayer komaField, colorField;

    private static int mode = 0;
    private static int state = 0;
    private static int menuMode = 0;

    private static int rangeMode = 0;

    private static Board board;

    private static Menu menu = null;

    ShogiBan()
    {
        super(false);
        makeStaticImages();

        Problem.setNormalGame();
        Game.init();
        board = Game.getInstance();
        clearMovable();
    }

    boolean isGameMode()
    {
        return mode == 0;
    }

    void menu()
    {
        switch (menuMode)
        {
        case 0:
            if (isGameMode())
            {
                openMenu(2);
                render();
            }
            break;
        case 2:
            closeMenu();
            render();
            break;
        }
    }

    void render()
    {
        Graphics g = getGraphics();

        g.drawImage(backgroundImage, 0, 0, Graphics.LEFT|Graphics.TOP);

        renderHands(g);

        colorField.paint(g);

        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                komaField.setCell(col, row, board.field(row, col));
            }
        }
        komaField.paint(g);

        modeMark.setFrame(mode);
        modeMark.paint(g);

        g.setColor(LINE_COLOR);
        int headerOffsetX = modeMark.getWidth() + SPACE;
        if (Problem.getStepLimit() > 0)
        {
            String s = Integer.toString(Problem.getStepLimit()) + WORDS[2];
            g.drawString(
                s,
                headerOffsetX,
                0,
                Graphics.LEFT|Graphics.TOP
            );
            headerOffsetX += SMALL_FONT.stringWidth(s) + SPACE;
        }

        if (isGameMode())
        {
            renderGameInfo(g, headerOffsetX);
        }

        renderCursor(g);

        if (menuMode != 0)
        {
            menu.paint(g);
        }

        flushGraphics();
    }

    private void renderCursor(Graphics g)
    {
        if (isGameMode())
        {
            if (state != 0)
            {
                renderBanCursor(g, selX, selY, GREEN);
            }
            renderBanCursor(g, curX, curY, CURSOR_COLOR);
        }
    }

    private void renderBanCursor(Graphics g, int x, int y, int color)
    {
        g.setColor(color);
        if (y < 9)
        {
            g.drawRect(
                x*CELL_SIZE + BAN_OFFSET_X,
                y*CELL_SIZE + BAN_OFFSET_Y,
                CELL_SIZE,
                CELL_SIZE
            );
        }
        else if (y == 9)
        {
            g.drawRect(
                x*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                MY_HAND_OFFSET_Y,
                HANDS_CELL_WIDTH,
                CELL_SIZE
            );
        }
        else
        {
            g.drawRect(
                x*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                OPPO_HAND_OFFSET_Y,
                HANDS_CELL_WIDTH,
                CELL_SIZE
            );
        }
    }

    private void renderGameInfo(Graphics g, int headerOffsetX)
    {
        g.setFont(SMALL_FONT);
        g.setColor(LINE_COLOR);

        // ??? TE-ME
        headerOffsetX += SMALL_FONT.stringWidth("0000" + WORDS[3]);
        g.drawString(
            Integer.toString(Game.getCurrentStep()+1) + WORDS[3],
            headerOffsetX,
            0,
            Graphics.RIGHT|Graphics.TOP
        );

        // SEN-TE-BAN | GO-TE-BAN
        headerOffsetX += SPACE;
        g.drawString(
            WORDS[4 + (Game.getFirstPlayer() ^ Game.getCurrentPlayer())],
            headerOffsetX,
            0,
            Graphics.LEFT|Graphics.TOP
        );

        // OU-TE
        if (Game.isDanger())
        {
            headerOffsetX += SMALL_FONT.stringWidth(
                WORDS[4 + (Game.getFirstPlayer() ^ Game.getCurrentPlayer())]
            ) + SPACE;
            String msg = WORDS[Game.isCheckmate() ? 10 : 9];
            g.setColor(Game.isCheckmate() ? LINE_COLOR : RED);
            g.fillRect(
                headerOffsetX,
                0,
                SMALL_FONT.stringWidth(msg) + 3,
                SMALL_FONT.getHeight()
            );
            g.setColor(WHITE);
            g.drawString(msg, headerOffsetX+2, 0, Graphics.LEFT|Graphics.TOP);
            g.setColor(LINE_COLOR);
        }

        // SEN-TE GO-TE
        g.drawString(
            WORDS[1^Game.getFirstPlayer()],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(WORDS[1^Game.getFirstPlayer()]))/2,
            OPPO_HAND_OFFSET_Y+(CELL_SIZE-SMALL_FONT.getHeight())/2,
            Graphics.LEFT|Graphics.TOP
        );
        g.drawString(
            WORDS[Game.getFirstPlayer()],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(WORDS[Game.getFirstPlayer()]))/2,
            MY_HAND_OFFSET_Y+(CELL_SIZE-SMALL_FONT.getHeight())/2,
            Graphics.LEFT|Graphics.TOP
        );
    }

    private void renderHands(Graphics g)
    {
        g.setFont(SMALL_FONT);
        for (int k = 0; k < 8; k++)
        {
            if (board.hands(0,k) > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    MY_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k);
                komaStamp.paint(g);
                if (board.hands(0,k) > 1)
                {
                    g.setColor(WHITE);
                    g.fillRect(
                        (k+1)*HANDS_CELL_WIDTH - 7 + BAN_OFFSET_X,
                        CELL_SIZE - 12 + MY_HAND_OFFSET_Y,
                        6,
                        11
                    );
                    g.setColor(LINE_COLOR);
                    g.drawString(
                        Integer.toString(board.hands(0,k)),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + MY_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );
                }
            }
            if (board.hands(1,k) > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    OPPO_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k+16 + ((k/7)*8));
                komaStamp.paint(g);
                if (board.hands(1,k) > 1)
                {
                    g.setColor(WHITE);
                    g.fillRect(
                        (k+1)*HANDS_CELL_WIDTH - 7 + BAN_OFFSET_X,
                        CELL_SIZE - 12 + OPPO_HAND_OFFSET_Y,
                        6,
                        11
                    );
                    g.setColor(LINE_COLOR);
                    g.drawString(
                        Integer.toString(board.hands(1,k)),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + OPPO_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );

                }
            }
        }
    }

    private void openMenu(int menuId)
    {
        // push stack submenu?
        menuMode = menuId;
        switch (menuId)
        {
        case 1:
            menu = Menu.getRankUpMenu().cleanUp();
            break;
        case 2:
            menu = Menu.getGameMenu();
            // TOOD
            menu.setEnable(1, false);
            menu.setEnable(2, false);
            menu.setEnable(3, false);
            menu.setEnable(4, false);
            menu.setEnable(5, false);
            menu.setEnable(6, false);
            break;
        }
    }

    private void closeMenu()
    {
        // pop stack submenu?
        menuMode = 0;
        menu = null;
    }

    protected void keyPressed(int keyCode)
    {
        int action = getGameAction(keyCode);
        if (menuMode != 0)
        {
            if (menu.keyPressed(keyCode, action))
            {
                render();
                return;
            }
        }
        switch (menuMode)
        {
        case 0:
            if (isGameMode())
            {
                movePlayModeCursor(keyCode, action);
            }
            break;
        case 1:
            actRankUpMenu(keyCode, action);
            break;
        case 2:
            actGameMenu(keyCode, action);
            break;
        }
    }

    private void actGameMenu(int keyCode, int action)
    {
        if (action != Canvas.FIRE)
        {
            return;
        }
        switch (menu.getSelect())
        {
        case 0:
            rangeMode = (rangeMode + 1) % 4;
            if (state == 0)
            {
                clearMovable();
            }
            else
            {
                showMovable(true);
            }
            render();
            break;
        }
    }

    private void actRankUpMenu(int keyCode, int action)
    {
        if (action != Canvas.FIRE)
        {
            return;
        }
        if (Game.move(selY, selX, curY, curX, menu.getSelect() == 0))
        {
            state = 0;
            clearMovable();
            closeMenu();
            render();
        }
    }

    private void movePlayModeCursor(int keyCode, int action)
    {
        switch (action)
        {
        case Canvas.UP:
            curY = (curY + 10) % 11;
            if (curY == 10 && curX == 8)
            {
                curX = 7;
            }
            break;
        case Canvas.DOWN:
            curY = (curY + 1) % 11;
            if (curY == 9 && curX == 8)
            {
                curX = 7;
            }
            break;
        case Canvas.LEFT:
            if (curY < 9)
            {
                curX = (curX + 8) % 9;
            }
            else
            {
                curX = (curX + 7) % 8;
            }
            break;
        case Canvas.RIGHT:
            if (curY < 9)
            {
                curX = (curX + 1) % 9;
            }
            else
            {
                curX = (curX + 1) % 8;
            }
            break;
        case Canvas.FIRE:
            if (!firePlayeMode())
            {
                return;
            }
            break;
        default:
            return;
        }
        render();
    }

    private void clearMovable()
    {
        if (rangeMode == 0)
        {
            colorField.fillCells(0, 0, 9, 9, 0);
            return;
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int cell = 0;
                if ((rangeMode&1) != 0 && Game.getRange(0, row, col) > 0)
                {
                    cell = 2;
                }
                if ((rangeMode&2) != 0 && Game.getRange(1, row, col) > 0)
                {
                    cell = Math.max(1, cell) + 4;
                }
                colorField.setCell(col, row, cell);
            }
        }
    }

    private void showMovable(boolean showCurrentPlayer)
    {
        int frame = 3;
        if (!showCurrentPlayer)
        {
            int another = 1^Game.getCurrentPlayer();
            frame = (rangeMode & (1 << another)) == 0
                ? 2 + 3*another
                : 1;
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int cell = 0;
                if (Game.canMoveTo(row, col))
                {
                   cell = frame;
                }
                if (cell == 0 || showCurrentPlayer)
                {
                    if ((rangeMode&1) != 0 && Game.getRange(0, row, col) > 0)
                    {
                        cell = Math.max(1, cell) + 1;
                    }
                    if ((rangeMode&2) != 0 && Game.getRange(1, row, col) > 0)
                    {
                        cell = Math.max(1, cell) + 4;
                    }
                }
                colorField.setCell(col, row, cell);
            }
        }
    }

    private boolean firePlayeMode()
    {
        switch (state)
        {
        case 0:
        case 4:
            if (curY >= 9)
            {
                if (Game.selectHand(curY - 9, curX))
                {
                    if (Game.getCurrentPlayer() == curY - 9)
                    {
                        state = 3;
                        selX = curX;
                        selY = curY;
                    }
                    showMovable(curY - 9 == Game.getCurrentPlayer());
                    return true;
                }
                if (state == 4)
                {
                    state = 0;
                    clearMovable();
                    return true;
                }
                return false;
            }
            if (!Game.select(curY, curX))
            {
                if (state == 4)
                {
                    state = 0;
                    clearMovable();
                    return true;
                }
                return false;
            }
            showMovable(Game.isCurrentPlayer(curY, curX));
            if (Game.isCurrentPlayer(curY, curX))
            {
                selX = curX;
                selY = curY;
                state = 1;
            }
            else
            {
                state = 0;
            }
            return true;
        case 1:
            if (Game.canMoveTo(curY, curX))
            {
                if (Game.canRankUp(selY, selX, curY))
                {
                    if (!Game.needRankUp(selY, selX, curY))
                    {
                        state = 2;
                        openMenu(1);
                        return true;
                    }
                }
                if (Game.move(selY, selX, curY, curX))
                {
                    state = 0;
                    clearMovable();
                    return true;
                }
            }
            else if (curY >= 9 || !Game.isEmpty(curY, curX))
            {
                state = 4;
                return firePlayeMode();
            }
            break;
        case 3:
            if (curY >= 9 || !Game.isEmpty(curY, curX))
            {
                state = 4;
                return firePlayeMode();
            }
            if (Game.put(selX, curY, curX))
            {
                state = 0;
                clearMovable();
                return true;
            }
            break;
        }
        return false;
    }

    private static void makeStaticImages()
    {
         {
            Image img = Image.createImage(DISP_W, DISP_H);
            Graphics g = img.getGraphics();

            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, DISP_W, DISP_H);

            g.setColor(LINE_COLOR);
            for (int i = 0; i < 10; i++)
            {
                g.drawLine(
                    i*CELL_SIZE + BAN_OFFSET_X,
                    BAN_OFFSET_Y,
                    i*CELL_SIZE + BAN_OFFSET_X,
                    9*CELL_SIZE + BAN_OFFSET_Y
                );

                g.drawLine(
                    BAN_OFFSET_X,
                    i*CELL_SIZE + BAN_OFFSET_Y,
                    9*CELL_SIZE + BAN_OFFSET_X,
                    i*CELL_SIZE + BAN_OFFSET_Y
                );
            }

            g.drawRect(
                BAN_OFFSET_X,
                OPPO_HAND_OFFSET_Y,
                9*CELL_SIZE,
                CELL_SIZE
            );

            g.drawRect(
                BAN_OFFSET_X,
                MY_HAND_OFFSET_Y,
                9*CELL_SIZE,
                CELL_SIZE
            );

            g.setFont(SMALL_FONT);
            g.setColor(LINE_COLOR);

            for (int i = 0; i < 9; i++)
            {
                g.drawString(
                    Integer.toString(9-i),
                    i*CELL_SIZE+CELL_SIZE/2 + BAN_OFFSET_X,
                    12 + NUMBER_H_OFFSET_Y,
                    Graphics.HCENTER|Graphics.BOTTOM
                );

                g.drawString(
                    CHARS[i],
                    NUMBER_V_OFFSET_X,
                    (i+1)*CELL_SIZE + BAN_OFFSET_Y,
                    Graphics.LEFT|Graphics.BOTTOM
                );
            }

            backgroundImage = Image.createImage(img);
        }

        // Sprite
        {
            Image img = Image.createImage(8*CELL_SIZE, 4*CELL_SIZE);
            Graphics g = img.getGraphics();

            g.setFont(SMALL_FONT);

            int invisible = 0xFFFFFF; // dummy color
            g.setColor(invisible);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());

            for (int i = 0; i < 8; i++)
            {
                for (int k = 0; k < 2; k++)
                {
                    g.setColor(KOMA_COLOR);
                    g.fillTriangle(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE/2,
                        k*CELL_SIZE+2
                    );
                    g.fillRect(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        CELL_SIZE-4,
                        CELL_SIZE-7
                    );
                    g.setColor(LINE_COLOR);
                    g.drawLine(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE/2,
                        k*CELL_SIZE+2
                    );
                    g.drawLine(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+CELL_SIZE-2
                    );
                    g.drawLine(
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE/2,
                        k*CELL_SIZE+2
                    );
                    g.drawLine(
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+CELL_SIZE-2
                    );
                    g.drawLine(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+CELL_SIZE-2,
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+CELL_SIZE-2
                    );

                }

                g.setColor(LINE_COLOR);

                g.drawString(
                    CHARS[i+9],
                    i*CELL_SIZE+CELL_SIZE/2+1,
                    CELL_SIZE-1,
                    Graphics.HCENTER|Graphics.BOTTOM
                );

                if (i == 4)
                {
                    continue;
                }

                if (i < 7)
                {
                    g.setColor(RED);
                }

                g.drawString(
                    CHARS[i < 4 ? (i+17) : (i+16)],
                    i*CELL_SIZE+CELL_SIZE/2+1,
                    2*CELL_SIZE-1,
                    Graphics.HCENTER|Graphics.BOTTOM
                );

            }

            int[] rgbData = new int[img.getWidth() * img.getHeight()];
            img.getRGB(rgbData, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
            int mask = 0xFFFFFF;
            for (int i = 0; i < rgbData.length; i++)
            {
                if ((rgbData[i] & mask) == invisible)
                {
                    rgbData[i] = 0;
                }
                else
                {
                    rgbData[i] = rgbData[i] | 0xFF000000;
                }
            }

            for (int row = 0; row < 2; row++)
            {
                for (int col = 0; col < 8; col++)
                {
                    for (int y = 1; y < CELL_SIZE; y++)
                    {
                        int py = (row*CELL_SIZE+y) * img.getWidth();
                        int ry = ((row+2)*CELL_SIZE+(CELL_SIZE-y)) * img.getWidth();
                        for (int x = 1; x < CELL_SIZE; x++)
                        {
                            int px = col*CELL_SIZE+x;
                            int rx = col*CELL_SIZE+(CELL_SIZE-x);
                            rgbData[ry+rx] = rgbData[py+px];
                        }
                    }
                }
            }

            img = Image.createRGBImage(rgbData, img.getWidth(), img.getHeight(), true);
            komaStamp = new Sprite(img, CELL_SIZE, CELL_SIZE);
            komaField = new TiledLayer(9, 9, img, CELL_SIZE, CELL_SIZE);
            komaField.setPosition(BAN_OFFSET_X, BAN_OFFSET_Y);
        }

        // color field
        {
            Image img = Image.createImage(8*CELL_SIZE, CELL_SIZE);
            Graphics g = img.getGraphics();

            for (int i = 0; i < 8; i++)
            {
                int c = (((i>>0) & 1) * BLUE)
                      | (((i>>1) & 1) * GREEN)
                      | (((i>>2) & 1) * RED);
                g.setColor(c);
                g.fillRect(i*CELL_SIZE, 0, CELL_SIZE, CELL_SIZE);
            }

            int[] rgbData = new int[img.getWidth() * img.getHeight()];
            img.getRGB(rgbData, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
            for (int i = 0; i < rgbData.length; i++)
            {
                rgbData[i] = rgbData[i] & 0x77FFFFFF;
            }

            img = Image.createRGBImage(rgbData, img.getWidth(), img.getHeight(), true);
            colorField = new TiledLayer(9, 9, img, CELL_SIZE, CELL_SIZE);
            colorField.setPosition(BAN_OFFSET_X, BAN_OFFSET_Y);
        }

        // mode mark
        {
            int w = Math.max(
                SMALL_FONT.stringWidth("PLAY"),
                SMALL_FONT.stringWidth("EDIT")
            );

            Image img = Image.createImage(w, SMALL_FONT.getHeight()*2);
            Graphics g = img.getGraphics();

            g.setFont(SMALL_FONT);

            g.setColor(GREEN);
            g.fillRect(0, 0, img.getWidth(), img.getHeight()/2);
            g.setColor(DARK_GREEN);
            g.drawString("PLAY", 0, 0, Graphics.LEFT|Graphics.TOP);

            g.setColor(MAGENTA);
            g.fillRect(0, img.getHeight()/2, img.getWidth(), img.getHeight()/2);
            g.setColor(DARK_MAGENTA);
            g.drawString("EDIT", 0, img.getHeight()/2, Graphics.LEFT|Graphics.TOP);

            img = Image.createImage(img);
            modeMark = new Sprite(img, img.getWidth(), img.getHeight()/2);
            modeMark.setPosition(0, 0);
        }
    }
}