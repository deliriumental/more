package msifeed.mc.aorta.locks;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import msifeed.mc.aorta.genesis.blocks.templates.DoorTemplate;
import msifeed.mc.aorta.locks.items.*;
import msifeed.mc.aorta.rpc.Rpc;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public enum Locks {
    INSTANCE;

    private static final HashFunction hasher = Hashing.murmur3_128(3364);
    private static final LocksRpc rpcHandler = new LocksRpc();

    public static void init() {
        LockType.locks().forEach(t -> GameRegistry.registerItem(new LockItem(t), LockItem.getItemId(t)));
        GameRegistry.registerItem(new BlankKeyItem(), BlankKeyItem.ID);
        GameRegistry.registerItem(new KeyItem(), KeyItem.ID);
        GameRegistry.registerItem(new LockpickItem(), LockpickItem.ID);
        GameRegistry.registerItem(new AccessTunerItem(), AccessTunerItem.ID);
        GameRegistry.registerItem(new SkeletalKeyItem(), SkeletalKeyItem.ID);
        GameRegistry.registerTileEntity(LockTileEntity.class, LockTileEntity.ID);
        GameRegistry.addRecipe(new CopyKeyRecipe());

        Rpc.register(rpcHandler);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.block instanceof DoorTemplate))
            return;

        final LockTileEntity lock = LockTileEntity.find(event.world, event.x, event.y, event.z);
        if (lock != null && lock.hasLock() && !lock.isLocked())
            event.world.spawnEntityInWorld(lock.makeEntityItem());
    }

    public static int makeKeyHash(String input) {
        return hasher.hashUnencodedChars(input).asInt();
    }
}
