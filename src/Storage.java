import java.io.*;
import javax.microedition.rms.*;

final class Storage
{
    private static RecordStore
        stateRS = null,
        tempGameRS = null,
        tempProblemRS = null,
        gameRS = null,
        problemRS = null;

    private static boolean
        updatedProblemList = false,
        updatedGameList = false;

    private static int[]
        problemIDs = null,
        gameIDs = null;

    private static String[]
        problemTitles = null,
        gameTitles = null;

    static void init()
    {
        try
        {
            stateRS = RecordStore.openRecordStore("shgbn.state", true);
            tempGameRS = RecordStore.openRecordStore("shgbn.temp.game", true);
            tempProblemRS = RecordStore.openRecordStore("shgbn.temp.problem", true);
            gameRS = RecordStore.openRecordStore("shgbn.game", true);
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
        tempGameRS = close(tempGameRS);
        tempProblemRS = close(tempProblemRS);
        gameRS = close(gameRS);
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

    static boolean hasGame()
    {
        try
        {
            return gameRS.getNumRecords() > 0;
        }
        catch (RecordStoreException _)
        {
            return false;
        }
    }

    static boolean hasGame(int id)
    {
        try
        {
            return id > 0 && gameRS.getRecordSize(id) > 0;
        }
        catch (RecordStoreException _)
        {
            return false;
        }
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

    static boolean hasProblem(int id)
    {
        try
        {
            return id > 0 && problemRS.getRecordSize(id) > 0;
        }
        catch (RecordStoreException _)
        {
            return false;
        }
    }

    static void loadAppState()
    {
        try
        {
            if (stateRS.getNumRecords() > 0)
            {
                byte[] data = stateRS.getRecord(1);
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);
                ShogiBan.readFrom(dis);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
    }

    static void saveAppState()
    {
        ByteArrayOutputStream baos = null;
        try
        {
            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            ShogiBan.writeTo(dos);
            dos.flush();
            byte[] data = baos.toByteArray();
            if (stateRS.getNumRecords() > 0)
            {
                stateRS.setRecord(1, data, 0, data.length);
            }
            else
            {
                stateRS.addRecord(data, 0, data.length);
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

    static String[] listUpGame()
    {
        if (gameTitles != null && !updatedGameList)
        {
            return gameTitles;
        }
        RecordEnumeration re = null;
        try
        {
            int n = gameRS.getNumRecords();
            if (gameIDs == null || gameIDs.length != n)
            {
                gameIDs = new int[n];
                gameTitles = new String[n];
            }

            re = gameRS.enumerateRecords(null, null, false);
            int i = 0;
            while (re.hasNextElement())
            {
                byte[] data = re.nextRecord();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);
                gameIDs[i] = dis.readInt(); // Game.recordID
                dis.readLong(); // Game.date
                dis.readLong(); // Game.update
                gameTitles[i] = dis.readUTF(); // Game.title
                dis.readInt(); // Problem.recordID
                dis.readLong(); // Problem.date
                dis.readLong(); // Problem.update
                String pTitle = dis.readUTF(); // Problem.title
                if (gameTitles[i] == null || gameTitles[i].length() == 0)
                {
                    gameTitles[i] = pTitle;
                }
                int stepLimit = dis.readInt(); // Problem.stepLimit
                if (stepLimit > 0)
                {
                    gameTitles[i] = Integer.toString(stepLimit)
                                     + GConstants.WORDS[2]
                                     + " "
                                     + gameTitles[i];
                }
                i++;
            }
            updatedGameList = false;
            return gameTitles;
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
                problemIDs[i] = dis.readInt(); // Problem.recordID
                dis.readLong(); // Problem.date
                dis.readLong(); // Problem.update
                problemTitles[i] = dis.readUTF(); // Problem.title
                int stepLimit = dis.readInt(); // Problem.stepLimit
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

    static void deleteGame(int sel)
    {
        try
        {
            int id = gameIDs[sel];
            gameRS.deleteRecord(id);
            updatedGameList = true;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
    }

    static void deleteProblem(int sel)
    {
        try
        {
            int id = problemIDs[sel];
            problemRS.deleteRecord(id);
            updatedProblemList = true;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
    }

    static void loadGame(int sel)
    {
        try
        {
            int id = gameIDs[sel];
            byte[] data = gameRS.getRecord(id);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);
            Game.readFrom(dis);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
        }
    }

    static void loadTemporaryGame()
    {
        try
        {
            if (tempGameRS.getNumRecords() > 0)
            {
                byte[] data = tempGameRS.getRecord(1);
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);
                Game.readFrom(dis);
                Game.ready();
            }
            else
            {
                Game.clear();
                Problem.setNormalGame();
                Game.initPlay();
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.toString());
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

    static void loadTemporaryProblem()
    {
        try
        {
            if (tempProblemRS.getNumRecords() > 0)
            {
                byte[] data = tempProblemRS.getRecord(1);
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);
                Problem.readFrom(dis);
            }
            else
            {
                Problem.setPuzzleTemplate();
            }
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

    static void saveTemporaryProblem()
    {
        ByteArrayOutputStream baos = null;
        try
        {
            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            Problem.writeTo(dos);
            dos.flush();
            byte[] data = baos.toByteArray();
            if (tempProblemRS.getNumRecords() > 0)
            {
                tempProblemRS.setRecord(1, data, 0, data.length);
            }
            else
            {
                tempProblemRS.addRecord(data, 0, data.length);
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

    static void saveGame(boolean overwrite)
    {
        ByteArrayOutputStream baos = null;
        try
        {
            Game.update = System.currentTimeMillis();
            if (!overwrite)
            {
                Game.recordID = gameRS.getNextRecordID();
            }
            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            Game.writeTo(dos);
            dos.flush();
            byte[] data = baos.toByteArray();
            if (overwrite)
            {
                int id = Game.recordID;
                gameRS.setRecord(id, data, 0, data.length);
            }
            else
            {
                gameRS.addRecord(data, 0, data.length);
            }
            updatedGameList = true;
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

    static void saveTemporaryGame()
    {
        ByteArrayOutputStream baos = null;
        try
        {
            baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            Game.writeTo(dos);
            dos.flush();
            byte[] data = baos.toByteArray();
            if (tempGameRS.getNumRecords() > 0)
            {
                tempGameRS.setRecord(1, data, 0, data.length);
            }
            else
            {
                tempGameRS.addRecord(data, 0, data.length);
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
