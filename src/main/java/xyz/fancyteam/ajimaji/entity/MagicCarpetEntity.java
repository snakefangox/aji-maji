package xyz.fancyteam.ajimaji.entity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.component.MagicCarpetComponent;
import xyz.fancyteam.ajimaji.item.AMItems;

import java.util.UUID;

public class MagicCarpetEntity extends Entity implements JumpingMount {
    private static final TrackedData<Integer> GROUNDED_TICKS =
        DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SPEED =
        DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final float VERTICAL_DEADZONE = 25F;
    public static final int MAX_GROUNDED_TICKS = 20;
    private static final RegistryKey<Enchantment> WIND_SPEED_KEY =
        RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(AjiMaji.MOD_ID, "wind_speed"));

    private UUID owner;
    private int lerpTicks;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private float lerpYaw;
    private float launchHeight = 0F;

    public MagicCarpetEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public void readDataFromItemStack(ItemStack stack) {
        owner = stack.getOrDefault(AMDataComponents.MAGIC_CARPET_DATA, MagicCarpetComponent.DEFAULT).owner();
        var windSpeed = getWindSpeedEntry();
        dataTracker.set(SPEED, stack.getEnchantments().getLevel(windSpeed));
    }

    public ItemStack writeDataToItemStack() {
        ItemStack stack = new ItemStack(AMItems.MAGIC_CARPET);
        var nbt = new NbtCompound();
        writeCustomDataToNbt(nbt);
        stack.set(AMDataComponents.MAGIC_CARPET_DATA, new MagicCarpetComponent(owner));
        int speed = getSpeed();
        if (speed > 0) {stack.addEnchantment(getWindSpeedEntry(), speed);}
        return stack;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(GROUNDED_TICKS, MAX_GROUNDED_TICKS);
        builder.add(SPEED, 0);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("owner")) {
            owner = nbt.getUuid("owner");
        }
        dataTracker.set(SPEED, nbt.getInt("speed"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (hasOwner()) {
            nbt.putUuid("owner", getOwner());
        }
        nbt.putInt("speed", getSpeed());
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
            lerpTicks = 0;
            updateTrackedPosition(getX(), getY(), getZ());
        }

        if (lerpTicks > 0) {
            lerpPosAndRotation(lerpTicks, lerpX, lerpY, lerpZ, lerpYaw, getPitch());
            --lerpTicks;
        }

        if (isLogicalSideForUpdatingMovement()) {
            updateTrackedPosition(getX(), getY(), getZ());
            updateVelocity();
            move(MovementType.SELF, getVelocity());
        } else {
            setVelocity(Vec3d.ZERO);
        }

        var onGround = !getWorld().isBlockSpaceEmpty(this, getBoundingBox().offset(0, -0.1, 0));
        int groundedTicks = getGroundedTicks();
        if (onGround) {
            if (groundedTicks < MAX_GROUNDED_TICKS) dataTracker.set(GROUNDED_TICKS, getGroundedTicks() + 1);
        } else {
            if (groundedTicks > 0) dataTracker.set(GROUNDED_TICKS, getGroundedTicks() - 1);
        }
    }

    private void updateVelocity() {
        float speedMulti = (getSpeed() + 1) / 3F;

        if (launchHeight != 0F) {
            float launchSpeed = speedMulti * 5F * MathHelper.sign(launchHeight);
            float remainingHeight = launchHeight - launchSpeed;
            if (MathHelper.sign(launchSpeed) != MathHelper.sign(remainingHeight)) remainingHeight = 0F;
            launchHeight = remainingHeight;
            setVelocity(0, launchSpeed, 0);
        } else if (hasControllingPassenger()) {
            LivingEntity controller = getControllingPassenger();
            assert controller != null;

            var movement = getControllerMovementDirection(controller);
            setVelocity(movement.normalize().multiply(speedMulti));

            float yawLerp = MathHelper.lerp(0.3F, getYaw(), controller.getYaw());
            setYaw(yawLerp);
        } else {
            boolean notSupportingWeight = getWorld().getOtherEntities(this, getBoundingBox().expand(0, 1, 0)).isEmpty();
            setVelocity(new Vec3d(0, notSupportingWeight ? -getFinalGravity() : 0, 0));
        }
    }

    private Vec3d getControllerMovementDirection(LivingEntity controller) {
        assert controller != null;

        if (getGroundedTicks() > 0) return Vec3d.ZERO;

        var riderInput = new Vec3d(controller.sidewaysSpeed / 2F, 0, controller.forwardSpeed);
        var movement = riderInput.rotateY((float) (-controller.getYaw() * (Math.PI / 180.0F)));
        if (controller.getPitch() < -VERTICAL_DEADZONE || controller.getPitch() > VERTICAL_DEADZONE) {
            movement =
                movement.add(0, (controller.getPitch() / -(90F - VERTICAL_DEADZONE)) * controller.forwardSpeed, 0);
        }
        return movement;
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    @Override
    public double getLerpTargetX() {
        return lerpTicks > 0 ? lerpX : getX();
    }

    @Override
    public double getLerpTargetY() {
        return lerpTicks > 0 ? lerpY : getY();
    }

    @Override
    public double getLerpTargetZ() {
        return lerpTicks > 0 ? lerpZ : getZ();
    }

    @Override
    public float getLerpTargetYaw() {
        return lerpTicks > 0 ? lerpYaw : getYaw();
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch,
                                               int interpolationSteps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYaw = yaw;
        lerpTicks = 10;
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

    public int getGroundedTicks() {
        return this.dataTracker.get(GROUNDED_TICKS);
    }

    public int getSpeed() {
        return this.dataTracker.get(SPEED);
    }

    private RegistryEntry.Reference<Enchantment> getWindSpeedEntry() {
        return getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(WIND_SPEED_KEY);
    }

    @Override
    public boolean canJump() {
        int groundedTicks = getGroundedTicks();
        return (groundedTicks == 0 || groundedTicks == MAX_GROUNDED_TICKS) && launchHeight == 0F;
    }

    @Override
    public void setJumpStrength(int strength) {
        startLaunch(strength * 10);
    }

    @Override
    public void startJumping(int height) {
        startLaunch(height);
    }

    private void startLaunch(int height) {
        if (isLogicalSideForUpdatingMovement()) {
            float dir = getGroundedTicks() > 0 ? 1F : -1F;
            launchHeight = height / 20F * dir;
        }
    }

    @Override
    public void stopJumping() {}
}
