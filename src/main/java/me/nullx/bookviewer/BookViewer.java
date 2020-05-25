package me.nullx.bookviewer;

import me.nullx.bookviewer.listener.MouseInputListener;
import me.nullx.bookviewer.uiscreen.GuiScreenUnclickableBook;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class BookViewer extends LabyModAddon {

    private String commandName;

    @Override
    public void onEnable() {
        getApi().getEventManager().register(this::handleMessage);
        getApi().registerForgeListener(new MouseInputListener());
    }

    public static void openBook(ItemStack book) {
        new Thread(() -> {
            try {
                Thread.sleep(10);
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenUnclickableBook(book));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean handleMessage(String message) {
        if (commandName.equalsIgnoreCase(message.split(" ")[0])) {
            ItemStack itemStack = Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem();
            if (itemStack != null && itemStack.getItem() == Items.written_book) {
                openBook(itemStack);
            } else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("You need to hold a written book."));
            }
            return true;
        }
        return false;
    }

    private void loadCommandPrefix() {
        if (!getConfig().has("command_name")) {
            getConfig().addProperty("command_name", "/readbook");
        }
        commandName = getConfig().get("command_name").getAsString();
    }

    @Override
    public void loadConfig() {
        loadCommandPrefix();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new StringElement("Command", this, new ControlElement.IconData(Material.BOOK), "command_name", commandName));
    }
}
