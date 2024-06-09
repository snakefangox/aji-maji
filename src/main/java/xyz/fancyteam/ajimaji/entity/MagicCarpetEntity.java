package xyz.fancyteam.ajimaji.entity;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.item.AMItems;

import java.util.UUID;

public class MagicCarpetEntity extends Entity {
    private UUID owner;

    public MagicCarpetEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public void readDataFromItemStack(ItemStack stack) {
        stack.getOrDefault(AMDataComponents.MAGIC_CARPET_DATA, NbtComponent.DEFAULT).apply(this::readCustomDataFromNbt);
    }

    public ItemStack writeDataToItemStack() {
        ItemStack stack = new ItemStack(AMItems.MAGIC_CARPET);
        var nbt = new NbtCompound();
        writeCustomDataToNbt(nbt);
        stack.set(AMDataComponents.MAGIC_CARPET_DATA, NbtComponent.of(nbt));
        return stack;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("owner")) {
            owner = nbt.getUuid("owner");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (hasOwner()) {
            nbt.putUuid("owner", getOwner());
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ActionResult actionResult = super.interact(player, hand);

        if (actionResult != ActionResult.PASS) {
            return actionResult;
        } else if (!getWorld().isClient && player.isSneaking() && player.getStackInHand(hand).isEmpty()) {
            tryPickUpCarpet(player, hand);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private void tryPickUpCarpet(PlayerEntity player, Hand hand) {
        if (!player.getUuid().equals(getOwner())) {
            player.sendMessage(AjiMaji.tt("message", "not_carpet_owner"), true);
        } else {
            var stack = writeDataToItemStack();
            player.setStackInHand(hand, stack);
            discard();
        }
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean collidesWith(Entity other) {
        return (other.isCollidable() || other.isPushable()) && !isConnectedThroughVehicle(other);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
