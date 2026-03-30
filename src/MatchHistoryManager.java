import java.io.*;
import java.util.*;

/** Simple file-based match history (CSV) */
public class MatchHistoryManager {
    private static final String FILE = "match_history.csv";

    public static void save(String playerA, String playerB, String winner) {
        try (FileWriter fw = new FileWriter(FILE, true)) {
            String line = String.format("%s,%s,%s,%s\n", new Date().toString(), playerA, playerB, winner);
            fw.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
