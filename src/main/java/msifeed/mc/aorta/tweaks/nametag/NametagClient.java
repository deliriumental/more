package msifeed.mc.aorta.tweaks.nametag;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.ReflectionHelper;
import msifeed.mc.aorta.sys.rpc.Rpc;
import msifeed.mc.aorta.sys.rpc.RpcMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;

public class NametagClient extends Nametag {
    private static final int MAX_NAMETAG_DISTANCE = 4;
    private static final int TYPING_PING_MS = 3000;
    private static final int TYPING_TAG_INTERVAL_MS = 400;
    private static final String[] TYPING_REPLACER = {".", "..", "..."};

    private HashMap<Integer, Long> typingPlayers = new HashMap<>();
    private long lastNotify = 0;
    private char lastCharPressed = 0;

    @Override
    public void init() {
        super.init();
        FMLCommonHandler.instance().bus().register(this);
    }

    @RpcMethod(Nametag.broadcastTyping)
    public void onBroadcastTyping(MessageContext ctx, int id) {
        typingPlayers.put(id, System.currentTimeMillis());
    }

    @SubscribeEvent
    public void onRenderLivingSpecialPre(RenderLivingEvent.Specials.Pre event) {
        if (!(event.entity instanceof EntityPlayer)) return;

        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        final EntityPlayer player = (EntityPlayer) event.entity;

        final Long typingStarted = typingPlayers.get(player.getEntityId());
        if (typingStarted != null) {
            final long now = System.currentTimeMillis();
            if (now - typingStarted > TYPING_PING_MS) {
                typingPlayers.remove(player.getEntityId());
                player.refreshDisplayName();
            } else {
                final String dots = TYPING_REPLACER[(int) (now / TYPING_TAG_INTERVAL_MS % TYPING_REPLACER.length)];
                ReflectionHelper.setPrivateValue(EntityPlayer.class, player, dots, "displayname");
            }
        } else {
            final String name = displayOriginalUsername() ? player.getCommandSenderName() : getPreferredName(player);
            if (!name.equals(player.getDisplayName()))
                ReflectionHelper.setPrivateValue(EntityPlayer.class, player, name, "displayname");
        }

        final float distance = self.getDistanceToEntity(player);
        if (distance > MAX_NAMETAG_DISTANCE)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat))
            return;
        final char c = Keyboard.getEventCharacter();
        if (lastCharPressed != c) {
            lastCharPressed = c;
            notifyTyping();
        }
    }

    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        super.onNameFormat(event);
    }

    private void notifyTyping() {
        if (System.currentTimeMillis() - lastNotify < TYPING_PING_MS / 2)
            return;
        lastNotify = System.currentTimeMillis();
        Rpc.sendToServer(Nametag.notifyTyping, Minecraft.getMinecraft().thePlayer.getEntityId());
    }

    private static boolean displayOriginalUsername() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU);
    }
}
