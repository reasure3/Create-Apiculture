package io.github.reasure3.compat.jei

import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import io.github.reasure3.CreateApiculture
import io.github.reasure3.content.hive.ReinforcedBeehiveConversion
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.recipe.RecipeType as JeiRecipeType
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeHolder

@JeiPlugin
class CreateApicultureJeiPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation =
        CreateApiculture.id("jei_plugin")

    override fun registerRecipes(registration: IRecipeRegistration) {
        registration.addRecipes(
            ITEM_APPLICATION,
            listOf(ReinforcedBeehiveConversion.createManualApplicationRecipe()),
        )
        registration.addRecipes(
            DEPLOYING,
            listOf(ReinforcedBeehiveConversion.createDisplayDeployerRecipe()),
        )
    }

    companion object {
        private val ITEM_APPLICATION: JeiRecipeType<RecipeHolder<ManualApplicationRecipe>> =
            JeiRecipeType.createRecipeHolderType(ResourceLocation.fromNamespaceAndPath("create", "item_application"))

        private val DEPLOYING: JeiRecipeType<RecipeHolder<DeployerApplicationRecipe>> =
            JeiRecipeType.createRecipeHolderType(ResourceLocation.fromNamespaceAndPath("create", "deploying"))
    }
}
