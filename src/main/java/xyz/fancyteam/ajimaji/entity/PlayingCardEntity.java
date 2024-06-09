package xyz.fancyteam.ajimaji.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import xyz.fancyteam.ajimaji.item.AMItems;

public class PlayingCardEntity extends PersistentProjectileEntity {

    public static final TrackedData<Integer> VARIANT = DataTracker.registerData(PlayingCardEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> STATE = DataTracker.registerData(PlayingCardEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // TODO: Account for potion effects
    private final int fallingAge = 7;
    private final float initialSpeed = 2.0f;

    public Vec3d initialVelocity;

    protected PlayingCardEntity(EntityType<? extends PlayingCardEntity> entityType, World world) {
        super(entityType, world);
    }

    public PlayingCardEntity(World world, LivingEntity owner, @Nullable ItemStack shotFrom) {
        super(AMEntities.PLAYING_CARD, owner, world,AMItems.CARD_DECK.getDefaultStack(), shotFrom);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
        builder.add(STATE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (getState() == State.GROUNDED) {
            return;
        }
        if (age > fallingAge && getState() == State.FLYING) {
            setState(State.FALLING);
        }
        else if (getState() == State.FALLING && getVelocity().lengthSquared() > 0.2f && !isOnGround()) {
            setVelocity(getVelocity().multiply(0.95f));
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        setState(State.GROUNDED);
    }

    @Override
    protected float getVelocityMultiplier() {
        return Math.min(1.0f - Math.max((age - fallingAge) * 0.1f, 0.0f), 0.2f);
    }

    @Override
    protected double getGravity() {
        return getState() == State.FALLING ? super.getGravity() * 0.5f : 0.0;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return AMItems.CARD_DECK.getDefaultStack();
    }

    // TODO: Custom hit sound
    @Override
    protected SoundEvent getHitSound() {
        return super.getHitSound();
    }

    public int getVariant() {
        return getDataTracker().get(VARIANT);
    }

    public void setVariant(int variant) {
        getDataTracker().set(VARIANT, variant);
    }

    public int getStateValue() {
        return getDataTracker().get(STATE);
    }

    public State getState() {
        return State.values()[getStateValue()];
    }

    public void setStateValue(int state) {
        getDataTracker().set(STATE, state);
    }

    public void setState(State state) {
        setStateValue(state.ordinal());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setVariant(nbt.getInt("card_variant"));
        setStateValue(nbt.getInt("card_state"));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("card_variant", getVariant());
        nbt.putInt("card_state", getStateValue());
        return super.writeNbt(nbt);
    }

    public enum State {
        FLYING,
        FALLING,
        GROUNDED
    }
}
