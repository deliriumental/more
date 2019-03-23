package msifeed.mc.aorta.locks.items;

import msifeed.mc.aorta.genesis.AortaCreativeTab;
import msifeed.mc.aorta.locks.LockTileEntity;
import msifeed.mc.aorta.locks.LockType;
import msifeed.mc.aorta.sys.utils.L10n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class LockItem extends Item {
    static final String DEFAULT_DIGITAL_SECRET = "0000";
    private static final String ID_BASE = "lock_";
    private LockType type;

    public LockItem(LockType type) {
        this.type = type;

        setCreativeTab(AortaCreativeTab.LOCKS);
        setUnlocalizedName(getItemId(type));
        setTextureName("aorta:" + getItemId(type));
    }

    public LockType getLockType() {
        return type;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List lines, boolean advanced) {
        if (isBlank(itemStack))
            lines.add(L10n.tr("aorta.lock.blank_lock"));
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        final LockTileEntity lock = LockTileEntity.find(world, x, y, z);
        if (lock == null)
            return false;
        install(itemStack, lock, player, world);
        return true;
    }

    private void install(ItemStack itemStack, LockTileEntity lock, EntityPlayer player, World world) {
        if (world.isRemote)
            return;
        if (lock.hasLock() && lock.isLocked())
            return;

        final String messageId = lock.hasLock() ? "aorta.lock.replaced" : "aorta.lock.installed";
        itemStack.stackSize -= 1;

        if (lock.hasLock()) {
            player.inventory.addItemStackToInventory(lock.toItemStack());
        }

        if (isBlank(itemStack)) {
            final String secret = type == LockType.DIGITAL ? DEFAULT_DIGITAL_SECRET : String.valueOf(world.rand.nextInt());
            System.out.println("install client: " + world.isRemote + " sec: " + secret);
            lock.setSecret(secret);
            lock.setLockType(type);
            if (type != LockType.DIGITAL) {
                player.inventory.addItemStackToInventory(KeyItem.makeKeyItem(secret));
            }
        } else {
            // Used lock has no bonus key
            lock.fromItemStack(itemStack);
        }

        ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);

        if (!world.isRemote)
            player.addChatMessage(new ChatComponentTranslation(messageId));
    }

    private boolean isBlank(ItemStack itemStack) {
        return !itemStack.hasTagCompound() || itemStack.getTagCompound().getString("key").isEmpty();
    }

    public static String getItemId(LockType type) {
        return ID_BASE + type.toString().toLowerCase();
    }
}
