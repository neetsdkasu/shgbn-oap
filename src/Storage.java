import java.io.*;
import javax.microedition.rms.*;

final class Storage
{
    private static RecordStore
        stateRS = null,
        tempPlayRS = null,
        tempProblemRS = null,
        playRS = null,
        problemRS = null;

    private static boolean
        updatedProblemList = false;

    private static int[]
        problemIDs = null;

    private static String[]
        problemTitles = null;

    static void init()
    {
        try
        {
            stateRS = RecordStore.openRecordStore("shgbn.state", true);
            tempPlayRS = RecordStore.openRecordStore("shgbn.temp.play", true);
            tempProblemRS = RecordStore.openRecordStore("shgbn.temp.problem", true);
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
        stateRS = close(stateRS);
        tempPlayRS = close(tempPlayRS);
        tempProblemRS = close(tempProblemRS);
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

    static boolean hasProblem()
    {
        try
        {
            return problemRS.getNumRecords() > 0;
        }
        catch (RecordStoreException _)
        {
            return false;
        }
    }

    static String[] listUpProblem()
    {
        if (problemTitles != null && !updatedProblemList)
        {
            return problemTitles;
        }
        RecordEnumeration re = null;
        try
        {
            int n = problemRS.getNumRecords();
            if (problemIDs == null || problemIDs.length != n)
            {
                problemIDs = new int[n];
                problemTitles = new String[n];
            }

            re = problemRS.enumerateRecords(null, null, false);
            int i = 0;
            while (re.hasNextElement())
            {
                byte[] data = re.nextRecord();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);
                problemIDs[i] = dis.readInt();
                dis.readLong();
                dis.readLong();
                problemTitles[i] = dis.readUTF();
                int stepLimit = dis.readInt();
                if (stepLimit > 0)
                {
                    problemTitles[i] = Integer.toString(stepLimit)
                                     + GConstants.WORDS[2]
                                     + " "
                                     + problemTitles[i];
                }
                i++;
            }
            updatedProblemList = false;
            return problemTitles;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
        finally
        {
            if (re != null)
            {
                re.destroy();
            }
        }
    }

    static void loadProblem(int sel)
    {
        try
        {
            int id = problemIDs[sel];
            byte[] data = problemRS.getRecord(id);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            Problem.readFrom(dis);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
    }

    static void saveProblem(boolean overwrite)
    {
        ByteArrayOutputStream baos = null;
        try
        {
            Problem.update = System.currentTimeMillis();
            if (!overwrite)
            {
                Problem.recordID = problemRS.getNextRecordID();
            }
            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            Problem.writeTo(dos);
            dos.flush();
            byte[] data = baos.toByteArray();
            if (overwrite)
            {
                int id = Problem.recordID;
                problemRS.setRecord(id, data, 0, data.length);
            }
            else
            {
                problemRS.addRecord(data, 0, data.length);
            }
            updatedProblemList = true;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
        finally
        {
            if (baos != null)
            {
                try
                {
                    baos.close();
                }
                catch (Exception __)
                {
                    // do nothing
                }
            }
        }
    }

    static void saveGame(boolean overwrite)
    {
        ByteArrayOutputStream baos = null;
        try
        {
            Game.update = System.currentTimeMillis();
            if (!overwrite)
            {
                Game.recordID = playRS.getNextRecordID();
            }
            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            Game.writeTo(dos);
            dos.flush();
            byte[] data = baos.toByteArray();
            if (overwrite)
            {
                int id = Game.recordID;
                playRS.setRecord(id, data, 0, data.length);
            }
            else
            {
                playRS.addRecord(data, 0, data.length);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
        finally
        {
            if (baos != null)
            {
                try
                {
                    baos.close();
                }
                catch (Exception __)
                {
                    // do nothing
                }
            }
        }
    }
}
