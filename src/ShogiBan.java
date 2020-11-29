import java.io.*;
import java.util.Random;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

class ShogiBan extends GameCanvas
{
    private static final boolean WTK =
        String.valueOf(System.getProperty("microedition.platform"))
            .startsWith("Sun");

    private static final int
        BACKGROUND_COLOR = 0xFFB000,
        CURSOR_COLOR = 0x00FFFF,
        LINE_COLOR = 0x000000,
        RED = 0xFF0000,
        DISP_W = 240,
        DISP_H = 268,
        SPACE = 4,
        CELL_SIZE = 20,
        HANDS_CELL_WIDTH = 9*CELL_SIZE/8,
        OPPO_HAND_OFFSET_Y = 20,
        NUMBER_H_OFFSET_Y = OPPO_HAND_OFFSET_Y+CELL_SIZE+SPACE,
        BAN_OFFSET_X = (DISP_W - 9*CELL_SIZE) / 2,
        BAN_OFFSET_Y = NUMBER_H_OFFSET_Y + 12,
        MY_HAND_OFFSET_Y = BAN_OFFSET_Y + 9*CELL_SIZE + SPACE,
        NUMBER_V_OFFSET_X = BAN_OFFSET_X + 9*CELL_SIZE + SPACE;

    private final Font
        SMALL_FONT = Font.getFont(0, 0, WTK ? Font.SIZE_MEDIUM : Font.SIZE_SMALL);

    private int curX, curY, selX, selY, menuCur;

    private Image backgroundImage;
    private Sprite komaStamp, modeMark;
    private TiledLayer komaField, colorField;

    private String[] resChars, resWords;

    private int mode = 0;
    private int state = 0;
    private int menuMode = 0;

    private Game game;
    private Problem problem;
    private Board board;

    ShogiBan()
    {
        super(false);
        loadResources();
        makeStaticImages();

        game = Game.newNormalGame(false);
        game.init();
        board = game;
    }

    boolean isGameMode()
    {
        return mode == 0;
    }

    void menu()
    {

    }

    void render()
    {
        Graphics g = getGraphics();

        g.drawImage(
            backgroundImage,
            0,
            0,
            Graphics.LEFT|Graphics.TOP
        );

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
        if (board.getStepLimit() > 0)
        {
            String s = Integer.toString(board.getStepLimit()) + resWords[2];
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

        switch (menuMode)
        {
        case 1:
            renderRankUpMenu(g);
            break;
        }

        flushGraphics();
    }

    private void renderRankUpMenu(Graphics g)
    {
        g.setColor(0xE0E0E0);
        g.fillRect(40, 100, 60, 60);
        g.setColor(0xFFFF00);
        g.fillRect(40, 100, 60, 20);
        g.setColor(LINE_COLOR);
        g.drawRect(40, 100, 60, 20);
        g.drawRect(40, 120, 60, 20);
        g.drawRect(40, 140, 60, 20);
        g.drawString(resWords[6], 40, 100, Graphics.LEFT|Graphics.TOP);
        g.drawString(resWords[7], 40, 120, Graphics.LEFT|Graphics.TOP);
        g.drawString(resWords[8], 40, 140, Graphics.LEFT|Graphics.TOP);
        g.setColor(0x7F7F7F);
        g.drawRect(39, 99, 62, 62);
        g.setColor(RED);
        g.drawRect(40, 100, 60, 20);
    }

    private void renderCursor(Graphics g)
    {
        if (isGameMode())
        {
            if (state == 1)
            {
                renderBanCursor(g, selX, selY, 0x00FF00);
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
        headerOffsetX += SMALL_FONT.stringWidth("000" + resWords[3]);
        g.drawString(
            Integer.toString(game.currentStep+1) + resWords[3],
            headerOffsetX,
            0,
            Graphics.RIGHT|Graphics.TOP
        );

        // SEN-TE-BAN | GO-TE-BAN
        headerOffsetX += SPACE;
        g.drawString(
            resWords[4 + (game.firstPlayer ^ game.currentPlayer)],
            headerOffsetX,
            0,
            Graphics.LEFT|Graphics.TOP
        );

        // OU-TE
        if (game.isDanger())
        {
            headerOffsetX += SMALL_FONT.stringWidth(
                resWords[4 + (game.firstPlayer ^ game.currentPlayer)]
            ) + SPACE;
            g.drawString(
                resWords[9],
                headerOffsetX,
                0,
                Graphics.LEFT|Graphics.TOP
            );
        }

        // SEN-TE GO-TE
        g.drawString(
            resWords[1^game.firstPlayer],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(resWords[1^game.firstPlayer]))/2,
            OPPO_HAND_OFFSET_Y+(CELL_SIZE-SMALL_FONT.getHeight())/2,
            Graphics.LEFT|Graphics.TOP
        );
        g.drawString(
            resWords[game.firstPlayer],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(resWords[game.firstPlayer]))/2,
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
                    g.setColor(0xFFFFFF);
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
                    g.setColor(0xFFFFFF);
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
        menuCur = 0;
    }

    private void closeMenu()
    {
        // pop stack submenu?
    }

    protected void keyPressed(int keyCode)
    {
        switch (menuMode)
        {
        case 0:
            if (isGameMode())
            {
                movePlayModeCursor(keyCode);
            }
            break;
        }
    }

    private void movePlayModeCursor(int keyCode)
    {
        switch (getGameAction(keyCode))
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

    private boolean firePlayeMode()
    {
        switch (state)
        {
        case 0:
            if (curY >= 9)
            {
                // TODO (select hands)
                return false;
            }
            if (!game.select(curY, curX))
            {
                return false;
            }
            int frame = (game.isCurrentPlayer(curY, curX) ? 2 : 4) + 1;
            for (int row = 0; row < 9; row++)
            {
                for (int col = 0; col < 9; col++)
                {
                    colorField.setCell(
                        col,
                        row,
                        game.canMoveTo(row, col) ? frame : 0
                    );
                }
            }
            if (game.isCurrentPlayer(curY, curX))
            {
                selX = curX;
                selY = curY;
                state = 1;
            }
            return true;
        case 1:
            if (game.canMoveTo(curY, curX))
            {
                if (game.canRankUp(selY, selX, curY))
                {
                    if (!game.needRankUp(selY, selX, curY))
                    {
                        // state = 2;
                        // openMenu(1);
                        // return true;
                    }
                }
                if (game.move(selY, selX, curY, curX))
                {
                    state = 0;
                    colorField.fillCells(0, 0, 9, 9, 0);
                    return true;
                }
            }
            else if (curY >= 9 || game.field(curY, curX) != 0)
            {
                state = 0;
                return firePlayeMode();
            }
        }
        return false;
    }

    void loadResources()
    {
        InputStream is = getClass().getResourceAsStream("/text.txt");
        try
        {
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            StringBuffer sb = new StringBuffer();
            for (;;)
            {
                int ch = isr.read();
                if (ch < 0)
                {
                    break;
                }
                sb.append((char)ch);
            }
            char[] chars = sb.toString().toCharArray();
            resChars = new String[chars.length];
            for (int i = 0; i < chars.length; i++)
            {
                resChars[i] = String.valueOf(chars[i]);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception ex)
                {
                }
            }
        }

        resWords = new String[10];

        resWords[0] = resChars[31] + resChars[24]; // first player SEN-TE
        resWords[1] = resChars[32] + resChars[24]; // second player GO-TE
        resWords[2] = resChars[24] + resChars[25] + resChars[26]; // step limit TE-DU-ME
        resWords[3] = resChars[24] + resChars[33]; // step TE-ME
        resWords[4] = resWords[0] + resChars[34]; // first player's turn SEN-TE-BAN
        resWords[5] = resWords[1] + resChars[34]; // second player's turn GO-TE-BAN
        resWords[6] = resChars[29] + resChars[30]; // rank up NA-RU
        resWords[7] = resChars[28] + resChars[29]; // keep rank FU-NARI
        resWords[8] = resChars[35] + resChars[36]; // cancel TORI-KESHI
        resWords[9] = resChars[23] + resChars[24]; // check OU-TE
    }

    void makeStaticImages()
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
                    resChars[i],
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

            int invisible = 0xFFFFFF;
            g.setColor(invisible);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());

            for (int i = 0; i < 8; i++)
            {
                for (int k = 0; k < 2; k++)
                {
                    g.setColor(0xFFE010);
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
                    resChars[i+9],
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
                    resChars[i < 4 ? (i+17) : (i+16)],
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
                int c = (((i>>0) & 1) * 0xFF)
                      | (((i>>1) & 1) * 0xFF00)
                      | (((i>>2) & 1) * 0xFF0000);
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

            g.setColor(0x00FF00);
            g.fillRect(0, 0, img.getWidth(), img.getHeight()/2);
            g.setColor(0x007F00);
            g.drawString("PLAY", 0, 0, Graphics.LEFT|Graphics.TOP);

            g.setColor(0xFF00FF);
            g.fillRect(0, img.getHeight()/2, img.getWidth(), img.getHeight()/2);
            g.setColor(0x7F007F);
            g.drawString("EDIT", 0, img.getHeight()/2, Graphics.LEFT|Graphics.TOP);

            img = Image.createImage(img);
            modeMark = new Sprite(img, img.getWidth(), img.getHeight()/2);
            modeMark.setPosition(0, 0);
        }
    }
}