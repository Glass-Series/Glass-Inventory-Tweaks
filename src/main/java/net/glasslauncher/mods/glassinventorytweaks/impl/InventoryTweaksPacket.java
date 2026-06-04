package net.glasslauncher.mods.glassinventorytweaks.impl;

import lombok.SneakyThrows;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ClientNetworkHandler;
import net.minecraft.network.NetworkHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.modificationstation.stationapi.api.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;

public class InventoryTweaksPacket extends TemplateManagedPacket<InventoryTweaksPacket> {
    private static final PacketType<InventoryTweaksPacket> TYPE = PacketType.builder(false, true, InventoryTweaksPacket::new).build();

    private int syncId;
    private ClickType clickType;
    private int[] sourceSlotIndexes;

    public InventoryTweaksPacket(ScreenHandler handler, ClickType clickType, int[] sourceSlotIndexes, int[] targetSlotIndexes) {
        syncId = handler.syncId;
        this.clickType = clickType;
        this.sourceSlotIndexes = sourceSlotIndexes;
    }

    public InventoryTweaksPacket() {
    }

    @Override
    public @NotNull PacketType<InventoryTweaksPacket> getType() {
        return TYPE;
    }

    @Override
    @SneakyThrows
    public void read(DataInputStream stream) {
        syncId = stream.readInt();
        clickType = ClickType.values()[stream.readInt()];
        int indexes = stream.readInt();
        sourceSlotIndexes = new int[indexes];
        for (int i = 0; i < indexes; i++) {
            sourceSlotIndexes[i] = stream.readInt();
        }
    }

    @Override
    @SneakyThrows
    public void write(TrackingOutputStream stream) {
        stream.writeInt(syncId);
        stream.writeInt(clickType.ordinal());
        stream.writeInt(sourceSlotIndexes.length);
        for (int sourceSlotIndex : sourceSlotIndexes) {
            stream.writeInt(sourceSlotIndex);
        }
    }

    @Override
    public void apply(NetworkHandler networkHandler) {
        switch (clickType) {
            case SHIFT_CLICK -> {
                ScreenHandler handler;
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && networkHandler instanceof ServerPlayNetworkHandler serverPlayNetworkHandler) {
                    if (serverPlayNetworkHandler.player.currentScreenHandler.syncId != syncId) {
                        return;
                    }
                    handler = serverPlayNetworkHandler.player.currentScreenHandler;
                }
                else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    handler = Minecraft.INSTANCE.player.currentScreenHandler;
                }
                else {
                    return; // Something invalid happened
                }
                for (int slot : sourceSlotIndexes) {
                    ModdedClickImpl.shiftClick(handler.getSlot(slot), handler);
                }
            }
            case SCROLL -> {
                break;
            }
        }
    }
}
