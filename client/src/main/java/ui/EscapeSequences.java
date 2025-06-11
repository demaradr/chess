package ui;

/**
 * This class contains constants and functions relating to ANSI Escape Sequences that are useful in the Client display
 */
public class EscapeSequences {

    private static final String UNICODE_ESCAPE = "\u001b";
    private static final String ANSI_ESCAPE = "\033";

    public static final String ERASE_SCREEN = UNICODE_ESCAPE + "[H" + UNICODE_ESCAPE + "[2J";
    public static final String ERASE_LINE = UNICODE_ESCAPE + "[2K";

    public static final String SET_TEXT_BOLD = UNICODE_ESCAPE + "[1m";
    public static final String SET_TEXT_FAINT = UNICODE_ESCAPE + "[2m";
    public static final String RESET_TEXT_BOLD_FAINT = UNICODE_ESCAPE + "[22m";
    public static final String SET_TEXT_ITALIC = UNICODE_ESCAPE + "[3m";
    public static final String RESET_TEXT_ITALIC = UNICODE_ESCAPE + "[23m";
    public static final String SET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[4m";
    public static final String RESET_TEXT_UNDERLINE = UNICODE_ESCAPE + "[24m";
    public static final String SET_TEXT_BLINKING = UNICODE_ESCAPE + "[5m";
    public static final String RESET_TEXT_BLINKING = UNICODE_ESCAPE + "[25m";

    private static final String SET_TEXT_COLOR = UNICODE_ESCAPE + "[38;5;";
    private static final String SET_BG_COLOR = UNICODE_ESCAPE + "[48;5;";

    public static final String SET_TEXT_COLOR_BLACK = SET_TEXT_COLOR + "0m";
    public static final String SET_TEXT_COLOR_LIGHT_GREY = SET_TEXT_COLOR + "242m";
    public static final String SET_TEXT_COLOR_DARK_GREY = SET_TEXT_COLOR + "235m";
    public static final String SET_TEXT_COLOR_RED = SET_TEXT_COLOR + "160m";
    public static final String SET_TEXT_COLOR_GREEN = SET_TEXT_COLOR + "46m";
    public static final String SET_TEXT_COLOR_YELLOW = SET_TEXT_COLOR + "226m";
    public static final String SET_TEXT_COLOR_BLUE = SET_TEXT_COLOR + "12m";
    public static final String SET_TEXT_COLOR_MAGENTA = SET_TEXT_COLOR + "5m";
    public static final String SET_TEXT_COLOR_WHITE = SET_TEXT_COLOR + "15m";
    public static final String RESET_TEXT_COLOR = UNICODE_ESCAPE + "[39m";

    public static final String SET_BG_COLOR_BLACK = SET_BG_COLOR + "0m";
    public static final String SET_BG_COLOR_LIGHT_GREY = SET_BG_COLOR + "242m";
    public static final String SET_BG_COLOR_DARK_GREY = SET_BG_COLOR + "235m";
    public static final String SET_BG_COLOR_RED = SET_BG_COLOR + "160m";
    public static final String SET_BG_COLOR_GREEN = SET_BG_COLOR + "46m";
    public static final String SET_BG_COLOR_DARK_GREEN = SET_BG_COLOR + "22m";
    public static final String SET_BG_COLOR_YELLOW = SET_BG_COLOR + "226m";
    public static final String SET_BG_COLOR_BLUE = SET_BG_COLOR + "12m";
    public static final String SET_BG_COLOR_MAGENTA = SET_BG_COLOR + "5m";
    public static final String SET_BG_COLOR_WHITE = SET_BG_COLOR + "15m";
    public static final String RESET_BG_COLOR = UNICODE_ESCAPE + "[49m";
    public static final String SET_BG_COLOR_BROWN = SET_BG_COLOR + "94m";
    public static final String SET_BG_COLOR_IVORY = SET_BG_COLOR + "230m";


    public static final String ROW_COL_FORMAT = SET_BG_COLOR_DARK_GREEN + SET_TEXT_COLOR_WHITE;
    public static final String SET_TEXT_BOLD_AND_BLUE = SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE;
    public static final String SET_TEXT_BOLD_AND_WHITE = SET_TEXT_BOLD + SET_TEXT_COLOR_WHITE;

    public static final String SET_BG_COLOR_GREEN_ON_BROWN = "\u001B[42;2m";
    public static final String SET_BG_COLOR_GREEN_ON_IVORY = "\u001B[102m";

    public static final String BLACK_FOREGROUND = "\u001B[30m";
    public static final String RESET_COLOR = "\u001B[0m";

    public static final String WHITE_KING = BLACK_FOREGROUND + " ♔ " + SET_TEXT_BOLD + RESET_COLOR;
    public static final String WHITE_QUEEN = BLACK_FOREGROUND + " ♕ " + SET_TEXT_BOLD + RESET_COLOR;
    public static final String WHITE_BISHOP = BLACK_FOREGROUND + " ♗ " + SET_TEXT_BOLD + RESET_COLOR;
    public static final String WHITE_KNIGHT = BLACK_FOREGROUND + " ♘ " + SET_TEXT_BOLD + RESET_COLOR;
    public static final String WHITE_ROOK = BLACK_FOREGROUND + " ♖ " + SET_TEXT_BOLD + RESET_COLOR;
    public static final String WHITE_PAWN = BLACK_FOREGROUND + " ♙ " + SET_TEXT_BOLD + RESET_COLOR;
    public static final String BLACK_KING = " ♚ " + SET_TEXT_BOLD;
    public static final String BLACK_QUEEN = " ♛ " + SET_TEXT_BOLD;
    public static final String BLACK_BISHOP = " ♝ " + SET_TEXT_BOLD;
    public static final String BLACK_KNIGHT = " ♞ " + SET_TEXT_BOLD;
    public static final String BLACK_ROOK = " ♜ " + SET_TEXT_BOLD;
    public static final String BLACK_PAWN = " ♟ " + SET_TEXT_BOLD;
    public static final String EMPTY = " \u2003 ";

    public static String moveCursorToLocation(int x, int y) { return UNICODE_ESCAPE + "[" + y + ";" + x + "H"; }
}
