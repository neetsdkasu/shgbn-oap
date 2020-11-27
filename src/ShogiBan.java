import java.io.*;
import java.util.Random;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

class ShogiBan extends GameCanvas
{
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
        OPPO_HAND_OFFSET_Y = 14,
        NUMBER_H_OFFSET_Y = OPPO_HAND_OFFSET_Y+CELL_SIZE+SPACE,
        BAN_OFFSET_X = (DISP_W - 9*CELL_SIZE) / 2,
        BAN_OFFSET_Y = NUMBER_H_OFFSET_Y + 12,
        MY_HAND_OFFSET_Y = BAN_OFFSET_Y + 9*CELL_SIZE + SPACE,
        NUMBER_V_OFFSET_X = BAN_OFFSET_X + 9*CELL_SIZE + SPACE;

    private int curX, curY;

    private Image backgroundImage;
    private Sprite komaStamp;
    private TiledLayer komaField, colorField;

    private String[] resChars = null;

    private int[][] hands = new int[2][8];

    ShogiBan()
    {
        super(false);
        makeStaticImages();
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

        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        for (int i = 0; i < 9; i++)
        {
            for (int k = 0; k < 9; k++)
            {
                if ((rand.nextInt()&7) < 3)
                {
                    colorField.setCell(i,k,(i+k)%4+1);
                }
                else
                {
                    colorField.setCell(i,k,0);
                }
            }
        }
        for (int i = 0; i < 2; i++)
            for (int k = 0; k < 8; k++)
                hands[i][k] = rand.nextInt(4);


        g.setFont(Font.getFont(0,0,Font.SIZE_SMALL));
        for (int k = 0; k < 8; k++)
        {
            if (hands[0][k] > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    MY_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k);
                komaStamp.paint(g);
                if (hands[0][k] > 1)
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
                        Integer.toString(hands[0][k]),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + MY_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );
                }
            }
            if (hands[1][k] > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    OPPO_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k+16 + ((k/7)*8));
                komaStamp.paint(g);
                if (hands[1][k] > 1)
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
                        Integer.toString(hands[1][k]),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + OPPO_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );

                }
            }
        }

        colorField.paint(g);

        for (int i = 0; i < 32; i++)
        {
            komaStamp.setFrame(i);
            komaStamp.setPosition(
                (i%8)*CELL_SIZE+BAN_OFFSET_X,
                (i/8)*CELL_SIZE+BAN_OFFSET_Y
            );
            komaStamp.paint(g);

            komaField.setCell(i%8,i/8+5, i+1);
        }

        komaField.paint(g);

        g.setColor(CURSOR_COLOR);
        g.drawRect(
            curX*CELL_SIZE + BAN_OFFSET_X,
            curY*CELL_SIZE + BAN_OFFSET_Y,
            CELL_SIZE,
            CELL_SIZE
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

    void makeStaticImages()
    {
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
        }

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

            g.setFont(Font.getFont(0,0,Font.SIZE_SMALL));
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

    }
}