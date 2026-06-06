package net.glasslauncher.mods.glassinventorytweaks.impl;

import lombok.SneakyThrows;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.modificationstation.stationapi.api.network.packet.PacketType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;

public class InventoryTweaksPacket extends TemplateManagedPacket<InventoryTweaksPacket> {
    public static final PacketType<InventoryTweaksPacket> TYPE = PacketType.builder(false, true, InventoryTweaksPacket::new).build();

    private int syncId;
    private ClickType clickType;
    private int[] targetSlotIndexes;

    public InventoryTweaksPacket(ScreenHandler handler, ClickType clickType, int[] targetSlotIndexes) {
        syncId = handler.syncId;
        this.clickType = clickType;
        this.targetSlotIndexes = targetSlotIndexes;
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
        targetSlotIndexes = new int[indexes];
        for (int i = 0; i < indexes; i++) {
            targetSlotIndexes[i] = stream.readInt();
        }
    }

    @Override
    @SneakyThrows
    public void write(TrackingOutputStream stream) {
        apply(null); // Only runs on client, server isn't very good at syncing slots, so we yolo it on the client too.
        stream.writeInt(syncId);
        stream.writeInt(clickType.ordinal());
        stream.writeInt(targetSlotIndexes.length);
        for (int sourceSlotIndex : targetSlotIndexes) {
            stream.writeInt(sourceSlotIndex);
        }
    }

    @Override
    public void apply(NetworkHandler networkHandler) {
        ScreenHandler handler;
        ItemStack cursorStack;
        PlayerEntity player;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            player = getPlayer(networkHandler, syncId);
            if (player == null) {
                return;
            }
        }
        else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            player = getPlayerClient();
        }
        else {
            return; // Something invalid happened
        }

        handler = player.currentScreenHandler;
        cursorStack = player.inventory.getCursorStack();

        switch (clickType) {
            case SHIFT_CLICK -> {
                for (int slot : targetSlotIndexes) {
                    ModdedClickImpl.shiftClick(handler.getSlot(slot), handler);
                }
            }
            case SCROLL -> {
                for (int slotId : targetSlotIndexes) {
                    Slot slot = handler.getSlot(slotId);
                    ModdedClickImpl.shiftClick(slot, handler, 1);
                }
            }
            case SPREAD_ONE -> ModdedClickImpl.spreadSingle(cursorStack, targetSlotIndexes, handler, player);
            case EVEN_SPREAD -> ModdedClickImpl.spreadEven(cursorStack, targetSlotIndexes, handler, player);
            case HOTBAR -> ModdedClickImpl.handleHotbar(targetSlotIndexes, handler, player);
        }
    }

    @Environment(EnvType.SERVER)
    private static PlayerEntity getPlayer(NetworkHandler networkHandler, int syncId) {
        if (((ServerPlayNetworkHandler) networkHandler).player.currentScreenHandler.syncId != syncId) {
            return null;
        }
        return ((ServerPlayNetworkHandler) networkHandler).player;
    }

    @Environment(EnvType.CLIENT)
    private static PlayerEntity getPlayerClient() {
        return Minecraft.INSTANCE.player;
    }
}
