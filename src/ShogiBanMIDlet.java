import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class ShogiBanMIDlet extends MIDlet implements CommandListener
{
    static final boolean WTK =
        String.valueOf(System.getProperty("microedition.platform"))
            .startsWith("Sun");

    private final ShogiBan shogiBan;

    private final Command exitCommand, menuCommand;

    public ShogiBanMIDlet()
    {
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

}