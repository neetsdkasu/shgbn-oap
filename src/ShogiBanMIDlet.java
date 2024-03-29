import java.io.InputStream;
import java.io.InputStreamReader;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public final class ShogiBanMIDlet extends MIDlet implements CommandListener
{
    static final boolean WTK =
        String.valueOf(System.getProperty("microedition.platform"))
            .startsWith("Sun");

    private static ShogiBanMIDlet midlet = null;

    private static TextBox
        titleTextBox = null,
        exportTextBox = null,
        importTextBox = null;

    private static ShogiBan shogiBan = null;

    private static Command
        exitCommand = null,
        menuCommand = null,
        saveCommand = null,
        cancelCommand = null,
        closeCommand = null,
        importCommand = null;

    public ShogiBanMIDlet()
    {
        Storage.init();
        loadResources();

        midlet = this;

        exitCommand = new Command("EXIT", Command.EXIT, 1);
        menuCommand = new Command("MENU", Command.SCREEN, 1);

        shogiBan = new ShogiBan();

        shogiBan.addCommand(exitCommand);
        shogiBan.addCommand(menuCommand);

        shogiBan.setCommandListener(this);

        Display.getDisplay(this).setCurrent(shogiBan);
    }

    private void release()
    {
        Storage.saveAppState();
        if (ShogiBan.isGameMode())
        {
            Storage.saveTemporaryGame();
        }
        else if (ShogiBan.isEditMode())
        {
            Storage.saveTemporaryProblem();
        }
        Storage.closeAll();
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
    {
        release();
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
        if (cmd == null)
        {
            return;
        }
        if (cmd == exitCommand)
        {
            release();
			notifyDestroyed();
        }
        else if (cmd == menuCommand)
        {
            shogiBan.menu();
        }
        else if (cmd == saveCommand)
        {
            String title = titleTextBox.getString();
            if (title != null && (title = title.trim()) != "")
            {
                shogiBan.save(title);
                Display.getDisplay(this).setCurrent(shogiBan);
            }
            else
            {
                titleTextBox.setTicker(new Ticker("empty is wrong"));
            }
        }
        else if (cmd == importCommand)
        {
            String data = importTextBox.getString();
            if (data != null && (data = data.trim()) != "")
            {
                shogiBan.importData(data);
                Display.getDisplay(this).setCurrent(shogiBan);
            }
            else
            {
                importTextBox.setTicker(new Ticker("empty is wrong"));
            }
        }
        else if (cmd == closeCommand)
        {
            Display.getDisplay(this).setCurrent(shogiBan);
        }
        else if (cmd.getCommandType() == Command.CANCEL)
        {
            if (titleTextBox != null && titleTextBox.isShown())
            {
                shogiBan.save(null);
            }
            Display.getDisplay(this).setCurrent(shogiBan);
        }
    }

    static void showTitleTextBox(String title)
    {
        if (titleTextBox == null)
        {
            titleTextBox = new TextBox(GConstants.WORDS[26], title, 50, TextField.ANY);
            saveCommand = new Command("SAVE", Command.OK, 1);
            cancelCommand = new Command("CANCEL", Command.CANCEL, 2);
            titleTextBox.addCommand(saveCommand);
            titleTextBox.addCommand(cancelCommand);
            titleTextBox.setCommandListener(midlet);
        }
        else
        {
            titleTextBox.setString(title);
            titleTextBox.setTicker(null);
        }
        Display.getDisplay(midlet).setCurrent(titleTextBox);
    }

    static void showImport()
    {
        if (importTextBox == null)
        {

            importTextBox = new TextBox(GConstants.WORDS[39], "", 5000, TextField.ANY);
            importCommand = new Command("IMPORT", Command.OK, 1);
            cancelCommand = new Command("CANCEL", Command.CANCEL, 1);
            importTextBox.addCommand(importCommand);
            importTextBox.addCommand(cancelCommand);
            importTextBox.setCommandListener(midlet);
        }
        else
        {
            importTextBox.setString("");
            importTextBox.setTicker(null);
        }
        Display.getDisplay(midlet).setCurrent(importTextBox);
    }

    static void showExport(String data)
    {
        if (data.length() > 5000)
        {
            data = data.substring(0, 5000); // what?
        }
        if (exportTextBox == null)
        {

            exportTextBox = new TextBox(GConstants.WORDS[40], data, 5000, TextField.ANY);
            closeCommand = new Command("CLOSE", Command.SCREEN, 1);
            exportTextBox.addCommand(closeCommand);
            exportTextBox.setCommandListener(midlet);
        }
        else
        {
            exportTextBox.setString(data);
        }
        Display.getDisplay(midlet).setCurrent(exportTextBox);
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
        resWords[2] = resChars[24] + resChars[25]; // step limit TE-DUME
        resWords[3] = resChars[24] + resChars[33]; // step TE-ME
        resWords[4] = resWords[0] + resChars[34]; // first player's turn SEN-TE-BAN
        resWords[5] = resWords[1] + resChars[34]; // second player's turn GO-TE-BAN
        resWords[6] = resChars[29] + resChars[30]; // rank up NA-RU
        resWords[7] = resChars[28] + resChars[29]; // keep rank FU-NARI
        resWords[8] = resChars[35] + resChars[36]; // cancel TORI-KESHI
        resWords[9] = resChars[23] + resChars[24]; // check OU-TE
        resWords[10] = resChars[25] + resChars[27]; // step TSU-MI
        resWords[11] = resChars[37] + resChars[38]
                     + resChars[39] + resChars[40]
                     + resChars[41] + resChars[42]; // switch range view KI-KI-HYO-JI-KIRI-KAE
        resWords[12] = "1" + resChars[24] + resChars[43] + resChars[30]; // undo 1 step 1-TE-MODO-RU
        resWords[13] = "1" + resChars[24] + resChars[44] + resChars[45]; // redo 1 step 1-TE-SUSU-MU
        resWords[14] = resChars[46] + resChars[47]; // overwrite UWA-GAKI
        resWords[15] = resChars[48] + resChars[49]; // new SHIN-KI
        resWords[16] = resChars[50] + resChars[51]; // save HO-ZON
        resWords[17] = resChars[52] + resChars[53]; // replay SAI-KAI
        resWords[18] = resChars[54] + resChars[29]
                     + resChars[55] + resChars[56] + resChars[57]; // make mode SAKU-SEI-MO---DO
        resWords[19] = resChars[58] + resChars[59]; // move I-DOU
        resWords[20] = resChars[60] + resChars[61] + resChars[62]; // flip URA-GAE-SU
        resWords[21] = resChars[63] + resChars[38]
                     + resChars[64] + resChars[65]; // change owner MU-KI-HEN-KOU
        resWords[22] = resChars[66] + resChars[67] + resChars[62]; // increment FU-YA-SU
        resWords[23] = resChars[68] + resChars[69] + resChars[62]; // decrement HE-RA-SU
        resWords[24] = resChars[70] + resChars[71]
                     + resChars[55] + resChars[56] + resChars[57]; // play mode SHI-KOU-MO---DO
        resWords[25] = resChars[72] + resChars[73]; // read YOMI-DASHI
        resWords[26] = resChars[50] + resChars[51]
                     + resChars[74] + resChars[75] + resChars[76]; // save HO-ZON-MEI-SHI-TEI
        resWords[27] = resChars[77] + resChars[78]; // delete SAKU-JO
        resWords[28] = resChars[79] + resChars[80] + resChars[81]; // HON-SHOU-GI
        resWords[29] = resChars[25] + resChars[80] + resChars[81]; // TUME-SHOU-GI
        resWords[30] = resChars[70] + resChars[71] + resChars[82]; // playing-game SHI-KOU-CHU
        resWords[31] = resChars[50] + resChars[51]
                     + resChars[70] + resChars[71]; // saved-game HO-ZON-SHI-KOU
        resWords[32] = resChars[54] + resChars[29] + resChars[82]; // editing-problem SAKU-SEI-CHU
        resWords[33] = resChars[54] + resChars[29]; // edit-problem SAKU-SEI
        resWords[34] = resChars[70] + resChars[71]; // play-game SHI-KOU
        resWords[35] = resChars[77] + resChars[78]
                     + resChars[91] + resChars[90]
                     + resChars[91] + resChars[92]; // deleted SAKU-JO-SHI-MA-SHI-TA
        resWords[36] = resChars[50] + resChars[51]
                     + resChars[91] + resChars[90]
                     + resChars[91] + resChars[92]; // saved HO-ZON-SHI-MA-SHI-TA
        resWords[37] = resChars[72] + resChars[27]
                     + resChars[93] + resChars[27]
                     + resChars[90] + resChars[91] + resChars[92]; // loaded YO-MI-KO-MI-MA-SHI-TA
        resWords[38] = resWords[8]
                     + resChars[90] + resChars[91] + resChars[92]; // canceled TORI-KESHI-MA-SHI-TA
        resWords[39] = resChars[83] + resChars[84]
                     + resChars[85] + resChars[56] + resChars[86]; // import I-N-PO---TO
        resWords[40] = resChars[87] + resChars[88] + resChars[89]
                     + resChars[85] + resChars[56] + resChars[86]; // export E-KU-SU-PO---TO
        resWords[41] = resWords[0]
                     + resChars[96] + resChars[97] + resChars[98]
                     + resWords[19]; // SEN-TE-BAN-DAI-HE-I-DOU
        resWords[42] = resWords[1]
                     + resChars[96] + resChars[97] + resChars[98]
                     + resWords[19]; // GO-TE-BAN-DAI-HE-I-DOU
    }
}