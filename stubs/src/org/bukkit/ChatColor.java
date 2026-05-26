package org.bukkit;
public enum ChatColor {
    BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
    DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE,
    MAGIC, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET;
    public static final char COLOR_CHAR = (char)0xA7;
    public static String translateAlternateColorCodes(char altCol, String text){return text;}
    public char getChar(){return ' ';}
    @Override public String toString(){return "§"+name().charAt(0);}
}
