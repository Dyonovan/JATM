package com.teambrmodding.neotech.common.tile;

import com.teambr.nucleus.util.EnergyUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;

/**
 * This file was created for NeoTech
 * <p>
 * NeoTech is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * @author Paul Davis - pauljoda
 * @since 9/18/19
 */
public abstract class MachineGenerator extends AbstractMachine {

    /*******************************************************************************************************************
     * Global Variables                                                                                                *
     *******************************************************************************************************************/

    // Burn time sync variable
    public static final int BURN_TIME    = 40;

    // Current burn sync variable
    public static final int CURRENT_BURN = 41;

    /*******************************************************************************************************************
     * Variables                                                                                                       *
     *******************************************************************************************************************/

    // Burn variables
    protected int burnTime, currentObjectBurnTime = 0;

    // If we did something recently
    protected boolean didWork = false;

    /**
     * Main constructor, load things needed here
     *
     * @param type The registered type
     */
    public MachineGenerator(TileEntityType<?> type) {
        super(type);
    }

    /*******************************************************************************************************************
     * Abstract Methods                                                                                                *
     *******************************************************************************************************************/

    /**
     * Called to tick generation. This is where you add power to the generator
     */
    public abstract void generate();

    /**
     * Called per tick to manage burn time. You can do nothing here if there is nothing to generate. You should decrease burn time here
     * You should be handling checks if burnTime is 0 in this method, otherwise the tile won't know what to do
     *
     * @return True if able to continue generating
     */
    public abstract boolean manageBurnTime();

    /**
     * This method handles how much energy to produce per tick
     *
     * @return How much energy to produce per tick
     */
    public abstract int getEnergyProduced();

    /*******************************************************************************************************************
     * Generator Methods                                                                                               *
     *******************************************************************************************************************/

    /**
     * Used to actually do the processes needed. For processors this should be cooking items and generators should
     * generate RF. This is called every tick allowed, provided redstone mode requirements are met
     */
    private boolean needsUpdate = true;
    @Override
    protected void doWork() {
        didWork = burnTime == 1;

        // Transfer
        if(energyStorage.getEnergyStored() > 0) {
            getCapability(CapabilityEnergy.ENERGY, null).ifPresent(capability -> {
                EnergyUtils.distributePowerToFaces(capability, world, pos,
                        energyStorage.getMaxExtract(), false);
            });
        }

        // Generate
        if(manageBurnTime()) {
            sendValueToClient(CURRENT_BURN, currentObjectBurnTime);
            generate();
            didWork = true;
            needsUpdate = true; // We are processing, so we need to make sure to mark updated needed when done
        } else
            reset();

        if(didWork)
            sendValueToClient(BURN_TIME, burnTime);

        // Update renderer
        if(burnTime == 0 && needsUpdate) {
            markForUpdate(3);
            needsUpdate = false; // Only called on first idle tick
        }
    }

    /**
     * Use this to set all variables back to the default values, usually means the operation failed
     */
    @Override
    public void reset() {
        burnTime = 0;
        currentObjectBurnTime = 0;
    }

    /**
     * Used to check if this tile is active or not
     *
     * @return True if active state
     */
    @Override
    public boolean isActive() {
        return burnTime > 0 && super.isActive();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("BurnTime", burnTime);
        compound.putInt("CurrentObjectBurnTime", currentObjectBurnTime);
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        burnTime              = compound.getInt("BurnTime");
        currentObjectBurnTime = compound.getInt("CurrentObjectBurnTime");
    }

    /**
     * Client side method to get how far along this process is to a scale variable
     *
     * @param scaleVal What scale to move to, usually pixels
     * @return What value on new scale this is complete
     */
    @OnlyIn(Dist.CLIENT)
    public int getBurnProgressScaled(int scaleVal) {
        return (int) ((burnTime * scaleVal) / Math.max(currentObjectBurnTime, 0.001));
    }

    /*******************************************************************************************************************
     * Energy Methods                                                                                                  *
     *******************************************************************************************************************/

    /**
     * Used to define the default size of this energy bank
     *
     * @return The default size of the energy bank
     */
    @Override
    public int getDefaultEnergyStorageSize() {
        return 32000;
    }

    /**
     * Is this tile an energy provider
     *
     * @return True to allow energy out
     */
    @Override
    protected boolean isProvider() {
        return true;
    }

    /**
     * Is this tile an energy reciever
     *
     * @return True to accept energy
     */
    @Override
    protected boolean isReceiver() {
        return false;
    }

    /*******************************************************************************************************************
     * Inventory Methods                                                                                               *
     *******************************************************************************************************************/

    /**
     * Get the slots for the given face
     *
     * @param face The face
     * @return What slots can be accessed
     */
    @Override
    public int[] getSlotsForFace(Direction face) {
        if(isDisabled(face))
            return new int[] {};
        switch (face) {
            case UP:
                return getInputSlots(getModeForSide(face));
            case DOWN:
                return getOutputSlots(getModeForSide(face));
            default:
                int[] inputSlots  = getInputSlots(getModeForSide(face));
                int[] outputSlots = getOutputSlots(getModeForSide(face));
                int[] combinedInOut = new int[inputSlots.length + outputSlots.length];
                System.arraycopy(inputSlots,  0, combinedInOut, 0, inputSlots.length);
                System.arraycopy(outputSlots, 0, combinedInOut, inputSlots.length, outputSlots.length);
                return combinedInOut;
        }
    }

    /*******************************************************************************************************************
     * Syncable                                                                                                        *
     *******************************************************************************************************************/

    /**
     * Used to set the variable for this tile, the Syncable will use this when you send a value to the server
     *
     * @param id    The ID of the variable to send
     * @param value The new value to set to (you can use this however you want, eg using the ordinal of EnumFacing)
     */
    @Override
    public void setVariable(int id, double value) {
        switch (id) {
            case BURN_TIME:
                this.burnTime = (int) value;
                return;
            case CURRENT_BURN:
                this.currentObjectBurnTime = (int) value;
                return;
            default:
        }
        super.setVariable(id, value);
    }
}
