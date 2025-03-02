package com.doodlechaos.playersync.Sync;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerKeyframe {
    public final long frame;
    public final Vec3d playerPos;
    public float playerYaw;
    public float playerPitch;
    public final List<Integer> heldKeyboardKeys;

    // New fields for mouse information.
    public final double mouseX;
    public final double mouseY;
    public final List<Integer> heldMouseButtons;
    public final double scrollX;
    public final double scrollY;

    /**
     * Constructs a PlayerKeyframe with both player and mouse data.
     *
     * @param frame            The frame number.
     * @param playerPos        The player's position.
     * @param playerYaw        The player's yaw.
     * @param playerPitch      The player's pitch.
     * @param keyEvents        List of held keyboard key codes.
     * @param mouseX           The mouse X position.
     * @param mouseY           The mouse Y position.
     * @param mouseButtons     List of held mouse button codes.
     * @param scrollX          Accumulated scroll offset in the X direction.
     * @param scrollY          Accumulated scroll offset in the Y direction.
     */
    public PlayerKeyframe(long frame, Vec3d playerPos, float playerYaw, float playerPitch, List<Integer> keyEvents,
                          double mouseX, double mouseY, List<Integer> mouseButtons, double scrollX, double scrollY) {
        this.frame = frame;
        this.playerPos = playerPos;
        this.playerYaw = playerYaw;
        this.playerPitch = playerPitch;
        this.heldKeyboardKeys = keyEvents;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.heldMouseButtons = mouseButtons;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }

    /**
     * Constructs a PlayerKeyframe from a single-line string.
     * Expected format:
     * frame=123|playerPos=[1.234,2.345,3.456]|playerRotation=[45.0,30.0]|heldKeyboardKeys=[32,17]
     * |mousePos=[456.78,123.45]|heldMouseButtons=[0,1]|scroll=[0.0,0.0]
     */
    public PlayerKeyframe(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid keyframe line: " + line);
        }

        // Parse frame: "frame=123"
        String framePart = parts[0].trim();
        if (!framePart.startsWith("frame=")) {
            throw new IllegalArgumentException("Invalid frame part: " + framePart);
        }
        this.frame = Long.parseLong(framePart.substring("frame=".length()));

        // Parse playerPos: "playerPos=[1.234,2.345,3.456]"
        String posPart = parts[1].trim();
        if (!posPart.startsWith("playerPos=[") || !posPart.endsWith("]")) {
            throw new IllegalArgumentException("Invalid playerPos part: " + posPart);
        }
        String posContent = posPart.substring("playerPos=[".length(), posPart.length() - 1);
        String[] posValues = posContent.split(",");
        if (posValues.length != 3) {
            throw new IllegalArgumentException("Invalid number of values for playerPos: " + posContent);
        }
        double posX = Double.parseDouble(posValues[0].trim());
        double posY = Double.parseDouble(posValues[1].trim());
        double posZ = Double.parseDouble(posValues[2].trim());
        this.playerPos = new Vec3d(posX, posY, posZ);

        // Parse playerRotation: "playerRotation=[yaw,pitch]"
        String rotPart = parts[2].trim();
        if (!rotPart.startsWith("playerRotation=[") || !rotPart.endsWith("]")) {
            throw new IllegalArgumentException("Invalid playerRotation part: " + rotPart);
        }
        String rotContent = rotPart.substring("playerRotation=[".length(), rotPart.length() - 1);
        String[] rotValues = rotContent.split(",");
        if (rotValues.length != 2) {
            throw new IllegalArgumentException("Invalid number of values for playerRotation: " + rotContent);
        }
        this.playerYaw = Float.parseFloat(rotValues[0].trim());
        this.playerPitch = Float.parseFloat(rotValues[1].trim());

        // Parse heldKeyboardKeys: "heldKeyboardKeys=[32,17]"
        String keysPart = parts[3].trim();
        if (!keysPart.startsWith("heldKeyboardKeys=[") || !keysPart.endsWith("]")) {
            throw new IllegalArgumentException("Invalid heldKeyboardKeys part: " + keysPart);
        }
        String keysContent = keysPart.substring("heldKeyboardKeys=[".length(), keysPart.length() - 1);
        List<Integer> keyboardKeys = new ArrayList<>();
        if (!keysContent.isEmpty()) {
            String[] keyValues = keysContent.split(",");
            for (String key : keyValues) {
                keyboardKeys.add(Integer.parseInt(key.trim()));
            }
        }
        this.heldKeyboardKeys = keyboardKeys;

        // Parse mousePos: "mousePos=[456.78,123.45]"
        String mousePosPart = parts[4].trim();
        if (!mousePosPart.startsWith("mousePos=[") || !mousePosPart.endsWith("]")) {
            throw new IllegalArgumentException("Invalid mousePos part: " + mousePosPart);
        }
        String mousePosContent = mousePosPart.substring("mousePos=[".length(), mousePosPart.length() - 1);
        String[] mousePosValues = mousePosContent.split(",");
        if (mousePosValues.length != 2) {
            throw new IllegalArgumentException("Invalid number of values for mousePos: " + mousePosContent);
        }
        this.mouseX = Double.parseDouble(mousePosValues[0].trim());
        this.mouseY = Double.parseDouble(mousePosValues[1].trim());

        // Parse heldMouseButtons: "heldMouseButtons=[0,1]"
        String mouseButtonsPart = parts[5].trim();
        if (!mouseButtonsPart.startsWith("heldMouseButtons=[") || !mouseButtonsPart.endsWith("]")) {
            throw new IllegalArgumentException("Invalid heldMouseButtons part: " + mouseButtonsPart);
        }
        String mouseButtonsContent = mouseButtonsPart.substring("heldMouseButtons=[".length(), mouseButtonsPart.length() - 1);
        List<Integer> mouseButtons = new ArrayList<>();
        if (!mouseButtonsContent.isEmpty()) {
            String[] buttonValues = mouseButtonsContent.split(",");
            for (String button : buttonValues) {
                mouseButtons.add(Integer.parseInt(button.trim()));
            }
        }
        this.heldMouseButtons = mouseButtons;

        // Parse scroll: "scroll=[0.0,0.0]"
        String scrollPart = parts[6].trim();
        if (!scrollPart.startsWith("scroll=[") || !scrollPart.endsWith("]")) {
            throw new IllegalArgumentException("Invalid scroll part: " + scrollPart);
        }
        String scrollContent = scrollPart.substring("scroll=[".length(), scrollPart.length() - 1);
        String[] scrollValues = scrollContent.split(",");
        if (scrollValues.length != 2) {
            throw new IllegalArgumentException("Invalid number of values for scroll: " + scrollContent);
        }
        this.scrollX = Double.parseDouble(scrollValues[0].trim());
        this.scrollY = Double.parseDouble(scrollValues[1].trim());
    }

    /**
     * Serializes the PlayerKeyframe to a single line of text with full precision.
     * Format:
     * frame=123|playerPos=[1.234,2.345,3.456]|playerRotation=[45.0,30.0]|heldKeyboardKeys=[32,17]
     * |mousePos=[456.78,123.45]|heldMouseButtons=[0,1]|scroll=[0.0,0.0]
     */
    public String ToLine() {
        String posString = playerPos.x + "," + playerPos.y + "," + playerPos.z;
        String rotString = playerYaw + "," + playerPitch;
        String keyboardKeysString = heldKeyboardKeys.isEmpty() ? "" : heldKeyboardKeys.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String mousePosString = mouseX + "," + mouseY;
        String mouseButtonsString = heldMouseButtons.isEmpty() ? "" : heldMouseButtons.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String scrollString = scrollX + "," + scrollY;

        return "frame=" + frame +
                "|playerPos=[" + posString + "]" +
                "|playerRotation=[" + rotString + "]" +
                "|heldKeyboardKeys=[" + keyboardKeysString + "]" +
                "|mousePos=[" + mousePosString + "]" +
                "|heldMouseButtons=[" + mouseButtonsString + "]" +
                "|scroll=[" + scrollString + "]";
    }

    @Override
    public String toString() {
        return "PlayerKeyframe {" +
                "frame=" + frame +
                ", playerPos=" + playerPos +
                ", playerRotation=(yaw=" + String.format("%.2f", playerYaw) +
                ", pitch=" + String.format("%.2f", playerPitch) + ")" +
                ", heldKeyboardKeys=" + heldKeyboardKeys +
                ", mousePos=(" + mouseX + "," + mouseY + ")" +
                ", heldMouseButtons=" + heldMouseButtons +
                ", scroll=(" + scrollX + "," + scrollY + ")" +
                '}';
    }

    // Getters for playerYaw and playerPitch if needed.
    public float getPlayerYaw() {
        return playerYaw;
    }

    public float getPlayerPitch() {
        return playerPitch;
    }

}
