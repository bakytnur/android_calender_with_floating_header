package bakha.ms.outlook.data;

import java.util.HashMap;
import java.util.Map;

public enum AlertType {
    NONE(0),
    ON_TIME(1),
    BEFORE_5_MINUTES(2),
    BEFORE_10_MINUTES(3),
    BEFORE_15_MINUTES(4),
    BEFORE_30_MINUTES(5),
    BEFORE_1_HOUR(6),
    BEFORE_1_DAY(7);

    private final int value;

    AlertType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    public static AlertType forValue(int value) {
        return intToTypeMap.get(value);
    }

    private static final Map<Integer, AlertType> intToTypeMap = new HashMap<>();
    static {
        for (AlertType type : AlertType.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

}
