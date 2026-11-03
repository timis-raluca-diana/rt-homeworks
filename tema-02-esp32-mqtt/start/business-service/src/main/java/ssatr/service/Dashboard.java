package ssatr.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Ultima stare cunoscută a fiecărui device: ultima temperatură, momentul
 * ultimului mesaj, numărul de alarme etc.
 *
 * Este actualizată de thread-urile MessageProcessor și citită de CommandConsole,
 * deci accesul trebuie sincronizat.
 */
public class Dashboard {

    // TODO 5: alegeți structura de date pentru starea device-urilor
    //   (de exemplu un Map<String, ...> cu cheia deviceId)
    //   și adăugați metodele de actualizare apelate din MessageProcessor.

    /** Afișează un tabel cu starea tuturor device-urilor. */
    private static class DeviceState {
        double lastTemp;
        long lastMessTime;
        int alarmCount;
    }
    private final Map<String, DeviceState> devices = new HashMap<>();

    public synchronized void updateTelemetry(String deviceId, double temp){
        DeviceState state = devices.computeIfAbsent(deviceId, k -> new DeviceState());
        state.lastTemp = temp;
        state.lastMessTime = System.currentTimeMillis();
    }
    public synchronized void updateAlarm(String deviceId, double temp){
        DeviceState state = devices.computeIfAbsent(deviceId, k -> new DeviceState());
        state.lastTemp = temp;
        state.lastMessTime = System.currentTimeMillis();
        state.alarmCount++;
    }
    public synchronized void updateStatus(String deviceId){
        DeviceState state = devices.computeIfAbsent(deviceId, k -> new DeviceState());
        state.lastMessTime = System.currentTimeMillis();
    }

    public synchronized void print() {
        // TODO 5: afișați câte un rând pentru fiecare device, de exemplu:
        //   DEVICE     TEMP    ULTIMUL MESAJ   ALARME
        //   device-1   23.41   acum 1 s        2
        System.out.println("DEVICE   TEMP   ULTIMUL MESAJ   ALARME");
        for (Map.Entry<String, DeviceState> entry : devices.entrySet()) {
            String id = entry.getKey();
            DeviceState state = entry.getValue();
            long ageSec = (System.currentTimeMillis() - state.lastMessTime) / 1000;
            System.out.printf("%-10s %.2f acum %d s %d%n", id, state.lastTemp, ageSec, state.alarmCount);
        }
    }
}
