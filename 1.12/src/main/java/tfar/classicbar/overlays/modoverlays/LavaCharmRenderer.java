package tfar.classicbar.overlays.modoverlays;

import baubles.api.BaublesApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static tfar.classicbar.ColorUtilities.cU;
import static tfar.classicbar.ModUtils.*;
import static tfar.classicbar.config.ModConfig.*;
import static tfar.classicbar.overlays.modoverlays.LavaCharmNoBaublesRenderer.ICON_LAVA;

/*
    Class handles the drawing of the lava charm
 */
//TODO Fix edge case of having random things but no baubles installed
public class LavaCharmRenderer {
  private final Minecraft mc = Minecraft.getMinecraft();
  public static final Item Lava_Charm = ForgeRegistries.ITEMS.getValue(new ResourceLocation("randomthings:lavacharm"));


  @Config.Name("Random Things Options")
  public static ConfigRandomThings configRandomThings = new ConfigRandomThings();

  public static class ConfigRandomThings {
    @Config.Name("Lava Bar Color")
    public String lavaBarColor = "#FF8000";
  }

  public LavaCharmRenderer() {
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void renderLavaBar(RenderGameOverlayEvent.Pre event) {

    Entity renderViewEnity = this.mc.getRenderViewEntity();
    if (event.isCanceled()
            || !(renderViewEnity instanceof EntityPlayer)) {
      return;
    }
    EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
    if (player.capabilities.isCreativeMode) return;
    int i1 = BaublesApi.isBaubleEquipped(player, Lava_Charm);
    ItemStack stack;
    if (i1 == -1) {
      stack = LavaCharmNoBaublesRenderer.getLavaCharm(player);
      if (stack == null) return;
    } else stack = BaublesApi.getBaublesHandler(player).getStackInSlot(i1);
    NBTTagCompound nbt = stack.getTagCompound();
    if (nbt == null) {
      System.out.println("error");
      return;
    }
    int charge = nbt.getInteger("charge");
    int scaledWidth = event.getResolution().getScaledWidth();
    int scaledHeight = event.getResolution().getScaledHeight();
    //Push to avoid lasting changes

    int absorb = MathHelper.ceil(player.getAbsorptionAmount());

    int xStart = scaledWidth / 2 - 91;
    int yStart = scaledHeight - 49;
    if (absorb > 0) yStart -= 10;
    if (player.getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue() > 0) yStart -= 10;
    mc.profiler.startSection("charge");
    //GlStateManager.pushMatrix();
    GlStateManager.enableBlend();

    //Bind our Custom bar
    mc.getTextureManager().bindTexture(ICON_BAR);
    //Bar background
    GlStateManager.color(1, 1, 1, 1);
    drawTexturedModalRect(xStart, yStart, 0, 0, 81, 9, general.style, false, true);

    //Pass 1, draw bar portion
    cU.color2Gl(cU.hex2Color(configRandomThings.lavaBarColor));
    //calculate bar color
    //draw portion of bar based on charge amount
    drawTexturedModalRect(xStart + 1, yStart + 1, 1, 10, getWidth(charge, 200), 7, general.style, true, true);
    int i2 = charge;
    //draw charge amount
    if (numbers.showPercent) i2 /= 3;
    int i3 = getStringLength(i2 + "");
    int i4 = (general.displayIcons) ? 1 : 0;
    int c = Integer.decode(configRandomThings.lavaBarColor);
    drawStringOnHUD(i2 + "", xStart - 9 * i4 - i3 + leftTextOffset, yStart - 1, c);

    mc.getTextureManager().bindTexture(ICON_LAVA);
    GlStateManager.color(1, 1, 1, 1);

    if (general.displayIcons)
      //Draw charge icon
      drawTexturedModalRect(xStart - 10, yStart, 1, 1, 9, 9, 0, false, true);
    //Reset back to normal settings

    mc.getTextureManager().bindTexture(ICON_VANILLA);

    GuiIngameForge.left_height += 10;
    //GlStateManager.disableBlend();
    //Revert our state back
    //GlStateManager.popMatrix();
    mc.profiler.endSection();
  }

}