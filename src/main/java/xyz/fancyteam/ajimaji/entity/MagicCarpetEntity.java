package xyz.fancyteam.ajimaji.entity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.component.MagicCarpetComponent;
import xyz.fancyteam.ajimaji.item.AMItems;
import xyz.fancyteam.ajimaji.util.EnchantmentUtils;

import java.util.List;
import java.util.UUID;

public class MagicCarpetEntity extends Entity implements JumpingMount {
    private static final TrackedData<Integer> GROUNDED_TICKS =
        DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SPEED =
        DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SIZE =
        DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<ItemStack> MAP =
        DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final float VERTICAL_DEADZONE = 25F;
    public static final int MAX_GROUNDED_TICKS = 20;

    private UUID owner;
    private int lerpTicks;
    private int rescueBlanket;
    private boolean fourWinds;
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
        var windSpeed = EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.WIND_SPEED_KEY);
        var broad = EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.BROAD_KEY);
        dataTracker.set(SPEED, stack.getEnchantments().getLevel(windSpeed));
        dataTracker.set(SIZE, stack.getEnchantments().getLevel(broad));
        rescueBlanket = stack.getEnchantments()
            .getLevel(EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.RESCUE_BLANKET_KEY));
        fourWinds = stack.getEnchantments()
            .getLevel(EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.FOUR_WINDS)) > 0;
    }

    public ItemStack writeDataToItemStack() {
        ItemStack stack = new ItemStack(AMItems.MAGIC_CARPET);
        var nbt = new NbtCompound();
        writeCustomDataToNbt(nbt);
        stack.set(AMDataComponents.MAGIC_CARPET_DATA, new MagicCarpetComponent(owner));
        int speed = getSpeed();
        int size = getSize();
        if (speed > 0) {
            stack.addEnchantment(EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.WIND_SPEED_KEY),
                speed);
        }
        if (size > 0) {
            stack.addEnchantment(EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.BROAD_KEY), size);
        }
        if (rescueBlanket > 0) {
            stack.addEnchantment(EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.RESCUE_BLANKET_KEY),
                rescueBlanket);
        }
        if (fourWinds) {
            stack.addEnchantment(EnchantmentUtils.getEnchantmentEntry(getWorld(), EnchantmentUtils.FOUR_WINDS),
                1);
        }

        return stack;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(GROUNDED_TICKS, MAX_GROUNDED_TICKS);
        builder.add(SPEED, 0);
        builder.add(SIZE, 0);
        builder.add(MAP, ItemStack.EMPTY);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("owner")) {
            owner = nbt.getUuid("owner");
        }
        dataTracker.set(SPEED, nbt.getInt("speed"));
        dataTracker.set(SIZE, nbt.getInt("size"));
        rescueBlanket = nbt.getInt("rescueBlanket");
        fourWinds = nbt.getBoolean("fourWinds");
        var res = ItemStack.CODEC.decode(NbtOps.INSTANCE, nbt.get("map"));
        if (res.hasResultOrPartial())
            dataTracker.set(MAP, res.getOrThrow().getFirst());
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (hasOwner()) {
            nbt.putUuid("owner", getOwner());
        }
        nbt.putInt("speed", getSpeed());
        nbt.putInt("size", getSize());
        nbt.putInt("rescueBlanket", rescueBlanket);
        nbt.putBoolean("fourWinds", fourWinds);
        if (!getMap().isEmpty()) {
            var map = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, getMap()).getOrThrow();
            nbt.put("map", map);
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

        if (fourWinds) {
            if (player.getStackInHand(hand).getItem() == Items.FILLED_MAP && getMap().isEmpty()) {
                dataTracker.set(MAP, player.getStackInHand(hand).copyWithCount(1));
                player.getStackInHand(hand).decrement(1);
                return ActionResult.SUCCESS;
            } else if (player.getStackInHand(hand).isEmpty() && player.isSneaking() && !getMap().isEmpty()) {
                player.setStackInHand(hand, getMap());
                dataTracker.set(MAP, ItemStack.EMPTY);
                return ActionResult.SUCCESS;
            }
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

        if (!getWorld().isClient && hasControllingPassenger()) {
            var entities = getWorld().getOtherEntities(this, getBoundingBox().expand(0.2));
            for (var e : entities) {
                if (!e.hasVehicle() && !(e instanceof PlayerEntity)) e.startRiding(this);
            }
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

        float fwSpeed = controller.forwardSpeed;
        var riderInput = new Vec3d(controller.sidewaysSpeed / 2F, 0, fwSpeed > 0F ? fwSpeed : fwSpeed / 2F);
        var movement = riderInput.rotateY((float) (-controller.getYaw() * (Math.PI / 180.0F)));
        if (controller.getPitch() < -VERTICAL_DEADZONE || controller.getPitch() > VERTICAL_DEADZONE) {
            movement = movement.add(0, (controller.getPitch() / -(90F - VERTICAL_DEADZONE)) * fwSpeed, 0);
        }
        return movement;
    }

    @Override
    protected double getGravity() {
        return 0.4;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (SIZE.equals(data)) {
            this.calculateDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        var size = getSize();
        var sizeScale = (size * 0.5F);
        var scaledDim = super.getDimensions(pose).scaled(sizeScale + 1F, 1);

        var attachmentBuilder = EntityAttachments.builder();
        for (int i = 0; i <= size; i++) {
            var t = (i / (size + 1F)) * Math.PI * 2.0;
            var radius = sizeScale * 0.75F;
            var x = Math.sin(t) * radius;
            var z = Math.cos(t) * radius;
            attachmentBuilder.add(EntityAttachmentType.PASSENGER, (float) x, scaledDim.height(), (float) z);
        }

        return scaledDim.withAttachments(attachmentBuilder);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < getSize() + 1;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return getPassengerRidingPos(passenger);
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

    public int getSize() {
        return this.dataTracker.get(SIZE);
    }

    public ItemStack getMap() {
        return this.dataTracker.get(MAP);
    }

    @Override
    public boolean canJump() {
        int groundedTicks = getGroundedTicks();
        return (groundedTicks == 0 || groundedTicks == MAX_GROUNDED_TICKS) && launchHeight == 0F;
    }

    @Override
    public void setJumpStrength(int strength) {
        startLaunch(strength / 2);
    }

    @Override
    public void startJumping(int height) {
        startLaunch(height / 20);
    }

    public void startLaunch(int height) {
        if (isLogicalSideForUpdatingMovement()) {
            float dir = getGroundedTicks() > 0 ? 1F : -1F;
            launchHeight = height * dir;
        }
    }

    @Override
    public void stopJumping() {}
}
