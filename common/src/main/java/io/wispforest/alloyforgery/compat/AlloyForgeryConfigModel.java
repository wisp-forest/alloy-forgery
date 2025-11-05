package io.wispforest.alloyforgery.compat;

import io.wispforest.owo.config.annotation.*;
import io.wispforest.alloyforgery.AlloyForgery;

@Modmenu(modId = AlloyForgery.MOD_ID)
@Config(name = AlloyForgery.MOD_ID, wrapperName = "AlloyForgeryConfig")
public class AlloyForgeryConfigModel {

    public boolean strictRecipeChecks = true;

    @SectionHeader("blasting_adaption")
    public boolean allowHigherTierOutput = true;
    public boolean allowBlastingFurnaceAdaption = true;

    public int baseInputAmount = 2;

    public int higherTierOutputIncrease = 1;

    @SectionHeader("screen")
    public boolean darkModeTheme = false;
}
