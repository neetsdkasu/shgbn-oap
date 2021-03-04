import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class Game implements Board
{
    private static Game instance = null;

    static Game getInstance()
    {
        if (instance == null)
        {
            instance = new Game();
        }

        return instance;
    }

    private Game() {}

    public int hands(int player, int kind)
    {
        return currentHands[player][kind];
    }

    public int field(int row, int col)
    {
        return currentField[row][col];
    }

    static void writeTo(DataOutput out) throws IOException
    {
        out.writeInt(recordID);
        out.writeLong(date);
        out.writeLong(update);
        out.writeUTF(title);

        Problem.writeTo(out);

        out.writeInt(firstPlayer);
        out.writeInt(currentPlayer);
        out.writeInt(currentStep);
        for (int i = 0; i < 2; i++)
        {
            for (int k = 0; k < 8; k++)
            {
                out.writeByte(currentHands[i][k]);
            }
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                out.writeByte(currentField[row][col]);
            }
        }

        History.writeTo(out);
    }

    static void readFrom(DataInput in) throws IOException
    {
        recordID = in.readInt();
        date = in.readLong();
        update = in.readLong();
        title = in.readUTF();

        Problem.readFrom(in);

        firstPlayer = in.readInt();
        currentPlayer = in.readInt();
        currentStep = in.readInt();
        for (int i = 0; i < 2; i++)
        {
            for (int k = 0; k < 8; k++)
            {
                currentHands[i][k] = in.readUnsignedByte();
            }
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                currentField[row][col] = in.readUnsignedByte();
            }
        }

        History.readFrom(in);
    }

    static void copyToProblem()
    {
        Problem.date = System.currentTimeMillis();
        Problem.update = date;
        Problem.recordID = 0;
        if (!getTitle().equals(Problem.getTitle()))
        {
            Problem.setTitle(title);
        }
        System.arraycopy(currentHands[0], 0, Problem.initialHands[0], 0, 8);
        System.arraycopy(currentHands[1], 0, Problem.initialHands[1], 0, 8);
        for (int i = 0; i < 9; i++)
        {
            System.arraycopy(currentField[i], 0, Problem.initialField[i], 0, 9);
        }
        Problem.checkInvalid();
    }

    private static int
        currentStep = 0,
        firstPlayer = 0,
        currentPlayer = 0;

    private static final int[][][]
        range = new int[2][9][9];

    private static final int[][]
        currentField = new int[9][9],
        currentHands = new int[2][8],
        guardian = new int[9][9];

    private static final boolean[][][]
        dangerZone = new boolean[2][9][9];

    private static final boolean[][]
        movable = new boolean[9][9];

    private static final boolean[]
        danger = new boolean[2],
        checkmate = new boolean[2];

    private static String title = "";
    static long date = 0L, update = 0L;
    static int recordID = 0;

    static int getFirstPlayer()
    {
        return firstPlayer;
    }

    static void setFirstPlayer(int p)
    {
        firstPlayer = p;
    }

    static int getCurrentPlayer()
    {
        return currentPlayer;
    }

    static int getCurrentStep()
    {
        return currentStep;
    }

    static String getTitle()
    {
        if (title == null || title.length() == 0)
        {
            return Problem.getTitle();
        }
        return title;
    }

    static void setTitle(String t)
    {
        title = t;
    }

    static boolean isDanger()
    {
        return danger[currentPlayer];
    }

    static boolean isCheckmate()
    {
        return checkmate[currentPlayer];
    }

    static boolean isEmpty(int row, int col)
    {
        return currentField[row][col] == 0;
    }

    static boolean isOpponent(int row, int col)
    {
        return currentField[row][col] > OPPONENT;
    }

    static boolean isMine(int row, int col)
    {
        return 0 < currentField[row][col] && !isOpponent(row, col);
    }

    static boolean inField(int row, int col)
    {
        return 0 <= row && row < 9 && 0 <= col && col < 9;
    }

    static boolean has(int player, int row, int col)
    {
        return player == whose(row, col);
    }

    static int whose(int row, int col)
    {
        return isOpponent(row, col)
            ? 1
            : Math.min(0, currentField[row][col] - 1);
    }

    static int kind(int row, int col)
    {
        return isOpponent(row, col)
            ? currentField[row][col] - OPPONENT
            : currentField[row][col];
    }

    static int getRange(int player, int row, int col)
    {
        return inField(row, col) ? range[player][row][col] : 0;
    }

    static boolean isCurrentPlayer(int row, int col)
    {
        return has(currentPlayer, row, col);
    }

    static boolean isRankUpRow(int row)
    {
        return isRankUpRow(currentPlayer, row);
    }

    static boolean isRankUpRow(int player, int row)
    {
        return player == 0 ? (row <= 2) : (row >= 6);
    }

    static boolean needRankUp(int row, int col, int toRow)
    {
        switch (kind(row, col))
        {
        case FU:
        case KYO:
            return isOpponent(row, col) ? (toRow == 8) : (toRow == 0);
        case KEI:
            return isOpponent(row, col) ? (toRow >= 7) : (toRow <= 1);
        default:
            return false;
        }
    }

    static boolean canRankUp(int row, int col, int toRow)
    {
        int k = kind(row, col);
        if (k == KIN || k >= GYOKU)
        {
            return false;
        }
        if (isOpponent(row, col))
        {
            return isRankUpRow(1, row) || isRankUpRow(1, toRow);
        }
        else
        {
            return isMine(row, col)
                && (isRankUpRow(0, row) || isRankUpRow(0, toRow));
        }
    }

    static boolean canMoveTo(int row, int col)
    {
        if (!inField(row, col))
        {
            return false;
        }
        int k = kind(row, col);
        if (k == GYOKU || k == OU)
        {
            if (!isCurrentPlayer(row, col))
            {
                return false;
            }
        }
        return movable[row][col];
    }

    static void initPlay()
    {
        currentStep = 0;
        currentPlayer = firstPlayer;
        System.arraycopy(Problem.initialHands[0], 0, currentHands[0], 0, 8);
        System.arraycopy(Problem.initialHands[1], 0, currentHands[1], 0, 8);
        for (int i = 0; i < 9; i++)
        {
            System.arraycopy(Problem.initialField[i], 0, currentField[i], 0, 9);
        }
        ready();
        History.clear();
    }

    static void clear()
    {
        firstPlayer = 0;
        title = "";
        recordID = 0;
        date = System.currentTimeMillis();
        update = date;
    }

    static void ready()
    {
        clearRange();
        calcRange();
        clearOute();
        checkOute();
        clearMovable();
    }

    static boolean put(int kind, int row, int col)
    {
        if (!selectHand(currentPlayer, kind))
        {
            return false;
        }

        if (!canMoveTo(row, col))
        {
            return false;
        }

        currentHands[currentPlayer][kind]--;
        currentField[row][col] = (kind + 1) + OPPONENT*currentPlayer;

        History.addFromHand(row, col, currentField[row][col]);

        switchNextPlayer();

        return true;
    }

    static boolean selectHand(int player, int kind)
    {
        if (currentHands[player][kind] == 0)
        {
            return false;
        }

        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                movable[row][col] = isEmpty(row, col);
            }
        }

        switch (kind+1)
        {
        case FU:
            omit2FU(player);
            omitUchiFU(player);
        case KYO:
            for (int col = 0; col < 9; col++)
            {
                movable[8*player][col] = false;
            }
            break;
        case KEI:
            for (int col = 0; col < 9; col++)
            {
                movable[8*player][col] = false;
                movable[6*player+1][col] = false;
            }
            break;
        default:
            break;
        }

        if (danger[player])
        {
            filterDanger(player);
        }

        return true;
    }

    static boolean move(int fromRow, int fromCol, int toRow, int toCol)
    {
        return move(fromRow, fromCol, toRow, toCol, false);
    }

    static boolean move(int fromRow, int fromCol, int toRow, int toCol, boolean rankUp)
    {
        if (currentPlayer == 0)
        {
            if (!isMine(fromRow, fromCol))
            {
                return false;
            }
        }
        else if (!isOpponent(fromRow, fromCol))
        {
            return false;
        }

        if (!select(fromRow, fromCol))
        {
            return false;
        }

        if (!canMoveTo(toRow, toCol))
        {
            return false;
        }

        if (rankUp)
        {
            int k = kind(fromRow, fromCol);
            if (k == KIN || k >= GYOKU)
            {
                rankUp = false;
            }
            else
            {
                rankUp = isRankUpRow(fromRow) || isRankUpRow(toRow);
            }
        }
        else
        {
            rankUp = needRankUp(fromRow, fromCol, toRow);
        }

        History.add(
            fromRow, fromCol,
            toRow, toCol,
            currentField[fromRow][fromCol],
            currentField[toRow][toCol],
            rankUp
        );

        if (!isEmpty(toRow, toCol))
        {
            currentHands[currentPlayer][(kind(toRow, toCol)-1)%8]++;
        }
        currentField[toRow][toCol] = currentField[fromRow][fromCol]
                                   + (rankUp ? 8 : 0);
        currentField[fromRow][fromCol] = 0;

        switchNextPlayer();

        return true;
    }

    static boolean select(int row, int col)
    {
        if (isEmpty(row, col))
        {
            return false;
        }

        clearMovable();

        switch (kind(row, col))
        {
        case FU:
            fillMovableFU(row, col);
            break;
        case KYO:
            fillMovableKYO(row, col);
            break;
        case KEI:
            fillMovableKEI(row, col);
            break;
        case GIN:
            fillMovableGIN(row, col);
            break;
        case KIN:
            fillMovableKIN(row, col);
            break;
        case KAKU:
            fillMovableKAKU(row, col);
            break;
        case HI:
            fillMovableHI(row, col);
            break;
        case GYOKU:
            fillMovableGYOKU(row, col);
            return true;
        case TO:
        case NKYO:
        case NKEI:
        case NGIN:
            fillMovableKIN(row, col);
            break;
        case UMA:
            fillMovableUMA(row, col);
            break;
        case RYU:
            fillMovableRYU(row, col);
            break;
        case OU:
            fillMovableGYOKU(row, col);
            return true;
        }

        int player = whose(row, col);
        if (danger[player])
        {
            filterDanger(player);
        }

        return true;
    }

    static void goHistoryPrev()
    {
        currentStep--;
        currentPlayer ^= 1;

        int toRow = History.getToRow();
        int toCol = History.getToCol();

        if (History.isFromHand())
        {
            currentHands[currentPlayer][(kind(toRow, toCol)-1)%8]++;
            currentField[toRow][toCol] = 0;
        }
        else
        {
            int fromRow = History.getFromRow();
            int fromCol = History.getFromCol();
            int fromKoma = History.getFromKoma();
            int toKoma = History.getToKoma();
            currentField[fromRow][fromCol] = fromKoma;
            currentField[toRow][toCol] = toKoma;
            if (toKoma != 0)
            {
                currentHands[currentPlayer][(kind(toRow, toCol)-1)%8]--;
            }
        }

        History.movePrev();
        ready();
    }

    static void goHistoryNext()
    {
        History.moveNext();

        int toRow = History.getToRow();
        int toCol = History.getToCol();
        int fromKoma = History.getFromKoma();

        if (History.isFromHand())
        {
            currentField[toRow][toCol] = fromKoma;
            currentHands[currentPlayer][(kind(toRow, toCol)-1)%8]--;
        }
        else
        {
            int fromRow = History.getFromRow();
            int fromCol = History.getFromCol();
            int toKoma = History.getToKoma();
            boolean rankUp = History.getRankUp();
            if (toKoma != 0)
            {
                currentHands[currentPlayer][(kind(toRow, toCol)-1)%8]++;
            }
            currentField[fromRow][fromCol] = 0;
            currentField[toRow][toCol] = fromKoma + (rankUp ? 8 : 0);
        }

        currentStep++;
        currentPlayer ^= 1;
        ready();
    }

    private static boolean canGoTo(int row, int col, int dr, int dc)
    {
        return inField(row+dr, col+dc)
            && (guardian[row][col] & (1 << (4+3*dr+dc))) == 0;
    }

    private static void switchNextPlayer()
    {
        currentStep++;
        currentPlayer ^= 1;
        ready();
    }

    private static boolean addRange(int player, int row, int col)
    {
        if (!inField(row, col))
        {
            return true;
        }
        range[player][row][col]++;
        return !isEmpty(row, col);
    }

    private static void clearRange()
    {
        for (int i = 0; i < 2; i++)
        {
            for (int row = 0; row < 9; row++)
            {
                for (int col = 0; col < 9; col++)
                {
                    range[i][row][col] = 0;
                }
            }
        }

    }

    private static void clearMovable()
    {
        for (int i = 0; i < 9; i++)
        {
            for (int k = 0; k < 9; k++)
            {
                movable[i][k] = false;
            }
        }
    }

    private static boolean hasAnyMovable()
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                if (movable[row][col])
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static void clearOute()
    {
        for (int i = 0; i < 2; i++)
        {
            danger[i] = false;
            checkmate[i] = false;
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                guardian[row][col] = 0;
                dangerZone[0][row][col] = false;
                dangerZone[1][row][col] = false;
            }
        }
    }

    private static void filterDanger(int player)
    {
        boolean[][] zone = dangerZone[player];
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                movable[row][col] &= zone[row][col];
            }
        }
    }

    private static boolean setMovable(int row, int col, boolean opponent)
    {
        if (!inField(row, col))
        {
            return true;
        }

        movable[row][col] = opponent
            ? !isOpponent(row, col)
            : !isMine(row, col);

        return !isEmpty(row, col);
    }

    private static void checkOute()
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                switch (kind(row, col))
                {
                case GYOKU:
                case OU:
                    int player = whose(row, col);
                    checkOutePathHI(row, col);
                    checkOutePathKAKU(row, col);
                    if (getRange(1^player, row, col) > 0)
                    {
                        danger[player] = true;
                        checkOuteAround(player, row, col);
                    }
                    break;
                default:
                    break;
                }
            }
        }
        for (int i = 0; i < 2; i++)
        {
            if (danger[i])
            {
                checkmate[i] = calcCheckmate(i);
                if (checkmate[i])
                {
                    for (int row = 0; row < 9; row++)
                    {
                        for (int col = 0; col < 9; col++)
                        {
                            dangerZone[i][row][col] = false;
                        }
                    }
                }
            }
        }
    }

    private static boolean calcCheckmate(int player)
    {
        boolean safe = false;
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                if (!has(player, row, col))
                {
                    continue;
                }
                select(row, col);
                switch (kind(row, col))
                {
                case GYOKU:
                case OU:
                    if (hasAnyMovable())
                    {
                        return false;
                    }
                    if (range[1^player][row][col] >= 2)
                    {
                        return true;
                    }
                    break;
                default:
                    if (hasAnyMovable())
                    {
                        safe = true;
                    }
                    break;
                }
            }
        }
        if (safe)
        {
            return false;
        }
        for (int k = 0; k < 8; k++)
        {
            if (selectHand(player, k))
            {
                if (hasAnyMovable())
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static void checkOuteAround(int player, int row, int col)
    {
        for (int dr = -2; dr <= 2; dr++)
        {
            int tmpRow = row + dr;
            for (int dc = -1; dc < 2; dc++)
            {
                int tmpCol = col + dc;
                if (!inField(tmpRow, tmpCol))
                {
                    continue;
                }
                if (!has(1^player, tmpRow, tmpCol))
                {
                    continue;
                }
                if (!select(tmpRow, tmpCol))
                {
                    continue;
                }
                dangerZone[player][tmpRow][tmpCol] |= movable[row][col];
            }
        }
    }

    private static void checkOutePathHI(int row, int col)
    {
        int player = whose(row, col);
        for (int d = 0; d < 4; d++)
        {
            int dd = d >> 1;
            int dm = 2 * (d & 1) - 1;
            int dr = (1 - dd) * dm;
            int dc = dd * dm;
            int defRow = -1, defCol = -1;
            int defCount = 0;
            boolean foundHI = false;
            int tmpRow = row + dr;
            int tmpCol = col + dc;
            int len = 1;
            while (inField(tmpRow, tmpCol) && defCount < 2)
            {
                if (has(1^player, tmpRow, tmpCol))
                {
                    int k = kind(tmpRow, tmpCol);
                    if (k == HI || k == RYU)
                    {
                        if (len > 1)
                        {
                            foundHI = true;
                            break;
                        }
                        guardian[row][col] |= (1 << (4-3*dr-dc));
                    }
                    else if (k == KYO && dr == 2*player - 1)
                    {
                        if (len > 1)
                        {
                            foundHI = true;
                            break;
                        }
                        guardian[row][col] |= (1 << (4-3*dr-dc));
                    }
                    defCount++;
                }
                else if (has(player, tmpRow, tmpCol))
                {
                    defRow = tmpRow;
                    defCol = tmpCol;
                    defCount++;
                }
                tmpRow += dr;
                tmpCol += dc;
                len++;
            }
            if (!foundHI || defCount >= 2)
            {
                continue;
            }
            if (defRow >= 0)
            {
                guardian[defRow][defCol] |= (dr != 0)
                    ? (0x1FF ^ (1 << 1) ^ (1 << 7))
                    : (0x1FF ^ (1 << 3) ^ (1 << 5));
            }
            else if (defCount == 0)
            {
                while (len > 0)
                {
                    dangerZone[player][tmpRow][tmpCol] = true;
                    tmpRow -= dr;
                    tmpCol -= dc;
                    len--;
                }
                guardian[row][col] |= (dr != 0)
                    ? ((1 << 1) | (1 << 7))
                    : ((1 << 3) | (1 << 5));
            }
        }
    }

    private static void checkOutePathKAKU(int row, int col)
    {
        int player = whose(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
            for (int dc = -1; dc < 2; dc += 2)
            {
                int defRow = -1, defCol = -1;
                int defCount = 0;
                boolean foundKAKU = false;
                int tmpRow = row + dr;
                int tmpCol = col + dc;
                int len = 1;
                while (inField(tmpRow, tmpCol) && defCount < 2)
                {
                    if (has(1^player, tmpRow, tmpCol))
                    {
                        int k = kind(tmpRow, tmpCol);
                        if (k == KAKU || k == UMA)
                        {
                            if (len > 1)
                            {
                                foundKAKU = true;
                                break;
                            }
                            guardian[row][col] |= (1 << (4-3*dr-dc));
                        }
                        defCount++;
                    }
                    else if (has(player, tmpRow, tmpCol))
                    {
                        defRow = tmpRow;
                        defCol = tmpCol;
                        defCount++;
                    }
                    tmpRow += dr;
                    tmpCol += dc;
                    len++;
                }
                if (!foundKAKU || defCount >= 2)
                {
                    continue;
                }
                if (defRow >= 0)
                {
                    guardian[defRow][defCol] |= (dr == dc)
                        ? (0x1FF ^ (1 << 0) ^ (1 << 8))
                        : (0x1FF ^ (1 << 2) ^ (1 << 6));
                }
                else if (defCount == 0)
                {
                    while (len > 0)
                    {
                        dangerZone[player][tmpRow][tmpCol] = true;
                        tmpRow -= dr;
                        tmpCol -= dc;
                        len--;
                    }
                    guardian[row][col] |= (dr == dc)
                        ? ((1 << 0) | (1 << 8))
                        : ((1 << 2) | (1 << 6));
                }
            }
        }
    }

    private static void calcRange()
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                switch (kind(row, col))
                {
                case FU:
                    fillRangeFU(row, col);
                    break;
                case KYO:
                    fillRangeKYO(row, col);
                    break;
                case KEI:
                    fillRangeKEI(row, col);
                    break;
                case GIN:
                    fillRangeGIN(row, col);
                    break;
                case KIN:
                    fillRangeKIN(row, col);
                    break;
                case KAKU:
                    fillRangeKAKU(row, col);
                    break;
                case HI:
                    fillRangeHI(row, col);
                    break;
                case GYOKU:
                    fillRangeGYOKU(row, col);
                    break;
                case TO:
                case NKYO:
                case NKEI:
                case NGIN:
                    fillRangeKIN(row, col);
                    break;
                case UMA:
                    fillRangeUMA(row, col);
                    break;
                case RYU:
                    fillRangeRYU(row, col);
                    break;
                case OU:
                    fillRangeGYOKU(row, col);
                    break;
                default:
                    break;
                }
            }
        }
    }

    private static void omitUchiFU(int player)
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                switch (kind(row, col))
                {
                case GYOKU:
                case OU:
                    if (has(1^player, row, col))
                    {
                        int tmpRow = row + 1 - 2*player;
                        if (!inField(tmpRow, col) || !isEmpty(tmpRow, col))
                        {
                            break;
                        }
                        int count = 0;
                        for (int dr = -1; dr < 2; dr++)
                        {
                            for (int dc = -1; dc < 2; dc++)
                            {
                                if (!inField(row+dr, col+dc))
                                {
                                    count++;
                                }
                                else if (!isEmpty(row+dr, col+dc))
                                {
                                    count++;
                                }
                                else if (getRange(player, row+dr, col+dc) > 0)
                                {
                                    count++;
                                }
                            }
                        }
                        if (count < 9)
                        {
                            break;
                        }
                        if (getRange(1^player, tmpRow, col) != 1)
                        {
                            break;
                        }
                        if (getRange(player, tmpRow, col) > 0)
                        {
                            movable[tmpRow][col] = false;
                        }
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    private static void omit2FU(int player)
    {
        for (int col = 0; col < 9; col++)
        {
            for (int row = 0; row < 9; row++)
            {
                if (kind(row, col) != FU)
                {
                    continue;
                }
                if (!has(player, row, col))
                {
                    continue;
                }
                // dangerous using variable !
                for (row = 0; row < 9; row++)
                {
                    movable[row][col] = false;
                }
                break;
            }
        }
    }

    private static void fillMovableRYU(int row, int col)
    {
        fillMovableGIN(row, col);
        fillMovableHI(row, col);
    }

    private static void fillMovableUMA(int row, int col)
    {
        fillMovableKIN(row, col);
        fillMovableKAKU(row, col);
    }

    private static void fillMovableGYOKU(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        for (int dr = -1; dr < 2; dr++)
        {
            int tmpRow = row + dr;
            for (int dc = -1; dc < 2; dc++)
            {
                if (dc == 0 && dr == 0)
                {
                    continue;
                }
                if (!canGoTo(row, col, dr, dc))
                {
                    continue;
                }
                if (getRange(opponent ? 0 : 1, tmpRow, col+dc) > 0)
                {
                    continue;
                }
                setMovable(tmpRow, col+dc, opponent);
            }
        }
    }

    private static void fillMovableHI(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
            if (!canGoTo(row, col, dr, 0))
            {
                continue;
            }
            for (int tmpRow = row+dr; true; tmpRow += dr)
            {
                if (setMovable(tmpRow, col, opponent))
                {
                    break;
                }
            }
        }
        for (int dc = -1; dc < 2; dc += 2)
        {
            if (!canGoTo(row, col, 0, dc))
            {
                continue;
            }
            for (int tmpCol = col+dc; true; tmpCol += dc)
            {
                if (setMovable(row, tmpCol, opponent))
                {
                    break;
                }
            }
        }
    }

    private static void fillMovableKAKU(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
            for (int dc = -1; dc < 2; dc += 2)
            {
                if (!canGoTo(row, col, dr, dc))
                {
                    continue;
                }
                int tmpRow = row + dr;
                int tmpCol = col + dc;
                for (;;)
                {
                    if (setMovable(tmpRow, tmpCol, opponent))
                    {
                        break;
                    }
                    tmpRow += dr;
                    tmpCol += dc;
                }
            }
        }
    }

    private static void fillMovableKIN(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        int omitDr = opponent ? -1 : 1;
        for (int dr = -1; dr < 2; dr++)
        {
            int tmpRow = row + dr;
            for (int dc = -1; dc < 2; dc++)
            {
                if (dc == 0 && dr == 0)
                {
                    continue;
                }
                if (dc != 0 && dr == omitDr)
                {
                    continue;
                }
                if (!canGoTo(row, col, dr, dc))
                {
                    continue;
                }
                setMovable(tmpRow, col+dc, opponent);
            }
        }
    }

    private static void fillMovableGIN(int row, int col)
    {
        fillMovableFU(row, col);
        boolean opponent = isOpponent(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
            for (int dc = -1; dc < 2; dc += 2)
            {
                if (canGoTo(row, col, dr, dc))
                {
                    setMovable(row+dr, col+dc, opponent);
                }
            }
        }
    }

    private static void fillMovableKEI(int row, int col)
    {
        if (guardian[row][col] != 0)
        {
            return;
        }
        boolean opponent = isOpponent(row, col);
        int tmpRow = opponent ? (row+2) : (row-2);
        setMovable(tmpRow, col-1, opponent);
        setMovable(tmpRow, col+1, opponent);
    }

    private static void fillMovableKYO(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        int dr = opponent ? 1 : -1;
        if (!canGoTo(row, col, dr, 0))
        {
            return;
        }
        int tmpRow = row + dr;
        while (!setMovable(tmpRow, col, opponent))
        {
            tmpRow += dr;
        }
    }

    private static void fillMovableFU(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        int dr = opponent ? 1 : -1;
        if (canGoTo(row, col, dr, 0))
        {
            setMovable(row+dr, col, opponent);
        }
    }

    private static void fillRangeRYU(int row, int col)
    {
        fillRangeGIN(row, col);
        fillRangeHI(row, col);
    }

    private static void fillRangeUMA(int row, int col)
    {
        fillRangeKIN(row, col);
        fillRangeKAKU(row, col);
    }

    private static void fillRangeGYOKU(int row, int col)
    {
        int player = whose(row, col);
        for (int dr = -1; dr < 2; dr++)
        {
            int tmpRow = row + dr;
            for (int dc = -1; dc < 2; dc++)
            {
                if (dc == 0 && dr == 0)
                {
                    continue;
                }
                addRange(player, tmpRow, col+dc);
            }
        }
    }

    private static void fillRangeHI(int row, int col)
    {
        int player = whose(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
            for (int tmpRow = row+dr; true; tmpRow += dr)
            {
                if (addRange(player, tmpRow, col))
                {
                    break;
                }
            }
        }
        for (int dc = -1; dc < 2; dc += 2)
        {
            for (int tmpCol = col+dc; true; tmpCol += dc)
            {
                if (addRange(player, row, tmpCol))
                {
                    break;
                }
            }
        }
    }

    private static void fillRangeKAKU(int row, int col)
    {
        int player = whose(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
            for (int dc = -1; dc < 2; dc += 2)
            {
                int tmpRow = row + dr;
                int tmpCol = col + dc;
                for (;;)
                {
                    if (addRange(player, tmpRow, tmpCol))
                    {
                        break;
                    }
                    tmpRow += dr;
                    tmpCol += dc;
                }
            }
        }
    }

    private static void fillRangeKIN(int row, int col)
    {
        int player = whose(row, col);
        int omitDr = isOpponent(row, col) ? -1 : 1;
        for (int dr = -1; dr < 2; dr++)
        {
            int tmpRow = row + dr;
            for (int dc = -1; dc < 2; dc++)
            {
                if (dc == 0 && dr == 0)
                {
                    continue;
                }
                if (dc != 0 && dr == omitDr)
                {
                    continue;
                }
                addRange(player, tmpRow, col+dc);
            }
        }
    }

    private static void fillRangeGIN(int row, int col)
    {
        fillRangeFU(row, col);
        int player = whose(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
            for (int dc = -1; dc < 2; dc += 2)
            {
               addRange(player, row+dr, col+dc);
            }
        }
    }

    private static void fillRangeKEI(int row, int col)
    {
        int player = whose(row, col);
        int tmpRow = isOpponent(row, col) ? (row+2) : (row-2);
        addRange(player, tmpRow, col-1);
        addRange(player, tmpRow, col+1);
    }

    private static void fillRangeKYO(int row, int col)
    {
        int player = whose(row, col);
        int dr = isOpponent(row, col) ? 1 : -1;
        int tmpRow = row + dr;
        while (!addRange(player, tmpRow, col))
        {
            tmpRow += dr;
        }
    }

    private static void fillRangeFU(int row, int col)
    {
        int dr = isOpponent(row, col) ? 1 : -1;
        addRange(whose(row, col), row + dr, col);
    }
}