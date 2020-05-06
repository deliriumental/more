package msifeed.mc.extensions.nametag;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import msifeed.mc.Bootstrap;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.sys.rpc.Rpc;
import msifeed.mc.sys.rpc.RpcMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class Nametag {
    @SidedProxy(
            serverSide = "msifeed.mc.extensions.nametag.Nametag",
            clientSide = "msifeed.mc.extensions.nametag.NametagClient"
    )
    public static Nametag INSTANCE;

    static final String broadcastTyping = Bootstrap.MODID + ":nametags.broadcast";
    private static final String notifyTyping = Bootstrap.MODID + ":nametags.notify";

    public static void preInit() {
        Rpc.register(INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    public static void notifyTyping() {
        Rpc.sendToServer(Nametag.notifyTyping, Minecraft.getMinecraft().thePlayer.getEntityId());
    }

    @RpcMethod(Nametag.notifyTyping)
    public void onNotifyTyping(MessageContext ctx, int id) {
        final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        final ChunkCoordinates coord = player.getPlayerCoordinates();
        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                player.dimension, coord.posX, coord.posY, coord.posZ, getSpeechRadius()
        );
        Rpc.sendToAllAround(Nametag.broadcastTyping, point, id);
    }

    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        event.displayname = getPreferredName(event.entityPlayer);
    }

    protected String getPreferredName(EntityPlayer player) {
        final Character c = CharacterAttribute.require(player);
        return c.name.isEmpty() ? player.getCommandSenderName() : c.name;
    }

    static int getSpeechRadius() {
        final int[] speechRadius = More.DEFINES.get().chat.speechRadius;
        final int mid = (speechRadius.length - 1) / 2;
        return speechRadius[mid];
    }
}
