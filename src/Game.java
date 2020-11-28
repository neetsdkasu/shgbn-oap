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

    Game(Problem p)
    {
        currentField = new int[9][9];
        currentHands = new int[2][8];
        movable = new boolean[9][9];
        range = new int[2][9][9];
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
        resetRange();
    }

    private void resetRange()
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

    private void removeRange(int player)
    {
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                if (movable[row][col])
                {
                    range[player][row][col]--;
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

    boolean select(int row, int col)
    {
        return select(row, col, true);
    }

    boolean select(int row, int col, boolean useRange)
    {
        if (field(row, col) == 0)
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
            fillMovableGYOKU(row, col, useRange);
            break;
        }

        return true;
    }

    int getRange(int player, int row, int col)
    {
        return inField(row, col)
            ? range[player][row][col]
            : 0;
    }

    boolean canMoveTo(int row, int col)
    {
        return inField(row, col)
            ? movable[row][col]
            : false;
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
        boolean opponent = isOpponent(row, col);
        setMovable(row+1, col+1, opponent);
        setMovable(row+1, col-1, opponent);
        setMovable(row-1, col+1, opponent);
        setMovable(row-1, col-1, opponent);
        fillMovableHI(row, col);
    }

    private void fillMovableUMA(int row, int col)
    {
        boolean opponent = isOpponent(row, col);
        setMovable(row+1, col, opponent);
        setMovable(row+1, col, opponent);
        setMovable(row, col+1, opponent);
        setMovable(row, col-1, opponent);
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
        setMovable(row+1, col+1, opponent);
        setMovable(row+1, col-1, opponent);
        setMovable(row-1, col+1, opponent);
        setMovable(row-1, col-1, opponent);
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