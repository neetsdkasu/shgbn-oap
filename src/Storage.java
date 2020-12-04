import javax.microedition.rms.*;

final class Storage
{
    private static RecordStore
        tempRS = null,
        playRS = null,
        problemRS = null;

    static void init()
    {
        try
        {
            tempRS = RecordStore.openRecordStore("shgbn.temp", true);
            playRS = RecordStore.openRecordStore("shgbn.play", true);
            problemRS = RecordStore.openRecordStore("shgbn.problem", true);
        }
        catch (RecordStoreException ex)
        {
            closeAll();
            throw new RuntimeException(ex.toString());
        }
    }

    static void closeAll()
    {
        tempRS = close(tempRS);
        playRS = close(playRS);
        problemRS = close(problemRS);
    }

    private static RecordStore close(RecordStore rs)
    {
        if (rs != null)
        {
            try
            {
                rs.closeRecordStore();
            }
            catch (RecordStoreException _)
            {
                // do nothing
            }
        }
        return null;
    }

    static void saveProblem(boolean overwrite)
    {

    }

    static void saveGame(boolean overwrite)
    {

    }
}
