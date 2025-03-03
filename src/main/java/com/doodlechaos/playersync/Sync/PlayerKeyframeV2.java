package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.Sync.InputEventContainers.KeyboardEvent;
import com.doodlechaos.playersync.Sync.InputEventContainers.MouseButtonEvent;
import com.doodlechaos.playersync.Sync.InputEventContainers.MouseScrollEvent;
import com.doodlechaos.playersync.Sync.InputEventContainers.MousePosEvent;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PlayerKeyframeV2 {

    public final long frame;
    public final Vec3d playerPos;
    public float playerYaw;
    public float playerPitch;

    public final List<MouseButtonEvent> recordedMouseButtonEvents;
    public final List<MouseScrollEvent> recordedMouseScrollEvents;
    public final List<MousePosEvent> recordedMousePosEvents;
    public final List<KeyboardEvent> recordedKeyboardEvents;

    /**
     * Constructs a PlayerKeyframeV2 with both player and input data.
     *
     * @param frame               The frame number.
     * @param playerPos           The player's position.
     * @param playerYaw           The player's yaw.
     * @param playerPitch         The player's pitch.
     * @param mouseButtonEvents   List of mouse button events.
     * @param mouseScrollEvents   List of mouse scroll events.
     * @param mousePosEvents      List of mouse position events.
     * @param keyboardEvents      List of keyboard events.
     */
    public PlayerKeyframeV2(long frame, Vec3d playerPos, float playerYaw, float playerPitch,
                            List<MouseButtonEvent> mouseButtonEvents,
                            List<MouseScrollEvent> mouseScrollEvents,
                            List<MousePosEvent> mousePosEvents,
                            List<KeyboardEvent> keyboardEvents) {
        this.frame = frame;
        this.playerPos = playerPos;
        this.playerYaw = playerYaw;
        this.playerPitch = playerPitch;
        this.recordedMouseButtonEvents = mouseButtonEvents;
        this.recordedMouseScrollEvents = mouseScrollEvents;
        this.recordedMousePosEvents = mousePosEvents;
        this.recordedKeyboardEvents = keyboardEvents;
    }

    /**
     * Constructs a PlayerKeyframeV2 from a single-line string.
     * Expected format:
     * frame=123|playerPos=[1.234,2.345,3.456]|playerYaw=90.0|playerPitch=45.0|
     * mouseButtonEvents=[0,1,0;1,0,0]|mouseScrollEvents=[0.5,0.2;1.0,0.3]|
     * mousePosEvents=[100.0,200.0;150.0,250.0]|keyboardEvents=[65,30,1,0;66,31,0,0]
     */
    public PlayerKeyframeV2(String line) {
        // Default values in case a field is missing.
        long frameValue = 0;
        Vec3d posValue = new Vec3d(0, 0, 0);
        float yawValue = 0;
        float pitchValue = 0;
        List<MouseButtonEvent> mbEvents = new ArrayList<>();
        List<MouseScrollEvent> msEvents = new ArrayList<>();
        List<MousePosEvent> mpEvents = new ArrayList<>();
        List<KeyboardEvent> kbEvents = new ArrayList<>();

        // Split the line by '|'
        String[] tokens = line.split("\\|");
        for (String token : tokens) {
            if (token.startsWith("frame=")) {
                frameValue = Long.parseLong(token.substring("frame=".length()));
            } else if (token.startsWith("playerPos=")) {
                String posStr = token.substring("playerPos=".length());
                posStr = posStr.substring(1, posStr.length() - 1); // Remove '[' and ']'
                String[] coords = posStr.split(",");
                if (coords.length == 3) {
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);
                    double z = Double.parseDouble(coords[2]);
                    posValue = new Vec3d(x, y, z);
                }
            } else if (token.startsWith("playerYaw=")) {
                yawValue = Float.parseFloat(token.substring("playerYaw=".length()));
            } else if (token.startsWith("playerPitch=")) {
                pitchValue = Float.parseFloat(token.substring("playerPitch=".length()));
            } else if (token.startsWith("mouseButtonEvents=")) {
                String eventsStr = token.substring("mouseButtonEvents=".length());
                eventsStr = eventsStr.substring(1, eventsStr.length() - 1); // Remove '[' and ']'
                if (!eventsStr.isEmpty()) {
                    String[] eventTokens = eventsStr.split(";");
                    for (String e : eventTokens) {
                        String[] parts = e.split(",");
                        if (parts.length == 3) {
                            int button = Integer.parseInt(parts[0]);
                            int action = Integer.parseInt(parts[1]);
                            int mods = Integer.parseInt(parts[2]);
                            mbEvents.add(new MouseButtonEvent(button, action, mods));
                        }
                    }
                }
            } else if (token.startsWith("mouseScrollEvents=")) {
                String eventsStr = token.substring("mouseScrollEvents=".length());
                eventsStr = eventsStr.substring(1, eventsStr.length() - 1); // Remove '[' and ']'
                if (!eventsStr.isEmpty()) {
                    String[] eventTokens = eventsStr.split(";");
                    for (String e : eventTokens) {
                        String[] parts = e.split(",");
                        if (parts.length == 2) {
                            double horizontal = Double.parseDouble(parts[0]);
                            double vertical = Double.parseDouble(parts[1]);
                            msEvents.add(new MouseScrollEvent(horizontal, vertical));
                        }
                    }
                }
            } else if (token.startsWith("mousePosEvents=")) {
                String eventsStr = token.substring("mousePosEvents=".length());
                eventsStr = eventsStr.substring(1, eventsStr.length() - 1); // Remove '[' and ']'
                if (!eventsStr.isEmpty()) {
                    String[] eventTokens = eventsStr.split(";");
                    for (String e : eventTokens) {
                        String[] parts = e.split(",");
                        if (parts.length == 2) {
                            double x = Double.parseDouble(parts[0]);
                            double y = Double.parseDouble(parts[1]);
                            mpEvents.add(new MousePosEvent(x, y));
                        }
                    }
                }
            } else if (token.startsWith("keyboardEvents=")) {
                String eventsStr = token.substring("keyboardEvents=".length());
                eventsStr = eventsStr.substring(1, eventsStr.length() - 1); // Remove '[' and ']'
                if (!eventsStr.isEmpty()) {
                    String[] eventTokens = eventsStr.split(";");
                    for (String e : eventTokens) {
                        String[] parts = e.split(",");
                        // Expected parts: key, scancode, action, mods.
                        if (parts.length == 4) {
                            int key = Integer.parseInt(parts[0]);
                            int scancode = Integer.parseInt(parts[1]);
                            int action = Integer.parseInt(parts[2]);
                            int mods = Integer.parseInt(parts[3]);
                            kbEvents.add(new KeyboardEvent(key, scancode, action, mods));
                        }
                    }
                }
            }
        }
        this.frame = frameValue;
        this.playerPos = posValue;
        this.playerYaw = yawValue;
        this.playerPitch = pitchValue;
        this.recordedMouseButtonEvents = mbEvents;
        this.recordedMouseScrollEvents = msEvents;
        this.recordedMousePosEvents = mpEvents;
        this.recordedKeyboardEvents = kbEvents;
    }

    /**
     * Serializes the PlayerKeyframeV2 to a single line of text with full precision.
     * Format:
     * frame=123|playerPos=[1.234,2.345,3.456]|playerYaw=90.0|playerPitch=45.0|
     * mouseButtonEvents=[0,1,0;1,0,0]|mouseScrollEvents=[0.5,0.2;1.0,0.3]|
     * mousePosEvents=[100.0,200.0;150.0,250.0]|keyboardEvents=[65,30,1,0;66,31,0,0]
     */
    public String ToLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("frame=").append(frame);
        sb.append("|playerPos=[").append(playerPos.x).append(",")
                .append(playerPos.y).append(",")
                .append(playerPos.z).append("]");
        sb.append("|playerYaw=").append(playerYaw);
        sb.append("|playerPitch=").append(playerPitch);

        // Serialize mouse button events
        sb.append("|mouseButtonEvents=[");
        for (int i = 0; i < recordedMouseButtonEvents.size(); i++) {
            MouseButtonEvent mbe = recordedMouseButtonEvents.get(i);
            sb.append(mbe.button).append(",")
                    .append(mbe.action).append(",")
                    .append(mbe.mods);
            if (i < recordedMouseButtonEvents.size() - 1) {
                sb.append(";");
            }
        }
        sb.append("]");

        // Serialize mouse scroll events using horizontal and vertical.
        sb.append("|mouseScrollEvents=[");
        for (int i = 0; i < recordedMouseScrollEvents.size(); i++) {
            MouseScrollEvent mse = recordedMouseScrollEvents.get(i);
            sb.append(mse.horizontal).append(",")
                    .append(mse.vertical);
            if (i < recordedMouseScrollEvents.size() - 1) {
                sb.append(";");
            }
        }
        sb.append("]");

        // Serialize mouse position events
        sb.append("|mousePosEvents=[");
        for (int i = 0; i < recordedMousePosEvents.size(); i++) {
            MousePosEvent mpe = recordedMousePosEvents.get(i);
            sb.append(mpe.x).append(",")
                    .append(mpe.y);
            if (i < recordedMousePosEvents.size() - 1) {
                sb.append(";");
            }
        }
        sb.append("]");

        // Serialize keyboard events
        sb.append("|keyboardEvents=[");
        for (int i = 0; i < recordedKeyboardEvents.size(); i++) {
            KeyboardEvent ke = recordedKeyboardEvents.get(i);
            sb.append(ke.key).append(",")
                    .append(ke.scancode).append(",")
                    .append(ke.action).append(",")
                    .append(ke.modifiers);
            if (i < recordedKeyboardEvents.size() - 1) {
                sb.append(";");
            }
        }
        sb.append("]");

        return sb.toString();
    }
}
