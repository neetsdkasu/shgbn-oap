import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class ShogiBanMIDlet extends MIDlet
{
    private ShogiBan shogiBan = null;

    public ShogiBanMIDlet()
    {
        shogiBan = new ShogiBan();
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
}