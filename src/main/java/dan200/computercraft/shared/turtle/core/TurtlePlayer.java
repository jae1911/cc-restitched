/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.event.FakePlayer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.ItemStorage;
import dan200.computercraft.shared.util.WorldUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class TurtlePlayer extends FakePlayer {
    private static final GameProfile DEFAULT_PROFILE = new GameProfile(UUID.fromString("0d0c4ca0-4ff1-11e4-916c-0800200c9a66"), "[ComputerCraft]");

    private TurtlePlayer(World world) {
        super((ServerWorld) world, DEFAULT_PROFILE);
    }

    private TurtlePlayer(ITurtleAccess turtle) {
        super((ServerWorld) turtle.getWorld(), getProfile(turtle.getOwningPlayer()));
        this.setState(turtle);
    }

    private static GameProfile getProfile(@Nullable GameProfile profile) {
        return profile != null && profile.isComplete() ? profile : DEFAULT_PROFILE;
    }

    private void setState(ITurtleAccess turtle) {
        BlockPos position = turtle.getPosition();

        this.pos.add(0.5, 0.5, 0.5);

        this.yaw = turtle.getDirection()
                         .asRotation();
        this.pitch = 0.0f;

        this.inventory.clear();
    }

    public static TurtlePlayer get(ITurtleAccess access) {
        if (!(access instanceof TurtleBrain)) {
            return new TurtlePlayer(access);
        }

        TurtleBrain brain = (TurtleBrain) access;
        TurtlePlayer player = brain.m_cachedPlayer;
        if (player == null || player.getGameProfile() != getProfile(access.getOwningPlayer()) || player.getEntityWorld() != access.getWorld()) {
            player = brain.m_cachedPlayer = new TurtlePlayer(brain);
        } else {
            player.setState(access);
        }

        return player;
    }

    public void loadInventory(@Nonnull ItemStack currentStack) {
        // Load up the fake inventory
        this.inventory.selectedSlot = 0;
        this.inventory.setStack(0, currentStack);
    }

    public ItemStack unloadInventory(ITurtleAccess turtle) {
        // Get the item we placed with
        ItemStack results = this.inventory.getStack(0);
        this.inventory.setStack(0, ItemStack.EMPTY);

        // Store (or drop) anything else we found
        BlockPos dropPosition = turtle.getPosition();
        Direction dropDirection = turtle.getDirection()
                                        .getOpposite();
        ItemStorage storage = ItemStorage.wrap(turtle.getInventory());
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                ItemStack remainder = InventoryUtil.storeItems(stack, storage, turtle.getSelectedSlot());
                if (!remainder.isEmpty()) {
                    WorldUtil.dropItemStack(remainder, turtle.getWorld(), dropPosition, dropDirection);
                }
                this.inventory.setStack(i, ItemStack.EMPTY);
            }
        }
        this.inventory.markDirty();
        return results;
    }

    @Override
    public Vec3d getCameraPosVec(float float_1) {
        this.pos.subtract(0, this.getStandingEyeHeight(), 0);
        Vec3d r = super.getCameraPosVec(float_1);
        this.pos.add(0, this.getStandingEyeHeight(), 0);
        return r;
    }
}
