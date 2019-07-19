package msifeed.mc.aorta.tweaks;

import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import msifeed.mc.aorta.sys.config.ConfigEvent;
import msifeed.mc.aorta.sys.config.ConfigManager;
import msifeed.mc.aorta.sys.config.ConfigMode;
import msifeed.mc.aorta.sys.config.JsonConfig;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.Display;

public enum GameWindowOptions {
    INSTANCE;

    private final TypeToken<Content> configContentType = new TypeToken<Content>() {};
    private JsonConfig<Content> config = ConfigManager.getConfig(ConfigMode.CLIENT, configContentType, "window.json");


    public static void preInit() {
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            setTitle();
    }

    private void setTitle() {
        final String title = config.get().title;
        if (!Display.getTitle().equals(title))
            Display.setTitle(title);
    }

    public static class Content {
        public String title = "Ortega";
    }
}
