import java.io.*;
import java.util.*;

class Game extends Board
{

    static Game newNormalGame(boolean opponentFirst)
    {
        Game game = new Game(Problem.getNormalGame());

        if (opponentFirst)
        {
            game.firstPlayer = 1;
        }

        return game;
    }

    int currentStep;
    int firstPlayer, currentPlayer;

    final Problem problem;
    final int[][] currentField, currentHands;
    final boolean[][] movable;
    final int[][][] range;
    final int[][] guardian;
    final boolean[][][] dangerZone;
    final boolean[] danger;

    Game(Problem p)
    {
        currentField = new int[9][9];
        currentHands = new int[2][8];
        movable = new boolean[9][9];
        range = new int[2][9][9];
        guardian = new int[9][9];
        dangerZone = new boolean[2][9][9];
        danger = new boolean[2];
        problem = p;
    }

    void init()
    {
        currentStep = 0;
        currentPlayer = firstPlayer;
        for (int i = 0; i < 9; i++)
        {
            System.arraycopy(
                problem.initialField[i],
                0,
                currentField[i],
                0,
                currentField[i].length
            );
        }
        for (int i = 0; i < 2; i++)
        {
            System.arraycopy(
                problem.initialHands[i],
                0,
                currentHands[i],
                0,
                currentHands[i].length
            );
        }
        clearOute();
        calcRange();
        checkOute();
    }

    boolean isDanger()
    {
        return danger[currentPlayer];
    }

    private void clearOute()
    {
        danger[0] = false;
        danger[1] = false;
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

    private void checkOute()
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
                    if (getRange(1^player, row, col) > 0)
                    {
                        danger[player] = true;
                        checkOuteAround(player, row, col);
                    }
                    checkOutePathHI(row, col);
                    checkOutePathKAKU(row, col);
                    break;
                default:
                    break;
                }
            }
        }
    }

    private void checkOuteAround(int player, int row, int col)
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

    private void checkOutePathHI(int row, int col)
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
                    else if (k == KYO && dr == 1 - 2*player)
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

    private void checkOutePathKAKU(int row, int col)
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

    private void calcRange()
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

        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                if (select(row, col, false))
                {
                    addRange(isOpponent(row, col) ? 1 : 0);
                }
            }
        }
    }

    private void addRange(int player)
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                if (movable[row][col])
                {
                    range[player][row][col]++;
                }
            }
        }
    }

    public String getTitle()
    {
        return problem.getTitle();
    }

    public int getStepLimit()
    {
        return problem.getStepLimit();
    }

    public int hands(int player, int kind)
    {
        return currentHands[player][kind];
    }

    public int field(int row, int col)
    {
        return currentField[row][col];
    }

    void clearMovable()
    {
        for (int i = 0; i < 9; i++)
        {
            for (int k = 0; k < 9; k++)
            {
                movable[i][k] = false;
            }
        }
    }

    boolean needRankUp(int row, int col, int toRow)
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

    boolean canRankUp(int row, int col, int toRow)
    {
        if (isOpponent(row, col))
        {
            return isRankUpRow(1, toRow);
        }
        else
        {
            return isMine(row, col) ? isRankUpRow(0, toRow) : false;
        }
    }

    boolean isRankUpRow(int row)
    {
        return isRankUpRow(currentPlayer, row);
    }

    boolean isRankUpRow(int player, int row)
    {
        return player == 0 ? (row <= 2) : (row >= 6);
    }

    boolean put(int kind, int row, int col)
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

        switchNextPlayer();

        return true;
    }

    boolean selectHand(int player, int kind)
    {
        if (hands(player, kind) == 0)
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

    private void omitUchiFU(int player)
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

    private void omit2FU(int player)
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

    boolean move(int fromRow, int fromCol, int toRow, int toCol)
    {
        return move(fromRow, fromCol, toRow, toCol, false);
    }

    boolean move(int fromRow, int fromCol, int toRow, int toCol, boolean rankUp)
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

    private void switchNextPlayer()
    {
        currentStep++;
        currentPlayer ^= 1;
        clearOute();
        calcRange();
        checkOute();
    }

    boolean select(int row, int col)
    {
        return select(row, col, true);
    }

    boolean select(int row, int col, boolean useRange)
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
            fillMovableGYOKU(row, col, useRange);
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
            fillMovableGYOKU(row, col, useRange);
            return true;
        }

        int player = whose(row, col);
        if (danger[player])
        {
            filterDanger(player);
        }

        return true;
    }

    private void filterDanger(int player)
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

    int getRange(int player, int row, int col)
    {
        return inField(row, col)
            ? range[player][row][col]
            : 0;
    }

    boolean isCurrentPlayer(int row, int col)
    {
        return has(currentPlayer, row, col);
    }

    boolean canMoveTo(int row, int col)
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

    boolean canGoTo(int row, int col, int dr, int dc)
    {
        return inField(row+dr, col+dc)
            && (guardian[row][col] & (1 << (4+3*dr+dc))) == 0;
    }

    private boolean setMovable(int row, int col, boolean opponent)
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

    private void fillMovableRYU(int row, int col)
    {
        fillMovableGIN(row, col);
        fillMovableHI(row, col);
    }

    private void fillMovableUMA(int row, int col)
    {
        fillMovableKIN(row, col);
        fillMovableKAKU(row, col);
    }

    private void fillMovableGYOKU(int row, int col, boolean useRange)
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
                if (useRange && getRange(opponent ? 0 : 1, tmpRow, col+dc) > 0)
                {
                    continue;
                }
                setMovable(tmpRow, col+dc, opponent);
            }
        }
    }

    private void fillMovableHI(int row, int col)
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

    private void fillMovableKAKU(int row, int col)
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

    private void fillMovableKIN(int row, int col)
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

    private void fillMovableGIN(int row, int col)
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

    private void fillMovableKEI(int row, int col)
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

    private void fillMovableKYO(int row, int col)
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

    private void fillMovableFU(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        int dr = opponent ? 1 : -1;
        if (canGoTo(row, col, dr, 0))
        {
            setMovable(row+dr, col, opponent);
        }
    }
}