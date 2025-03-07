package com.doodlechaos.playersync.Sync;
import com.doodlechaos.playersync.Sync.InputEventContainers.InputEvent;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import java.util.ArrayList;
import java.util.List;

public class PlayerKeyframe {

    public final long frame;
    public final float tickDelta;
    public final Vec3d playerPos;
    public float playerYaw;
    public float playerPitch;
    public final Vec3d playerVel;

    public final Vec3d camPos;
    public final Quaternionf camRot;

    public final List<InputEvent> recordedInputEvents;

    public final List<String> cmds;

    /**
     * Constructs a PlayerKeyframe with both player and input data.
     *
     * @param frame       The frame number.
     * @param tickDelta   The tick delta.
     * @param playerPos   The player's position.
     * @param playerYaw   The player's yaw.
     * @param playerPitch The player's pitch.
     * @param playerVel   The player's velocity.
     * @param camPos      The camera's position.
     * @param camRot      The camera's rotation.
     * @param inputEvents The list of recorded input events.
     * @param cmds        The list of commands.
     */
    public PlayerKeyframe(long frame, float tickDelta, Vec3d playerPos, float playerYaw, float playerPitch, Vec3d playerVel,
                          Vec3d camPos, Quaternionf camRot, List<InputEvent> inputEvents, List<String> cmds) {
        this.frame = frame;
        this.tickDelta = tickDelta;
        this.playerPos = playerPos;
        this.playerYaw = playerYaw;
        this.playerPitch = playerPitch;
        this.playerVel = playerVel;
        this.camPos = camPos;
        this.camRot = camRot;
        this.recordedInputEvents = inputEvents;
        this.cmds = cmds;
    }

    /**
     * Constructs a PlayerKeyframe from a single-line string.
     * Expected format:
     * frame=123|tickDelta=0.0|playerPos=[1.234,2.345,3.456]|playerYaw=90.0|playerPitch=45.0|playerVel=[0.0,0.0,0.0]|camPos=[0.0,0.0,0.0]|camRot=[0.0,0.0,0.0,1.0]|inputEvents=[...|cmds=[cmd1,cmd2]
     */
    public PlayerKeyframe(String line) {
        String[] parts = line.split("\\|");
        long frame = 0;
        float tickDelta = 0;
        Vec3d pos = null;
        float yaw = 0;
        float pitch = 0;
        // Default velocity is set to (0,0,0)
        Vec3d vel = new Vec3d(0, 0, 0);
        // Initialize with defaults for camPos and camRot in case they are missing in the line
        Vec3d camPos = new Vec3d(0, 0, 0);
        Quaternionf camRot = new Quaternionf(0, 0, 0, 1);
        List<InputEvent> events = new ArrayList<>();
        List<String> cmds = new ArrayList<>();

        for (String part : parts) {
            if (part.startsWith("frame=")) {
                frame = Long.parseLong(part.substring("frame=".length()));
            } else if (part.startsWith("tickDelta=")) {
                tickDelta = Float.parseFloat(part.substring("tickDelta=".length()));
            } else if (part.startsWith("playerPos=")) {
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
            } else if (part.startsWith("playerVel=")) {
                int start = part.indexOf('[');
                int end = part.indexOf(']');
                String[] coords = part.substring(start + 1, end).split(",");
                double vx = Double.parseDouble(coords[0]);
                double vy = Double.parseDouble(coords[1]);
                double vz = Double.parseDouble(coords[2]);
                vel = new Vec3d(vx, vy, vz);
            } else if (part.startsWith("camPos=")) {
                int start = part.indexOf('[');
                int end = part.indexOf(']');
                String[] coords = part.substring(start + 1, end).split(",");
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                double z = Double.parseDouble(coords[2]);
                camPos = new Vec3d(x, y, z);
            } else if (part.startsWith("camRot=")) {
                int start = part.indexOf('[');
                int end = part.indexOf(']');
                String[] values = part.substring(start + 1, end).split(",");
                float x = Float.parseFloat(values[0]);
                float y = Float.parseFloat(values[1]);
                float z = Float.parseFloat(values[2]);
                float w = Float.parseFloat(values[3]);
                camRot = new Quaternionf(x, y, z, w);
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
            } else if (part.startsWith("cmds=")) {
                int start = part.indexOf('[');
                int end = part.indexOf(']');
                String cmdsStr = part.substring(start + 1, end);
                if (!cmdsStr.trim().isEmpty()) {
                    String[] cmdArray = cmdsStr.split(",");
                    for (String cmd : cmdArray) {
                        cmds.add(cmd.trim());
                    }
                }
            }
        }
        this.frame = frame;
        this.tickDelta = tickDelta;
        this.playerPos = pos;
        this.playerYaw = yaw;
        this.playerPitch = pitch;
        this.playerVel = vel;
        this.camPos = camPos;
        this.camRot = camRot;
        this.recordedInputEvents = events;
        this.cmds = cmds;
    }

    /**
     * Serializes the PlayerKeyframe to a single line of text with full precision.
     * Format:
     * frame=123|tickDelta=0.0|playerPos=[1.234,2.345,3.456]|playerYaw=90.0|playerPitch=45.0|playerVel=[0.0,0.0,0.0]|camPos=[0.0,0.0,0.0]|camRot=[0.0,0.0,0.0,1.0]|inputEvents=[...|cmds=[cmd1,cmd2]
     */
    public String ToLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("frame=").append(frame);
        sb.append("|tickDelta=").append(tickDelta);
        sb.append("|playerPos=[").append(playerPos.x).append(",").append(playerPos.y).append(",").append(playerPos.z).append("]");
        sb.append("|playerYaw=").append(playerYaw);
        sb.append("|playerPitch=").append(playerPitch);
        sb.append("|playerVel=[").append(playerVel.x).append(",").append(playerVel.y).append(",").append(playerVel.z).append("]");
        sb.append("|camPos=[").append(camPos.x).append(",").append(camPos.y).append(",").append(camPos.z).append("]");
        sb.append("|camRot=[").append(camRot.x).append(",").append(camRot.y).append(",").append(camRot.z).append(",").append(camRot.w).append("]");
        sb.append("|inputEvents=[");
        for (int i = 0; i < recordedInputEvents.size(); i++) {
            sb.append(recordedInputEvents.get(i).toLine());
            if (i < recordedInputEvents.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("|cmds=[");
        for (int i = 0; i < cmds.size(); i++) {
            sb.append(cmds.get(i));
            if (i < cmds.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

