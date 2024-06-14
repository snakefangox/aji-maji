package xyz.fancyteam.ajimaji.block_entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.component.TopHatIdComponent;
import xyz.fancyteam.ajimaji.recipe.TopHatRecipe;
import xyz.fancyteam.ajimaji.recipe.TopHatRecipeInput;
import xyz.fancyteam.ajimaji.top_hat.TopHatManager;

public class TopHatBlockEntity extends BlockEntity {
    private List<ItemStack> items = new ArrayList<>();
    private UUID id = UUID.randomUUID();

    public TopHatBlockEntity(BlockPos pos, BlockState state) {
        super(AMBlockEntities.TOP_HAT, pos, state);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        items.clear();
        NbtList itemsList = nbt.getList("items", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : itemsList) {
            NbtCompound item = (NbtCompound) element;
            items.add(ItemStack.fromNbtOrEmpty(registryLookup, item));
        }

        id = nbt.getUuid("top_hat_id");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        NbtList itemsList = new NbtList();
        for (ItemStack stack : items) {
            itemsList.add(stack.encode(registryLookup));
        }
        nbt.put("items", itemsList);

        nbt.putUuid("top_hat_id", id);
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove("top_hat_id");
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        TopHatIdComponent component = components.get(AMDataComponents.TOP_HAT_ID);
        if (component != null) id = component.topHatId();
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(AMDataComponents.TOP_HAT_ID, new TopHatIdComponent(id));
    }

    public void insertItem(ItemStack stack) {
        assert world != null;

        items.add(stack);

        TopHatRecipeInput input = new TopHatRecipeInput(items, world.random);
        Optional<RecipeEntry<TopHatRecipe>> firstMatch =
            world.getRecipeManager().getFirstMatch(TopHatRecipe.TYPE, input, world);
        if (firstMatch.isPresent()) {
            TopHatRecipe recipe = firstMatch.get().value();
            for (ItemStack result : recipe.craft(input)) {
                dropStack(result);
            }
            items.clear();
        }
    }

    public void dropAllStacks() {
        for (ItemStack stack : items) {
            dropStack(stack);
        }
        items.clear();
    }

    public void insertEntity(Entity entity) {
        if (world instanceof ServerWorld world) {
            TopHatManager.insertEntity(world.getServer(), id, entity);
        }
    }

    private void dropStack(ItemStack stack) {
        assert world != null;
        if (!world.isClient) {
            Vec3d itemPos = Vec3d.of(pos).add(0.5, 11.0 / 16.0, 0.5);
            double direction = world.random.nextDouble() * Math.PI * 2.0;
            Vec3d itemVel =
                new Vec3d(Math.cos(direction) * 0.125, 0.375, -Math.sin(direction) * 0.125);
            ItemEntity entity =
                new ItemEntity(world, itemPos.x, itemPos.y, itemPos.z, stack.copy(), itemVel.x, itemVel.y, itemVel.z);
            entity.setPickupDelay(10);
            world.spawnEntity(entity);
        }
    }
}
