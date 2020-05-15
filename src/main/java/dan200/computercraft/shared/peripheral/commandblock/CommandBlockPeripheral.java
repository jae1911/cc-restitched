/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.commandblock;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.CapabilityUtil;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL;

@Mod.EventBusSubscriber
public class CommandBlockPeripheral implements IPeripheral, ICapabilityProvider
{
    private static final ResourceLocation CAP_ID = new ResourceLocation( ComputerCraft.MOD_ID, "command_block" );

    private final CommandBlockTileEntity commandBlock;
    private LazyOptional<IPeripheral> self;

    public CommandBlockPeripheral( CommandBlockTileEntity commandBlock )
    {
        this.commandBlock = commandBlock;
    }

    @Nonnull
    @Override
    public String getType()
    {
        return "command";
    }

    @LuaFunction( mainThread = true )
    public final String getCommand()
    {
        return commandBlock.getCommandBlockLogic().getCommand();
    }

    @LuaFunction( mainThread = true )
    public final void setCommand( String command )
    {
        commandBlock.getCommandBlockLogic().setCommand( command );
        commandBlock.getCommandBlockLogic().updateCommand();
    }

    @LuaFunction( mainThread = true )
    public final Object runCommand()
    {
        commandBlock.getCommandBlockLogic().trigger( commandBlock.getWorld() );
        int result = commandBlock.getCommandBlockLogic().getSuccessCount();
        return result > 0 ? new Object[] { true } : new Object[] { false, "Command failed" };
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        return other != null && other.getClass() == getClass();
    }

    @Nonnull
    @Override
    public Object getTarget()
    {
        return commandBlock;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability( @Nonnull Capability<T> cap, @Nullable Direction side )
    {
        if( cap == CAPABILITY_PERIPHERAL )
        {
            if( self == null ) self = LazyOptional.of( () -> this );
            return self.cast();
        }
        return LazyOptional.empty();
    }

    private void invalidate()
    {
        self = CapabilityUtil.invalidate( self );
    }

    @SubscribeEvent
    public static void onCapability( AttachCapabilitiesEvent<TileEntity> event )
    {
        TileEntity tile = event.getObject();
        if( tile instanceof CommandBlockTileEntity )
        {
            CommandBlockPeripheral peripheral = new CommandBlockPeripheral( (CommandBlockTileEntity) tile );
            event.addCapability( CAP_ID, peripheral );
            event.addListener( peripheral::invalidate );
        }
    }
}
