package com.foodtrackerclitool;


import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Utilities {
    public static boolean QuitPrompt(String s) {
        boolean quit = s.equalsIgnoreCase("q") || s.equalsIgnoreCase("quit");
        if (quit) System.out.println();
        return quit;
    }

    public static String ToTitleCase(String inputString)
    {
        if (inputString.isBlank()) {
            return "";
        }

        if (inputString.length() == 1) {
            return inputString.toUpperCase();
        }

        StringBuffer resultPlaceHolder = new StringBuffer(inputString.length());

        Stream.of(inputString.split(" ")).forEach(stringPart ->
        {
            if (stringPart.length() > 1)
                resultPlaceHolder.append(stringPart.substring(0, 1)
                        .toUpperCase())
                        .append(stringPart.substring(1)
                                .toLowerCase());
            else
                resultPlaceHolder.append(stringPart.toUpperCase());

            resultPlaceHolder.append(" ");
        });
        return resultPlaceHolder.toString().trim();
    }

    static void RenameCorruptedFile(File file) {
        boolean renamed = file.renameTo(new File(file.getName() + ".corrupted"));
        if (!renamed) {
            System.out.println("Could Not Rename Corrupted File: File Will Be Deleted On Program Exit");
        }
    }

    static int isConsecutiveSum(int i){
        //input i is a sum of consecutive numbers if 1 -8i is a perfect square
        int x = 1 + (8 * i);

        double root = Math.sqrt(x);
        if((root - Math.floor(root)) == 0){
            return ((int)root - 1) / 2;
        }
        return -1;
    }
}
