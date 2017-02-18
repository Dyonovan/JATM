package com.teambrmodding.neotech.common.fluids;

import com.teambr.bookshelf.util.ClientUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * This file was created for NeoTech
 * <p>
 * NeoTech is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * @author Paul Davis - pauljoda
 * @since 2/15/2017
 */
public class FluidGas extends Fluid {
    private int color;

    /**
     * Creates the Fluid
     * @param fluidName The name
     * @param still     Still Texture
     * @param flowing   Flowing Texture
     */
    public FluidGas(int color, String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
        this.color = color;
    }

    /*******************************************************************************************************************
     * Fluid                                                                                                           *
     *******************************************************************************************************************/

    /**
     * Get the color for this fluid
     * @return The color
     */
    @Override
    public int getColor() {
        return color;
    }

    /**
     * Returns the localized name of this fluid.
     *
     * @param stack The stack
     */
    @Override
    public String getLocalizedName(FluidStack stack) {
        return ClientUtils.translate("fluid." + getName() + ".name");
    }
}

