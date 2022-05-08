/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.monitor;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.TileEntityMonitorRenderer;
import net.fabricmc.loader.api.FabricLoader;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * The render type to use for monitors.
 *
 * @see TileEntityMonitorRenderer
 * @see ClientMonitor
 */
public enum MonitorRenderer
{
    /**
     * Determine the best monitor backend.
     */
    BEST,

    /**
     * Render using texture buffer objects.
     *
     * @see org.lwjgl.opengl.GL31#glTexBuffer(int, int, int)
     */
    TBO,

    /**
     * Render using VBOs.
     *
     * @see com.mojang.blaze3d.vertex.VertexBuffer
     */
    VBO;

    /**
     * Get the current renderer to use.
     *
     * @return The current renderer. Will not return {@link MonitorRenderer#BEST}.
     */
    @Nonnull
    public static MonitorRenderer current()
    {
        MonitorRenderer current = ComputerCraft.monitorRenderer;
        if( current == BEST ) current = ComputerCraft.monitorRenderer = best();
        return current;
    }

    private static MonitorRenderer best()
    {
        if( !GL.getCapabilities().OpenGL31 )
        {
            ComputerCraft.log.warn( "Texture buffers are not supported on your graphics card. Falling back to VBO monitor renderer." );
            return VBO;
        }

        if( shaderMod )
        {
            ComputerCraft.log.warn( "Shader mod detected. Falling back to VBO monitor renderer." );
            return VBO;
        }

        return TBO;
    }

    private static final List<String> shaderModIds = Arrays.asList( "iris", "canvas", "optifabric" );
    private static boolean shaderMod = FabricLoader.getInstance().getAllMods().stream()
        .map( modContainer -> modContainer.getMetadata().getId() )
        .anyMatch( shaderModIds::contains );

    public static final boolean canvasModPresent = FabricLoader.getInstance().isModLoaded( "canvas" );
}
