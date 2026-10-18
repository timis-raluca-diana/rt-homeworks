package ssatr.device;

import java.util.concurrent.BlockingQueue;

/**
 * TASK APERIODIC: execută comenzile primite de la business service.
 *
 * Comenzile sosesc oricând, pe MQTT, fără un interval minim între ele, și sunt
 * puse în coada commands. Fiecare comandă eliberează un job al taskului.
 * Comenzi care trebuie suportate:
 *
 *   SET_INTERVAL <ms>       schimbă perioada T a taskului periodic
 *   SET_THRESHOLD <valoare> schimbă pragul de alarmă
 *   RESET                   readuce senzorul la valoarea inițială
 *   STATUS                  raportează perioada și pragul curente
 *
 * După executarea fiecărei comenzi, device-ul publică pe Config.statusTopic(deviceId)
 * o confirmare: "deviceId;timestamp;text", de exemplu
 * "device-1;1727600000000;OK SET_INTERVAL 1000".
 */
public class AperiodicCommandTask implements Runnable {

    private final String deviceId;
    private final TemperatureSensor sensor;
    private final MqttConnection mqtt;
    private final BlockingQueue<String> commands;
    private final PeriodicTelemetryTask periodic;

    public AperiodicCommandTask(String deviceId, TemperatureSensor sensor, MqttConnection mqtt,
                                BlockingQueue<String> commands, PeriodicTelemetryTask periodic) {
        this.deviceId = deviceId;
        this.sensor = sensor;
        this.mqtt = mqtt;
        this.commands = commands;
        this.periodic = periodic;
    }

    @Override
    public void run() {
        // TODO 3: implementați taskul aperiodic:
        //   - preluați comenzile din coadă: commands.take()
        //   - separați numele comenzii de parametru: command.split(" ")
        //   - executați comanda (periodic.setPeriodMs(...), sensor.reset() etc.)
        //   - publicați confirmarea pe Config.statusTopic(deviceId)
        //   - o comandă necunoscută sau un parametru invalid nu trebuie să
        //     oprească taskul: publicați "ERROR <motiv>"
        //
        // Vezi exemplul exemple/01-taskuri/.../AperiodicTaskExample.java

        try{
            while(true){
                String command = commands.take();
                String[] parts = command.split(" ");
                String reply = "";
                try {
                    switch (parts[0]) {
                        case "RESET" -> {
                            sensor.reset();
                            reply = "OK RESET";
                        }
                        case "SET_INTERVAL" -> {
                            periodic.setPeriodMs(Long.parseLong(parts[1]));
                            reply = "OK SET_INTERVAL " + parts[1];
                        }
                        case "SET_THRESHOLD" -> {
                            periodic.setThreshold(Double.parseDouble((parts[1])));
                            reply = "OK SET_THRESHOLD " + parts[1];
                        }
                        case "STATUS" -> {
                            reply = "OK STATUS " + periodic.getPeriodMs() + " " + periodic.getThreshold();
                        }
                        default -> {
                            reply = "ERROR Comanda necunoscuta " + parts[0];
                        }
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
                    reply = "ERROR Parametru invalid sau lipsa";
                }
                String msg = deviceId + ";" + System.currentTimeMillis() + ";" + reply;
                mqtt.publish(Config.statusTopic(deviceId), msg);
            }
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

    }
}
