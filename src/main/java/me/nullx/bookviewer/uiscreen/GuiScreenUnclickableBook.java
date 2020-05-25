package me.nullx.bookviewer.uiscreen;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class GuiScreenUnclickableBook extends GuiScreen {
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private final ItemStack bookObj;
    /**
     * Whether the book is signed or can still be edited
     */
    private final boolean bookIsUnsigned;
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private int bookTotalPages = 1;
    private int currPage;
    private NBTTagList bookPages;
    private List<IChatComponent> field_175386_A;
    private int field_175387_B = -1;
    private GuiScreenUnclickableBook.NextPageButton buttonNextPage;
    private GuiScreenUnclickableBook.NextPageButton buttonPreviousPage;
    private GuiButton buttonDone;

    public GuiScreenUnclickableBook(ItemStack book) {
        this.bookObj = book;
        this.bookIsUnsigned = true;

        if (book.hasTagCompound()) {
            NBTTagCompound nbttagcompound = book.getTagCompound();
            this.bookPages = nbttagcompound.getTagList("pages", 8);

            if (this.bookPages != null) {
                this.bookPages = (NBTTagList) this.bookPages.copy();
                this.bookTotalPages = this.bookPages.tagCount();

                if (this.bookTotalPages < 1) {
                    this.bookTotalPages = 1;
                }
            }
        }

        if (this.bookPages == null) {
            this.bookPages = new NBTTagList();
            this.bookPages.appendTag(new NBTTagString(""));
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        super.updateScreen();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        this.buttonList.add(this.buttonDone = new GuiButton(0, this.width / 2 - 100, 4 + this.bookImageHeight, 200, 20, I18n.format("gui.done", new Object[0])));

        int i = (this.width - this.bookImageWidth) / 2;
        int j = 2;
        this.buttonList.add(this.buttonNextPage = new GuiScreenUnclickableBook.NextPageButton(1, i + 120, j + 154, true));
        this.buttonList.add(this.buttonPreviousPage = new GuiScreenUnclickableBook.NextPageButton(2, i + 38, j + 154, false));
        this.updateButtons();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void updateButtons() {
        this.buttonNextPage.visible = this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned;
        this.buttonPreviousPage.visible = this.currPage > 0;
        this.buttonDone.visible = true;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 0) {
                mc.displayGuiScreen(null);
            } else if (button.id == 1) {
                if (this.currPage < this.bookTotalPages - 1) {
                    ++this.currPage;
                }
            } else if (button.id == 2) {
                if (this.currPage > 0) {
                    --this.currPage;
                }
            }

            this.updateButtons();
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookGuiTextures);
        int i = (this.width - this.bookImageWidth) / 2;
        int j = 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.bookImageWidth, this.bookImageHeight);

        try {
            Mouse.setNativeCursor(Mouse.getNativeCursor());
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        String s4 = I18n.format("book.pageIndicator", new Object[]{Integer.valueOf(this.currPage + 1), Integer.valueOf(this.bookTotalPages)});
        String s5 = "";

        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
            s5 = this.bookPages.getStringTagAt(this.currPage);
        }

        if (this.field_175387_B != this.currPage) {
            if (ItemEditableBook.validBookTagContents(this.bookObj.getTagCompound())) {
                try {
                    IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(s5);
                    this.field_175386_A = ichatcomponent != null ? GuiUtilRenderComponents.func_178908_a(ichatcomponent, 116, this.fontRendererObj, true, true) : null;
                } catch (JsonParseException var13) {
                    this.field_175386_A = null;
                }
            } else {
                ChatComponentText chatcomponenttext = new ChatComponentText(EnumChatFormatting.DARK_RED.toString() + "* Invalid book tag *");
                this.field_175386_A = Lists.newArrayList(chatcomponenttext);
            }

            this.field_175387_B = this.currPage;
        }

        int j1 = this.fontRendererObj.getStringWidth(s4);
        this.fontRendererObj.drawString(s4, i - j1 + this.bookImageWidth - 44, j + 16, 0);

        if (this.field_175386_A == null) {
            this.fontRendererObj.drawSplitString(s5, i + 36, j + 16 + 16, 116, 0);
        } else {
            int k1 = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.field_175386_A.size());

            for (int l1 = 0; l1 < k1; ++l1) {
                IChatComponent ichatcomponent2 = this.field_175386_A.get(l1);
                this.fontRendererObj.drawString(ichatcomponent2.getUnformattedText(), i + 36, j + 16 + 16 + l1 * this.fontRendererObj.FONT_HEIGHT, 0);
            }

            IChatComponent ichatcomponent1 = this.func_175385_b(mouseX, mouseY);

            if (ichatcomponent1 != null) {
                this.handleComponentHover(ichatcomponent1, mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            IChatComponent ichatcomponent = this.func_175385_b(mouseX, mouseY);

            if (this.handleComponentClick(ichatcomponent)) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Executes the click event specified by the given chat component
     */
    protected boolean handleComponentClick(IChatComponent p_175276_1_) {
        ClickEvent clickevent = p_175276_1_ == null ? null : p_175276_1_.getChatStyle().getChatClickEvent();

        if (clickevent == null) {
            return false;
        } else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String s = clickevent.getValue();

            try {
                int i = Integer.parseInt(s) - 1;

                if (i >= 0 && i < this.bookTotalPages && i != this.currPage) {
                    this.currPage = i;
                    this.updateButtons();
                    return true;
                }
            } catch (Throwable var5) {
                ;
            }

            return false;
        }
        return false;
    }

    public IChatComponent func_175385_b(int p_175385_1_, int p_175385_2_) {
        if (this.field_175386_A == null) {
            return null;
        } else {
            int i = p_175385_1_ - (this.width - this.bookImageWidth) / 2 - 36;
            int j = p_175385_2_ - 2 - 16 - 16;

            if (i >= 0 && j >= 0) {
                int k = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.field_175386_A.size());

                if (i <= 116 && j < this.mc.fontRendererObj.FONT_HEIGHT * k + k) {
                    int l = j / this.mc.fontRendererObj.FONT_HEIGHT;

                    if (l >= 0 && l < this.field_175386_A.size()) {
                        IChatComponent ichatcomponent = (IChatComponent) this.field_175386_A.get(l);
                        int i1 = 0;

                        for (IChatComponent ichatcomponent1 : ichatcomponent) {
                            if (ichatcomponent1 instanceof ChatComponentText) {
                                i1 += this.mc.fontRendererObj.getStringWidth(((ChatComponentText) ichatcomponent1).getChatComponentText_TextValue());

                                if (i1 > i) {
                                    return ichatcomponent1;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    static class NextPageButton extends GuiButton {
        private final boolean field_146151_o;

        public NextPageButton(int p_i46316_1_, int p_i46316_2_, int p_i46316_3_, boolean p_i46316_4_) {
            super(p_i46316_1_, p_i46316_2_, p_i46316_3_, 23, 13, "");
            this.field_146151_o = p_i46316_4_;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(GuiScreenUnclickableBook.bookGuiTextures);
                int i = 0;
                int j = 192;

                if (flag) {
                    i += 23;
                }

                if (!this.field_146151_o) {
                    j += 13;
                }

                this.drawTexturedModalRect(this.xPosition, this.yPosition, i, j, 23, 13);
            }
        }
    }
}