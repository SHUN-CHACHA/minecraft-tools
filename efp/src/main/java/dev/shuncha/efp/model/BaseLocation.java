package dev.shuncha.efp.model;

import java.util.UUID;

public class BaseLocation {

    private final UUID id;
    private final UUID ownerId;
    private String ownerName;
    private String name;
    private final String world;
    private final String environment; // NORMAL / NETHER / THE_END
    private final int x;
    private final int y;
    private final int z;
    private boolean publicFlag;

    public BaseLocation(UUID id, UUID ownerId, String ownerName, String name,
                         String world, String environment, int x, int y, int z, boolean publicFlag) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.world = world;
        this.environment = environment;
        this.x = x;
        this.y = y;
        this.z = z;
        this.publicFlag = publicFlag;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorld() { return world; }
    public String getEnvironment() { return environment; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public boolean isPublic() { return publicFlag; }
    public void setPublic(boolean publicFlag) { this.publicFlag = publicFlag; }

    /** 表示用のディメンション名(日本語) */
    public String getDimensionLabel() {
        if (environment == null) {
            return "不明";
        }
        return switch (environment) {
            case "NETHER" -> "ネザー";
            case "THE_END" -> "エンド";
            default -> "オーバーワールド";
        };
    }
}