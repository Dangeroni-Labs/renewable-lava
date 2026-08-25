package dev.dangeroni.renewablelava.state;

import com.mojang.serialization.Codec;
import dev.dangeroni.renewablelava.RenewableLava;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class RenewableLavaWorldState extends SavedData {
    private static final Codec<RenewableLavaWorldState> CODEC = Codec.BOOL
        .optionalFieldOf("enabled", true)
        .codec()
        .xmap(RenewableLavaWorldState::new, RenewableLavaWorldState::isEnabled);

    private static final SavedDataType<RenewableLavaWorldState> TYPE = new SavedDataType<>(
        RenewableLava.id("world_state"),
        RenewableLavaWorldState::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    private boolean enabled;

    public RenewableLavaWorldState() {
        this(true);
    }

    public RenewableLavaWorldState(boolean enabled) {
        this.enabled = enabled;
    }

    public static RenewableLavaWorldState get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            this.setDirty();
        }
    }
}
