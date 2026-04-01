package com.minecraft.mod.ewhaw.menu;

import com.minecraft.mod.ewhaw.entity.AdventurerEntity;
import com.minecraft.mod.ewhaw.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

import net.minecraft.world.inventory.InventoryMenu;

public class AdventurerMenu extends AbstractContainerMenu {
    private final AdventurerEntity adventurer;

    public AdventurerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, (AdventurerEntity) playerInventory.player.level().getEntity(buffer.readInt()));
    }

    public AdventurerMenu(int containerId, Inventory playerInventory, AdventurerEntity adventurer) {
        super(ModMenuTypes.ADVENTURER_MENU.get(), containerId);
        this.adventurer = adventurer;

        // 1. Slots d'Armure (x=8)
        this.addSlot(new AdventurerEquipmentSlot(adventurer, net.minecraft.world.entity.EquipmentSlot.HEAD, 8, 8, 0)).setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
        this.addSlot(new AdventurerEquipmentSlot(adventurer, net.minecraft.world.entity.EquipmentSlot.CHEST, 8, 26, 1)).setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
        this.addSlot(new AdventurerEquipmentSlot(adventurer, net.minecraft.world.entity.EquipmentSlot.LEGS, 8, 44, 2)).setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
        this.addSlot(new AdventurerEquipmentSlot(adventurer, net.minecraft.world.entity.EquipmentSlot.FEET, 8, 62, 3)).setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);

        // 2. Mains (Main hand x=77, Off hand x=77)
        this.addSlot(new AdventurerEquipmentSlot(adventurer, net.minecraft.world.entity.EquipmentSlot.MAINHAND, 77, 44, 4));
        this.addSlot(new AdventurerEquipmentSlot(adventurer, net.minecraft.world.entity.EquipmentSlot.OFFHAND, 77, 62, 5)).setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);

        // 3. Sac à dos (x=116)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(adventurer.getAdventurerInventory(), j + i * 3, 116 + j * 18, 17 + i * 18));
            }
        }

        // 4. Inventaire du Joueur
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // Si on clique dans l'inventaire de l'aventurier (slots 0 à 14)
            if (index < 15) {
                if (!this.moveItemStackTo(itemstack1, 15, 51, true)) {
                    return ItemStack.EMPTY;
                }
            } 
            // Si on clique dans l'inventaire du joueur
            else if (!this.moveItemStackTo(itemstack1, 0, 15, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.adventurer != null && this.adventurer.isAlive() && this.adventurer.distanceTo(player) < 8.0F;
    }

    public AdventurerEntity getAdventurer() { return adventurer; }

    static class AdventurerEquipmentSlot extends Slot {
        private final AdventurerEntity adventurer;
        private final net.minecraft.world.entity.EquipmentSlot slotType;

        public AdventurerEquipmentSlot(AdventurerEntity adventurer, net.minecraft.world.entity.EquipmentSlot slotType, int x, int y, int index) {
            super(new SimpleContainer(1), index, x, y);
            this.adventurer = adventurer;
            this.slotType = slotType;
        }

        @Override
        public ItemStack getItem() {
            return adventurer.getItemBySlot(slotType);
        }

        @Override
        public void set(ItemStack stack) {
            adventurer.setItemSlot(slotType, stack.copy());
        }

        @Override
        public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {
            this.set(newStack);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return switch (slotType) {
                case HEAD -> stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor && armor.getType() == net.minecraft.world.item.ArmorItem.Type.HELMET;
                case CHEST -> stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor && armor.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE;
                case LEGS -> stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor && armor.getType() == net.minecraft.world.item.ArmorItem.Type.LEGGINGS;
                case FEET -> stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor && armor.getType() == net.minecraft.world.item.ArmorItem.Type.BOOTS;
                case MAINHAND, OFFHAND -> true;
                default -> false;
            };
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack current = adventurer.getItemBySlot(slotType);
            if (current.isEmpty()) return ItemStack.EMPTY;
            ItemStack removed = current.split(amount);
            adventurer.setItemSlot(slotType, current);
            return removed;
        }

        @Override
        public int getMaxStackSize() { return 64; }
    }
}
