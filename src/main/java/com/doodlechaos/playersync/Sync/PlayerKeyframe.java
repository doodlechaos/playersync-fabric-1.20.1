package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.Sync.InputEventContainers.InputEvent;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PlayerKeyframe {

    public final long frame;
    public final float tickDelta;
    public final Vec3d playerPos;
    public float playerYaw;
    public float playerPitch;
    public final List<InputEvent> recordedInputEvents;

    /**
     * Constructs a PlayerKeyframeV2 with both player and input data.
     *
     * @param frame       The frame number.
     * @param playerPos   The player's position.
     * @param playerYaw   The player's yaw.
     * @param playerPitch The player's pitch.
     * @param inputEvents The list of recorded input events.
     */
    public PlayerKeyframe(long frame, float tickDelta, Vec3d playerPos, float playerYaw, float playerPitch, List<InputEvent> inputEvents) {
        this.frame = frame;
        this.tickDelta = tickDelta;
        this.playerPos = playerPos;
        this.playerYaw = playerYaw;
        this.playerPitch = playerPitch;
        this.recordedInputEvents = inputEvents;
    }

    /**
     * Constructs a PlayerKeyframeV2 from a single-line string.
     * Expected format:
     * frame=123|playerPos=[1.234,2.345,3.456]|playerYaw=90.0|playerPitch=45.0|inputEvents=[KeyboardEvent;key=65;scancode=30;action=1;modifiers=0,MouseButtonEvent;button=0;action=1;mods=0]
     */
    public PlayerKeyframe(String line) {
        String[] parts = line.split("\\|");
        long frame = 0;
        float tickDelta = 0;
        Vec3d pos = null;
        float yaw = 0;
        float pitch = 0;
        List<InputEvent> events = new ArrayList<>();

        for (String part : parts) {
            if (part.startsWith("frame=")) {
                frame = Long.parseLong(part.substring("frame=".length()));
            }else if(part.startsWith("tickDelta=")){
                tickDelta = Float.parseFloat(part.substring("tickDelta=".length()));
            }
            else if (part.startsWith("playerPos=")) {
                int start = part.indexOf('[');
                int end = part.indexOf(']');
                String[] coords = part.substring(start + 1, end).split(",");
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                double z = Double.parseDouble(coords[2]);
                pos = new Vec3d(x, y, z);
            } else if (part.startsWith("playerYaw=")) {
                yaw = Float.parseFloat(part.substring("playerYaw=".length()));
            } else if (part.startsWith("playerPitch=")) {
                pitch = Float.parseFloat(part.substring("playerPitch=".length()));
            } else if (part.startsWith("inputEvents=")) {
                int start = part.indexOf('[');
                int end = part.lastIndexOf(']');
                String eventsStr = part.substring(start + 1, end);
                if (!eventsStr.trim().isEmpty()) {
                    String[] eventLines = eventsStr.split(",");
                    for (String eventLine : eventLines) {
                        events.add(InputEvent.fromLine(eventLine));
                    }
                }
            }
        }
        this.frame = frame;
        this.tickDelta = tickDelta;
        this.playerPos = pos;
        this.playerYaw = yaw;
        this.playerPitch = pitch;
        this.recordedInputEvents = events;
    }

    /**
     * Serializes the PlayerKeyframeV2 to a single line of text with full precision.
     * Format:
     * frame=123|playerPos=[1.234,2.345,3.456]|playerYaw=90.0|playerPitch=45.0|inputEvents=[KeyboardEvent;key=65;scancode=30;action=1;modifiers=0,MouseButtonEvent;button=0;action=1;mods=0]
     */
    public String ToLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("frame=").append(frame);
        sb.append("|tickDelta=").append(tickDelta);
        sb.append("|playerPos=[").append(playerPos.x).append(",").append(playerPos.y).append(",").append(playerPos.z).append("]");
        sb.append("|playerYaw=").append(playerYaw);
        sb.append("|playerPitch=").append(playerPitch);
        sb.append("|inputEvents=[");
        for (int i = 0; i < recordedInputEvents.size(); i++) {
            sb.append(recordedInputEvents.get(i).toLine());
            if (i < recordedInputEvents.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
