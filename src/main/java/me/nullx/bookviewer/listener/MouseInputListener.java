package me.nullx.bookviewer.listener;

import me.nullx.bookviewer.BookViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class MouseInputListener {

    @SubscribeEvent
    public void onInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack currentItem = mc.thePlayer.inventory.getCurrentItem();
        if (mc.gameSettings.keyBindUseItem.isPressed() && mc.currentScreen == null && currentItem != null && currentItem.getItem() == Items.written_book) {
            BookViewer.openBook(currentItem);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }
    }

}