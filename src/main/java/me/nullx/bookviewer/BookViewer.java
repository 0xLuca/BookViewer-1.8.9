package me.nullx.bookviewer;

import me.nullx.bookviewer.uiscreen.GuiScreenNewBook;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class BookViewer extends LabyModAddon {

    private String commandName;

    @Override
    public void onEnable() {
        getApi().getEventManager().register(this::handleMessage);
    }

    private boolean handleMessage(String message) {
        if (commandName.equalsIgnoreCase(message.split(" ")[0])) {
            ItemStack itemStack = Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem();
            if (itemStack != null && itemStack.getItem() == Items.written_book) {
                new Thread(() -> {
                    NBTTagList cmp = itemStack.getTagCompound().getTagList("pages", 8);
                    String[] pages = new String[cmp.tagCount()];
                    for (int i = 0; i < cmp.tagCount(); i++) {
                        NBTBase base = cmp.get(i);
                        if (base instanceof NBTTagString) {
                            NBTTagString s = (NBTTagString) base;
                            pages[i] = s.getString().substring(1, s.getString().length() - 1);
                        }
                    }
                    try {
                        Thread.sleep(10);
                        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenNewBook(pages));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("You need to hold a book."));
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
