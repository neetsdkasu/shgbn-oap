import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

final class Problem implements Board
{
    private static Problem instance = null;

    static Problem getInstance()
    {
        if (instance == null)
        {
            instance = new Problem();
        }

        return instance;
    }

    private Problem() {}

    public int field(int row, int col)
    {
        return initialField[row][col];
    }

    public int hands(int player, int kind)
    {
        return initialHands[player][kind];
    }

    private static final int MAX_INITIAL_HAND = 18;

    private static int stepLimit = 0;
    private static String title = "";

    static int recordID = 0;
    static long date = 0L, update = 0L;

    static final int[][]
        initialField = new int[9][9],
        initialHands = new int[2][8];

    static final boolean[][]
        invalidPlaced = new boolean[9][9];

    static String getTitle()
    {
        return title;
    }

    static void setTitle (String t)
    {
        title = t;
    }

    static int getStepLimit()
    {
        return stepLimit;
    }

    static void setStepLimit(int v)
    {
        stepLimit = v;
    }

    static void writeTo(DataOutput out) throws IOException
    {
        out.writeInt(recordID);
        out.writeLong(date);
        out.writeLong(update);
        out.writeUTF(title);

        out.writeInt(stepLimit);
        for (int i = 0; i < 2; i++)
        {
            for (int k = 0; k < 8; k++)
            {
                out.writeByte(initialHands[i][k]);
            }
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                out.writeByte(initialField[row][col]);
            }
        }
    }

    static void readFrom(DataInput in) throws IOException
    {
        recordID = in.readInt();
        date = in.readLong();
        update = in.readLong();
        title = in.readUTF();

        stepLimit = in.readInt();
        for (int i = 0; i < 2; i++)
        {
            for (int k = 0; k < 8; k++)
            {
                initialHands[i][k] = in.readUnsignedByte();
            }
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                initialField[row][col] = in.readUnsignedByte();
            }
        }
    }

    private static void generateTitle()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(date));
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DATE);
        int h = c.get(Calendar.HOUR_OF_DAY);
        int n = c.get(Calendar.MINUTE);
        title = Integer.toString(y)
              + "-"
              + Integer.toString(100+m).substring(1)
              + "-"
              + Integer.toString(100+d).substring(1)
              + " "
              + Integer.toString(100+h).substring(1)
              + ":"
              + Integer.toString(100+n).substring(1);
    }

    private static void clear()
    {
        stepLimit = 0;
        recordID = 0;
        date = System.currentTimeMillis();
        update = date;
        generateTitle();

        for (int k = 0; k < 8; k++)
        {
            initialHands[0][k] = 0;
            initialHands[1][k] = 0;
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                initialField[row][col] = 0;
                invalidPlaced[row][col] = false;
            }
        }
    }

    static void setNormalGame()
    {
        clear();

        for (int i = 0; i < 4; i++)
        {
            initialField[0][i] = KYO + i + OPPONENT;
            initialField[0][8-i] = KYO + i + OPPONENT;
            initialField[8][i] = KYO + i;
            initialField[8][8-i] = KYO + i;
        }

        initialField[0][4] = OU + OPPONENT;
        initialField[1][1] = HI + OPPONENT;
        initialField[1][7] = KAKU + OPPONENT;
        initialField[8][4] = GYOKU;
        initialField[7][7] = HI;
        initialField[7][1] = KAKU;

        for (int i = 0; i < 9; i++)
        {
            initialField[2][i] = FU + OPPONENT;
            initialField[6][i] = FU;
        }
    }

    static void setPuzzleTemplate()
    {
        clear();

        initialHands[1][FU-1] = 18;
        initialHands[1][KYO-1] = 4;
        initialHands[1][KEI-1] = 4;
        initialHands[1][GIN-1] = 4;
        initialHands[1][KIN-1] = 4;
        initialHands[1][HI-1] = 2;
        initialHands[1][KAKU-1] = 2;

        initialField[1][7] = OU + OPPONENT;
    }

    static boolean isEmpty(int row, int col)
    {
        return initialField[row][col] == 0;
    }

    static boolean isEmptyInHands(int player, int kind)
    {
        return initialHands[player][kind] == 0;
    }

    static boolean isFullyInHands(int player, int kind)
    {
        return initialHands[player][kind] >= MAX_INITIAL_HAND;
    }

    static boolean isOpponent(int row, int col)
    {
        return initialField[row][col] > OPPONENT;
    }

    static int kind(int row, int col)
    {
        return isOpponent(row, col)
            ? initialField[row][col] - OPPONENT
            : initialField[row][col];
    }

    static int whose(int row, int col)
    {
        return isOpponent(row, col)
            ? 1
            : Math.min(0, initialField[row][col] - 1);
    }

    static boolean canFlip(int row, int col)
    {
        switch (kind(row, col))
        {
        case 0:
        case KIN:
        case GYOKU:
        case OU:
            return false;
        default:
            return true;
        }
    }

    static void flip(int row, int col)
    {
        if (!canFlip(row, col))
        {
            return;
        }
        int k = kind(row, col);
        k = k >= GYOKU ? (k-8) : (k+8);
        initialField[row][col] = isOpponent(row, col) ? k + OPPONENT : k;
        checkInvalid();
    }

    static void changeOwner(int row, int col)
    {
        if (isEmpty(row, col))
        {
            return;
        }
        int k = kind(row, col);
        if (k == GYOKU)
        {
            k = OU;
        }
        else if (k == OU)
        {
            k = GYOKU;
        }
        initialField[row][col] = isOpponent(row, col) ? k : (k+OPPONENT);
        checkInvalid();
    }

    static void incrementInHands(int player, int kind)
    {
        initialHands[player][kind] = Math.min(
            MAX_INITIAL_HAND,
            initialHands[player][kind] + 1
        );
    }

    static void decrementInHands(int player, int kind)
    {
        initialHands[player][kind] = Math.max(
            0,
            initialHands[player][kind] - 1
        );
    }

    static void move(int fromRow, int fromCol, int toRow, int toCol)
    {
        if (fromRow == toRow && fromCol == toCol)
        {
            return;
        }
        if (isEmpty(fromRow, fromCol))
        {
            return;
        }
        if (!isEmpty(toRow, toCol))
        {
            incrementInHands(whose(toRow, toCol), (kind(toRow, toCol)-1)%8);
        }
        initialField[toRow][toCol] = initialField[fromRow][fromCol];
        initialField[fromRow][fromCol] = 0;
        checkInvalid();
    }

    static void moveIntoHands(int fromRow, int fromCol, int toPlayer)
    {
        if (isEmpty(fromRow, fromCol))
        {
            return;
        }
        incrementInHands(toPlayer, (kind(fromRow, fromCol)-1)%8);
        initialField[fromRow][fromCol] = 0;
        checkInvalid();
    }

    static void put(int fromPlayer, int kind, int toRow, int toCol)
    {
        if (isEmptyInHands(fromPlayer, kind))
        {
            return;
        }
        if (!isEmpty(toRow, toCol))
        {
            incrementInHands(whose(toRow, toCol), (kind(toRow, toCol)-1)%8);
        }
        decrementInHands(fromPlayer, kind);
        int koma = kind+1;
        if (koma == GYOKU && fromPlayer == 1)
        {
            koma = OU;
        }
        else if (koma == OU && fromPlayer == 0)
        {
            koma = GYOKU;
        }
        initialField[toRow][toCol] = koma + fromPlayer*OPPONENT;
        checkInvalid();
    }

    static void give(int fromPlayer, int kind, int toPlayer)
    {
        if (isEmptyInHands(fromPlayer, kind))
        {
            return;
        }
        if (isFullyInHands(toPlayer, kind))
        {
            return;
        }
        incrementInHands(toPlayer, kind);
        decrementInHands(fromPlayer, kind);
    }

    static boolean isInvalid(int row, int col)
    {
        return invalidPlaced[row][col];
    }

    static void checkInvalid()
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                switch (kind(row, col))
                {
                case FU:
                    if (isOpponent(row, col))
                    {
                        invalidPlaced[row][col] = row == 8;
                    }
                    else
                    {
                        invalidPlaced[row][col] = row == 0;
                    }
                    for (int tr = 0; tr < 9; tr++)
                    {
                        if (tr != row && initialField[row][col] == initialField[tr][col])
                        {
                            invalidPlaced[row][col] = true;
                            break;
                        }
                    }
                    break;
                case KYO:
                    if (isOpponent(row, col))
                    {
                        invalidPlaced[row][col] = row == 8;
                    }
                    else
                    {
                        invalidPlaced[row][col] = row == 0;
                    }
                    break;
                case KEI:
                    if (isOpponent(row, col))
                    {
                        invalidPlaced[row][col] = row >= 7;
                    }
                    else
                    {
                        invalidPlaced[row][col] = row <= 1;
                    }
                    break;
                case GYOKU:
                case OU:
                    invalidPlaced[row][col] = nearGYOKU(row, col);
                    break;
                default:
                    invalidPlaced[row][col] = false;
                    break;
                }
            }
        }
    }

    private static boolean nearGYOKU(int row, int col)
    {
        int player = whose(row, col);
        for (int dr = -1; dr < 2; dr++)
        {
            for (int dc = -1; dc < 2; dc++)
            {
                if (!Game.inField(row+dr,col+dc))
                {
                    continue;
                }
                if (dr == 0 && dc == 0)
                {
                    continue;
                }
                switch (kind(row+dr,col+dc))
                {
                case GYOKU:
                case OU:
                    if (whose(row+dr,col+dc) != player)
                    {
                        return true;
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return false;
    }
}