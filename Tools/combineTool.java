package Tools;

import Figures.*;
import java.util.Map;

public class combineTool {
    public static long combine_figure_bitboards(Map<String, Figure> figure_map) {
        long return_value = 0L;


        for (Map.Entry<String, Figure> entry : figure_map.entrySet()) {
            Figure figure = entry.getValue();
            if(figure == null) continue;
            return_value |= figure.getPosition();
        }

        return return_value;
    }
    public static long combine_longs_to_bitboard(long long1, long long2) {
        return long1 | long2;
    }  
    
}
