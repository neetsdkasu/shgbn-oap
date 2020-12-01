
final class History
{
    private static final int S = 128;

    private static int[][] history = null;

    static
    {
        history = new int[10][];
        history[0] = new int[S];
    }

    private static int
        insertPos = 0,
        count = 0;

    static int getCount()
    {
        return count;
    }

    static boolean hasPrev()
    {
        return insertPos > 0;
    }

    static boolean hasNext()
    {
        return insertPos < count;
    }

    static boolean movePrev()
    {
        if (!hasPrev())
        {
            return false;
        }
        insertPos--;
        return true;
    }

    static boolean moveNext()
    {
        if (!hasNext())
        {
            return false;
        }
        insertPos++;
        return true;
    }

    static int getFromRow()
    {
        if (insertPos == 0)
        {
            return -1;
        }
        return (get()&127)/9;
    }

    static int getFromCol()
    {
        if (insertPos == 0)
        {
            return -1;
        }
        return (get()&127)%9;
    }

    static int getToRow()
    {
        if (insertPos == 0)
        {
            return -1;
        }
        return ((get()>>7)&127)/9;
    }

    static int getToCol()
    {
        if (insertPos == 0)
        {
            return -1;
        }
        return ((get()>>7)&127)%9;
    }

    static int getFromKoma()
    {
        if (insertPos == 0)
        {
            return -1;
        }
        return ((get()>>14)&63);
    }

    static int getToKoma()
    {
        if (insertPos == 0)
        {
            return -1;
        }
        return ((get()>>20)&63);
    }

    static boolean getRankUp()
    {
        if (insertPos == 0)
        {
            return false;
        }
        return ((get()>>26)&1) != 0;
    }

    static boolean isFromHand()
    {
        if (insertPos == 0)
        {
            return false;
        }
        return getFromRow() == 9;
    }

    static void clear()
    {
        insertPos = 0;
        count = 0;
    }

    static void addFromHand(int toRow, int toCol, int fromKoma)
    {
        add(9, 0, toRow, toCol, fromKoma, 0, false);
    }

    static void add(int fromRow, int fromCol, int toRow, int toCol, int fromKoma, int toKoma, boolean rankUp)
    {
        int hi = getHi(insertPos);
        int lo = getLo(insertPos);
        if (hi >= history.length)
        {
            int[][] tmp = new int[history.length+2][];
            System.arraycopy(history, 0, tmp, 0, history.length);
            history = tmp;
        }
        if (history[hi] == null)
        {
            history[hi] = new int[S];
        }
        history[hi][lo] = (fromRow * 9 + fromCol)
                        | ((toRow * 9 + toCol) << 7)
                        | (fromKoma << 14)
                        | (toKoma << 20)
                        | ((rankUp ? 1 : 0) << 26);
        insertPos++;
        count = insertPos;
    }

    private static int get()
    {
        return history[getHi(insertPos-1)][getLo(insertPos-1)];
    }

    private static int getHi(int x)
    {
        return x / S;
    }

    private static int getLo(int x)
    {
        return x % S;
    }
}