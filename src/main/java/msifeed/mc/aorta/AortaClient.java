package msifeed.mc.aorta;

import com.google.common.io.CharStreams;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import msifeed.mc.aorta.client.Keybinds;
import msifeed.mc.aorta.client.ResponsiveEntityStatus;
import msifeed.mc.aorta.genesis.AortaCreativeTab;
import msifeed.mc.aorta.sys.utils.DRM;
import msifeed.mc.mellow.Mellow;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AortaClient extends Aorta {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        initMellow();
    }

    @Override
    public void init() {
        super.init();
        AortaCreativeTab.init();
        DRM.apply();
        GUI_HANDLER.init();
        Keybinds.INSTANCE.init();
        ResponsiveEntityStatus.init();
    }

    private void initMellow() {
        final IResourcePack resourcePack = FMLClientHandler.instance().getResourcePackFor(Aorta.MODID);
        final ResourceLocation themeSprite = new ResourceLocation(Aorta.MODID + ":theme/theme.png");
        final ResourceLocation themeMeta = new ResourceLocation(Aorta.MODID + ":theme/theme.json");

        try {
            final InputStream metaInput = resourcePack.getInputStream(themeMeta);
            final String json = CharStreams.toString(new InputStreamReader(metaInput, StandardCharsets.UTF_8));
            Mellow.loadTheme(themeSprite, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
