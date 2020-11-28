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

    GameState()
    {
        initialField = new int[9][9];
        currentField = new int[9][9];
        initialHands = new int[2][8];
        currentHands = new int[2][8];
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
}