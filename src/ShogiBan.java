import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

class ShogiBan extends GameCanvas
{
    private static final int
        BACKGROUND_COLOR = 0xFFB000,
        CURSOR_COLOR = 0x00FFFF,
        LINE_COLOR = 0x000000,
        DISP_W = 240,
        DISP_H = 268,
        SPACE = 4,
        CELL_SIZE = 20,
        OPPO_HAND_OFFSET_Y = 14,
        NUMBER_H_OFFSET_Y = OPPO_HAND_OFFSET_Y+CELL_SIZE+SPACE,
        BAN_OFFSET_X = (DISP_W - 9*CELL_SIZE) / 2,
        BAN_OFFSET_Y = NUMBER_H_OFFSET_Y + 12,
        MY_HAND_OFFSET_Y = BAN_OFFSET_Y + 9*CELL_SIZE + SPACE,
        NUMBER_V_OFFSET_X = BAN_OFFSET_X + 9*CELL_SIZE + SPACE;

    private int curX, curY;

    private Image backgroundImage;

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
        char[] resChars = null;
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
                resChars = sb.toString().toCharArray();
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
                    String.valueOf(resChars[i]),
                    NUMBER_V_OFFSET_X,
                    (i+1)*CELL_SIZE + BAN_OFFSET_Y,
                    Graphics.LEFT|Graphics.BOTTOM
                );
            }

            backgroundImage = Image.createImage(img);
        }

    }
}