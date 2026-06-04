package net.glasslauncher.mods.glassinventorytweaks.impl;

import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.network.ServerLoginSuccessEvent;
import net.modificationstation.stationapi.api.event.world.WorldEvent;
import net.modificationstation.stationapi.api.network.ModdedPacketHandler;
import net.modificationstation.stationapi.api.server.event.network.PlayerAttemptLoginEvent;
import net.modificationstation.stationapi.api.server.event.network.PlayerLoginEvent;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class GlassInventoryTweaks {
    public static final Namespace NAMESPACE = Namespace.resolve();
    public static final Logger LOGGER = NAMESPACE.getLogger("GlassInventoryTweaks");
    public static boolean runningWithMod = true;

    @Environment(EnvType.CLIENT)
    @EventListener
    public void onJoin(ServerLoginSuccessEvent event) {
        ModdedPacketHandler moddedHandler = ((ModdedPacketHandler) event.clientNetworkHandler);
        runningWithMod = moddedHandler.isModded() && moddedHandler.getMods().containsKey(NAMESPACE.toString());
        LOGGER.info("Joined server {}running {}", runningWithMod ? "" : "not ", NAMESPACE.getName());
    }
}
