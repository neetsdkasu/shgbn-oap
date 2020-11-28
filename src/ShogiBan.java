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

    private int curX, curY;

    private Image backgroundImage;
    private Sprite komaStamp, modeMark;
    private TiledLayer komaField, colorField;

    private String[] resChars, resWords;

    private int mode = 0;
    private int state = 0;
    private int menuMode = 0;

    private GameState gameState;

    ShogiBan()
    {
        super(false);
        loadResources();
        makeStaticImages();

        gameState = GameState.newNormalGame(false);
        gameState.init();
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

        g.setFont(SMALL_FONT);
        for (int k = 0; k < 8; k++)
        {
            if (gameState.hands(0,k) > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    MY_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k);
                komaStamp.paint(g);
                if (gameState.hands(0,k) > 1)
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
                        Integer.toString(gameState.hands(0,k)),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + MY_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );
                }
            }
            if (gameState.hands(1,k) > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    OPPO_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k+16 + ((k/7)*8));
                komaStamp.paint(g);
                if (gameState.hands(1,k) > 1)
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
                        Integer.toString(gameState.hands(1,k)),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + OPPO_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );

                }
            }
        }

        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                komaField.setCell(col, row, gameState.field(row, col));
            }
        }

        colorField.paint(g);
        komaField.paint(g);

        g.setColor(CURSOR_COLOR);
        g.drawRect(
            curX*CELL_SIZE + BAN_OFFSET_X,
            curY*CELL_SIZE + BAN_OFFSET_Y,
            CELL_SIZE,
            CELL_SIZE
        );

        modeMark.setFrame(mode);
        modeMark.paint(g);

        g.setColor(LINE_COLOR);
        int headerOffsetX = modeMark.getWidth() + SPACE;
        if (gameState.stepLimit > 0)
        {
            String s = Integer.toString(gameState.stepLimit) + resWords[2];
            g.drawString(
                s,
                headerOffsetX,
                0,
                Graphics.LEFT|Graphics.TOP
            );
            headerOffsetX += SMALL_FONT.stringWidth(s) + SPACE;
        }
        headerOffsetX += SMALL_FONT.stringWidth("000" + resWords[3]);
        g.drawString(
            Integer.toString(gameState.currentStep+1) + resWords[3],
            headerOffsetX,
            0,
            Graphics.RIGHT|Graphics.TOP
        );
        headerOffsetX += SPACE;
        g.drawString(
            resWords[4 + (gameState.firstPlayer ^ gameState.currentPlayer)],
            headerOffsetX,
            0,
            Graphics.LEFT|Graphics.TOP
        );

        g.setColor(LINE_COLOR);
        g.setFont(SMALL_FONT);
        g.drawString(
            resWords[1-gameState.firstPlayer],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(resWords[1-gameState.firstPlayer]))/2,
            OPPO_HAND_OFFSET_Y+(CELL_SIZE-SMALL_FONT.getHeight())/2,
            Graphics.LEFT|Graphics.TOP
        );
        g.drawString(
            resWords[gameState.firstPlayer],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(resWords[gameState.firstPlayer]))/2,
            MY_HAND_OFFSET_Y+(CELL_SIZE-SMALL_FONT.getHeight())/2,
            Graphics.LEFT|Graphics.TOP
        );

        flushGraphics();
    }

    protected void keyPressed(int keyCode)
    {
        switch (getGameAction(keyCode))
        {
        case Canvas.UP:
            curY = (curY + 8) % 9;
            render();
            break;
        case Canvas.DOWN:
            curY = (curY + 1) % 9;
            render();
            break;
        case Canvas.LEFT:
            curX = (curX + 8) % 9;
            render();
            break;
        case Canvas.RIGHT:
            curX = (curX + 1) % 9;
            render();
            break;
        }
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

        resWords = new String[6];

        resWords[0] = resChars[31] + resChars[24]; // first player SEN-TE
        resWords[1] = resChars[32] + resChars[24]; // second player GO-TE
        resWords[2] = resChars[24] + resChars[25]+ resChars[26]; // step limit TE-DU-ME
        resWords[3] = resChars[24] + resChars[33]; // step TE-ME
        resWords[4] = resWords[0] + resChars[34]; // first player's turn SEN-TE-BAN
        resWords[5] = resWords[1] + resChars[34]; // second player's turn GO-TE-BAN
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
            Image img = Image.createImage(4*CELL_SIZE, CELL_SIZE);
            Graphics g = img.getGraphics();

            g.setColor(0x0000FF);
            g.fillRect(0, 0, CELL_SIZE, CELL_SIZE);

            g.setColor(0xFF0000);
            g.fillRect(CELL_SIZE, 0, CELL_SIZE, CELL_SIZE);

            g.setColor(0x00FF00);
            g.fillRect(2*CELL_SIZE, 0, CELL_SIZE, CELL_SIZE);

            g.setColor(0x00FFFF);
            g.fillRect(3*CELL_SIZE, 0, CELL_SIZE, CELL_SIZE);

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