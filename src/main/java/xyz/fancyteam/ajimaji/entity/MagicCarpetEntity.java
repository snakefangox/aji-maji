package xyz.fancyteam.ajimaji.entity;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
    private static final TrackedData<Boolean> GROUNDED =
        DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

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
        builder.add(GROUNDED, true);
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
            updateVelocity();
            move(MovementType.SELF, this.getVelocity());
        } else {
            setVelocity(Vec3d.ZERO);
        }

        if (getWorld().getTime() % 13 == 0) {checkPeriodic();}
    }

    private void checkPeriodic() {
        var onGround = !getWorld().isBlockSpaceEmpty(this, getBoundingBox().offset(0, -0.1, 0));
        dataTracker.set(GROUNDED, onGround);
    }

    private static final float VERTICAL_DEADZONE = 15F;

    private void updateVelocity() {
        if (hasControllingPassenger()) {
            LivingEntity controller = getControllingPassenger();
            assert controller != null;

            var riderInput = new Vec3d(controller.sidewaysSpeed, 0, controller.forwardSpeed).normalize();
            var movement = riderInput.rotateY((float) (-controller.getYaw() * (Math.PI / 180.0F)));
            if (controller.getPitch() < -VERTICAL_DEADZONE || controller.getPitch() > VERTICAL_DEADZONE) {
                movement =
                    movement.add(0, (controller.getPitch() / -(90F - VERTICAL_DEADZONE)) * controller.forwardSpeed, 0);
            }
            setVelocity(movement);

            float yawLerp = MathHelper.lerp(0.3F, getYaw(), controller.getYaw());
            setYaw(yawLerp);
        } else {
            boolean notSupportingWeight = getWorld().getOtherEntities(this, getBoundingBox().expand(0.5)).isEmpty();
            if (notSupportingWeight) {
                setVelocity(new Vec3d(0, -getFinalGravity(), 0));
            }
        }
    }

    @Override
    protected double getGravity() {
        return 0.04;
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

    public boolean isGrounded() {
        return this.dataTracker.get(GROUNDED);
    }
}
