import java.io.InputStream;
import java.io.InputStreamReader;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public final class ShogiBanMIDlet extends MIDlet implements CommandListener
{
    static final boolean WTK =
        String.valueOf(System.getProperty("microedition.platform"))
            .startsWith("Sun");

    private final ShogiBan shogiBan;

    private final Command exitCommand, menuCommand;

    public ShogiBanMIDlet()
    {
        loadResources();

        exitCommand = new Command("EXIT", Command.EXIT, 1);
        menuCommand = new Command("MENU", Command.SCREEN, 1);

        shogiBan = new ShogiBan();

        shogiBan.addCommand(exitCommand);
        shogiBan.addCommand(menuCommand);

        shogiBan.setCommandListener(this);

        Display.getDisplay(this).setCurrent(shogiBan);
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
    {
    }

    protected void pauseApp()
    {
    }

    protected void startApp() throws MIDletStateChangeException
    {
        shogiBan.render();
    }

	public void commandAction(Command cmd, Displayable disp)
	{
        if (cmd == exitCommand)
        {
			notifyDestroyed();
        }
        else if (cmd == menuCommand)
        {
            shogiBan.menu();
        }
    }

    private static void loadResources()
    {
        String[] resChars = GConstants.CHARS;
        InputStream is = ShogiBanMIDlet.class.getResourceAsStream("/text.txt");
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

        String[] resWords = GConstants.WORDS;

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
        resWords[10] = resChars[25] + resChars[27]; // step TSU-MI
    }
}