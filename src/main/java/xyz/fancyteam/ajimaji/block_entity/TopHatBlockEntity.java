package xyz.fancyteam.ajimaji.block_entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class TopHatBlockEntity extends BlockEntity {
    private List<ItemStack> items = new ArrayList<>();

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
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        NbtList itemsList = new NbtList();
        for (ItemStack stack : items) {
            itemsList.add(stack.encode(registryLookup));
        }
        nbt.put("items", itemsList);
    }

    @Override
    public void setStackNbt(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        super.setStackNbt(stack, registries);
    }

    public void insertItem(ItemStack stack) {

    }

    public void dropAllStacks() {

    }

    private void dropStack(ItemStack stack) {

    }
}
