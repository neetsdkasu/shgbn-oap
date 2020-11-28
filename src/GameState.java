import java.io.*;
import java.util.*;

class GameState
{
    static final int
        OPPONENT = 16,
        FU = 1,
        KYO = 2,
        KEI = 3,
        GIN = 4,
        KIN = 5,
        KAKU = 6,
        HI = 7,
        GYOKU = 8,
        TO = 9,
        NKYO = 10,
        NKEI = 11,
        NGIN = 12,
        UMA = 14,
        RYU = 15,
        OU = 16;

    static GameState newNormalGame(boolean opponentFirst)
    {
        GameState gs = new GameState();

        if (opponentFirst)
        {
            gs.firstPlayer = 1;
        }

        for (int i = 0; i < 4; i++)
        {
            gs.initialField[0][i] = KYO + i + OPPONENT;
            gs.initialField[0][8-i] = KYO + i + OPPONENT;
            gs.initialField[8][i] = KYO + i;
            gs.initialField[8][8-i] = KYO + i;
        }

        gs.initialField[0][4] = OU + OPPONENT;
        gs.initialField[1][1] = HI + OPPONENT;
        gs.initialField[1][7] = KAKU + OPPONENT;
        gs.initialField[8][4] = GYOKU;
        gs.initialField[7][7] = HI;
        gs.initialField[7][1] = KAKU;

        for (int i = 0; i < 9; i++)
        {
            gs.initialField[2][i] = FU + OPPONENT;
            gs.initialField[6][i] = FU;
        }

        return gs;
    }

    int stepLimit, currentStep;
    String title;

    int firstPlayer, currentPlayer;

    int[][] initialField, currentField, initialHands, currentHands;
    boolean[][] movable;

    GameState()
    {
        initialField = new int[9][9];
        currentField = new int[9][9];
        initialHands = new int[2][8];
        currentHands = new int[2][8];
        movable = new boolean[9][9];
    }

    void init()
    {
        currentStep = 0;
        currentPlayer = firstPlayer;
        for (int i = 0; i < initialField.length; i++)
        {
            System.arraycopy(
                initialField[i],
                0,
                currentField[i],
                0,
                currentField[i].length
            );
        }
        for (int i = 0; i < initialHands.length; i++)
        {
            System.arraycopy(
                initialHands[i],
                0,
                currentHands[i],
                0,
                currentHands[i].length
            );
        }
    }

    int hands(int player, int koma)
    {
        return currentHands[player][koma];
    }

    int field(int row, int col)
    {
        return currentField[row][col];
    }

    boolean select(int row, int col)
    {
        if (field(row, col) == 0)
        {
            return false;
        }

        for (int i = 0; i < 9; i++)
        {
            for (int k = 0; k < 9; k++)
            {
                movable[i][k] = false;
            }
        }

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
            break;
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
            break;
        }

        return true;
    }

    boolean canMoveTo(int row, int col)
    {
        return movable[row][col];
    }

    int kind(int row, int col)
    {
        if (isOpponent(row, col))
        {
            return field(row, col) - OPPONENT;
        }
        else
        {
            return field(row, col);
        }
    }

    boolean isOpponent(int row, int col)
    {
        return field(row, col) > OPPONENT;
    }

    boolean isMine(int row, int col)
    {
        return 0 < field(row, col) && !isOpponent(row, col);
    }

    boolean inField(int row, int col)
    {
        return 0 <= row && row < 9 && 0 <= col && col < 9;
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

        return field(row, col) != 0;
    }

    private void fillMovableRYU(int row, int col)
    {
        fillMovableGYOKU(row, col);
        fillMovableHI(row, col);
    }

    private void fillMovableUMA(int row, int col)
    {
        fillMovableGYOKU(row, col);
        fillMovableKAKU(row, col);
    }

    private void fillMovableGYOKU(int row, int col)
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
                setMovable(tmpRow, col+dc, opponent);
            }
        }
    }

    private void fillMovableHI(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        for (int dr = -1; dr < 2; dr += 2)
        {
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
            int tmpRow = row + dr;
            for (int dc = -1; dc < 2; dc += 2)
            {
                setMovable(tmpRow, col+dc, opponent);
            }
        }
    }

    private void fillMovableKEI(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        int tmpRow = opponent ? (row+2) : (row-2);
        setMovable(tmpRow, col-1, opponent);
        setMovable(tmpRow, col+1, opponent);
    }

    private void fillMovableKYO(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        int dr = opponent ? 1 : -1;
        for (int tmpRow = row+dr; true; tmpRow += dr)
        {
            if (setMovable(tmpRow, col, opponent))
            {
                break;
            }
        }
    }

    private void fillMovableFU(int row, int col)
    {
        if (isOpponent(row, col))
        {
            setMovable(row+1, col, true);
        }
        else
        {
            setMovable(row-1, col, false);
        }
    }
}