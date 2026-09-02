/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bop.src.main.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author ِAshraf.M.Fahmawi
 */
public class LogHelper {

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";

    public static void logInfo(String message) {
        System.out.println(GREEN + formatMessage("INFO", message) + RESET);
    }

    public static void logWarning(String message) {
        System.out.println(YELLOW + formatMessage("WARNING", message) + RESET);
    }

    public static void logError(String message) {
        System.out.println(RED + formatMessage("ERROR", message) + RESET);
    }

    private static String formatMessage(String level, String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedMessage = String.format("[%s] [%s] %s", now.format(formatter), level, message);
        return boxMessage(formattedMessage);
    }

    private static String boxMessage(String message) {
        int length = message.length();
        String topBorder = "┌" + repeatChar('─', length + 2) + "┐";
        String bottomBorder = "└" + repeatChar('─', length + 2) + "┘";
        return topBorder + "\n  " + message + "\n" + bottomBorder;
    }

    private static String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
