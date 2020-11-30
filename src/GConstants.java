import javax.microedition.lcdui.Font;

interface GConstants
{
    int
        BLACK = 0x000000,
        RED = 0xFF0000,
        GREEN = 0x00FF00,
        BLUE = 0x0000FF,
        CYAN = 0x00FFFF,
        YELLOW = 0xFFFF00,
        GRAY = 0x7F7F7F,
        MAGENTA = 0xFF00FF,
        WHITE = 0xFFFFFF,
        DARK_GREEN = 0x007F00,
        DARK_MAGENTA = 0x7F007F,
        DISP_W = 240,
        DISP_H = 268;

    String[]
        CHARS = new String[50],
        WORDS = new String[20];

    Font
        SMALL_FONT = Font.getFont(
            0,
            0,
            ShogiBanMIDlet.WTK ? Font.SIZE_MEDIUM : Font.SIZE_SMALL
        );
}