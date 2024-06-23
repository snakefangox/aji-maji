package xyz.fancyteam.ajimaji.entity;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

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
        }

        if (getWorld().isClient) {
            return ActionResult.SUCCESS;
        }

        if (!checkOwnerAndNotify(player)) {
            return ActionResult.PASS;
        }

        if (player.getStackInHand(hand).isEmpty() && player.isSneaking()) {
            var stack = writeDataToItemStack();
            player.setStackInHand(hand, stack);
            discard();

            return ActionResult.SUCCESS;
        }

        if (!player.isSneaking()) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        }

        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (getFirstPassenger() instanceof LivingEntity livingPassenger) return livingPassenger;

        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (isLogicalSideForUpdatingMovement()) {
            updateTrackedPosition(getX(), getY(), getZ());
        }

        if (isLogicalSideForUpdatingMovement()) {
            updateVelocity();
            move(MovementType.SELF, this.getVelocity());
        } else {
            setVelocity(Vec3d.ZERO);
        }
    }

    private void updateVelocity() {
        if (hasControllingPassenger()) {
            LivingEntity controller = getControllingPassenger();
            assert controller != null;

            var riderInput = new Vec3d(controller.sidewaysSpeed, 0, controller.forwardSpeed).normalize();
            var movement = riderInput.rotateY((float) (-controller.getYaw() * (Math.PI / 180.0F)));
            if (controller.getPitch() < -15.0 || controller.getPitch() > 15.0) {
                movement = movement.add(0, (controller.getPitch() / -75F) * controller.forwardSpeed, 0);
            }
            setVelocity(movement);
        }
    }

    private boolean checkOwnerAndNotify(PlayerEntity player) {
        if (!player.getUuid().equals(getOwner())) {
            player.sendMessage(AjiMaji.tt("message", "not_carpet_owner"), true);
            return false;
        }
        return true;
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
